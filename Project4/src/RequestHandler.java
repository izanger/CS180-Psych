

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by ianzanger on 11/23/16.
 */
public class RequestHandler extends Thread {
    private Socket clientSocket;
    private int clientId;
    public PrintWriter out = null;
    private BufferedReader in = null;
    private User thisUser;

    static HashMap<String, User> loggedInUsers = new HashMap<String, User>(); //collection of Users that are logged in, defined by their unique IDs
    //TODO: Implement Hashmap for games, using gameID as key. ?Maybe

    public RequestHandler(Socket clientSocket, int clientId) {
        this.clientId = clientId;
        this.clientSocket = clientSocket;
    }

    public void run() {
        connectWithClient();

    }

    public void connectWithClient() {
        String latestLine;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            registerOrLogin();

        } catch (IOException e) {
            System.err.println("IO Error with client" + clientId + "\n" + e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Unexpected IOException when closing socket of client" + clientId + "\n" + e.getMessage());

            }

        }

    }

    public void registerOrLogin() {
        try {
            String latestLine = in.readLine();
            if (latestLine.contains("CREATENEWUSER")) {
                registerNewUser(latestLine);
                return;
            } else if (latestLine.contains("LOGIN")) {
                login(latestLine);
                return;
            } else {
                out.println("RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT");
                registerOrLogin();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String s){
        String latestLine = s;
        String username, password;
        User newUser;
        int chosenUserIndex = 0;

        if(latestLine.replaceAll("--", "").length() != (latestLine.length() -4)){
            out.println("RESPONSE--LOGIN--INVALIDMESSAGEFORMAT--");
            registerOrLogin();
            return;
        }

        username = latestLine.substring(7, latestLine.indexOf("--", 7));
        latestLine = latestLine.replaceFirst("LOGIN--" + username + "--", "");
        password = latestLine;

        if (thisUser != null) {
            out.println("RESPONSE--LOGIN--USERALREADYLOGGEDIN--");
            registerOrLogin();
            return;
        }

        for (int i = 0; i < Server.registeredUsers.size(); i++) {
            if (username.equals(Server.registeredUsers.get(i).getUsername()) ) {
                chosenUserIndex = i;
                break;
            }
            if (i == Server.registeredUsers.size() - 1) {
                out.println("RESPONSE--LOGIN--UNKNOWNUSER--");
                registerOrLogin();
                return;
            }
        }
        if (!password.equals(Server.registeredUsers.get(chosenUserIndex).getPassword())) {
            out.println("RESPONSE--LOGIN--INVALIDUSERPASSWORD--");
            registerOrLogin();
            return;
        } else {
            String token = generateUserToken();
            out.println("RESPONSE--LOGIN--SUCCESS--" + token);
            thisUser = Server.registeredUsers.get(chosenUserIndex);
            thisUser.setUserToken(token);
            thisUser.setThisUsersHandler(this);
            startOrJoinGame();
            return;
        }
    }

    public void registerNewUser(String s) {
        String latestLine = s;
        String username, password;

        if (latestLine.replaceAll("--", "").length() != (latestLine.length() - 4)) {
            out.println("RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT");
            registerOrLogin();
            return;
        }

        username = latestLine.substring(15, latestLine.indexOf("--",15));
        latestLine = latestLine.replaceFirst("CREATENEWUSER--" + username + "--", "");
        password = latestLine;

        if (!isValidUsername(username)) {
            out.println("RESPONSE--CREATENEWUSER--INVALIDUSERNAME--");
            registerOrLogin();
        } else if (!isValidPassword(password)) {
            out.println("RESPONSE--CREATENEWUSER--INVALIDUSERPASSWORD--");
            registerOrLogin();
        } else if (userNameIsRegistered(username)) {
            out.println("RESPONSE--CREATENEWUSER--USERALREADYEXISTS--");
            registerOrLogin();
        } else {
            User newUser = new User(username, password);
            Server.registeredUsers.add(newUser);
            Server.writeUserDatabase();

            out.println("RESPONSE--CREATENEWUSER--SUCCESS--");
            registerOrLogin();
        }
    }

    public void startOrJoinGame() {
        try {
            String latestLine = in.readLine();
            if (latestLine.contains("STARTNEWGAME")) {
                startNewGame(latestLine);
                return;
            } else if (latestLine.contains("JOINGAME")) {
                joinGame(latestLine);
                return;
            } else {
                System.err.println("Unexpected message received - startOrJoinGame()");
                startOrJoinGame();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startNewGame(String s) {
        String latestLine = s;
        String submittedToken = latestLine.substring(14, latestLine.length());
        if (!submittedToken.equals(thisUser.getUserToken())) {
            out.println("RESPONSE--STARTNEWGAME--USERNOTLOGGEDIN--");
            startOrJoinGame();
            return;
        } else {
            String token = generateGameToken();
            thisUser.setCurrentGame(new Game(thisUser, token));
            out.println("RESPONSE--STARTNEWGAME--SUCCESS--" + token);
            launchGame();
            return;
        }
    }

    public void joinGame(String s) {
        String latestLine = s;
        String submittedUserToken = latestLine.substring(10, latestLine.indexOf("--", 10));
        String submittedGameToken = latestLine.replace("JOINGAME--" + submittedUserToken + "--", "");

        if (!submittedUserToken.equals(thisUser.getUserToken())) {
            out.println("RESPONSE--JOINGAME--USERNOTLOGGEDIN--" + submittedGameToken);
            startOrJoinGame();
            return;
        } else if (!checkIfGameExists(submittedGameToken)) {
            out.println("RESPONSE--JOINGAME--GAMEKEYNOTFOUND--" + submittedGameToken);
            startOrJoinGame();
            return;
        } else {
            thisUser.setCurrentGame(findGame(submittedGameToken));
            thisUser.getCurrentGame().addPlayer(thisUser);
            thisUser.getCurrentGame().getLeaderPrintWriter().println("NEWPARTICIPANT--" + thisUser.getUsername() + "--" + thisUser.getCumulativeScore());
            out.println("RESPONSE--JOINGAME--SUCCESS--" + submittedGameToken);
            System.out.println("Leader: " + thisUser.getCurrentGame().getLeader().getUsername());
            synchronized (this) {
                while(!thisUser.getCurrentGame().gameReadyToBegin); //Loop until game is ready
            }
            sendNewWordOrQuit();
            return;
        }

    }

    //only leaders call this method
    public void launchGame() {
        String submittedUserToken, submittedGameToken;
        try {
            String latestLine = in.readLine();
            if (latestLine.contains("ALLPARTICIPANTSHAVEJOINED")) {
                submittedUserToken = latestLine.substring(27, latestLine.indexOf("--", 27));
                submittedGameToken = latestLine.replace("ALLPARTICIPANTSHAVEJOINED--" + submittedUserToken + "--", "");

                if (!submittedGameToken.equals(thisUser.getCurrentGame().getGameToken())) {
                    out.println("RESPONSE-ALLPARTICIPANTSHAVEJOINED--INVALIDGAMETOKEN--");
                    launchGame();
                    return;
                } else if (!submittedUserToken.equals(thisUser.getUserToken())) {
                    out.println("RESPONSE-ALLPARTICIPANTSHAVEJOINED--USERNOTLOGGEDIN");
                    launchGame();
                    return;
                } else {
                    //success
                    thisUser.getCurrentGame().setUpRandomWordleLists();
                    thisUser.getCurrentGame().randomizedWordleListLength = thisUser.getCurrentGame().randomizedWordleQuestions.size();
                    synchronized(this) {
                        thisUser.getCurrentGame().gameReadyToBegin = true;
                    }

                    sendNewWordOrQuit();
                    return;
                }

            } else {
                System.err.println("Something went wrong in launchGame(). Latest line didn't contain ALLPARTICIPANTSHAVEJOINED. Maybe you coded it wrong.");
                launchGame();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Called by both leaders and players. When new round begins, everyone returns here to either start new round or quit.
    public void sendNewWordOrQuit() {
        for(int i = 0; i < thisUser.getCurrentGame().players.size(); i++) {
            synchronized (this) {
                thisUser.getCurrentGame().players.get(i).hasSentSuggestion = false;
            }

        }
        synchronized (this) {
            thisUser.getCurrentGame().gameLogicDone = false;
        }


        if (thisUser.getCurrentGame().wordleListCounter < thisUser.getCurrentGame().randomizedWordleListLength) {
            out.println("NEWGAMEWORD--" + thisUser.getCurrentGame().randomizedWordleQuestions.get(thisUser.getCurrentGame().wordleListCounter) + "--" + thisUser.getCurrentGame().randomizedWordleAnswers.get(thisUser.getCurrentGame().wordleListCounter));
            System.out.println("Just sent: " + "NEWGAMEWORD--" + thisUser.getCurrentGame().randomizedWordleQuestions.get(thisUser.getCurrentGame().wordleListCounter) + "--" + thisUser.getCurrentGame().randomizedWordleAnswers.get(thisUser.getCurrentGame().wordleListCounter));
            if (thisUser == thisUser.getCurrentGame().getLeader()) {
                thisUser.getCurrentGame().playerSuggestions.add(thisUser.getCurrentGame().randomizedWordleAnswers.get(thisUser.getCurrentGame().wordleListCounter));
                //synchronized (this) {
                    //thisUser.getCurrentGame().correctAnswer = thisUser.getCurrentGame().randomizedWordleAnswers.get(wordleListCounter);

                    for(int i = 0; i < thisUser.getCurrentGame().players.size(); i++) {
                        thisUser.getCurrentGame().players.get(i).correctAnswer = thisUser.getCurrentGame().randomizedWordleAnswers.get(thisUser.getCurrentGame().wordleListCounter);
                    }
                //}

                try {
                    sleep(2000); //Concern is, leader could be slightly ahead and get through this code, throwing off count before players have utilized it.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                thisUser.getCurrentGame().wordleListCounter++;
                receiveLeaderSuggestion();
                return;
            }
            receivePlayerSuggestion();
            return;

        } else {
            System.out.println("made it to logout");
            logout();
            System.out.println("made it past logout");
            return;
        }





    }
    //leader only
    public void receiveLeaderSuggestion() {
        try {
            String latestLine = in.readLine();
            String submittedUserToken, submittedGameToken, playerSuggestion;

            if (!latestLine.contains("PLAYERSUGGESTION")) {
                out.println("RESPONSE--PLAYERSUGGESTION--UNEXPECTEDMESSAGETYPE--");
                receiveLeaderSuggestion();
                return;
            }
            if(latestLine.replaceAll("--", "").length() != (latestLine.length() - 6)){
                out.println("RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT--");
                receiveLeaderSuggestion();
                return;
            }

            submittedUserToken = latestLine.substring(18, latestLine.indexOf("--", 18));
            latestLine = latestLine.replaceFirst("PLAYERSUGGESTION--" + submittedUserToken + "--", "");
            submittedGameToken = latestLine.substring(0, latestLine.indexOf("--"));
            latestLine = latestLine.replaceFirst(submittedGameToken + "--", "");
            playerSuggestion = latestLine;
            if (latestLine.contains("--")) {
                System.err.println("Something is wonky with parsing the leader's PLAYERSUGGESTION message. Extra hyphens at the end?");
            }

            if (!submittedUserToken.equals(thisUser.getUserToken())) {
                out.println("RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN--");
                receiveLeaderSuggestion();
                return;
            } else if (!submittedGameToken.equals(thisUser.getCurrentGame().getGameToken())) {
                out.println("RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN--");
                receiveLeaderSuggestion();
                return;
            } else {
                thisUser.getCurrentGame().addPlayerSuggestion(playerSuggestion);
                thisUser.suggestion = playerSuggestion;
                synchronized (this) {
                    thisUser.hasSentSuggestion = true;
                }


                synchronized (this) {
                    while (!checkIfAllSuggestionsAreIn());
                }







                setUpRoundOptions();
                /*
                synchronized (this) {
                    thisUser.getCurrentGame().allSuggestionsAreIn = true;
                }
                */

                sendRoundOptions();
                return;
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //players only
    public void receivePlayerSuggestion() {
        try {
            String latestLine = in.readLine();
            String submittedUserToken, submittedGameToken, playerSuggestion;

            if (!latestLine.contains("PLAYERSUGGESTION")) {
                out.println("RESPONSE--PLAYERSUGGESTION--UNEXPECTEDMESSAGETYPE--");
                receivePlayerSuggestion();
                return;
            }
            if(latestLine.replaceAll("--", "").length() != (latestLine.length() - 6)){
                out.println("RESPONSE--PLAYERSUGGESTION--INVALIDMESSAGEFORMAT--");
                receivePlayerSuggestion();
                return;
            }

            submittedUserToken = latestLine.substring(18, latestLine.indexOf("--", 18));
            latestLine = latestLine.replaceFirst("PLAYERSUGGESTION--" + submittedUserToken + "--", "");
            submittedGameToken = latestLine.substring(0, latestLine.indexOf("--"));
            latestLine = latestLine.replaceFirst(submittedGameToken + "--", "");
            playerSuggestion = latestLine;
            if (latestLine.contains("--")) {
                System.err.println("Something is wonky with parsing the PLAYERSUGGESTION message. Extra hyphens at the end?");
            }

            if (!submittedUserToken.equals(thisUser.getUserToken())) {
                out.println("RESPONSE--PLAYERSUGGESTION--USERNOTLOGGEDIN--");
                receivePlayerSuggestion();
                return;
            } else if (!submittedGameToken.equals(thisUser.getCurrentGame().getGameToken())) {
                out.println("RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN--");
                receivePlayerSuggestion();
                return;
            } else {
                thisUser.getCurrentGame().addPlayerSuggestion(playerSuggestion);
                thisUser.suggestion = playerSuggestion;

                synchronized (this) {
                    thisUser.hasSentSuggestion = true;
                }
                synchronized (this) {
                    //while(!thisUser.getCurrentGame().allSuggestionsAreIn);
                    System.out.println("Made it to while loop!");
                    while(!checkIfAllSuggestionsAreIn());
                    System.out.println("Made it past while loop!");
                }

                sendRoundOptions();
                return;
            }



        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //everyone calls this method- leader and players
    public void sendRoundOptions() {
        synchronized (this) {
            out.println(thisUser.roundOptionsMessage);
        }

        getPlayerAndLeaderChoice();
        return;
    }

    //everyone calls this method
    public void getPlayerAndLeaderChoice() {
        try {
            String latestLine = in.readLine();
            String submittedUserToken, submittedGameToken, playerChoice;

            if (!latestLine.contains("PLAYERCHOICE")) {
                out.println("RESPONSE--PLAYERCHOICE--UNEXPECTEDMESSAGETYPE--");
                getPlayerAndLeaderChoice();
                return;
            }
            if(latestLine.replaceAll("--", "").length() != (latestLine.length() - 6)){
                out.println("RESPONSE--PLAYERCHOICE--INVALIDMESSAGEFORMAT--");
                getPlayerAndLeaderChoice();
                return;
            }

            submittedUserToken = latestLine.substring(14, latestLine.indexOf("--", 14));
            latestLine = latestLine.replaceFirst("PLAYERCHOICE--" + submittedUserToken + "--", "");
            submittedGameToken = latestLine.substring(0, latestLine.indexOf("--"));
            latestLine = latestLine.replaceFirst(submittedGameToken + "--", "");
            playerChoice = latestLine;
            if (latestLine.contains("--")) {
                System.err.println("Something is wonky with parsing the PLAYERCHOICE message. Extra hyphens at the end?");
            }

            if (!submittedUserToken.equals(thisUser.getUserToken())) {
                out.println("RESPONSE--PLAYERCHOICE--USERNOTLOGGEDIN--");
                getPlayerAndLeaderChoice();
                return;
            } else if (!submittedGameToken.equals(thisUser.getCurrentGame().getGameToken())) {
                out.println("RESPONSE--PLAYERSUGGESTION--INVALIDGAMETOKEN--");
                getPlayerAndLeaderChoice();
                return;
            } else {
                thisUser.playerChoice = playerChoice;
                thisUser.hasChosen = true;

                if (thisUser == thisUser.getCurrentGame().getLeader()) {
                    synchronized (this) {
                        while(!checkIfAllChoicesAreIn());
                    }

                    applyGameLogic();
                } else {
                    waitForGameLogic();
                }
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //only called by leader
    public void applyGameLogic() {
        synchronized (this) {
            for(int j = 0; j < thisUser.getCurrentGame().players.size(); j++) {
                thisUser.getCurrentGame().players.get(j).resultMessage = "";
            }

        }
        for (int i = 0; i < thisUser.getCurrentGame().players.size(); i++) {
            synchronized (this) {
                if (thisUser.getCurrentGame().players.get(i).playerChoice.equals(thisUser.getCurrentGame().players.get(i).correctAnswer)) {
                    thisUser.getCurrentGame().players.get(i).resultMessage = thisUser.getCurrentGame().players.get(i).resultMessage + "You got it right!";
                    thisUser.getCurrentGame().players.get(i).setCumulativeScore(10 + thisUser.getCurrentGame().players.get(i).getCumulativeScore());
                } else {
                    User fooledBy = null;
                    for (int j = 0; j < thisUser.getCurrentGame().players.size(); j++) {
                        if (thisUser.getCurrentGame().players.get(i).playerChoice.equals(thisUser.getCurrentGame().players.get(j).suggestion)) {
                            fooledBy = thisUser.getCurrentGame().players.get(j);
                        }
                    }
                    fooledBy.setCumulativeScore(5 + fooledBy.getCumulativeScore());
                    fooledBy.setNumTimesFooledOthers(fooledBy.getNumTimesFooledOthers() + 1);

                    thisUser.getCurrentGame().players.get(i).setNumTimesFooledByOthers(thisUser.getCurrentGame().players.get(i).getNumTimesFooledByOthers() + 1);

                    fooledBy.resultMessage = fooledBy.resultMessage + "You fooled " + thisUser.getCurrentGame().players.get(i).getUsername() + ".";
                    thisUser.getCurrentGame().players.get(i).resultMessage = thisUser.getCurrentGame().players.get(i).resultMessage + "You were fooled by " + fooledBy.getUsername() + ".";

                }
            }

        }

        String roundResultMessage = "ROUNDRESULT";

        for (int i = 0; i < thisUser.getCurrentGame().players.size(); i++) {
            synchronized (this) {
                roundResultMessage = roundResultMessage + "--" + thisUser.getCurrentGame().players.get(i).getUsername() + "--" + thisUser.getCurrentGame().players.get(i).resultMessage + "--" + thisUser.getCurrentGame().players.get(i).getCumulativeScore() + "--" + thisUser.getCurrentGame().players.get(i).getNumTimesFooledOthers() + "--" + thisUser.getCurrentGame().players.get(i).getNumTimesFooledByOthers();
            }
        }
        synchronized (this) {
            thisUser.getCurrentGame().roundResultMessage = roundResultMessage;
        }

        Server.writeUserDatabase();
        synchronized (this) {
            thisUser.getCurrentGame().gameLogicDone = true;
        }



        for(int i = 0; i < thisUser.getCurrentGame().players.size(); i++) {
            synchronized (this) {
                thisUser.getCurrentGame().players.get(i).hasChosen = false;
            }

        }



        sendRoundResult();
        return;
    }

    //only called by players
    public void waitForGameLogic() {
        synchronized (this) {
            while (!thisUser.getCurrentGame().gameLogicDone);
        }

        sendRoundResult();
        return;
    }

    //everyone calls this method
    public void sendRoundResult() {
        synchronized (this) {
            out.println(thisUser.getCurrentGame().roundResultMessage);
        }

        sendNewWordOrQuit();
        return;
    }

    //constructs the roundOptions message for everyone, and clears all playerSuggestions
    public void setUpRoundOptions() {
        long s = System.nanoTime();
        String msg = "ROUNDOPTIONS--";
        Collections.shuffle(thisUser.getCurrentGame().playerSuggestions, new Random(s));

        for(int i = 0; i < thisUser.getCurrentGame().playerSuggestions.size(); i ++) {
            if (i == thisUser.getCurrentGame().playerSuggestions.size() - 1) {
                msg = msg + thisUser.getCurrentGame().playerSuggestions.get(i);
                break;
            }
            msg = msg + thisUser.getCurrentGame().playerSuggestions.get(i) + "--";
        }

        for (int i = 0; i < thisUser.getCurrentGame().players.size(); i++) {
            synchronized (this) {
                thisUser.getCurrentGame().players.get(i).roundOptionsMessage = msg;
            }

        }

        thisUser.getCurrentGame().clearPlayerSuggestions();
        return;
    }

    public boolean checkIfAllSuggestionsAreIn() {

            for (int i = 0; i < thisUser.getCurrentGame().players.size(); i ++) {
                synchronized (this) {
                    if (thisUser.getCurrentGame().players.get(i).hasSentSuggestion == false) {
                        return false;
                    }
                }
            }
            return true;


    }

    public boolean checkIfAllChoicesAreIn() {
        synchronized (this) {
            for (int i = 0; i < thisUser.getCurrentGame().players.size(); i ++) {
                if (thisUser.getCurrentGame().players.get(i).hasChosen == false) {
                    return false;
                }
            }
            return true;
        }
    }

    public Game findGame(String gameToken) {
        for(int i = 0; i < Game.games.size(); i ++) {
            if (Game.games.get(i).getGameToken().equals(gameToken)) {
                return Game.games.get(i);
            }
        }
        return null;
    }

    public boolean checkIfGameExists(String submittedGameToken) {
        for(int i = 0; i < Game.games.size(); i ++) {
            if (Game.games.get(i).getGameToken().equals(submittedGameToken)) {
                return true;
            }
        }
        return false;
    }

    public String generateUserToken() {
        char[] validChars = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        char[] tokenChars = new char[10];
        Random r = new Random();

        for(int i = 0; i < 10; i++) {
            tokenChars[i] = validChars[r.nextInt(validChars.length)];
        }

        String token = new String(tokenChars);
        if (token.length() != 10) {
            JOptionPane.showMessageDialog(null, "You coded generateUserToken() wrong.", "Error", 1);
        }
        return token;
    }

    public String generateGameToken() {
        char[] validChars = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        char[] tokenChars = new char[3];
        Random r = new Random();

        for(int i = 0; i < 3; i++) {
            tokenChars[i] = validChars[r.nextInt(validChars.length)];
        }

        String token = new String(tokenChars);
        if (token.length() != 3) {
            JOptionPane.showMessageDialog(null, "You coded generateGameToken() wrong.", "Error", 1);
        }
        return token;
    }

    public boolean isValidUsername(String username) {
        char[] validChars = {'_','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','1','2','3','4','5','6','7','8','9','0'};
        if (username.equals("") || username.length() >= 10) {
            return false;
        }
        for (int i = 0; i < username.length(); i ++) {
            for (int j = 0; j < validChars.length; j++) {
                if (username.charAt(i) == validChars[j]) {
                    break;
                }
                if (j == validChars.length-1) {
                    return false;
                }
            }
        }
        return true;


    }

    public boolean isValidPassword(String password) {
        char[] nums = {'1','2','3','4','5','6','7','8','9','0'};
        char[] validChars = {'*','$','&','#','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','1','2','3','4','5','6','7','8','9','0'};
        if (password.equals("") || password.length() >= 10) {
            return false;
        }
        for (int i = 0; i < password.length(); i ++) {
            for (int j = 0; j < validChars.length; j++) {
                if (password.charAt(i) == validChars[j]) {
                    break;
                }
                if (j == validChars.length-1) {
                    return false;
                }
            }
        }
        if (password.toLowerCase().equals(password)) {
            return false;
        }
        for (int i = 0; i < password.length(); i ++) {
            for (int j = 0; j < nums.length; j++) {
                if (password.charAt(i) == nums[j]) {
                    return true;
                }
            }
        }
        return false;



    }

    public boolean userNameIsRegistered(String username) {
        for (int i = 0; i < Server.registeredUsers.size(); i ++) {
            if (Server.registeredUsers.get(i).getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void logout() {
        out.println("RESPONSE--LOGOUT--SUCCESS--");
        interrupt();
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Unexpected IOException when closing socket of client" + clientId + "\n" + e.getMessage());

        }
        return;
    }


}

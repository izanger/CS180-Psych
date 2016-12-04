

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
    public static int wordleListCounter = 0;
    public static int randomizedWordleListLength;

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
            out.println("RESPONSE--LOGIN--INVALIDMESSAGEFORMAT");
            registerOrLogin();
            return;
        }

        username = latestLine.substring(7, latestLine.indexOf("--", 7));
        latestLine = latestLine.replaceFirst("LOGIN--" + username + "--", "");
        password = latestLine;

        if (thisUser != null) {
            out.println("RESPONSE--LOGIN--USERALREADYLOGGEDIN");
            registerOrLogin();
            return;
        }

        for (int i = 0; i < Server.registeredUsers.size(); i++) {
            if (username.equals(Server.registeredUsers.get(i).getUsername()) ) {
                chosenUserIndex = i;
                break;
            }
            if (i == Server.registeredUsers.size() - 1) {
                out.println("RESPONSE--LOGIN--UNKNOWNUSER");
                registerOrLogin();
                return;
            }
        }
        if (!password.equals(Server.registeredUsers.get(chosenUserIndex).getPassword())) {
            out.println("RESPONSE--LOGIN--INVALIDUSERPASSWORD");
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
            while(!thisUser.getCurrentGame().gameReadyToBegin){} //Loop until game is ready
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
                    randomizedWordleListLength = thisUser.getCurrentGame().randomizedWordleQuestions.size();
                    thisUser.getCurrentGame().gameReadyToBegin = true;
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
        if (wordleListCounter < randomizedWordleListLength) {
            out.println("NEWGAMEWORD--" + thisUser.getCurrentGame().randomizedWordleQuestions.get(wordleListCounter) + "--" + thisUser.getCurrentGame().randomizedWordleAnswers.get(wordleListCounter));
            if (thisUser == thisUser.getCurrentGame().getLeader()) {
                try {
                    sleep(2000); //Concern is, leader could be slightly ahead and get through this code, throwing off count before players have utilized it.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                wordleListCounter++;

                //TODO: GoToNextLeaderMethod - accept suggestion from leader. Then check continually if everyone has sent their suggestions.
                //TODO: Then, set master static message, and set allReady to TRUE, so clients know to send it. Repeat for choices.
                return;
            }
            //TODO: GoToNextPlayerMethod - accept suggestion, then set a "ready" boolean to TRUE. Then constantly check if allReady boolean is true yet
            return;

        } else {
            //TODO: Everybody quit game.
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


}

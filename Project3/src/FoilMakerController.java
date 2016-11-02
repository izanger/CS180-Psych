/**
 * "Controller implements network programming bits, application protocol, etc."
 *
 * Created by Ian on 10/27/2016.
 */

import sun.text.resources.iw.FormatData_iw_IL;

import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.IntBuffer;


public class FoilMakerController {
    public static String s = FoilMakerNetworkProtocol.SEPARATOR;
    public static String msg;
    public static String response;

    //NETWORKING BLOCK

    //network stuff
    public static PrintWriter outToServer = null;
    public static BufferedReader inFromServer = null;
    private static String serverIP;
    private static int serverPort;

    //sends the given string to the server, then returns what the server responds with. If something went wrong, returns "ERROR"
    public static String sendMessageToServer(String s) {
        try {
            outToServer.println(s);
            String responseMessage = inFromServer.readLine();
            return responseMessage;
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }


    }

    public static void setUpSocket(String serverIP, int serverPort) {
        Socket socket = null;

        try {

            socket = new Socket(serverIP, serverPort);
            outToServer = new PrintWriter(socket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //SERVER INTERACTION BLOCK
    public static void createNewUser(String username, String password) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.CREATENEWUSER;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "CREATENEWUSER" + s + username + s + password;
        response = sendMessageToServer(msg);
        if (response.contains("INVALIDMESSAGEFORMAT"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        else if (response.contains("INVALIDUSERNAME"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDUSERNAME;
        else if (response.contains("INVALIDUSERPASSWORD"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDUSERPASSWORD;
        else if (response.contains("USERALREADYEXISTS"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERALREADYEXISTS;
        else if (response.contains("SUCCESS"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
        else
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE; //should not happen
    }

    public static void login(String username, String password) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.LOGIN;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "LOGIN" + s + username + s + password;
        response = sendMessageToServer(msg);
        if (response.contains("INVALIDMESSAGEFORMAT"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        else if (response.contains("UNKNOWNUSER"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.UNKNOWNUSER;
        else if (response.contains("INVALIDUSERPASSWORD"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDUSERPASSWORD;
        else if (response.contains("USERALREADYLOGGEDIN"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERALREADYLOGGEDIN;
        else if (response.contains("SUCCESS")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
            FoilMakerModel.username = username;
            FoilMakerModel.password = password;
            String token = response.replaceFirst("RESPONSE--LOGIN--SUCCESS--", "");
            FoilMakerModel.userToken = token;
            if (FoilMakerModel.userToken.length() != FoilMakerNetworkProtocol.LOGIN_TOKEN_LENGTH) {
                System.err.println("Something went wrong - the userToken is not the length it should be.");
            }

        } else
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE; //should not happen

    }

    public static void startNewGame(String userToken) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.STARTNEWGAME;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "STARTNEWGAME" + s + userToken;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        else if (response.contains("FAILURE"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE;
        else {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
            String token = response.replaceFirst("RESPONSE--STARTNEWGAME--SUCCESS--", "");
            FoilMakerModel.gameToken = token;
            if (FoilMakerModel.gameToken.length() != FoilMakerNetworkProtocol.GAME_KEY_LENGTH)
                System.err.println("Something went wrong - the gameToken isn't the length it should be.");
        }
    }

    public static void joinGame(String userToken, String gameToken) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.JOINGAME;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "JOINGAME" + s + userToken + s + gameToken;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        } else if (response.contains("GAMEKEYNOTFOUND")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.GAMEKEYNOTFOUND;
        } else if (response.contains("SUCCESS")) {
            //At this point, the player (who is not the host of the game), will wait for the host to start the game
        } else
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE; //shouldn't happen
    }

    //@MATT - we might need to loop this in the Leader client, until a new player joins.
    //Maybe set a loop on some sort of timer? Check every X seconds until currentNumberOfPlayers == MAX_NUMBER_OF_PLAYERS
    //(Unless just calling .readLine() waits for input, then we only have to call it once. I'm not entirely sure.)
    public static void checkForNewPlayer() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null && serverMsg.contains("NEWPARTICIPANT")) {
                FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.NEWPARTICIPANT;
                FoilMakerModel.player2Username = serverMsg.substring(16, serverMsg.indexOf('-', 17) );
                FoilMakerModel.player2Score = Integer.parseInt(serverMsg.replaceFirst(("NEWPARTICIPANT" + s + FoilMakerModel.player2Username + s), ""));
                FoilMakerModel.currentNumberOfPlayers++;
            }else
                System.err.println("Something went wrong - the expected NewParticipant message did not contain \"NEWPARTICIPANT\" ");
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }
    }

    public static void allParticipantsHaveJoined(String userToken, String gameToken) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.ALLPARTICIPANTSHAVEJOINED;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "ALLPARTICIPANTSHAVEJOINED" + s + userToken + s + gameToken;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        else if (response.contains("INVALIDGAMETOKEN")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDGAMETOKEN;
        } else if (response.contains("USERNOTGAMELEADER")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTGAMELEADER;
        } else if (response.contains("NEWGAMEWORD")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
            FoilMakerModel.currentQuestion = response.substring(13, response.indexOf('-', 14) );
            FoilMakerModel.currentCorrectAnswer = response.replaceFirst("NEWGAMEWORD" + s + FoilMakerModel.currentQuestion + s, "");
        } else
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE;
    }

    //This method will need to be run continually by the non-host players, to check for a new question/correct answer to be provided, or to see if the game is over.
    //The view will need to coninally check if isGameOver == true, so it knows to display a "Game Over" message.
    public static void checkForStatusOfNextRound() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null) {
                if (serverMsg.contains("NEWGAMEWORD")){
                    FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.NEWGAMEWORD;
                    FoilMakerModel.currentQuestion = serverMsg.substring(13, response.indexOf('-', 14) );
                    FoilMakerModel.currentCorrectAnswer = serverMsg.replaceFirst("NEWGAMEWORD" + s + FoilMakerModel.currentQuestion + s, "");
                }

                if (serverMsg.contains("GAMEOVER")) {
                    FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.GAMEOVER;
                    FoilMakerModel.isGameOver = true;
                }

            }
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }

    }

    public static void playerSuggestion(String userToken, String gameToken, String suggestion) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.PLAYERSUGGESTION;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "PLAYERSUGGESTION" + s + userToken + s + gameToken + s + suggestion;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN"))
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        else if (response.contains("INVALIDGAMETOKEN")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDGAMETOKEN;
        } else if (response.contains("UNEXPECTEDMESSAGETYPE")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.UNEXPECTEDMESSAGETYPE;
        } else if (response.contains("INVALIDMESSAGEFORMAT")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        }
    }

    //Again, run continually by all players (including host) to check for the options provided by the server.
    //Should start running for each player individually, after they submit their playerSuggestion
    public static void checkForRoundOptions() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null) {
                FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.ROUNDOPTIONS;
                serverMsg = serverMsg.replaceFirst("ROUNDOPTIONS--", "");
                FoilMakerModel.possibleAnswer1 = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(FoilMakerModel.possibleAnswer1 + s, "");
                FoilMakerModel.possibleAnswer2 = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(FoilMakerModel.possibleAnswer2 + s, "");
                FoilMakerModel.possibleAnswer3 = serverMsg;
            }
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }
    }

    public static void playerChoice(String userToken, String gameToken, String choice) {
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.PLAYERCHOICE;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "PLAYERCHOICE" + s + userToken + s + gameToken + s + choice;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        } else if (response.contains("INVALIDGAMETOKEN")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDGAMETOKEN;
        } else if (response.contains("UNEXPECTEDMESSAGETYPE")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.UNEXPECTEDMESSAGETYPE;
        } else if (response.contains("INVALIDMESSAGEFORMAT")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        }
    }

    //After RoundResult is sent to every player, a new NEWGAMEWORD or GAMEOVER message is sent to everyone by the server, with no further prompting needed
    public static void checkForRoundResult() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null) {
                FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.ROUNDRESULT;
                serverMsg = serverMsg.replaceFirst("ROUNDRESULT" + s, "");
                serverMsg = serverMsg.replaceFirst(serverMsg.substring(0, serverMsg.indexOf("--") + 2), "");
                FoilMakerModel.hostRoundResultMessage = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(FoilMakerModel.hostRoundResultMessage + s, "");
                FoilMakerModel.hostCumulativeScore = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf("--")));
                serverMsg = serverMsg.replaceFirst(Integer.toString(FoilMakerModel.hostCumulativeScore) + s, "");
                FoilMakerModel.numPlayersFooledByHost = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf("--")));
                serverMsg = serverMsg.replaceFirst(Integer.toString(FoilMakerModel.numPlayersFooledByHost) + s, "");
                FoilMakerModel.numTimesHostFooledByOthers = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf("--")));
                serverMsg = serverMsg.replaceFirst(Integer.toString(FoilMakerModel.numTimesHostFooledByOthers) + s, "");

                serverMsg = serverMsg.replaceFirst(serverMsg.substring(0, serverMsg.indexOf("--") + 2), "");
                FoilMakerModel.player2RoundResultMessage = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(FoilMakerModel.player2RoundResultMessage + s, "");
                FoilMakerModel.player2CumulativeScore = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf(s)));
                serverMsg = serverMsg.replaceFirst(Integer.toString(FoilMakerModel.player2CumulativeScore) + s, "");
                FoilMakerModel.numPlayersFooledByPlayer2 = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf(s)));
                serverMsg = serverMsg.replaceFirst(Integer.toString(FoilMakerModel.numPlayersFooledByPlayer2) + s, "");
                FoilMakerModel.numTimesPlayer2FooledByOthers = Integer.parseInt(serverMsg.substring(0, serverMsg.length()));
                serverMsg = serverMsg.replaceFirst(Integer.toString(FoilMakerModel.numTimesPlayer2FooledByOthers), "");

                //check
                if (serverMsg.length() != 0) {
                    System.err.println("Something went wrong with processing the Round Result.");
                }

            }
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }
    }

    public static void logOut() {
        msg = "LOGOUT" + s;
        response = sendMessageToServer(msg);
        FoilMakerModel.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.LOGOUT;
        FoilMakerModel.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        if (response.contains("USERNOTLOGGEDIN")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        } else if (response.contains("SUCCESS")) {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
        } else {
            FoilMakerModel.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE;
            System.err.println("Something went wrong while trying to logout.");
        }


    }

















    //MAIN

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Format: java FoilMakerController <serverIP> <serverPortNumber>");
            System.exit(0);
        }

        serverPort = Integer.parseInt(args[1]);
        serverIP = args[0];

        if (serverPort < 0) {
            System.err.println("Invalid port number.\nFormat: java FoilMakerController <serverIP> <serverPortNumber>");
            System.exit(1);
        }

        setUpSocket(serverIP, serverPort);
    }
}

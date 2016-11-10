/**
 * "Controller implements network programming bits, application protocol, etc."

 *
 * Created by Ian on 10/27/2016.
 */




import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.IntBuffer;


public class FoilMakerController {
    public static String s = FoilMakerNetworkProtocol.SEPARATOR;
    public String msg;
    public String response;
    
    public FoilMakerModel model;
    public FoilMakerView view;

    //NETWORKING BLOCK

    //network stuff
    public  PrintWriter outToServer;
    public  BufferedReader inFromServer;
    public  String serverIP = "localhost";
    public  int serverPort = 10000;
    
    public FoilMakerController(){
    	this.outToServer = null;
    	this.inFromServer = null;
    	
    	this.model = new FoilMakerModel(this);
    	this.view = new FoilMakerView(this, model);
    	
    
    
    }

    //sends the given string to the server, then returns what the server responds with. If something went wrong, returns "ERROR"
    public  String sendMessageToServer(String s) {
        try {
            outToServer.println(s);
            String responseMessage = inFromServer.readLine();
            return responseMessage;
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }


    }

    public  void setUpSocket(String serverIP, int serverPort) {
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
    public  void createNewUser(String username, String password) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.CREATENEWUSER;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "CREATENEWUSER" + s + username + s + password;
        response = sendMessageToServer(msg);
        if (response.contains("INVALIDMESSAGEFORMAT"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        else if (response.contains("INVALIDUSERNAME"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDUSERNAME;
        else if (response.contains("INVALIDUSERPASSWORD"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDUSERPASSWORD;
        else if (response.contains("USERALREADYEXISTS"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERALREADYEXISTS;
        else if (response.contains("SUCCESS"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
        else
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE; //should not happen
    }

    public void login(String username, String password) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.LOGIN;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "LOGIN" + s + username + s + password;
        response = sendMessageToServer(msg);
        if (response.contains("INVALIDMESSAGEFORMAT"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        else if (response.contains("UNKNOWNUSER"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.UNKNOWNUSER;
        else if (response.contains("INVALIDUSERPASSWORD"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDUSERPASSWORD;
        else if (response.contains("USERALREADYLOGGEDIN"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERALREADYLOGGEDIN;
        else if (response.contains("SUCCESS")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
            model.username = username;
            model.password = password;
            String token = response.replaceFirst("RESPONSE--LOGIN--SUCCESS--", "");
            model.userToken = token;
            if (model.userToken.length() != FoilMakerNetworkProtocol.LOGIN_TOKEN_LENGTH) {
                System.err.println("Something went wrong - the userToken is not the length it should be.");
            }

        } else
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE; //should not happen

    }

    public  void startNewGame(String userToken) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.STARTNEWGAME;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;
        model.yupHost = true;

        msg = "STARTNEWGAME" + s + userToken;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        else if (response.contains("FAILURE"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE;
        else {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
            String token = response.replaceFirst("RESPONSE--STARTNEWGAME--SUCCESS--", "");
            model.gameToken = token;
            if (model.gameToken.length() != FoilMakerNetworkProtocol.GAME_KEY_LENGTH)
                System.err.println("Something went wrong - the gameToken isn't the length it should be.");
        }
    }

    public  void joinGame(String userToken, String gameToken) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.JOINGAME;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "JOINGAME" + s + userToken + s + gameToken;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        } else if (response.contains("GAMEKEYNOTFOUND")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.GAMEKEYNOTFOUND;
        } else if (response.contains("SUCCESS")) {
            //At this point, the player (who is not the host of the game), will wait for the host to start the game
        } else
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE; //shouldn't happen
    }

    //@MATT - we might need to loop this in the Leader client, until a new player joins.
    //Maybe set a loop on some sort of timer? Check every X seconds until currentNumberOfPlayers == MAX_NUMBER_OF_PLAYERS
    //(Unless just calling .readLine() waits for input, then we only have to call it once. I'm not entirely sure.)
    public  void checkForNewPlayer() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null && serverMsg.contains("NEWPARTICIPANT")) {
                model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.NEWPARTICIPANT;
                model.player2Username = serverMsg.substring(16, serverMsg.indexOf('-', 17) );
                model.player2Score = Integer.parseInt(serverMsg.replaceFirst(("NEWPARTICIPANT" + s + model.player2Username + s), ""));
                model.currentNumberOfPlayers++;
            }else
                System.err.println("Something went wrong - the expected NewParticipant message did not contain \"NEWPARTICIPANT\" ");
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }
    }

    public  void allParticipantsHaveJoined(String userToken, String gameToken) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.ALLPARTICIPANTSHAVEJOINED;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "ALLPARTICIPANTSHAVEJOINED" + s + userToken + s + gameToken;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        else if (response.contains("INVALIDGAMETOKEN")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDGAMETOKEN;
        } else if (response.contains("USERNOTGAMELEADER")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTGAMELEADER;
        } else if (response.contains("NEWGAMEWORD")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
            model.currentQuestion = response.substring(13, response.indexOf('-', 14) );
            model.currentCorrectAnswer = response.replaceFirst("NEWGAMEWORD" + s + model.currentQuestion + s, "");
        } else
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE;
    }

    //This method will need to be run continually by the non-host players, to check for a new question/correct answer to be provided, or to see if the game is over.
    //The view will need to coninally check if isGameOver == true, so it knows to display a "Game Over" message.
    public  void checkForStatusOfNextRound() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null) {
                if (serverMsg.contains("NEWGAMEWORD")){
                    model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.NEWGAMEWORD;
                    model.currentQuestion = serverMsg.substring(13, response.indexOf('-', 14) );
                    model.currentCorrectAnswer = serverMsg.replaceFirst("NEWGAMEWORD" + s + model.currentQuestion + s, "");
                }

                if (serverMsg.contains("GAMEOVER")) {
                    model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.GAMEOVER;
                    model.isGameOver = true;
                }

            }
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }

    }

    public  void playerSuggestion(String userToken, String gameToken, String suggestion) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.PLAYERSUGGESTION;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "PLAYERSUGGESTION" + s + userToken + s + gameToken + s + suggestion;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN"))
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        else if (response.contains("INVALIDGAMETOKEN")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDGAMETOKEN;
        } else if (response.contains("UNEXPECTEDMESSAGETYPE")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.UNEXPECTEDMESSAGETYPE;
        } else if (response.contains("INVALIDMESSAGEFORMAT")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        }
    }

    //Again, run continually by all players (including host) to check for the options provided by the server.
    //Should start running for each player individually, after they submit their playerSuggestion
    public  void checkForRoundOptions() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null) {
                model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.ROUNDOPTIONS;
                serverMsg = serverMsg.replaceFirst("ROUNDOPTIONS--", "");
                model.possibleAnswer1 = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(model.possibleAnswer1 + s, "");
                model.possibleAnswer2 = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(model.possibleAnswer2 + s, "");
                model.possibleAnswer3 = serverMsg;
            }
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }
    }

    public  void playerChoice(String userToken, String gameToken, String choice) {
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.PLAYERCHOICE;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        msg = "PLAYERCHOICE" + s + userToken + s + gameToken + s + choice;
        response = sendMessageToServer(msg);

        if (response.contains("USERNOTLOGGEDIN")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        } else if (response.contains("INVALIDGAMETOKEN")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDGAMETOKEN;
        } else if (response.contains("UNEXPECTEDMESSAGETYPE")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.UNEXPECTEDMESSAGETYPE;
        } else if (response.contains("INVALIDMESSAGEFORMAT")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.INVALIDMESSAGEFORMAT;
        }
    }

    //After RoundResult is sent to every player, a new NEWGAMEWORD or GAMEOVER message is sent to everyone by the server, with no further prompting needed
    public  void checkForRoundResult() {
        String serverMsg = null;
        try {
            serverMsg = inFromServer.readLine();
            if (serverMsg != null) {
                model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.ROUNDRESULT;
                serverMsg = serverMsg.replaceFirst("ROUNDRESULT" + s, "");
                serverMsg = serverMsg.replaceFirst(serverMsg.substring(0, serverMsg.indexOf("--") + 2), "");
                model.hostRoundResultMessage = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(model.hostRoundResultMessage + s, "");
                model.hostCumulativeScore = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf("--")));
                serverMsg = serverMsg.replaceFirst(Integer.toString(model.hostCumulativeScore) + s, "");
                model.numPlayersFooledByHost = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf("--")));
                serverMsg = serverMsg.replaceFirst(Integer.toString(model.numPlayersFooledByHost) + s, "");
                model.numTimesHostFooledByOthers = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf("--")));
                serverMsg = serverMsg.replaceFirst(Integer.toString(model.numTimesHostFooledByOthers) + s, "");

                serverMsg = serverMsg.replaceFirst(serverMsg.substring(0, serverMsg.indexOf("--") + 2), "");
                model.player2RoundResultMessage = serverMsg.substring(0, serverMsg.indexOf("--"));
                serverMsg = serverMsg.replaceFirst(model.player2RoundResultMessage + s, "");
                model.player2CumulativeScore = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf(s)));
                serverMsg = serverMsg.replaceFirst(Integer.toString(model.player2CumulativeScore) + s, "");
                model.numPlayersFooledByPlayer2 = Integer.parseInt(serverMsg.substring(0, serverMsg.indexOf(s)));
                serverMsg = serverMsg.replaceFirst(Integer.toString(model.numPlayersFooledByPlayer2) + s, "");
                model.numTimesPlayer2FooledByOthers = Integer.parseInt(serverMsg.substring(0, serverMsg.length()));
                serverMsg = serverMsg.replaceFirst(Integer.toString(model.numTimesPlayer2FooledByOthers), "");

                //check
                if (serverMsg.length() != 0) {
                    System.err.println("Something went wrong with processing the Round Result.");
                }

            }
        } catch (IOException e) {
            System.err.println("Something went wrong.");
        }
    }

    public void logOut() {
        msg = "LOGOUT" + s;
        response = sendMessageToServer(msg);
        model.lastSentMessageType = FoilMakerNetworkProtocol.MSG_TYPE.LOGOUT;
        model.lastReceivedMessageType = FoilMakerNetworkProtocol.MSG_TYPE.RESPONSE;

        if (response.contains("USERNOTLOGGEDIN")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.USERNOTLOGGEDIN;
        } else if (response.contains("SUCCESS")) {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.SUCCESS;
        } else {
            model.lastReceivedMessageDetail = FoilMakerNetworkProtocol.MSG_DETAIL_T.FAILURE;
            System.err.println("Something went wrong while trying to logout.");
        }


    }

















    //MAIN

    public static void main(String[] args) {
    	FoilMakerController controller = new FoilMakerController();
    	
        /* if (args.length != 2) {
            System.err.println("Format: java FoilMakerController <serverIP> <serverPortNumber>");
            System.exit(0);
        }

        controller.serverPort = Integer.parseInt(args[1]);
        controller.serverIP = args[0];

        if (controller.serverPort < 0) {
            System.err.println("Invalid port number.\nFormat: java FoilMakerController <serverIP> <serverPortNumber>");
            System.exit(1);
        }
        

        controller.setUpSocket(controller.serverIP, controller.serverPort);
        
        */
    	
    	controller.setUpSocket("localhost",  9999);
        /* controller.createNewUser("Steve", "1234");
        controller.login("Steve", "1234");
        System.out.println(controller.model.username);
        System.out.println(controller.model.password);
        System.out.println(controller.model.lastReceivedMessageDetail);
        */
        controller.view.setUpView();
    }
}

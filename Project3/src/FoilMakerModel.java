/**
 * "Model captures data - questions, word suggestions, etc. received from server"
 *
 * Created by Ian on 10/27/2016.
 */
public class FoilMakerModel {
	public FoilMakerController controller;
	
    public  FoilMakerNetworkProtocol.MSG_TYPE lastSentMessageType;
    public  FoilMakerNetworkProtocol.MSG_TYPE lastReceivedMessageType;
    public  FoilMakerNetworkProtocol.MSG_DETAIL_T lastReceivedMessageDetail;

    public  String username;
    public  String password;
    public  String userToken;
    public  String gameToken;

    public  String currentQuestion;
    public  String currentCorrectAnswer;

    public  String player2Username;
    public  int player2Score;

    public  int currentNumberOfPlayers = 1;
    public  final int MAX_NUMBER_PLAYERS = 2;

    public  String possibleAnswer1;
    public  String possibleAnswer2;
    public  String possibleAnswer3;
    public  String chosenAnswer;

    public  String hostRoundResultMessage;
    public  int hostCumulativeScore;
    public  int numPlayersFooledByHost;
    public  int numTimesHostFooledByOthers;

    public  String player2RoundResultMessage;
    public  int player2CumulativeScore;
    public  int numPlayersFooledByPlayer2;
    public  int numTimesPlayer2FooledByOthers;

    public  boolean isGameOver = false;
    public boolean yupHost;
    
    public FoilMakerModel(FoilMakerController c){
    	this.controller = c;
    }
}

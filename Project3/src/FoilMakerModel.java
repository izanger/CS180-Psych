/**
 * "Model captures data - questions, word suggestions, etc. received from server"
 *
 * Created by Ian on 10/27/2016.
 */
public class FoilMakerModel {
    public static FoilMakerNetworkProtocol.MSG_TYPE lastSentMessageType;
    public static FoilMakerNetworkProtocol.MSG_TYPE lastReceivedMessageType;
    public static FoilMakerNetworkProtocol.MSG_DETAIL_T lastReceivedMessageDetail;

    public static String username;
    public static String password;
    public static String userToken;
    public static String gameToken;

    public static String currentQuestion;
    public static String currentCorrectAnswer;

    public static String player2Username;
    public static int player2Score;

    public static int currentNumberOfPlayers = 1;
    public static final int MAX_NUMBER_PLAYERS = 2;

    public static String possibleAnswer1;
    public static String possibleAnswer2;
    public static String possibleAnswer3;

    public static String hostRoundResultMessage;
    public static int hostCumulativeScore;
    public static int numPlayersFooledByHost;
    public static int numTimesHostFooledByOthers;

    public static String player2RoundResultMessage;
    public static int player2CumulativeScore;
    public static int numPlayersFooledByPlayer2;
    public static int numTimesPlayer2FooledByOthers;

    public static boolean isGameOver = false;
}

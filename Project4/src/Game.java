import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by ianzanger on 11/30/16.
 */
public class Game {
    private User leader;
    public ArrayList<User> players; //used to keep track of players in this game
    private String gameToken;
    public PrintWriter leaderPrintWriter = null;
    public volatile boolean gameReadyToBegin = false;
    public ArrayList<String> randomizedWordleQuestions;
    public ArrayList<String> randomizedWordleAnswers;
    public static ArrayList<Game> games = new ArrayList<>();
    public ArrayList<String> playerSuggestions = new ArrayList<>();
    public volatile boolean gameLogicDone = false;
    public volatile String roundResultMessage;
    public int wordleListCounter = 0;
    public int randomizedWordleListLength;




    public Game(User leader, String gameToken) {
        this.leader = leader;
        this.gameToken = gameToken;
        players = new ArrayList<>();
        players.add(leader);
        games.add(this);
        leaderPrintWriter = leader.getThisUsersHandler().out; //This is sketchy
    }

    public User getLeader() {
        return leader;
    }

    public ArrayList getPlayers() {
        return players;
    }

    public void addPlayer(User player) {
        players.add(player);
    }

    public String getGameToken() {
        return gameToken;
    }

    public PrintWriter getLeaderPrintWriter() {
        return leaderPrintWriter;
    }

    public void setUpRandomWordleLists() {
        randomizedWordleQuestions = Server.wordleQuestions;
        randomizedWordleAnswers = Server.wordleAnswers;
        long s = System.nanoTime();
        Collections.shuffle(randomizedWordleQuestions, new Random(s));
        Collections.shuffle(randomizedWordleAnswers, new Random(s));
    }

    public synchronized void addPlayerSuggestion(String suggestion) {
        playerSuggestions.add(suggestion);
    }

    public void clearPlayerSuggestions() {
        playerSuggestions.clear();
    }
}

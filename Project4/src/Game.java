import java.util.ArrayList;

/**
 * Created by ianzanger on 11/30/16.
 */
public class Game {
    private User leader;
    private ArrayList<User> players; //used to keep track of players in this game
    private String gameID;
    private static ArrayList<Game> games = new ArrayList<>();


    public Game(User leader) {
        this.leader = leader;
        players = new ArrayList<User>();
        players.add(leader);
        games.add(this);
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

    public void setGameID(String id) {
        this.gameID = id;
    }

    public String getGameID() {
        return gameID;
    }
}

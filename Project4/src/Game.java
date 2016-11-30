import java.util.ArrayList;

/**
 * Created by ianzanger on 11/30/16.
 */
public class Game {
    private User leader;
    private ArrayList<User> players; //used to keep track of players in this game

    public Game(User leader) {
        this.leader = leader;
        players = new ArrayList<User>();
        players.add(leader);
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
}

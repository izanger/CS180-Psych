/**
 * Created by ianzanger on 11/23/16.
 */
public class User {
    private String username;
    private String password;
    private int cumulativeScore, numTimesFooledOthers, numTimesFooledByOthers;
    private String userToken;
    private RequestHandler thisUsersHandler;
    private Game currentGame;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        cumulativeScore = 0;
        numTimesFooledByOthers = 0;
        numTimesFooledOthers = 0;
    }

    public User(String username, String password, int cumulativeScore, int numTimesFooledOthers, int numTimesFooledByOthers) {
        this.username = username;
        this.password = password;
        this.cumulativeScore = cumulativeScore;
        this.numTimesFooledByOthers = numTimesFooledByOthers;
        this.numTimesFooledOthers = numTimesFooledOthers;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCumulativeScore() {
        return cumulativeScore;
    }

    public void setCumulativeScore(int cumulativeScore) {
        this.cumulativeScore = cumulativeScore;
    }

    public int getNumTimesFooledOthers() {
        return numTimesFooledOthers;
    }

    public void setNumTimesFooledOthers(int numTimesFooledOthers) {
        this.numTimesFooledOthers = numTimesFooledOthers;
    }

    public int getNumTimesFooledByOthers() {
        return numTimesFooledByOthers;
    }

    public void setNumTimesFooledByOthers(int numTimesFooledByOthers) {
        this.numTimesFooledByOthers = numTimesFooledByOthers;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public void setThisUsersHandler(RequestHandler r) {
        thisUsersHandler = r;
    }

    public RequestHandler getThisUsersHandler() {
        return thisUsersHandler;
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }
}

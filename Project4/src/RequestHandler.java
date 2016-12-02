

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by ianzanger on 11/23/16.
 */
public class RequestHandler extends Thread {
    private Socket clientSocket;
    private int clientId;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private User thisUser;

    static HashMap<String, User> loggedInUsers = new HashMap<String, User>(); //collection of Users that are logged in, defined by their unique IDs
    //TODO: Implement Hashmap for games, using gameID as key.

    public RequestHandler(Socket clientSocket, int clientId) {
        this.clientId = clientId;
        this.clientSocket = clientSocket;
    }

    public void run() {
        connectWithClient();

    }

    private void connectWithClient() {
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

    private void registerOrLogin() {
        try {
            String latestLine = in.readLine();
            if (latestLine.contains("CREATENEWUSER")) {
                registerNewUser(latestLine);
            } else if (latestLine.contains("LOGIN")) {
                login(latestLine);
            } else {
                out.println("RESPONSE--CREATENEWUSER--INVALIDMESSAGEFORMAT");
                registerOrLogin();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //TODO: Implement login function, then call a new method for the next expected message type

    }

    private void login(String s){
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
            out.println("RESPONSE--LOGIN--SUCCESS--" + generateUserToken());
            thisUser = Server.registeredUsers.get(chosenUserIndex);
            //TODO: Start a new game or join a game

        }
    }

    private String generateUserToken() {
        //TODO: This
        //TODO: add userToken to User class?
        return "";
    }

    private void registerNewUser(String s) {
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

    private boolean isValidUsername(String username) {
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

    private boolean isValidPassword(String password) {
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

    private boolean userNameIsRegistered(String username) {
        for (int i = 0; i < Server.registeredUsers.size(); i ++) {
            if (Server.registeredUsers.get(i).getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


}

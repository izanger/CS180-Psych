import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import  java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ianzanger on 11/23/16.
 */
public class Server {
    public static ArrayList<User> registeredUsers = new ArrayList<>();
    public static ArrayList<String> wordleQuestions = new ArrayList<>();
    public static ArrayList<String> wordleAnswers = new ArrayList<>();

    //TODO: Implement hashmap to keep track of games, using gameToken as identifier?

    static HashMap<Integer, Socket> clientSockets = new HashMap<Integer, Socket>(); //collection of sockets to currently connected clients. Stored with client IDs as key
    static HashMap<Integer, RequestHandler> handlers = new HashMap<Integer, RequestHandler>(); //collection of request handlers -- one per client socket stored with same key as clientSockets
    static int nextClientID = 0; // id of next client


    //Reads UserDatabase file and populates the registeredUsers arraylist with Users. Will run at startup.
    public static void setUpRegisteredUsers() {
        String username;
        String password;
        int cumulativeScore;
        int numTimesFooledOthers;
        int numTimesFooledByOthers;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File("/Users/ianzanger/IdeaProjects/cs180/Project4/src/UserDatabase.txt"))); //Not working with just FileName, for some reason

            String latestLine;
            User user;
            while ((latestLine = in.readLine()) != null) {
                username = latestLine.substring(0, latestLine.indexOf(':'));
                latestLine = latestLine.replaceFirst(username + ":","");
                password = latestLine.substring(0, latestLine.indexOf(':'));
                latestLine = latestLine.replaceFirst(password + ":","");
                cumulativeScore = Integer.parseInt(latestLine.substring(0, latestLine.indexOf(':')));
                latestLine = latestLine.replaceFirst(cumulativeScore + "" + ":", "");
                numTimesFooledOthers = Integer.parseInt(latestLine.substring(0, latestLine.indexOf(':')));
                latestLine = latestLine.replaceFirst(numTimesFooledOthers + "" + ":", "");
                numTimesFooledByOthers = Integer.parseInt(latestLine);

                user = new User(username, password, cumulativeScore, numTimesFooledOthers, numTimesFooledByOthers);
                registeredUsers.add(user);

            }


        } catch (FileNotFoundException e) {
            System.out.println("Error accessing UserDatabase file.\n" + e.getMessage());

        } catch (IOException e) {
            System.out.println("Error reading line from file.\n" + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error closing BufferedReader of setUpRegisteredUsers()");
                }
            }
        }

    }

    //Reads WordleDeck file and populates wordleQuestions and wordleAnswers. Will run at startup.
    public static void setUpWordleArrayLists() {
        String latestLine;
        String question;
        String answer;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File("/Users/ianzanger/IdeaProjects/cs180/Project4/src/WordleDeck.txt")));
            while ((latestLine = in.readLine()) != null) {
                question = latestLine.substring(0, latestLine.indexOf(':'));
                answer = latestLine.substring(latestLine.indexOf(':') + 1, latestLine.length());
                wordleQuestions.add(question);
                wordleAnswers.add(answer);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error accessing WordleDeck file. \n" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading line from file.\n" + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Error closing BufferedReader of setUpWordleArrayLists()");
                }
            }
        }
    }

    //Updates UserDatabase file with new values for scores, registered users, etc. Should be called every time registeredUsers is added to or altered.
    //TODO: call every time registeredUsers is altered
    public static void writeUserDatabase() {
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(new File("UserDatabase.txt"), false ));
            for (int i = 0; i < registeredUsers.size(); i++) {
                User user = registeredUsers.get(i);
                out.write(user.getUsername() + ":" + user.getPassword() + ":" + user.getCumulativeScore() + ":" + user.getNumTimesFooledOthers() + ":" + user.getNumTimesFooledByOthers());
                out.newLine();

            }
        } catch (IOException e) {
            System.out.println("Error accessing UserDatabase file while writing.\n" + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing Bufferedwriter");

            }

        }


    }

    private static void serveClients(int portNumber) throws IOException {
        ServerSocket serverSocket = null;

        try{
            System.out.println("Creating Socket");
            serverSocket = new ServerSocket(portNumber);

            System.out.println("Listening");
            while(true){
                clientSockets.put(nextClientID, serverSocket.accept());
                System.out.println("Got a request: " + clientSockets.get(nextClientID).getPort());
                handlers.put(nextClientID, new RequestHandler(clientSockets.get(nextClientID), nextClientID));
                handlers.get(nextClientID).start();
                nextClientID++;
                checkForFinishedClients();
            }
        } catch (MalformedURLException e) {
            System.err.println("Unable to connect:\n" + e.getMessage());
            System.exit(3);
        } finally {
            if(serverSocket != null)
                serverSocket.close();
            for (Socket s : clientSockets.values())
                if(s != null && !s.isClosed())
                    s.close();
        }
    }

    private static void checkForFinishedClients(){
        Socket socket = null;
        RequestHandler handler = null;

        for(Integer cID : clientSockets.keySet()){
            socket = clientSockets.get(cID);
            if(socket.isClosed()){
                handler = handlers.get(cID);
                //should we wait for this handler thread to join?
                handlers.remove(cID);
                clientSockets.remove(cID);
            }
        }
    }

    public static void main(String[] args) {
        int portNumber;

        if(args.length !=1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }
        portNumber = Integer.parseInt(args[0]);

        try {
            setUpRegisteredUsers();
            setUpWordleArrayLists();
            serveClients(portNumber);
        } catch (IOException e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

    }



}

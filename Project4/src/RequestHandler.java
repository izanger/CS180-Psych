import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by ianzanger on 11/23/16.
 */
public class RequestHandler extends Thread {
    private Socket clientSocket;
    private int clientId;

    public RequestHandler(Socket clientSocket, int clientId) {
        this.clientId = clientId;
        this.clientSocket = clientSocket;
    }

    public void run() {
        connectWithClient();

    }

    private void connectWithClient() {
        PrintWriter out = null;
        BufferedReader in = null;
        String latestLine;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (IOException e) {
            System.err.println("IO Error with client" + clientId + "\n" + e.getMessage());
        }

    }


}

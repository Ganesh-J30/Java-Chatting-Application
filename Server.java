import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    ServerSocket server;
    Socket socket;
    int port = 7777;

    BufferedReader br;
    PrintWriter out;

    // This controls both threads
    private volatile boolean running = true;

    public Server() {
        try {
            server = new ServerSocket(port);

            System.out.println("Server is ready to accept connection");
            System.out.println("Waiting...");

            socket = server.accept();

            System.out.println("Client connected!");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream());

            startReading();
            startWriting();

        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    // READING DATA
    private void startReading() {

        Runnable reading = () -> {

            System.out.println("Reader Started");

            while (running) {
                try {

                    String msg = br.readLine();

                    // Connection closed
                    if (msg == null) {
                        System.out.println("Client disconnected.");
                        stopChat();
                        break;
                    }

                    // If the Client types exit
                    if (msg.equalsIgnoreCase("exit")) {
                        System.out.println("Client has terminated the chat");
                        stopChat();
                        break;
                    }

                    System.out.println("Client: " + msg);

                } catch (IOException e) {

                    if (running) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        };

        new Thread(reading, "Reader-Thread").start();
    }

    // WRITING DATA
    private void startWriting() {

        Runnable writing = () -> {

            System.out.println("Writer Started");

            // Creating BufferedReader to read from console
            BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));

            while (running) {
                try {

                    String msg = sc.readLine();

                    // If chat was already terminated
                    if (!running) {
                        break;
                    }

                    if (msg == null) {
                        stopChat();
                        break;
                    }

                    out.println(msg);
                    out.flush();

                    // If the Server types exit
                    if (msg.equalsIgnoreCase("exit")) {
                        System.out.println("You terminated the chat");
                        stopChat();
                        break;
                    }

                } catch (IOException e) {

                    if (running) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        };

        new Thread(writing, "Writer-Thread").start();
    }

    // STOP CHAT
    private void stopChat() {

        if (!running) {
            return;
        }

        running = false;

        System.out.println("Closing connection...");

        try {

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if (server != null && !server.isClosed()) {
                server.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Chat terminated.");
    }

    public static void main(String[] args) {

        System.out.println("Server is starting...");

        new Server();
    }
}

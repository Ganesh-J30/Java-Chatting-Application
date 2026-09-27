import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

public class Client {

    Socket socket;

    String host = "127.0.0.1";
    int port = 7777;

    BufferedReader br;
    PrintWriter out;

    // This controls both threads
    private volatile boolean running = true;

    public Client() {

        try {

            System.out.println("Sending Request to Server...");

            socket = new Socket(host, port);

            if (socket.isConnected()) {

                System.out.println("Connection Done!");

                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(socket.getOutputStream());

                startReading();
                startWriting();

            } else {
                System.out.println("Something Went Wrong, Try again!!!");
            }

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

                    // If Server disconnected
                    if (msg == null) {
                        System.out.println("Server disconnected.");

                        stopChat();
                        break;
                    }

                    // If the Server types exit
                    if (msg.equalsIgnoreCase("exit")) {

                        System.out.println("Server has terminated the chat");

                        stopChat();
                        break;
                    }

                    System.out.println("Server: " + msg);

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

                    if (!running) {
                        break;
                    }

                    if (msg == null) {
                        stopChat();
                        break;
                    }

                    out.println(msg);
                    out.flush();

                    // If the Client types exit
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

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Chat terminated.");
    }

    public static void main(String[] args) {

        new Client();
    }
}

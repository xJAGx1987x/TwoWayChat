import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DirectChatClient {
    private static final String EXIT_COMMAND = "/exit";

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);

        System.out.print("Enter Port to Listen: ");
        while (!keyboard.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid port number.");
            System.out.print("Enter Port: ");
            keyboard.next(); // Consume the invalid input
        }
        int listenPort = keyboard.nextInt();

        System.out.print("Enter Friend's IP address: ");
        String friendAddress = keyboard.next();

        System.out.print("Enter Friend's Port: ");
        while (!keyboard.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid port number.");
            System.out.print("Enter Port: ");
            keyboard.next(); // Consume the invalid input
        }
        int friendPort = keyboard.nextInt();

        new Thread(() -> startServer(listenPort)).start();
        startClient(friendAddress, friendPort);
    }

    private static void startServer(int listenPort) {
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            System.out.println("Server is listening on port " + listenPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Friend connected: " + clientSocket.getInetAddress());

                // Handle communication with the friend in a new thread
                new Thread(() -> handleFriend(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient(String friendAddress, int friendPort) {
        try (
                Socket friendSocket = new Socket(friendAddress, friendPort);
                BufferedReader input = new BufferedReader(new InputStreamReader(friendSocket.getInputStream()));
                PrintWriter output = new PrintWriter(friendSocket.getOutputStream(), true);
                Scanner userEntry = new Scanner(System.in)
        ) {
            System.out.println("\n* Connected to friend. Type '/exit' to end the chat. *");

            // Read messages from the friend in a separate thread
            new Thread(() -> {
                try {
                    String friendResponse;
                    while ((friendResponse = input.readLine()) != null) {
                        System.out.println("Friend: " + friendResponse);
                        if (friendResponse.equals(EXIT_COMMAND)) {
                            closeConnection(friendSocket, input, output);
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Send messages to the friend
            String userInput;
            while ((userInput = userEntry.nextLine()) != null) {
                if (userInput.equals(EXIT_COMMAND)) {
                    output.println(" User closed connecton: " + EXIT_COMMAND);
                    closeConnection(friendSocket, input, output);
                    return;
                }
                output.println(userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void closeConnection(Socket socket, BufferedReader input, PrintWriter output) {
        try {
            input.close();
            output.close();
            socket.close();
            System.out.println("\n* Connection closed. *");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleFriend(Socket friendSocket) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(friendSocket.getInputStream()))) {
            String friendMessage;
            while ((friendMessage = input.readLine()) != null) {
                System.out.println("Friend: " + friendMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// CS4065 - Programming Assignment 2
// Triet Pham
// Trien Dau

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try {
            while (!serverSocket.isClosed()){

                // Accepting new clients while the server socket is not closed
                Socket socket = serverSocket.accept();
                System.out.println("New Client Connected");

                //Create a client handler for each client
                ClientHandler clientHandler = new ClientHandler(socket);
                
                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        } catch (IOException e) {

        }
    }

    public void closeServerSocket(){
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Port number is: " + serverSocket.getLocalPort());
        Server server = new Server(serverSocket);
        server.startServer();

    }

}

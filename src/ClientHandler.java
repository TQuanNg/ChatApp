package src;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String clientUsername;
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private String chatroomName;

    public ClientHandler(Socket socket/* , String chatroomName*/) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //this.clientUsername = username; ///////////////
            this.clientUsername = reader.readLine();
            clientHandlers.add(this);
        } 
        catch (IOException e) {
            closeAll(socket, reader, writer);
        }
    }

    // broadcast to all user except yourself
    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(this.clientUsername)) {
                    clientHandler.writer.write(messageToSend);
                    clientHandler.writer.newLine();
                    clientHandler.writer.flush();
                }
            }
            catch (IOException e) {
                closeAll(socket, reader, writer);
            }
        }
    }

    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        closeBuffer(bufferedReader, bufferedWriter);
        closeSocket(socket);
    }
    
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeBuffer(BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = reader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeAll(socket, reader, writer);
                break;
            }
        }
    }
    
}

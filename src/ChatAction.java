package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAction {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String chatroomName;
    private boolean inChatroom;
    private Connection connection;
    private Account account;
    private Scanner scnr;
    private String userID;
	private Thread listeningThread;

    public ChatAction(Socket socket, Scanner scnr, Account account) {
        try {
            this.socket = socket;
            //this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = account.getUsername();
            this.account = account;
            this.scnr = scnr;
			connection = Utility.connectToDatabase();
            setID();
        } catch (Exception e) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    private void setID() {
		String username = this.username;
		System.out.println("IN set ID\n");

		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM accounts WHERE username LIKE \'" + username + "\';");
			if(rs.next()) {
				userID = rs.getString("id");
			}
			stmt.close();
			connection.close();
		}
		catch(Exception e) {
			Utility.onException(e);
		}
	}

    public void userPrompt() {
		String command = "";
		while(true) {
			System.out.println("Please select from the following options:");
			System.out.println("(J)oin, (C)reate, (A)ccount, (L)ogout");
			System.out.println("-----------------------------------------\n");
			
			if(scnr.hasNextLine()) {
				command = scnr.nextLine();
			}
			boolean returnCode = commandsAfterLogin(command);
			if (returnCode == false) {
				break;
			}
		}
	}

    public boolean commandsAfterLogin(String cmd) {	
		if(cmd.equals("J") || cmd.equals("j")) {
		    join();
			return true;
		}
		else if(cmd.equals("C") || cmd.equals("c")) {
			createChatRoom();
			return true;
		}
		else if(cmd.equals("A") || cmd.equals("a")) {
			account.manageAccount();
			return true;
		}
		else if(cmd.equals("L") || cmd.equals("l")) {
			account.logout();
			return false;
		}
		else {
			System.out.println("Unknown command");
			cmd = scnr.nextLine();
			return commandsAfterLogin(cmd);
			
		}
	}

    public void join() {
		System.out.print("-j ");
		if(scnr.hasNextLine()) {
			chatroomName = scnr.nextLine();
		}
		System.out.println(chatroomName);
		connection = Utility.connectToDatabase();
		if(ifChatroomExist(chatroomName)) {
			inChatroom = true;
			establishBufferConnection();
			updateChatroomColumnInAccTable(chatroomName);
			printChatroomWelcomeMsg(chatroomName);
            listenForMessages();
			handleChat();
		}
	}

    public void createChatRoom() {
		System.out.print("-c ");
		chatroomName = scnr.nextLine();
		System.out.println(chatroomName);

		connection = Utility.connectToDatabase();

		if(validateChatroomName(chatroomName) && ifChatroomExisted(chatroomName)) {
			inChatroom = true;
			establishBufferConnection();
			createNewChatroomTableSql(chatroomName);
			updateChatroomColumnInAccTable(chatroomName);
			printChatroomWelcomeMsg(chatroomName);
            listenForMessages();
			handleChat();
		}
		else {
			userPrompt();
		}
	}

	public void establishBufferConnection() {
		try {
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
		catch(Exception e) {
			e.printStackTrace();
			closeAll(socket, bufferedReader, bufferedWriter);
		}
	}

    private boolean validateChatroomName(String name) {
		boolean accepted = false;
		Pattern specialChar = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
		Matcher hasSpecial = specialChar.matcher(name);
		boolean result = hasSpecial.find();
		if(result == true) {
			System.out.println("Can not have weird character\n");
		}
		else {
			accepted = true;
			System.out.println("Valid chatroom name");
		}
		return accepted;
	}

    private boolean ifChatroomExisted(String name) {
		boolean accepted = false;
		
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM pg_catalog.pg_tables WHERE schemaname = 'public' "
					+ "AND tablename = \'" + name + "\';");
			if(rs.next()) {
				System.out.println("Chatroom " + name + " already existed\n");
			}
			else {
				accepted = true;
			}
			
			stmt.close();
		}
		catch(Exception e) {
		}
		return accepted;
	}

    private boolean ifChatroomExist(String name) {
		boolean isExisted = false;

		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM pg_catalog.pg_tables WHERE schemaname = 'public' "
					+ "AND tablename = \'" + name + "\';");
			if(rs.next()) {
				isExisted = true;
			}
			else {
				System.out.println("Chatroom does not exist\n");
				userPrompt();
			}
			stmt.close();
		}
		catch(Exception e) {
			Utility.onException(e);
		}
		return isExisted;
	}

    private void createNewChatroomTableSql(String chatroomName) {
		try {
			connection.setAutoCommit(false);
			Statement stmt = connection.createStatement();
			String sql = "CREATE TABLE " + chatroomName
					+ " (id INT NOT NULL, "
					+ "FOREIGN KEY (id) REFERENCES accounts (id), "
					+ "message VARCHAR(1024));";
			stmt.executeUpdate(sql);
			sql = "INSERT INTO " + chatroomName + 
					" (id) VALUES (" + userID + ");";
			stmt.executeUpdate(sql);
			stmt.close();
			connection.commit();
			System.out.println("Chatroom " + chatroomName + " created!");
		}
		catch(Exception e){
			Utility.onException(e);
		}
	}

    private void printChatroomWelcomeMsg(String chatroomName) {
		System.out.print("Welcome to " + chatroomName + ", "  + username);
		System.out.println(" (/help for commands)");
	}

    public void handleChat() {
        String newMsg;
		//System.out.println("Insideeesssssse\n");
        while (inChatroom) {
            if (scnr.hasNextLine()) {
                newMsg = scnr.nextLine().trim();
				
                if (newMsg.isEmpty()) {
                    continue; // Skip empty messages
                } else if (newMsg.charAt(0) != '/') {
                    sendMessage(newMsg); // Handle sending message
                } else {
                    commandInChatroom(newMsg); // Handle command
                }
            }
        }
    }

    public void sendMessage(String messageToSend) {
        try {
            // Send message to other clients
            bufferedWriter.write(username + ": " + messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Insert into database
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO " + chatroomName + " (id, message) VALUES ('" + userID + "', '" + messageToSend + "');");
            connection.commit();
            stmt.close();

        } 
        catch (IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
        catch(Exception e){
			Utility.onException(e);
		}
    }

    public void listenForMessages() {
        showHistory();
		
        listeningThread = new Thread(new Runnable() {
        @Override
        public void run() {
            String messageFromGroupChat;

            while (socket.isConnected() && inChatroom) {
                try {
					//System.out.println("Listening\n");
                    messageFromGroupChat = bufferedReader.readLine();
                    if (messageFromGroupChat != null) {
                        System.out.println(messageFromGroupChat);
                    }
                } catch (IOException e) {
                    closeAll(socket, bufferedReader, bufferedWriter);
					break;
                }
            }
        }
        });
		listeningThread.start();
    }

    private void showHistory() {
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT accounts.username, " + chatroomName + ".message"
					+ " FROM accounts JOIN " + chatroomName 
					+  " ON " + chatroomName + ".id = accounts.id;");
			while(rs.next()) {
				String username = rs.getString("username");
				String message = rs.getString("message");
				System.out.println(username + ":> " + message);
			}
			//System.out.println("That is all of history");
			rs.close();
			stmt.close();
		}
		catch(Exception e){
			Utility.onException(e);
		}
	}
 
    private void commandInChatroom(String cmd) {
		if(cmd.equals("/list")) {
			listUsers();
		}
		else if(cmd.equals("/leave")) {
			leaveChatRoom();
		}
		else if(cmd.equals("/history")) {
			showHistory();
		}
		else if(cmd.equals("/help")) {
			showHelpList();
		}
		else {
			System.out.println("Unkown command");
		}
	}

    private void listUsers() {
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT username FROM accounts "
					+ "WHERE chatroom = \'" + chatroomName + "\';");
			while(rs.next() ) {
				String userName = rs.getString("username");
				System.out.println("-" + userName);
			}
			stmt.close();
		}
		catch(Exception e) {
			Utility.onException(e);
		}
	}

    private void leaveChatRoom() {
		inChatroom = false;

		stopListeningThread();

		//closeAll(socket, bufferedReader, bufferedWriter); ////////////////////////////
		updateChatroomColumnInAccTable("NULL");
		try {
			connection.close();
		}
		catch (Exception e) {
			Utility.onException(e);
		}
	}

	public void stopListeningThread() {
		if (listeningThread != null && listeningThread.isAlive()) {
			listeningThread.interrupt(); // Interrupt the thread
		}
	}

    private void showHelpList() {
		System.out.println("");
		System.out.println("/list (Return a list of users currently in this chat room.)");
		System.out.println("/leave (Exits the chat room.)");
		System.out.println("/history (Print all past mesasges for the room.)");
		System.out.println("/help (Show this list)");
	}

    private void updateChatroomColumnInAccTable(String chatroomName) {
		try {
			connection.setAutoCommit(false);
			Statement stmt = connection.createStatement();
			String sql = "UPDATE accounts " + 
					"SET chatroom = \'" + chatroomName +
					"\' WHERE id = " + userID +";";
			stmt.executeUpdate(sql);
			stmt.close();
			connection.commit();
		}
		catch(Exception e){
			Utility.onException(e);
		}
	}

    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

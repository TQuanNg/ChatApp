package src;
import java.io.IOException;
import java.net.ServerSocket;
//import java.net.Socket;
//TAN MINH QUAN NGUYEN

// CHAT APPLICATION
public class Main {
	public static void main(String[] args) throws IOException {		
		
		System.out.println("Welcome to Chat App!");
		System.out.println();

		// run server anc client
		ServerSocket serverSocket = new ServerSocket(5000);
        Server server = new Server(serverSocket);
        server.startServer();
		
		// run client
		// Socket socket = new Socket("127.0.0.1", 5000);

		


		System.exit(0);
	}
}

//C:\Program Files\Java\postgresql-42.6.0.jar

//javac -cp ".;path_to_driver/postgresql-<version>.jar" src/*.java

//java -cp ".;C:\Program Files\Java\postgresql-42.6.0.jar" src.Main

//java -cp ".;C:\Program Files\Java\postgresql-42.6.0.jar" src.Account


// CUrrent problem 1/10/2025: when join or create chatroom, it show history and out the chat
// 1/13/2025: joining different chatroom does not make different, still hearing from other chatroom
	// potential fix: socket created in Account should be removed or moved to where joining/creating chatroom
	// then a new socket is established, clienthandler need to be modified to work with chatroom name

/*Reference:
https://www.codejava.net/java-se/jdbc/how-to-use-scrollable-result-sets-with-jdbc
for line 292, ChatRoom class
//----
 
 https://stackoverflow.com/questions/1795402/check-if-a-string-contains-a-special-character
 for line 217, ChatRoom class
 //----
  
 https://www.geeksforgeeks.org/a-group-chat-application-in-java/#
 for Thread, ChatRoom class
 //----
 
https://www.tutorialspoint.com/java-resultset-previous-method-with-example#
for lines 307, 308, ChatRoom class
*/

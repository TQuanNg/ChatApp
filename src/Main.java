package src;
import java.io.IOException;
import java.net.ServerSocket;

// Server Start
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

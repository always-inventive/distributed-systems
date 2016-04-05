import java.io.*;
import java.net.*;


public class DummyClient {
	
	public static void main(String [] args){
		new DummyClient().startClient();
	}
	
	public void startClient() {
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		String message;
		try {
			
			requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 1234);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());

			try {
				 
				message = (String) in.readObject();
				System.out.println("Server> " + message);

				out.writeObject(40.79);
				out.flush();

				out.writeObject(-74.0);
				out.flush();
				
				out.writeObject(40.791);
				out.flush();

				out.writeObject(-73.894);
				out.flush();
				
				
			} catch (ClassNotFoundException e1) {
				System.err.println("Data Received to Unknown Format");
			}

		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}

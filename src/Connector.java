import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connector {
	public static Object[] receiveData(int port, int dataItems) { 
		Object[] temp = null;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			temp = receiveDataFromConnection(serverSocket.accept(), dataItems);
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
			if (serverSocket != null)
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return temp;
	}

	public static Object[] receiveDataFromConnection(Socket connection, int dataItems) {
		Object[] data = null;
		try {
			ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
			data = new Object[dataItems];
			
			for (int i=0 ; i<data.length; i++){
				data[i] = in.readObject();
			}
			
			in.close();
			connection.close();
		}
		catch(ClassNotFoundException cnfe){
			System.err.println("Data received in unknown format.");
			cnfe.printStackTrace();
		}
		catch (IOException ioException){
			ioException.printStackTrace();
		}
		return data;
	}
	
	public static void sendData(String host, int port, Object... data){
		Socket requestSocket = null;
		
		try {
			requestSocket = new Socket(InetAddress.getByName(host), port);
			sendDataThroughConnection(requestSocket, data);
		} 
		catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} 
		catch (IOException ioException) {
			ioException.printStackTrace();
		} 
		finally {
			try {
				requestSocket.close();
			} 
			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	
	public static void sendDataThroughConnection(Socket connection, Object... data){
		ObjectOutputStream out = null;
		
		try {
			out = new ObjectOutputStream(connection.getOutputStream());
			for (Object o : data ){
				out.writeObject(o);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				out.close();
			}
			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FSCheckinApp {
	public static final int DEFAULT_SOCKET = 4321;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(DEFAULT_SOCKET);

			while (true) {
				Socket connection = serverSocket.accept();

				Thread t = new Master(connection);
				t.start();
			}
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		} 
		finally {
			try {
				serverSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Worker {
	
	private	int minLatitude;
	private int minLongitude;
	private int maxLatitude;
	private int maxLongitude;
	private int reducerKey; //max number of results to get from reducer
	
	public Worker() {
		initialize();
		waitForTasksThread();
	}
	
	
	public void initialize() {
		
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Input map coordinates: ");
		minLatitude = scanner.nextInt();
		minLongitude = scanner.nextInt();
		maxLatitude = scanner.nextInt();
		maxLongitude = scanner.nextInt();
		
		System.out.println("Input reducer key (max # of spots to show)");
		reducerKey = scanner.nextInt();
		
		 
		//apostolh stous mappers (xreiazomai dieuthunseis twn mappers, isws me port 0 automath anathesh)
		 
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			
			requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 1234);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());

			out.writeObject(minLatitude);
			out.flush();
			
			out.writeObject(minLongitude);
			out.flush();
			
			out.writeObject(maxLatitude);
			out.flush();
			
			out.writeObject(maxLongitude);
			out.flush();
			
			
		}
		catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} 
		catch (IOException ioException) {
			ioException.printStackTrace();
		} 
		finally {
			try {
				in.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		
		scanner.close();
		
		
	}
	
	

	public void waitForTasksThread() {
		openServer();
		/* perimenei prwta notification apo mappers,
		   meta stelnei acknowledgement ston reducer (mazi me to key),
		   meta perimenei apotelesmata
		*/
	}
	
	private void openServer() {
		ServerSocket sSocket = null;
		Socket connection = null;
		boolean map1, map2, map3;
		
		try{
			sSocket = new ServerSocket(0); //alla pws tha kserei o client to port?
			while(true){
				connection = sSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
				out.writeObject("Connection Successful.");
				out.flush();
				try {
					
					map1 = (boolean) in.readObject(); //boolean apo mappers x3
					map2 = (boolean) in.readObject();
					map3 = (boolean) in.readObject();
					if(map1 && map2 && map3) {
						out.writeObject(reducerKey); //prepei na paei ston reducer, pros to parwn to outputstream paei ston (enan) mapper
						
					}
					
					in.readObject(); //apotelesmata reducer (pali, xreiazetai diaforetiko inputstream)
					
				}
				catch(ClassNotFoundException cnfe){
					System.err.println("Data received in unknown format. ");
					cnfe.printStackTrace();
				}
				in.close();
				out.close();
				connection.close();
			}
			
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		finally{
			try{
				sSocket.close();
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
		}
		
	}
	
	
}

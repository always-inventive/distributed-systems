import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Worker {

	private int reducerKey; //max number of results to get from reducer
	
	private	int minLatitude;
	private int minLongitude;
	private int maxLatitude;
	private int maxLongitude;
	
	private Coords coords1;
	private Coords coords2;
	private Coords coords3;
	
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
		Split();
		
		System.out.println("Input reducer key (max # of spots to show)");
		reducerKey = scanner.nextInt();
		
		 
		//apostolh stous mappers (xreiazomai dieuthunseis twn mappers )
		 
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			
			requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 1234);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());

			out.writeObject(coords1);
			out.flush();
			
			out.writeObject(coords2);
			out.flush();
			
			out.writeObject(coords3);
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
					out.writeObject(coords1);
					out.flush();
					
					out.writeObject(coords2);
					out.flush();
					
					out.writeObject(coords3);
					out.flush();
					
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
	
	public void Split() {
		int diffLatitude = maxLatitude - minLatitude;
		coords1 = new Coords(minLatitude, minLongitude, minLatitude+(diffLatitude/3), maxLongitude);
		coords2 = new Coords(minLatitude+(diffLatitude/3), minLongitude, minLatitude+(2*diffLatitude/3), maxLongitude);
		coords3 = new Coords(minLatitude+(2*diffLatitude/3), minLongitude, maxLatitude, maxLongitude);
	}

}

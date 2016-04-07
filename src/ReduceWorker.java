import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.List;

public class ReduceWorker extends Worker {


	private List<Map<String, Long>> maps;
	private int key;
	
	public ReduceWorker() {
		
		//start server, get Maps from mappers: map1, map2, map3
		openServer();
		key = waitForMasterAck();
		sendResults(reduce(key, maps));
	}

	public int waitForMasterAck() {
		return key;
	}
	
	public Map<String, Long> reduce(int key, List<Map<String, Long>> maps) { 
		Map<String, Long> result = null;
		
		for(Map<String, Long> map : maps) {
			result = Stream.concat(result.entrySet().stream() , map.entrySet().stream())
					.parallel()
					.collect(Collectors.groupingByConcurrent(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));
		}
		result.entrySet().stream().parallel().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(key);
		return result;
	}
	
	public void sendResults(Map<String, Long> output) {
		
	}
	
	private void  openServer() {
		ServerSocket sSocket = null;
		Socket connection = null;
		
		try{
			sSocket = new ServerSocket(1234);
			while(true){
				connection = sSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
				out.writeObject("Connection Successful.");
				out.flush();
				try {
					//read maps, to list
					maps.add((Map<String, Long>)in.readObject());
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

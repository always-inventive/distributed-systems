import java.net.Socket;
import java.util.List;

public class Master extends Thread {
	public static final int DEFAULT_LISTENING_PORT = 1234;
	public static final String DEFAULT_MASTER_ADDRESS = "127.0.0.1";
	private Socket connection;
	private String[] workerAddresses = { "127.0.0.1" , "127.0.0.1", "127.0.0.1", "127.0.0.1"};
	private int[] workerPorts = {1896, 1897, 1898, 1899};
	
	public Master(Socket connection){
		this.connection = connection;
	}

	@Override
	public synchronized void run() {
		Double[] coordinates = new Double[4];
		
		Object[] temp = Connector.receiveDataFromConnection(connection, 4);
		for (int i=0; i<temp.length; i++)
			coordinates[i] = (Double) temp[i]; 
		
		ReduceJob reducer = new ReduceJob();
		// TODO get Master's local address
		String reducerAddress = workerAddresses[workerAddresses.length-1];
		Connector.sendData(reducerAddress, workerPorts[3], reducer, DEFAULT_MASTER_ADDRESS, DEFAULT_LISTENING_PORT);
		
		// compute the coordinates for the mappers
		int workers = workerAddresses.length;
		double[][] mapperCoords = new double[workers][4];
		double longStep = (coordinates[2]-coordinates[0])/workers;
		double latStep = (coordinates[3]-coordinates[1])/workers;
		
		for (int i = 0; i < mapperCoords[0].length; i++){
			mapperCoords[i][0] = coordinates[0]+ longStep*i;
			mapperCoords[i][1] = coordinates[1]+ latStep*i;
			mapperCoords[i][2] = coordinates[0]+ longStep*(i+1);
			mapperCoords[i][3] = coordinates[1]+ latStep*(i+1);
		}
		
		// then create the mappers
		for (int i=0; i<workerAddresses.length-1; i++){
			MapJob mapper = new MapJob();
			mapper.setCoordinates(mapperCoords[i][0], mapperCoords[i][1], mapperCoords[i][2], mapperCoords[i][3] );
			Connector.sendData(workerAddresses[i], workerPorts[i], mapper, reducerAddress, ReduceJob.DEFAULT_LISTENING_PORT);
		}
		
		// Finally, return the results
		Object o = Connector.receiveData(DEFAULT_LISTENING_PORT, 1);
		Connector.sendDataThroughConnection(connection, o);
	}
}

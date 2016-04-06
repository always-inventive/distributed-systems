import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MapJob extends Thread {

	private List<Checkin> checkins;
	private Worker worker;
	private double minLongitude, maxLongitude, minLatitude, maxLatitude;
	
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://83.212.117.76:3306/ds_systems_2016?user=omada80&password=omada80db";
	
	public MapJob(Worker worker) {
		this.worker = worker;
		checkins = new ArrayList<Checkin>();	
	}
	
	@Override
	public void run() {
		// Get the bounds
		getBounds();
		
		// Execute query to database
		doQuery();
		
		// Execute map function and notify done
		map();
		worker.notifyMasterOfCompletion();
		sendToReducers(null);
	}

	public Map<String, Long> map() {
		Stream<Checkin> stream = checkins.stream().parallel().distinct().filter(p -> p.getUrl()!=null);
		Map<String, Long> map = stream
				.collect(Collectors.groupingByConcurrent(Checkin::getKey, Collectors.counting()));
		map.entrySet().stream().parallel().sorted(Map.Entry.<String,Long>comparingByValue().reversed());
		
		return map;
	}
	
	@SuppressWarnings("unused")
	private void notifyMaster() {
		
	}
	
	public void sendToReducers(Map<Integer, Object> output) {
		
	}
	
	private void getBounds() {
		ServerSocket serverSocket = null;
		Socket connection = null;
		
		try {
			serverSocket = new ServerSocket(1234);
			connection = serverSocket.accept();
			ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
			out.writeObject("Connection to client successful.");
			out.flush();

			minLatitude = (double) in.readObject();
			minLongitude = (double) in.readObject();
			maxLatitude = (double) in.readObject();
			maxLongitude = (double) in.readObject();	
			
			in.close();
			out.close();
			connection.close();
			serverSocket.close();
		}
		catch(ClassNotFoundException cnfe){
			System.err.println("Data received in unknown format.");
			cnfe.printStackTrace();
		}
		catch (IOException ioException){
			ioException.printStackTrace();
		}
		finally {
			try {
				if (serverSocket != null)
					serverSocket.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 	
 	private void doQuery(){
 		Connection connection = null;
 		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL);
			System.out.println("Connected to database...");

			System.out.println("Creating statement...");
			stmt = connection.createStatement();
			System.out.println("Statement is created.");
			
			int cores = Runtime.getRuntime().availableProcessors();
			System.out.println(cores);
			
			String query =String.format("Select POI, photos"
					+ " from ds_systems_2016.checkins "
					+ "where (latitude between %f and %f) and (longitude between %f and %f);"
					,minLatitude, maxLatitude, minLongitude, maxLongitude);
			
			rs = stmt.executeQuery(query);
			
			// Add query results to the map
			while(rs.next()){
				String POI = rs.getString("POI");
				String url = rs.getString("photos");
				checkins.add(new Checkin(POI, url.equals("Not exists")? null: url));
			}
		}
		catch(ClassNotFoundException e){
			System.err.println("Database class wasn't found.");
			e.printStackTrace();
		}
		catch(SQLException sqlException ){
			sqlException.printStackTrace();
		}
		finally {
			try{
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (connection != null)
					connection.close();
			}	
			catch (SQLException sqlException){
				sqlException.printStackTrace();
			}
		}
	}
}

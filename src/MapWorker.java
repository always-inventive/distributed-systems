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


public class MapWorker extends Thread {

	private List<Checkin> checkins;
	private Worker worker;
	private double minLongitude, maxLongitude, minLatitude, maxLatitude;
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://83.212.117.76:3306/ds_systems_2016?user=omada80&password=omada80db";
	
	public MapWorker(Worker worker) {
		this.worker = worker;
		checkins = new ArrayList<Checkin>();	
	}
	
	@Override
	public void run() {
		ServerSocket s = openServer();
		Connection c = connectToDatabase(s);
		doQuery(c);
		closeDatabaseConnection(c);
		closeServer(s);
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
	
	private ServerSocket openServer() {
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(1234);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		return serverSocket;
	}
	
	private void closeServer(ServerSocket serverSocket){
		try{
			serverSocket.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
 	private Connection connectToDatabase(ServerSocket s){
		Connection connection = null;
		try{
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL);
			System.out.println("Connected to database...");
		}
		catch(ClassNotFoundException e){
			System.err.println("Database class wasn't found.");
			e.printStackTrace();
		}
		catch (SQLException e){
			System.err.println("An SQL exception was thrown.");
			e.printStackTrace();
		}
		return connection;
	}
 	
 	private void closeDatabaseConnection(Connection c){
 		try {
			c.close();
		} 
 		catch (SQLException sQLException) {
 			sQLException.printStackTrace();
		}
 	}
 	
	@SuppressWarnings("unused")
	private void getBounds(ServerSocket sSocket) throws IOException {
		Socket connection = null;
		
		while(true){
			connection = sSocket.accept();
			ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
			out.writeObject("Connection Successful.");
			out.flush();

			try{
				minLatitude = (double) in.readObject();
				minLongitude = (double) in.readObject();
				maxLatitude = (double) in.readObject();
				maxLongitude = (double) in.readObject();	
			}
			catch(ClassNotFoundException cnfe){
				System.err.println("Data received in unknown format. ");
				cnfe.printStackTrace();
			}
			in.close();
			out.close();
		}
	}
	
	private void doQuery(Connection connection){
		Statement stmt = null;
		ResultSet rs = null;
		try{
			System.out.println("Creating statement...");
			stmt = connection.createStatement();
			System.out.print("Statement is created");
			
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
		catch(SQLException se ){
			se.printStackTrace();
		}
		finally {
			try{
				if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			}
			catch (SQLException sqlException){
				sqlException.printStackTrace();
			}
			
		}
	}
}

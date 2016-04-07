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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MapWorker extends Worker {

	private List<Checkin> checkins; 	//adeio pros to parwn
	double minLongitude, maxLongitude, minLatitude, maxLatitude; //suntetagmenes
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://83.212.117.76:3306/ds_systems_2016?user=omada80&password=omada80db";
	
	
	public MapWorker() {
		checkins = new ArrayList<Checkin>();
		openServer();
		notifyMaster();
		sendToReducers(map());
	}
	
	public Map<String, Long> map() {
		Stream<Checkin> stream = checkins.stream()
				.parallel().distinct().filter(p -> p.getUrl()!=null);
		Map<String, Long> map = stream.map(p -> p.getKey())
				.collect(Collectors.groupingByConcurrent(Function.identity(), Collectors.counting()));
		map.entrySet().stream().parallel().sorted(Map.Entry.<String,Long>comparingByValue().reversed());
		
		return map;
	}
	
	public void notifyMaster() {
		
	}
	
	public void sendToReducers(Map<String, Long> output) {
		
	}
	
	private void openServer() {
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
				try{
					minLatitude = (double) in.readObject();
					minLongitude = (double) in.readObject();
					maxLatitude = (double) in.readObject();
					maxLongitude = (double) in.readObject();	

					connectToDatabase();
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
	
	private void connectToDatabase(){
		Connection conn = null;
		Statement stmt = null;
		try{
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL);
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			doQuery(stmt); // executes query to database.
			
			
			stmt.close();
			conn.close();
			
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
					stmt.close();
		    }catch(SQLException se2){}// nothing we can do
		    try{
		      if(conn!=null)
		    	  conn.close();
		    }catch(SQLException se){
		    	se.printStackTrace();
		    }//end finally try
		}//end try
	}
	
	private void doQuery(Statement statement) throws SQLException{
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println(cores);
		String sql =String.format("Select POI, photos"
				+ " from ds_systems_2016.checkins "
				+ "where (latitude between %f and %f) and (longitude between %f and %f);"
				,minLatitude, maxLatitude, minLongitude, maxLongitude);
		
		ResultSet rs = statement.executeQuery(sql);
		while(rs.next()){
			String POI = rs.getString("POI");
			String url = rs.getString("photos");
			checkins.add(new Checkin(POI, url.equals("Not exists")? null: url));
		}
		rs.close();
	}
	
	
}

import java.util.List;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MapJob extends Thread implements Job, Serializable {

	private List<Checkin> checkins;
	private Worker worker;
	private double minLongitude, maxLongitude, minLatitude, maxLatitude;
	
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://83.212.117.76:3306/ds_systems_2016?user=omada80&password=omada80db";
	
	public MapJob() {
		checkins = new ArrayList<Checkin>();	
	}
	
	public void setWorker(Worker worker){
		this.worker = worker;
	}
	public void setCoordinates(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude){
		this.minLongitude = minLongitude;
		this.minLatitude = minLatitude; 
		this.maxLongitude = maxLongitude;
		this.maxLatitude = maxLatitude;
	}
	
	@Override
	public void run() {
		// Execute query to database
		doQuery();
		
		// Execute map function and notify done
		map();
		isDone();
	}

	public void isDone() {
		worker.notifyDone();
	}
	
	public Object getResults(){
		return checkins;
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

	private Map<String, Long> map() {
		Stream<Checkin> stream = checkins.stream().parallel().distinct().filter(p -> p.getUrl()!=null);
		Map<String, Long> map = stream
				.collect(Collectors.groupingByConcurrent(Checkin::getKey, Collectors.counting()));
		map.entrySet().stream().parallel().sorted(Map.Entry.<String,Long>comparingByValue().reversed());
		
		return map;
	}
}

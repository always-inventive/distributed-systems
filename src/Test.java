
public class Test {
	
	public static void main(String [] args){
		Worker worker = new Worker();
		MapJob mapJob = new MapJob();
		Connector.sendData("127.0.0.1", 1234, 40.79, -74.0, 40.791, -73.894 );		
	}
}

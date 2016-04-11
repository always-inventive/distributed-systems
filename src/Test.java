
public class Test {
	
	@SuppressWarnings("unused")
	public static void main(String [] args){
		Worker worker1 = new Worker();
		worker1.setPort(1896);
		worker1.start();
		Worker worker2 = new Worker();
		worker2.setPort(1897);
		worker2.start();
		Worker worker3 = new Worker();
		worker3.setPort(1898);
		worker3.start();
		Worker worker4 = new Worker();
		worker4.setPort(1899);
		worker4.start();
		FSCheckinApp test = new FSCheckinApp();
		test.start();
		Connector.sendData("127.0.0.1", FSCheckinApp.DEFAULT_SOCKET, 40.79, -74.0, 40.791, -73.894 );		
	}
}

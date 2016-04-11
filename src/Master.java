import java.net.Socket;

public class Master extends Thread {
	private Socket connection;
	
	public Master(Socket connection){
		this.connection = connection;
	}

	@Override
	public synchronized void start() {
		super.start();
	}
}

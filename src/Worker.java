import java.net.Socket;

public class Worker {
	private Runnable job;
	private Socket socket;
	
	public Worker() {
		//TODO implement socket
	}
	
	public void setJob(Runnable job){
		this.job = job;
	}
	
	public void startWorking() {
		job.run();
	}
	public void notifyMasterOfCompletion(){
		
	}
	public Socket getSocket(){
		return socket;
	}
}

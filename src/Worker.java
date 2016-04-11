
public class Worker {
	public static final String DEFAULT_ADDRESS = "127.0.0.1";
	public static final String DEFAULT_MASTER = "127.0.0.1";
	public static final String DEFAULT_RESULTS_ADDRESS = "127.0.0.1";
	public static final int DEFAULT_INCOMING_PORT = 1234;
	public static final int DEFAULT_OUTGOING_PORT = 1234;
	
	private Job job;
	
	public Worker() {
		initialize();
	}

	public void setJob(Job job){
		this.job = job;
	}

	public void startWorking() {
		job.run();
	}

	public void notifyDone(){
		sendResults(DEFAULT_RESULTS_ADDRESS, DEFAULT_OUTGOING_PORT, this.job.getResults());
		initialize();
	}

	public void sendResults(String resultReceiver, int port, Object results){
		Connector.sendData(resultReceiver, port, results);
		initialize();
	}

	private void initialize(){
		this.job = (Job) Connector.receiveData(DEFAULT_INCOMING_PORT, 1)[0];
		startWorking();
	}
}

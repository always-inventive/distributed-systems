
public class Worker {
	public static final int DEFAULT_PORT = 1234;
	
	private Job job;
	private String forwardAddres;
	private int forwardPort;
	
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
		sendResults(forwardAddres, forwardPort, this.job.getResults());
		initialize();
	}

	public void sendResults(String resultReceiver, int port, Object results){
		Connector.sendData(resultReceiver, port, results);
		initialize();
	}

	private void initialize(){
		Object[] data = Connector.receiveData(DEFAULT_PORT, 3);
		this.job = (Job) data[0];
		this.job.setWorker(this);
		this.forwardAddres = (String) data[1];
		this.forwardPort = (int) data[2];
		startWorking();
	}
}

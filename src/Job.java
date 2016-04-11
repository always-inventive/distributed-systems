
public interface Job extends Runnable {
	public void setWorker(Worker worker);
	public void run();
	public void isDone();
	public Object getResults();
}

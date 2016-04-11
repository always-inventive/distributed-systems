
public interface Job extends Runnable {
	public void run();
	public void isDone();
	public Object getResults();
}


public class DummyMapper {

	public static void main(String[] args) {
		Worker w = new Worker();
		MapJob mj = new MapJob(w);
		w.setJob(mj);
		w.startWorking();
	}

}

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

public class ReduceJob implements Job {
	public final static int DEFAULT_LISTENING_PORT = 1234;
	private List<Map<String, Long>> maps;
	private int key;
	private Worker worker;
	
	public ReduceJob() {
	}
	
	@Override
	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Map<String, Long>[] mapArray = (Map<String, Long>[]) Connector.receiveData(Worker.DEFAULT_PORT, 3);
		maps = new ArrayList<Map<String, Long>>();
		for (Map<String, Long> map : mapArray)
			maps.add(map);
		reduce(0, maps);
		isDone();
	}

	@Override
	public void isDone() {
		worker.notifyDone();
	}

	@Override
	public Object getResults() {
		return maps;
	}

	private Map<String, Long> reduce(int key, List<Map<String, Long>> maps) { 
		Map<String, Long> result = null;
		
		for(Map<String, Long> map : maps) {
			result = Stream.concat(result.entrySet().stream() , map.entrySet()
					.stream()).parallel()
					.collect(Collectors.groupingByConcurrent(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));
		}
		result.entrySet().stream().parallel().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(key);
		return result;
	}	
}

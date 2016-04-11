import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReduceJob implements Job, Serializable{
	public final static int DEFAULT_LISTENING_PORT = 1568;
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
		maps = new ArrayList<Map<String, Long>>();
		Object[] mapArray = Connector.receiveData(ReduceJob.DEFAULT_LISTENING_PORT, 3);
		for (Object map : mapArray)
			maps.add((Map<String, Long>) map);
		reduce(3, maps);
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

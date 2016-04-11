import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

public class ReduceWorker implements Job {
	private List<Map<String, Long>> maps;
	private int key;
	private Worker worker;
	
	public ReduceWorker(Worker worker) {
		this.worker = worker;
	}
	
	@Override
	public void run() {
		maps = (List<Map<String, Long>>) Connector.receiveData(Worker.DEFAULT_INCOMING_PORT, 1)[0];		
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

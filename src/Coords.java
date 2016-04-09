import java.util.Arrays;
import java.util.List;

public class Coords {
	private double minLatitude;
	private double minLongitude;
	private double maxLatitude; 
	private double maxLongitude;
	
	public Coords(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
		this.minLatitude = minLatitude;
		this.minLongitude = minLongitude;
		this.maxLatitude = maxLatitude;
		this.maxLongitude = maxLongitude;
	}
	
	public List<Double> getCoords() {
		List<Double> coords = Arrays.asList(minLatitude, minLongitude, maxLatitude, maxLongitude);
		return coords;
	}
	
}

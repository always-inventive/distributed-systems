import java.io.Serializable;

public class Checkin implements Serializable {
	
	private String POI;
	private String url;
	
	//de kserw an tha xreastei, to ekana skeptomenos oti oi mappers tha kratane kapou ton arithmo twn checkin se kathe shmeio
	public Checkin(String key, String url) {
		this.POI = key;
		this.url = url;
	}
	
	public void setKey(String key) {
		this.POI = key;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	 
	public String getKey() {
		return POI;
	}
	
	public String getUrl() {
		return url;
	}
	

}

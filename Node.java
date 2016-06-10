import java.util.Comparator;

// Earth Radius
import fu.geo.Spherical;

public class Node implements Comparator<Node>{

	public int [] links;
	public int linkIDToPredecessor;
	public Node predecessor;
	public int crossingID;
	public int domainID;
	public int lat;
	public int lon;

	private double c;
	private double f;
	private double g;
	private double h;
	
	public static Spherical spherical;
	double speedLimit = 0.0;

	public Node(int _crossingID, int _lat, int _lon, double stop_lat_d, double stop_lon_d) {

		this.crossingID = _crossingID;
		this.lat = _lat;
		this.lon = _lon;
		this.c = 0;
		this.f = 0;
		this.g = 0;
		
		double beeLine = spherical.greatCircleMeters(Helper.convertCoordToDouble(lat), Helper.convertCoordToDouble(lon), stop_lat_d, stop_lon_d);
		this.h = Helper.getLinkCostsInSeconds(beeLine, Helper.MAX_SPEED_FOR_LINEAR_DISTANCE);
	}

	@Override
	public int compare(Node n1, Node n2) {

		 // n1 > n2 = 1 => sort asc
		 if(n1.getValue_f() > n2.getValue_f()) return 1;
		 else if(n1.getValue_f() < n2.getValue_f()) return -1;
		 else return 0;
	}

	// Setter and Getter
	public void setValue_h(double h) {
		this.h = h;
	}

	public double getValue_h() {
		return this.h;
	}

	public void setValue_c(double c) {
		this.c = c;
	}

	public double getValue_c() {
		return this.c;
	}

	public void setValue_g(double g) {
		this.g = g;
	}

	public double getValue_g() {
		return this.g;
	}

	public void setValue_f(double f) {
		this.f = f;
	}

	public double getValue_f() {
		return this.f;
	}
	
	public double getSpeedLimit() {
		return this.speedLimit;
	}
	/**
	 * sets costs of predecessor to node
	 */
	private void c()
	{
		//speedLimit = setSpeedLimit(predecessor, linkIDToPredecessor);
		c = c(predecessor, linkIDToPredecessor);
	}
	
	public double c(Node pre, int linkIDToPre){
		if (pre == null){
			return 0;
		}
		else{
			double distance = (double) Navigate.nd.getLengthMeters(linkIDToPre);
			double speed = setSpeedLimit(pre, linkIDToPre);
			
			return Helper.getLinkCostsInSeconds(distance, speed);
		}
	}
	
	public double setSpeedLimit(Node pre, int linkIDToPre){
		double speed = (double) Navigate.nd.getMaxSpeedKMperHours(linkIDToPre);
		
		//no explicit speed limitation
		if(speed == 0.0)
		{
			//get speedlimitation from type of road
			speed = Helper.getDefaultSpeed(pre,linkIDToPre);
			
			//System.out.println("speed linktype" + speed);
		}
		else{
			//System.out.println("speed link" + speed);
		}
		return speed;
	}
	/**
	 * sets costs of start to node
	 */	
	private void g(){
		//g = pre.getValue_g() + c;
		g = g(predecessor, linkIDToPredecessor);
	}
	public double g(Node pre, int linkIDToPre){
		if (pre ==null)
		{
			return 0;
		}
		else{
			return pre.getValue_g() + c(pre, linkIDToPre);
		}
	}
	/**
	 * sets estimated costs of start to end by crossing node
	 */
	private void f(){
		//f = g + h;
		f = f(predecessor, linkIDToPredecessor);
	}
	public double f(Node pre, int linkIDToPre){
		return g(pre, linkIDToPre) + h;
	}
	
	/**
	 * sets new predecessor and updates all depending values
	 */
	public void setPredecessor(Node newPredecessor, int newLinkIDToPredecessor) {
		if(predecessor == null || predecessor.crossingID != newPredecessor.crossingID)
		{
		predecessor = newPredecessor;
		linkIDToPredecessor = newLinkIDToPredecessor;
		domainID = Navigate.nd.getDomainID(Navigate.nd.getReverseLink(linkIDToPredecessor));
		speedLimit = setSpeedLimit(newPredecessor, newLinkIDToPredecessor);
		c();
		g();
		f();
		}
		
	}
	public void setPredecessor(Node newPredecessor, int newLinkIDToPredecessor, double newC, double newG, double newF) {
		if(predecessor == null || predecessor.crossingID != newPredecessor.crossingID)
		{
		predecessor = newPredecessor;
		linkIDToPredecessor = newLinkIDToPredecessor;
		domainID = Navigate.nd.getDomainID(Navigate.nd.getReverseLink(linkIDToPredecessor));
		speedLimit = setSpeedLimit(newPredecessor, newLinkIDToPredecessor);
		this.c = newC;
		this.g = newG;
		this.f = newF;
		}
		
	}

	public String toString() {

		//String eol = System.getProperty("line.separator");
		return "crossingID: " + this.crossingID +
				" linkIDToPredecessor: " + this.linkIDToPredecessor +
				//" lat: " + this.lat +
				//" lon: " + this.lon +
				" C: " + this.getValue_c() +
				" H: " +this.getValue_h() +
				" G: " +this.getValue_g() +
				" F: " +this.getValue_f();
	}
}

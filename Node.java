import java.util.Comparator;

// Earth Radius
import fu.geo.Spherical;

public class Node implements Comparator<Node>{

	public int [] links;
	public int linkIDToPredecessor;
	public Node predecessor;
	public int crossingID;
	public int domainID;

	private double c;
	private double f;
	private double g;
	private double h;
	private double speed;
	private double distance;

	public Node(int _crossingID, double _lat, double _lon, double stop_lat_d, double stop_lon_d) {

		this.crossingID = _crossingID;
		this.c = 0;
		this.f = 0;
		this.g = 0;
		this.h = Helper.getLinkCostsInSeconds(Spherical.greatCircleMeters(_lat, _lon, stop_lat_d, stop_lon_d), Helper.MAX_SPEED_FOR_LINEAR_DISTANCE);
	}

	@Override
	public int compare(Node n1, Node n2) {

		// n1 > n2 = 1 => sort asc
		if(n1.getValue_f() > n2.getValue_f()) return 1;
		else if(n1.getValue_f() < n2.getValue_f()) return -1;
		else return 0;
	}

	public double getValue_h() {
		return this.h;
	}

	public double getValue_c() {
		return this.c;
	}

	public double getValue_g() {
		return this.g;
	}

	public double getValue_f() {
		return this.f;
	}

	/**
	* sets costs of node pre to node
	*/
	public double c(Node pre, int linkIDToPre){
		if (pre == null){
			return 0;
		}
		else{
			distance = (double) Navigate.nd.getLengthMeters(linkIDToPre);
			speed = setSpeedLimit(pre, linkIDToPre);

			return Helper.getLinkCostsInSeconds(distance, speed);
		}
	}

	public double setSpeedLimit(Node pre, int linkIDToPre){
		speed = (double) Navigate.nd.getMaxSpeedKMperHours(linkIDToPre);

		//no explicit speed limitation
		if(speed == 0.0)
		{
			//get speedlimitation from type of road
			speed = Helper.getDefaultSpeed(linkIDToPre);

		}
		return speed;
	}

	/**
	* sets costs of start to node over node pre
	*/
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
	* sets estimated costs of start to end over node pre and crossing node
	*/
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
			c = c(predecessor, linkIDToPredecessor);
			g = g(predecessor, linkIDToPredecessor);
			f = f(predecessor, linkIDToPredecessor);
		}

	}
	/**
	* sets new predecessor and updates all depending values with known c, g, f
	*/
	public void setPredecessor(Node newPredecessor, int newLinkIDToPredecessor, double newC, double newG, double newF) {
		if(predecessor == null || predecessor.crossingID != newPredecessor.crossingID)
		{
			predecessor = newPredecessor;
			linkIDToPredecessor = newLinkIDToPredecessor;
			domainID = Navigate.nd.getDomainID(Navigate.nd.getReverseLink(linkIDToPredecessor));
			this.c = newC;
			this.g = newG;
			this.f = newF;
		}

	}
}

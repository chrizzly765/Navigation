import nav.NavData;
import java.util.Comparator;

// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

class Node implements Comparator<Node>
{
	
	private int [] links;
	private double beeLine;
	private Node predecessor;// = new Node();
	public int crossingID;
	public boolean statusClosed = false;
	public static Node destination;// = new Node();
	public static NavData  nd;
	
	private double value_c; // actual costs from predecessor to node
	private double value_g; // actual costs from start to node
	private double value_f; // estimated costs total
	private double value_h; // estimated costs from node to end
	
	private final static double EARTH_RADIUS = 6378.388;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 100;	
	
	/***************************************
	 * PROPERTIES
	 ***************************************/
	
	public double Value_c()
	{
		return value_c;
	}	
	public double Value_g()
	{
		return value_g;
	}
	public double Value_f()
	{
		return value_f;
	}
	public double Value_h()
	{
		return value_h;
	}		
	public Node Predecessor()
	{
		return predecessor;
	}
	
	/***************************************
	 * CONSTRUCTORS
	 ***************************************/
	
		public Node (int crossingID, Node predecessor){
			this.crossingID = crossingID;
			setLinks();
			setPredecessor(predecessor);
			
			if(destination != null)
				h();
		}
	
		public Node(NavData nd_Node, int latStart, int lonStart, int latZiel, int lonZiel)
		{
		this.nd = Navigate.nd;//nd_Node;		
		System.out.println( "NODE constructor start: " + this.nd);
		
		this.crossingID = nd.getNearestCrossing(latStart,lonStart);
		
		System.out.println("NODE crossingID start: " + this.crossingID);
		
		
		destination = new Node(this.nd.getNearestCrossing(latZiel,lonZiel), null);
		
		
		//Im Straßennetz?
		if (this.nd.isIsolatedCrossiong(this.crossingID))
		{
			// return error if not connected
			destination.crossingID = nd.getNearestCrossing(latZiel,lonZiel);
		}
		setLinks();
		setPredecessor(null);
		h();
	}
	
	/***************************************
	 * FUNCTIONS
	 ***************************************/
	
	private void setLinks ()
	{
		System.out.println("NODE nd: "+ this.nd);
		links = this.nd.getLinksForCrossing(this.crossingID);
		
	}
	
	public int[] getLinks()
	{
		return links;
	}
	
	/**
	 * sets costs of predecessor to node
	 */
	private void c()
	{
		value_c = c(predecessor);
	}
	
	private double c(Node pre)
	{
		if (pre == null){
			return 0;
		}
		else{
			int linkPredecessor = linkFromPredecessor(pre);
			double speedLimit = (double)nd.getMaxSpeedKMperHours(linkPredecessor);
			
			//no explicit speed limitation
			if(speedLimit == 0)
			{
				//get speedlimitation from type of road
				speedLimit = Helper.getSpeedFromType(crossingID);
			}
			
			System.out.println("C: " + nd.getLengthMeters(linkPredecessor) / speedLimit);
			return (double)nd.getLengthMeters(linkPredecessor) / speedLimit;
		}
	}
	
	/**
	 * returns link id of link from predecessor to node
	 */
	private int linkFromPredecessor()
	{
		/*System.out.println("NODE linkFromPredecessor() links: " + links);
		for(int link : links)
		{
			//System.out.println("crossingID: " + this.crossingID + "link: " + link + " getCrossingIDFrom(link): " + nd.getCrossingIDFrom(link));
			
			System.out.println("getCrossingIDTo(link): " + nd.getCrossingIDTo(link) + " predecessor.crossingID: " + predecessor.crossingID);
			if(nd.getCrossingIDTo(link) == predecessor.crossingID)
				return link;
		}
		return -1;*/
		return linkFromPredecessor(predecessor);//HIER LIEGT DER HUND BEGRABEN
	}
	
	private int linkFromPredecessor(Node pre)
	{
		System.out.println("links.Length: " + links.length);
		int n = 0;
		for(int link : links)
		{
			System.out.println("crossingID: " + this.crossingID + " " + n + " getCrossingIDTo(link): " + nd.getCrossingIDTo(link) + " predecessor.crossingID: " + pre.crossingID);
			if(nd.getCrossingIDTo(link) == pre.crossingID)
			{
				return link;
			}
			n++;
		}
		return -1;
	}
	
	/**
	 * sets costs of start to node
	 */	private void g()
	{
		value_g = g(predecessor);
		
	}
	
	private double g(Node pre){
		
		if (pre ==null)
		{
			System.out.println("G: 0");
			return value_g = 0;
		}
		else{
			System.out.println("G: "+ pre.Value_g() + c(pre));
			return pre.Value_g() + c(pre);
		}
	}
	
	
	
	/**
	 * sets estimated costs of start to end by crossing node
	 */
	private void f()
	{
		value_f = F(predecessor);
	}
	
	public double F(Node pre)
	{
		System.out.println("F: "+ g(pre) + value_h);
		return g(pre) + value_h;
	}
	
	/**
	 * sets new predecessor and updates all depending values
	 */
	public void setPredecessor(Node newPredecessor)
	{
		if(predecessor == null || predecessor.crossingID != newPredecessor.crossingID)
		{
			predecessor = newPredecessor;
			/* value_c = c();
			value_g = g();
			value_f = f(); */
			c();
			g();
			f();
		}
	}

	private static double getBeeLineInMeter(double latSource, double lonSource, double latDest, double lonDest) {

        latSource = Math.toRadians(latSource);
        lonSource = Math.toRadians(lonSource);
        latDest = Math.toRadians(latDest);
        lonDest = Math.toRadians(lonDest);		

        // maps.google: Strasse => 6,3km; Luftlinie => 5,52km
        // cos(g) = cos(90° - lat1) * cos(90° - lat2) + sin(90° - lat1) * sin(90° - lat2) * cos(lon2 - lon1)
        // vereinfacht => cos(g) = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)
        double cosG = Math.sin(latSource) * Math.sin(latDest) + Math.cos(latSource) * Math.cos(latDest) * Math.cos(lonDest - lonSource);
        //return Math.round((EARTH_RADIUS * Math.acos(cosG)) * 1000);
		return (EARTH_RADIUS * Math.acos(cosG)) * 1000;
    }
		
	// h = beeline from neighbor node to destination		
    public void h() //int crossingID, double stop_lat_d, double stop_lon_d) {	
	{
		System.out.println("NODE h(): " + destination);
		double stop_lat_d = this.nd.getCrossingLatE6(destination.crossingID);
		double stop_lon_d = this.nd.getCrossingLongE6(destination.crossingID);
		double neighborLat = Helper.convertCoordToDouble(nd.getCrossingLatE6(this.crossingID));
		double neighborLon = Helper.convertCoordToDouble(nd.getCrossingLongE6(this.crossingID));
		
		System.out.println("NODE Expand Neighbors lat/lon: " + neighborLat + "/" + neighborLon);
		System.out.println("NODE Expand Destination lat/lon: " + stop_lat_d + "/" + stop_lon_d);
		
		beeLine = getBeeLineInMeter(neighborLat,neighborLon,stop_lat_d,stop_lon_d);		
		this.value_h = (int)(Helper.getLinkCostsInSeconds(beeLine, MAX_SPEED_FOR_LINEAR_DISTANCE));//TODO ändern in double
	}
	
	@Override
	public int compare(Node n1, Node n2) {

		 // n1 > n2 = 1 => sort asc
		 if(n1.Value_f() > n2.Value_f()) return 1;
		 else if(n1.Value_f() < n2.Value_f()) return -1;
		 else return 0;
	}
}
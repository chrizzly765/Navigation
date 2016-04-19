import nav.NavData;

class Node{
	
	private int [] links;
	private int luftlinie;
	private Node predecessor;
	private int crossingID;
	private boolean statusOpen;
	static NavData nd;
	public static Node destination = new Node();
	
	private int value_c; // actual costs from predecessor to node
	private int value_g; // actual costs from start to node
	private int value_f; // estimated costs total
	private int value_h; // estimated costs from node to end
	
	/***************************************
	 * PROPERTIES
	 ***************************************/
	
	public int Value_c()
	{
		return value_c;
	}	
	public int Value_g()
	{
		return value_g;
	}
	public int Value_f()
	{
		return value_f;
	}
	
	/***************************************
	 * CONSTRUCTORS
	 ***************************************/
	
	public Node (){}
	
	public Node(NavData nd, int latStart, int lonStart, int latZiel, int lonZiel){
		nd = nd;
		this.crossingID = nd.getNearestCrossing(latStart,lonStart);
		
		//Im Straßennetz?
		if (nd.isIsolatedCrossiong(this.crossingID))
		{
			// return error if not connected
			//ziel.crossingID = nd.getNearestCrossing(latZiel,lonZiel);
		}
		
		predecessor = new Node();
	}
	
	/***************************************
	 * FUNCTIONS
	 ***************************************/
	
	private void getLinks (){
		links = nd.getLinksForCrossing(this.crossingID);
	}
	
	/**
	 * sets costs of predecessor to node
	 */
	private void c()
	{
		double costsToPredecessor;
		int linkPredecessor = linkFromPredecessor();
		int speedLimit = nd.getMaxSpeedKMperHours(linkPredecessor);
		
		//no explicit speed limitation
		if(speedLimit == 0)
		{
			//get speedlimitation from type of road
			//TODO
			//speedlimit = 
		}
		
		value_c = nd.getLengthMeters(linkPredecessor) / speedLimit;
	}
	
	/**
	 * returns link id of link from predecessor to node
	 */
	private int linkFromPredecessor()
	{
		for(int link : links)
		{
			if(nd.getCrossingIDFrom(link) == predecessor.CrossingID)
				return link;
		}
		return -1;
	}
	
	/**
	 * sets costs of start to node
	 */
	private void g()
	{
		value_g = predecessor.Value_g + value_c;
	}
	
	/**
	 * sets estimated costs of start to end by crossing node
	 */
	private void f()
	{
		value_f = value_g + value_h;
	}
	
	/**
	 * sets new predecessor and updates all depending values
	 */
	private void setPredecessor(Node newPredecessor)
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
}
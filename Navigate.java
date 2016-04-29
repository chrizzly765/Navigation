import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.io.File;
import java.util.Arrays;

public class Navigate {

	private static int start_lat;
    private static double start_lat_d;
    private static int start_lon;
    private static double start_lon_d;
    private static int stop_lat;
    private static double stop_lat_d;
    private static int stop_lon;
    private static double stop_lon_d;

	
	private final static String TURNS_TXT = "Turns.txt";
	private final static String ROUTE_TXT = "Route.txt";
	
	public static NavData  nd;

	public static void main(String[] args) {
	
		if (args.length < 1) {
            System.out.println("usage NavDemo <navcache file>");
            System.exit(1);
        }
		try
		{
			nd = new NavData(args[0],true);
			
			// convert coords into an int value by multiplying with a faktor			
			start_lat_d = Double.parseDouble(args[1]);
			start_lat = (int)(start_lat_d*Helper.FACTOR);
			start_lon_d = Double.parseDouble(args[2]);			
			start_lon = (int)(start_lon_d*Helper.FACTOR);			
			stop_lat_d = Double.parseDouble(args[3]);
			stop_lat = (int)(stop_lat_d*Helper.FACTOR);
			stop_lon_d = Double.parseDouble(args[4]);
			stop_lon = (int)(stop_lon_d*Helper.FACTOR);
			
			////TEST
			Node start = new Node(nd, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			Node destination = start.destination;
			
			// start timer
            long startTime = System.currentTimeMillis();
			// start position
            int nearestCrossingID = nd.getNearestCrossing(start_lat,start_lon);
		
			
			// stop timer
            long stopTime = System.currentTimeMillis();
            double elapsed = ((stopTime - startTime));
            System.out.println(stopTime + " - " + startTime + " = " + elapsed + "ms");
		}
		catch (Exception e) {
		e.printStackTrace();
		}

	}
	
/* 	public static void pushNode(Node [] list, Node newNode){
		
		for (int i = 0; i<list.length; i++){
			
		}
		newNode.toArray(list);
	} */
	
	public static void pushNodeSorted (Node [] openNodeList, Node newOpenNode){
		System.out.println("ArrayPushSortBinarySearch");
		
		//nächste Freie Position des Arrays ermitteln
		
		int nextFreePos = 0;
		int insertPosition = 0;
	
		while(openNodeList[nextFreePos] != null){
			nextFreePos++;
		}
		
		System.out.println("Free Position: " + nextFreePos);
		
		//INSERT POSITION SUCHEN
		
		//einfügen am letzten Platz falls es gleich größer ist
		
		if (openNodeList[0] == null){
			openNodeList[0] = newOpenNode;
		}
		else if (openNodeList[nextFreePos-1].Value_f() <= newOpenNode.Value_f()){
			openNodeList[nextFreePos] = newOpenNode;
		}
		//ansonsten binäre suche
		else{
			insertPosition = binarySearch(openNodeList, newOpenNode, nextFreePos);
			for (int i = nextFreePos; i > insertPosition; i--){
	
			openNodeList[i] = openNodeList[i-1];
		}
	
		openNodeList[insertPosition] = newOpenNode;
		}
	}
		
	public static int binarySearch (Node [] openNodeList, Node newOpenNode, int nextFreePos){
						
			int insertPosition = 0;
			int l = 0;
			int r = nextFreePos-1;
			
			
				do{
					insertPosition = (int)Math.ceil((double)(l + r) / 2);
					
					System.out.println("insertPosition in binarySearch: " + insertPosition);
				
					if(openNodeList[insertPosition].Value_f() <= newOpenNode.Value_f()){
						l = insertPosition+1;
					}
					else {
						r = insertPosition-1;
					}
				}
				while (openNodeList[insertPosition].Value_f() != newOpenNode.Value_f() && l<=r);
				
				// wenn das Element nicht gefunden wurde, dann richtige Einfügeposition überprüfen, an die das Element gehört
				if (openNodeList[insertPosition].Value_f() != newOpenNode.Value_f())
					{
						if (openNodeList[insertPosition].Value_f() < newOpenNode.Value_f())
						{
							insertPosition++;
						}
					}
                // wenn Element gefunden, so lange nach links gehen wie gleicher Wert vorhanden
				else
					{
						// (insertPosition > 0) Stellt sicher, dass wir nicht an content[-1] suchen
						while (insertPosition > 0 && openNodeList[insertPosition].Value_f() == openNodeList[insertPosition - 1].Value_f())
						{
							insertPosition--;
						}	
					}
				
				return insertPosition;
	}
		
	public static void deleteNode (Node [] openNodeList){
		
		for (int i = 1; i<= openNodeList.length; i++)
		{
			if(openNodeList[i]!= null){
			openNodeList[i-1] = openNodeList[i];
			}
			else{
				openNodeList[i-1] = null;
				break;
			}
		}
	}
	
	//Für uns zur Testzwecken
	static void printArray (Node [] openNodeList){
		System.out.println("Print ARRAY");
		for (int i = 0; i<= openNodeList.length; i++)
		{
			if (openNodeList[i] != null){
			System.out.println("position im Array: " + i + " vorhandene F: " + openNodeList[i].Value_f());
			}
			else{
				break;
			}
		}
	}

	
	
	/* private static double getLinearDistanceInMeter(double latSource, double lonSource, double latDest, double lonDest) {

        latSource = Math.toRadians(latSource);
        lonSource = Math.toRadians(lonSource);
        latDest = Math.toRadians(latDest);
        lonDest = Math.toRadians(lonDest);

        // maps.google: Strasse => 6,3km; Luftlinie => 5,52km
        // cos(g) = cos(90° - lat1) * cos(90° - lat2) + sin(90° - lat1) * sin(90° - lat2) * cos(lon2 - lon1)
        // vereinfacht => cos(g) = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)
        double cosG = Math.sin(latSource) * Math.sin(latDest) + Math.cos(latSource) * Math.cos(latDest) * Math.cos(lonDest - lonSource);
        return (EARTH_RADIUS * Math.acos(cosG)) * 1000;
    } */

	static boolean A_Star (Node start, Node destination){
		
		
		Node currentNode;// = new Node();
		Node [] openNodeList = new Node [nd.getCrossingCount()];
		boolean [] closedNodeList = new boolean [nd.getCrossingCount()];
		pushNodeSorted(openNodeList, start);
		
		do {
			currentNode = openNodeList[0];
			if (currentNode.crossingID == destination.crossingID)
			{
				return true;
			}
			deleteNode(openNodeList);
			expand(openNodeList, closedNodeList, currentNode);
			closedNodeList[currentNode.crossingID] = true;//currentNode.toArray(closedNodeList);
		}
		while(openNodeList[0] != null);
		
		return false;
	
	}
	
	static void expand (Node [] openNodeList, boolean [] closedNodeList, Node currentNode){
		
		boolean found = false;
		int[] tmpLinks = currentNode.getLinks();
		int crossingIDTo;
		// Neighbors	
		for (int i = 0; i< tmpLinks.length; i++) {
			
			crossingIDTo = nd.getCrossingIDTo(tmpLinks[i]);
						
			if(closedNodeList[crossingIDTo] == false) { 			
				
				for(int j=0; j < openNodeList.length || found == true; j++){
					if(crossingIDTo == openNodeList[j].crossingID){
						found = true;
					}
				}
			}
			
			if(found == false || currentNode.Value_f() < openNodeList[i].F(currentNode)){
				if (found == false){
					Node newNeighbourNode = new Node(crossingIDTo, currentNode);
					pushNodeSorted(openNodeList, newNeighbourNode);
				}
				else{
					openNodeList[i].setPredecessor(currentNode);
				}
			}
		}
		
	}
	
	
	
}



	

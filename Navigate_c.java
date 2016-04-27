import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;
import java.io.File;
import java.lang.*;
import java.util.*;

import fu.util.IntHisto;
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

// compile
// javac -cp .;nav.jar Navigate.java

// run
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 49.46591000 11.15800500 49.94795167 10.07600667
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.453025 11.093324

public class Navigate {

	private final static double EARTH_RADIUS = 6378.388;
    private final static double FAKTOR = 1000000.0;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 100;	
	private final static String TURNS_TXT = "Turns.txt";
	private final static String ROUTE_TXT = "Route.txt";

    private static int start_lat;
    private static double start_lat_d;
    private static int start_lon;
    private static double start_lon_d;
    private static int stop_lat;
    private static double stop_lat_d;
    private static int stop_lon;
    private static double stop_lon_d;
	
	public static double neighborLat;
	public static double neighborLon;
	public static double beeLine;
	public static double distance;
	public static double speed;
	public static double c;
	public static double f;
	public static double g_pre;
	public static double g_akt;
	public static double h;
	
	public static int crossingIDTo;
	
	public static int lsiClassNr;
	public static LSIClass lsiClass;   
		
	// create map with default speed for different streettypes <token, speed>
	public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();	
	public static NavData nd;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("usage NavDemo <navcache file>");
            System.exit(1);
        }

        try {

			// initialize map with default speed
			mapDefaultSpeed.put("BAUSTELLE_VERKEHR", 30);	
			mapDefaultSpeed.put("AUTOBAHN", 130);
			mapDefaultSpeed.put("KRAFTFAHRSTRASSE", 80);
			mapDefaultSpeed.put("LANDSTRASSE", 100);
			mapDefaultSpeed.put("BUNDESSTRASSE", 50);
			mapDefaultSpeed.put("LANDSTRASSE_SEKUNDAER", 80);
			mapDefaultSpeed.put("LANDSTRASSE_TERTIAER", 50);
			mapDefaultSpeed.put("LANDSTRASSE_UNKLASSIFIZIERT", 30);
			mapDefaultSpeed.put("INNERORTSTRASSE", 50);
			mapDefaultSpeed.put("VERKEHRSBERUHIGTER_BEREICH", 15);
			mapDefaultSpeed.put("ANSCHLUSSSTELLE_AUTOBAHN", 80);
			mapDefaultSpeed.put("ANSCHLUSSSTELLE_KRAFTFAHRSTRASSE", 50);
			mapDefaultSpeed.put("ANSCHLUSSSTELLE_BUNDESSTRASSE", 50);
			mapDefaultSpeed.put("ANSCHLUSSSTELLE_SEKUNDAER", 30);
			mapDefaultSpeed.put("ANSCHLUSSSTELLE_TERTIAER", 20);
			mapDefaultSpeed.put("KREISVERKEHR", 20);		
	
			nd = new NavData(args[0], true);	

			// start timer
			// ##########################################
            long startTime = System.currentTimeMillis();				
			
            // convert coords into an int by multiplying with a faktor			
			start_lat_d = Double.parseDouble(args[1]);			
			start_lat = convertCoordToInt(start_lat_d);
			start_lon_d = Double.parseDouble(args[2]);			
			start_lon = convertCoordToInt(start_lon_d);		
			stop_lat_d = Double.parseDouble(args[3]);
			stop_lat = convertCoordToInt(stop_lat_d);
			stop_lon_d = Double.parseDouble(args[4]);
			stop_lon = convertCoordToInt(stop_lon_d);
			
			
			
			// get start and destination id
			int crossingIdStart = nd.getNearestCrossing(start_lat, start_lon);
			int crossingIdStop = nd.getNearestCrossing(stop_lat, stop_lon);
			
			Node nodeStart = new Node(crossingIdStart,start_lat, start_lon);
			Node nodeDestination = new Node(crossingIdStop,stop_lat, stop_lon);
			
			//System.out.println(nodeStart.crossingID + " - " + nodeDestination.crossingID);
						
			if(A_Star(nodeStart, nodeDestination)) {

				// write textfiles route and turns
			}
			else {
				// no route exists
				System.out.println("No route found!");
			}			
						
			long stopTime = System.currentTimeMillis();
            double elapsed = ((stopTime - startTime));
            
            System.out.println(stopTime + " - " + startTime + " = " + elapsed + "ms");
			// ##########################################
			// stop timer
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static int convertCoordToInt(double coordinate) {		
		return (int)(coordinate*FAKTOR);	
	}
	
	public static double convertCoordToDouble(int coordinate) {		
		return (double)(coordinate/FAKTOR);	
	}
	
	/* public static void createDrivingCommands() {
	
		PrintWriter pw = new PrintWriter(TURNS_TXT);
		// walk through Nodes 
	
			lsiClassNr = nd.getLSIclass(currentNode.links[i].crossingID);
			lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);	
			
			// check if turn left/right or straight on by using angle
			// if straight on, check if domainName changed
			// format: 
			// Biegen Sie [(scharf)/links/rechts] ab auf [className] [domainName]
			// Fahren Sie weiter geradeaus auf [className] [domainName]
			
			String row = cmdTurn + lsi.className + " " + arrNode[i].domainName;				
			pw.println(row);
		
		pw.close();
	
	} */
	
	public static boolean A_Star(Node start, Node destination){		
		
		Node currentNode = new Node();
		Node [] openNodeList = new Node [nd.getCrossingCount()];
		pushNode(openNodeList, start);		
		
		do {
			currentNode = openNodeList[0];			
			if (currentNode.crossingID == destination.crossingID) {
				return true;
			}
			deleteNode(openNodeList);
			// make openNodeList global?!
			expand(openNodeList, currentNode);
			currentNode.statusClosed = true;
		}
		while(openNodeList[0] != null);
		
		return false;	
	}
	
	public static void expand (Node [] openNodeList, Node currentNode) {	
		
		currentNode.links = nd.getLinksForCrossing(currentNode.crossingID);
		
		// Neighbors	
		for (int i = 0; i< currentNode.links.length; i++) {
			
			crossingIDTo = nd.getCrossingIDTo(currentNode.links[i]);
						
			if(isNeighborClosed(openNodeList, crossingIDTo) != true) { 			
			
				// h = beeline from neighbor node to destination
				h = h(crossingIDTo, stop_lat_d, stop_lon_d);								
								
				System.out.println("Expand Neighbors LinkIDs: " + currentNode.links[i]);
				System.out.println("Expand Predecessor: " + currentNode.predecessor);
				
				// c = costs from current to predecessor	
				c = c(currentNode,currentNode.links[i]);
				
				g_pre = g(currentNode.predecessor);
				g_akt = g_pre + c;
				f = g_akt + h;
				
				System.out.println("g_pre: " + g_pre);
				System.out.println("g_akt: " + g_akt);
				System.out.println("f: " + f);
				
				// domain
                int domainID = nd.getDomainID(currentNode.links[i]);
                if(nd.isDomain(domainID)) {
                    System.out.println("Expand DomainName:" + nd.getDomainName(domainID));                    
                }
				
				/* 			
				
				for (int j=0; j< openNodeList.length; j++ )	{
					
				}*/
			}
			else {
				System.out.println("Expand Neighbor closed: " + crossingIDTo);        
			}
			
		}		
		
		/* if (currentNode) {
			
		} */
	}
	
	// iterate trough nodelist and check if neighbor is closed
	public static boolean isNeighborClosed(Node [] openNodeList, int crossingID) {
	
		for (int i = 0; i < openNodeList.length; i++) {

			if(openNodeList[i] != null) {
				if(openNodeList[i].crossingID == crossingID && openNodeList[i].statusClosed == true) {
					return true;
				}
			}				
		}
		return false;
	
	}
	
	// c = costs from current to predecessor	
	public static double c(Node currentNode, int crossingID) {
	
		// only if predecessor exists
		if(currentNode.predecessor != null) {		
						
			int crossingIDFrom = nd.getCrossingIDFrom(crossingID);				
			if(crossingIDFrom == currentNode.predecessor.crossingID) {
			
				distance = (double) nd.getLengthMeters(crossingID);
				speed = (double) nd.getMaxSpeedKMperHours(crossingID);
				
				// if no default speed limit is set
				if(speed == 0) {
					String token = lsiClass.classToken;
					speed = mapDefaultSpeed.get(token);
					System.out.println("Expand LSI Token: " + token);
				}
				
				c = getLinkCostsInSeconds(distance, speed);
				
				System.out.println("Expand Predecessor IDs: " + crossingID);
				System.out.println("Expand Predecessor c: " + c + " = " + distance + " / " + speed);
				
			}
		}
		else {
			c = 0;
			System.out.println("Expand No Predecessor c: " + c);
		}
		
		return c;
	}
	
	// h = beeline from neighbor node to destination		
	public static double h(int crossingID, double stop_lat_d, double stop_lon_d) {	
				
		neighborLat = convertCoordToDouble(nd.getCrossingLatE6(crossingID));
		neighborLon = convertCoordToDouble(nd.getCrossingLongE6(crossingID));				
		System.out.println("Expand Neighbors lat/lon: " + neighborLat + "/" + neighborLon);
		System.out.println("Expand Destination lat/lon: " + stop_lat_d + "/" + stop_lon_d);
		
		beeLine = getBeeLineInMeter(neighborLat,neighborLon,stop_lat_d,stop_lon_d);		
		h = getLinkCostsInSeconds(beeLine, MAX_SPEED_FOR_LINEAR_DISTANCE);				
		System.out.println("Expand Neighbors beeLine/h: " + beeLine + "/" + h);
		
		return h;
	
	}
	
	// g = costs from start to current node
	// use predecessors to go back and sum costs of each link
	public static double g(Node predecessor) {
	
		if(predecessor != null) {
			return 0;
		}
		else {
		
			
		
		}
		return 0;
	}
	
	/**
	 * sets new predecessor and updates all depending values
	 */
	/* private void setPredecessor(Node newPredecessor)
	{
		if(predecessor == null || predecessor.crossingID != newPredecessor.crossingID)
		{
			predecessor = newPredecessor;			
			c();
			g();
			f();
		}
	} */
	
	/* public int linkFromPredecessor(int[] links) {
		
		for(int link : links) {
		
			if(nd.getCrossingIDFrom(link) == predecessor.crossingID)
				return link;
		}
		return -1;
	} */
	
	public static void pushNode (Node [] openNodeList, Node newOpenNode) {
		
		System.out.println("ArrayPushSortBinarySearch");
		
		// nächste Freie Position des Arrays ermitteln
		
		int nextFreePos = 0;
		int insertPosition = 0;
	
		while(openNodeList[nextFreePos] != null){
			nextFreePos++;
		}
		
		System.out.println("Free Position: " + nextFreePos);
		
		// INSERT POSITION SUCHEN
		
		// einfügen am letzten Platz falls es gleich größer ist
		
		if (openNodeList[0] == null){
			openNodeList[0] = newOpenNode;
		}
		/* else if (openNodeList[nextFreePos-1].Value_f() <= newOpenNode.Value_f()){
			openNodeList[nextFreePos] = newOpenNode;
		} 
		// ansonsten binäre suche
		else {
			insertPosition = binarySearch(openNodeList, newOpenNode, nextFreePos);
			for (int i = nextFreePos; i > insertPosition; i--){	
				openNodeList[i] = openNodeList[i-1];
			}
	
			openNodeList[insertPosition] = newOpenNode;
		}*/
	} 
	
	public static void deleteNode (Node [] openNodeList){
				
		for (int i = 1; i<= openNodeList.length; i++) {
		
			System.out.println("Delete Node: " + i);
			if(openNodeList[i]!= null) {
				openNodeList[i-1] = openNodeList[i];
			}
			else {
				openNodeList[i-1] = null;
				break;
			}			
		}
	}
	
	/* public static int binarySearch (Node [] openNodeList, Node newOpenNode, int nextFreePos){
						
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
		if (openNodeList[insertPosition].Value_f() != newOpenNode.Value_f()) {
			if (openNodeList[insertPosition].Value_f() < newOpenNode.Value_f()) {
				insertPosition++;
			}
		}
		// wenn Element gefunden, so lange nach links gehen wie gleicher Wert vorhanden
		else {
			// (insertPosition > 0) Stellt sicher, dass wir nicht an content[-1] suchen
			while (insertPosition > 0 && openNodeList[insertPosition].Value_f() == openNodeList[insertPosition - 1].Value_f()) {
				insertPosition--;
			}	
		}
		
		return insertPosition;
	} */
	
		
	// c
    private static double getLinkCostsInSeconds(double distance, double speed) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // time = distance/speed => 70m/s : 8,333m/s = 8,4s
        return (distance / ((speed * 1000) / 3600));
    }
	
	// h
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
}



import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;
import java.io.File;
import java.lang.*;
import java.util.*;

// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

// Earth Radius
import fu.geo.Spherical;


// compile
// javac -cp .;nav.jar Navigate.java

// run
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 49.46591000 11.15800500 49.94795167 10.07600667
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.453025 11.093324

// coordinates from route.txt
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.48431 11.197552 49.474915 11.122614

// Chiemsee bis Norddeich
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 47.889674 12.417799 53.612192 07.150162
// 04.05. ca. 200 Sekunden
// 06.05. ca. 115 Sekunden (+PriorityQueue, openNodeList == closedNodeList)
// 06.05. ca. 0,5 Sekunden (+spherical.greatCircleMeters, no outputs!)

public class Navigate {

	//private final static double EARTH_RADIUS = 6378.388;
    private final static double FAKTOR = 1000000.0;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 100;
	private final static String TURNS_TXT = "Turns.txt";
	private final static String ROUTE_TXT = "Route.txt";

    private static int start_lat;
    public static double start_lat_d;
    private static int start_lon;
    private static double start_lon_d;
    private static int stop_lat;
    private static double stop_lat_d;
    private static int stop_lon;
    private static double stop_lon_d;

	public static double neighborLat_d;
	public static double neighborLon_d;
	public static int neighborLat;
	public static int neighborLon;
	public static double beeLine;
	public static double distance;
	public static double speed;

	// create map with default speed for different streettypes <token, speed>
	public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();
	public static NavData nd;
	public static Spherical spherical;

	public static PriorityQueue<Node> NodePriorityQueue;

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

			if(A_Star(nodeStart, nodeDestination)) {

				// write textfiles route and turns
				System.out.println("##### Route found ... write TxtFiles ");

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

		Node currentNode = null;
		Node [] openNodeList = new Node [nd.getCrossingCount()];
        boolean [] closedNodeList = new boolean [nd.getCrossingCount()];

		// use PriorityQueue to provide a structure sorted ascending by f
		NodePriorityQueue = new PriorityQueue<Node>(nd.getCrossingCount(), start);
		NodePriorityQueue.add(start);

		openNodeList[start.crossingID] = start;

		int x=0;
		do {

            // assign least element to currentNode and remove from queue
			currentNode = NodePriorityQueue.remove();

			if (currentNode.crossingID == destination.crossingID) {

				System.out.println("Size: -> " + NodePriorityQueue.size());

				// return queue?
				//pollDataFromQueue(NodePriorityQueue);
				return true;
			}
			expand(openNodeList, closedNodeList, currentNode);
			closedNodeList[currentNode.crossingID] = true;

			System.out.println("-----CloseNode: ->" + x++ + " crossingID: " + currentNode.crossingID);

			//if(x == 40) System.exit(1);
		}
		while(NodePriorityQueue.size() > 0);

		return false;
	}

    // iterate through queue
    // poll() removes the current element
	private static void pollDataFromQueue(PriorityQueue<Node> NodePriorityQueue) {

		while(true) {
            Node n = NodePriorityQueue.poll();
            if(n == null) break;
            System.out.println("F = " + n.getValue_f());
        }
    }

	public static void expand (Node [] openNodeList, boolean [] closedNodeList, Node currentNode) {

		currentNode.links = nd.getLinksForCrossing(currentNode.crossingID);
		boolean found = false;
		Node NeighborNode = null;
        int crossingIDTo;

		// Neighbors
		for (int i = 0; i< currentNode.links.length; i++) {

			// domain
			int domainID = nd.getDomainID(currentNode.links[i]);
			if(nd.isDomain(domainID)) {
				System.out.println("Expand DomainName: " + i + ":" + nd.getDomainName(domainID));
			}
			else {
				System.out.println("No Domain:" );
			}

			crossingIDTo = nd.getCrossingIDTo(currentNode.links[i]);

            if(nd.isIsolatedCrossiong(crossingIDTo)) {
                System.out.println("### Isolated: " + i + ": To: " + crossingIDTo);
                // continue;
            }

            if(nd.goesCounterOneway(currentNode.links[i])) {
                System.out.println("### One Way: " + i + ": To: " + crossingIDTo);
                continue;
            }

			System.out.println("Expand: " + i + ":  crossingID:" + currentNode.crossingID + " To: " + crossingIDTo);
            System.out.println("Expand: " + i + ":  lat:" + (long)currentNode.lat + " lon: " + (long)currentNode.lon);

			// is neighbor already closed
			if(closedNodeList[crossingIDTo] == false) {

				// is neighbor already open
				if(openNodeList[crossingIDTo] != null) {

					System.out.println("++++ Expand Found: " + crossingIDTo + "->" + openNodeList[crossingIDTo]);
					NeighborNode = openNodeList[crossingIDTo];
					found = true;
				}

				// if neighbor not open yet, create node
				if(found == false) {

					// create new neighbor Node
					neighborLat = nd.getCrossingLatE6(crossingIDTo);
					neighborLon = nd.getCrossingLongE6(crossingIDTo);
					NeighborNode = new Node(crossingIDTo, neighborLat, neighborLon);
					openNodeList[crossingIDTo] = NeighborNode;

					neighborLat_d = convertCoordToDouble(neighborLat);
					neighborLon_d = convertCoordToDouble(neighborLon);

					System.out.println("create Neighbor: " + NeighborNode.crossingID);
					NeighborNode.setValue_h(h(neighborLat_d, neighborLon_d, stop_lat_d, stop_lon_d));
				}


				// c = costs from current to neighbor
				currentNode.setValue_c(c(currentNode,currentNode.links[i]));
				//System.out.println("C= " + currentNode.getValue_c());

				// g
				//System.out.println("G= " + NeighborNode.getValue_g() + " + " + currentNode.getValue_c());
				currentNode.setValue_g(NeighborNode.getValue_g() + currentNode.getValue_c());

				// f
				//System.out.println("F= " + currentNode.getValue_g() + " + " + NeighborNode.getValue_h());
				currentNode.setValue_f(currentNode.getValue_g() + NeighborNode.getValue_h());

				//System.out.println("F: " + currentNode.getValue_f() + " > " + NeighborNode.getValue_f());

				// if currentNode exists in openNodeList and f(currentNode) > f(neighbor)
				if(found == true && currentNode.getValue_f() >= NeighborNode.getValue_f()) {
					continue;
				}
				else {
					NeighborNode.predecessor = currentNode;
					NeighborNode.setValue_g(currentNode.getValue_g());
					NeighborNode.setValue_f(currentNode.getValue_f());

					NodePriorityQueue.add(NeighborNode);
					System.out.println("Add to Queue: " + NeighborNode.crossingID);
				}
			}
			else {
				System.out.println("Expand Neighbor closed: " + crossingIDTo);
			}
		}
	}

	// c = costs from current to predecessor
	public static double c(Node currentNode, int linkID) {

		int lsiClassNr = nd.getLSIclass(currentNode.crossingID);
		LSIClass lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);

		distance = (double) nd.getLengthMeters(linkID);
		speed = (double) nd.getMaxSpeedKMperHours(linkID);

		//System.out.println("C crossingIDTo: " + crossingIDTo + " m/kmh: " + distance + "/" + speed);

		// if no default speed limit is set
		if(speed == 0) {
			String token = lsiClass.classToken;
			speed = mapDefaultSpeed.get(token);
			//System.out.println("Expand LSI Token: " + token + "Speed: " + speed);
		}

		return getLinkCostsInSeconds(distance, speed);
	}

	// h = beeline from neighbor node to destination
	public static double h(double neighborLat, double neighborLon, double stop_lat_d, double stop_lon_d) {

		// beeLine = getBeeLineInMeter(neighborLat,neighborLon,stop_lat_d,stop_lon_d);
		// System.out.println("Expand Neighbors beeLine: " + beeLine);
		beeLine = spherical.greatCircleMeters(neighborLat,neighborLon,stop_lat_d,stop_lon_d);
		//System.out.println("Roth beeLine: " + beeLine);

		return getLinkCostsInSeconds(beeLine, MAX_SPEED_FOR_LINEAR_DISTANCE);
	}

	// c = costs between two crossings
    private static double getLinkCostsInSeconds(double distance, double speed) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // time = distance/speed => 70m/s : 8,333m/s = 8,4s
        return (distance / ((speed * 1000) / 3600));
    }

}

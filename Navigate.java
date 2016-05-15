import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.io.*;
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
// route from Grünreutherstrasse to Kesslerplatz
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.453025 11.093324

// coordinates from route.txt
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.48431 11.197552 49.474915 11.122614

// route from Laufamholzstrasse to Moritzbergstrasse
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.465152 11.152112


// route from Chiemsee to Norddeich
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 47.889674 12.417799 53.612192 07.150162
// 04.05. ca. 200 Sekunden
// 06.05. ca. 115 Sekunden (+PriorityQueue, openNodeList == closedNodeList)
// 06.05. ca. 0,5 Sekunden (+spherical.greatCircleMeters, no outputs!)
// 13.05. ca. 1,6 Sekunden (+fixed bug, +write route.txt)

// draw map
// java -cp .;nav.jar pp.dorenda.client2.testapp.TestActivity -m webservice;geosrv.informatik.fh-nuernberg.de -c pp.dorenda.client2.additional.UniversalPainter -a Route.txt;s

/* TODO:
    - write turns.txt
    - Roth: MAX_SPEED_FOR_LINEAR_DISTANCE?
    - Roth: calculate f with costs of turns?
    - Roth: drivingCommands??
*/

public class Navigate {

    private final static double FAKTOR = 1000000.0;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 100;
	private final static String TURNS_TXT = "Turns.txt";
	private final static String ROUTE_TXT = "Route.txt";

    // coordinates for start and destination
    private static int start_lat;
    private static int start_lon;
    private static int stop_lat;
    private static int stop_lon;
    private static double start_lat_d;
    private static double start_lon_d;
    private static double stop_lat_d;
    private static double stop_lon_d;

    // coordinates for neighbors
    private static int neighborLat;
    private static int neighborLon;
	private static double neighborLat_d;
	private static double neighborLon_d;

	public static double beeLine;
	public static double distance;
	public static double speed;

	// create map with default speed for different streettypes <token, speed>
	public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();
	public static NavData nd;
	public static Spherical spherical;

    public static Node lastNode;
    public static Node currentNode;
    public static int nodeCount = 0;

    public static PrintWriter pwRoute;
    public static PrintWriter pwTurns;

	public static PriorityQueue<Node> NodePriorityQueue;

    public static boolean debug = false;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("usage NavDemo <navcache file>");
            System.exit(1);
        }

        try {

			nd = new NavData(args[0], true);

			// start timer
			// ##########################################
            long startTime = System.currentTimeMillis();

            // in case of speed is not given by the geo db
            setSpeedDefaults();

            // convert coords into an int by multiplying with a faktor
			start_lat_d = Double.parseDouble(args[1]);
			start_lat = convertCoordToInt(start_lat_d);
			start_lon_d = Double.parseDouble(args[2]);
			start_lon = convertCoordToInt(start_lon_d);
			stop_lat_d = Double.parseDouble(args[3]);
			stop_lat = convertCoordToInt(stop_lat_d);
			stop_lon_d = Double.parseDouble(args[4]);
			stop_lon = convertCoordToInt(stop_lon_d);

            // debug mode
            if(args.length > 5 && args[5].contains("debug")) debug = true;

			// get start and destination id
			int crossingIdStart = nd.getNearestCrossing(start_lat, start_lon);
			int crossingIdStop = nd.getNearestCrossing(stop_lat, stop_lon);

			Node nodeStart = new Node(crossingIdStart,start_lat, start_lon);
			Node nodeDestination = new Node(crossingIdStop,stop_lat, stop_lon);

			if(A_Star(nodeStart, nodeDestination)) {

				System.out.println("... Route found");

                // get count of nodes for array size
                int nodeCount = getNodeCount(currentNode);
                System.out.println("COUNT: " + nodeCount);

                // add 1 for the last node
                Node[] route = new Node [nodeCount+1];
                route = reverseRoute(nodeStart,currentNode,nodeCount);

                // write route.txt
                if(printRoute(route)) {
                    System.out.println("... Route printed");
                }
                else {
                    System.out.println("--- Route NOT printed");
                }

                // write turns.txt
                if(printTurns(route)) {
                    System.out.println("... Turns printed");
                }
                else {
                    System.out.println("--- Turns NOT printed");
                }

			}
			else {
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

    // initialize map with default speed
    private static void setSpeedDefaults() {

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
    }


    private static boolean printRoute(Node[] route) throws FileNotFoundException {

        pwRoute = new PrintWriter(ROUTE_TXT);
        pwRoute.println("LINE mode=4 col=0,255,0,200 rad=3 startflag=\"Start\" endflag=\"End\" middleflag=\"...Route...\"");

        String strLog = "";
        for (int i=0;i<route.length-1; i++) {
            pwRoute.println(convertCoordToDouble(route[i].lon) + "," + convertCoordToDouble(route[i].lat));
        }
        pwRoute.close();
        return true;
    }

    private static boolean printTurns(Node[] route) throws FileNotFoundException {

        pwTurns = new PrintWriter(TURNS_TXT);

        String strLog = "";
        for (int i=0;i<route.length-1; i++) {

            int domainID = nd.getDomainID(route[i].linkIDToPredecessor);
            if(nd.isDomain(domainID)) {

                strLog =
                "crossingID: ->" + route[i].crossingID
                + " DomainID:" + domainID
                + " DomainIDPosNrFrom:" + nd.getDomainPosNrFrom(route[i].linkIDToPredecessor)
                + " DomainIDPosNrTo:" + nd.getDomainPosNrTo(route[i].linkIDToPredecessor)
                + " Domain:" + nd.getDomainName(domainID)
                + " AngleFrom:" + route[i].angleFrom
                + " AngleTo:" + route[i].angleTo
                + " lat/lon:" + route[i].lat + ", " + route[i].lon;
            }
            System.out.println(strLog);
            pwTurns.println(strLog);
        }
        pwTurns.close();
        return true;
    }

    // count nodes which are concatinated by predecessors to determine what size of array is needed
    private static int getNodeCount(Node lastNode) {

        int nodeCount = 0;
        do {
            if(lastNode.predecessor != null) {
                nodeCount++;
                lastNode = lastNode.predecessor;
            }
            else {
                return nodeCount;
            }
        } while(true);
    }

    private static Node[] reverseRoute(Node start, Node lastNode, int size) {

        Node[] route = new Node[size+1];

        int i=size;
        route[i] = lastNode;

        do {
            if(lastNode.predecessor != null) {

                route[--i] = lastNode.predecessor;
                if(lastNode.predecessor.crossingID == start.crossingID) {
                    return route;
                }
                lastNode = lastNode.predecessor;
            }
            else {
                if(debug) System.out.println("Exit: " + lastNode);
                return null;
            }
        } while(true);
    }

	private static int convertCoordToInt(double coordinate) {
		return (int)(coordinate*FAKTOR);
	}

	private static double convertCoordToDouble(int coordinate) {
		return (double)(coordinate/FAKTOR);
	}

	private static boolean A_Star(Node start, Node destination){

		currentNode = null;
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
            if(debug) {
                System.out.println("+++++ Open Current: ->" + currentNode.crossingID);
                System.out.println("##### Current lat/lon: ->" + currentNode.lat + " " + currentNode.lon);
            }

            // if (currentNode.crossingID == 197969) {
            //     pollDataFromQueue(NodePriorityQueue);
            //     System.exit(1);
            // }

			if (currentNode.crossingID == destination.crossingID) {
                return true;
			}
            expand(openNodeList, closedNodeList, currentNode);
			closedNodeList[currentNode.crossingID] = true;

			if(debug) {
                System.out.println("+++++ Close Current: ->" + " crossingID: " + currentNode.crossingID);
                System.out.println("");
            }

            //x++;
			//if(x == 10000) System.exit(1);
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
            System.out.println("PQ Node - Pre: " + n.predecessor.crossingID + " crossingID: " + n.crossingID + " F:" + n.getValue_f());
        }
    }

	private static void expand (Node [] openNodeList, boolean [] closedNodeList, Node currentNode) {

		currentNode.links = nd.getLinksForCrossing(currentNode.crossingID);
		boolean found;
		Node NeighborNode = null;
        int crossingIDTo;

        double g;
        double f;

		// Neighbors
		for (int i = 0; i< currentNode.links.length; i++) {

            found = false;
			// domain
            if(debug) {
    			int domainID = nd.getDomainID(currentNode.links[i]);
    			if(nd.isDomain(domainID)) {
                    System.out.println("");
				    System.out.println("Neighbor: " + i + ":" + nd.getDomainName(domainID));
                    System.out.println("------------------------------------------------------");
    			}
    			else {
    				System.out.println("No Domain:" );
    			}
            }

			crossingIDTo = nd.getCrossingIDTo(currentNode.links[i]);

            if(nd.isIsolatedCrossiong(crossingIDTo)) {
                if(debug) System.out.println("### Isolated: " + i + ": To: " + crossingIDTo);
                continue;
            }

            if(nd.goesCounterOneway(currentNode.links[i])) {
                if(debug) System.out.println("### One Way: " + i + ": To: " + crossingIDTo);
                continue;
            }

			// is neighbor already closed
			if(closedNodeList[crossingIDTo] == false) {

                // is neighbor already open
				if(openNodeList[crossingIDTo] != null) {

					if(debug) System.out.println("+++++ Found Neighbor: " + crossingIDTo + "->" + openNodeList[crossingIDTo]);
					NeighborNode = openNodeList[crossingIDTo];
					found = true;
				}

				// if neighbor not open yet, create node
				if(found == false) {

					neighborLat = nd.getCrossingLatE6(crossingIDTo);
					neighborLon = nd.getCrossingLongE6(crossingIDTo);
					NeighborNode = new Node(crossingIDTo, neighborLat, neighborLon);
					openNodeList[crossingIDTo] = NeighborNode;

					neighborLat_d = convertCoordToDouble(neighborLat);
					neighborLon_d = convertCoordToDouble(neighborLon);

					if(debug) System.out.println("+++++ Open Neighbor: " + NeighborNode.crossingID);

                    // h
                    beeLine = spherical.greatCircleMeters(neighborLat_d,neighborLon_d,stop_lat_d,stop_lon_d);
					NeighborNode.setValue_h(getLinkCostsInSeconds(beeLine, MAX_SPEED_FOR_LINEAR_DISTANCE));
				}

                if(debug) System.out.println("##### Neighbor lat/lon: " + crossingIDTo + " - " + (long)NeighborNode.lat + " " + (long)NeighborNode.lon);

				// c = costs from current to neighbor
                distance = getDistancePerLink(currentNode.links[i]);
                speed = getMaxSpeedPerLink(currentNode,currentNode.links[i]);
				currentNode.setValue_c(getLinkCostsInSeconds(distance, speed));
				if(debug) System.out.println("C= " + currentNode.getValue_c());

				// g
				if(debug) System.out.println("G= " + currentNode.getValue_g() + " + " + currentNode.getValue_c());
                g = currentNode.getValue_g() + currentNode.getValue_c();

				// f
                if(debug) System.out.println("F= " + g + " + " + NeighborNode.getValue_h());
                f = g + NeighborNode.getValue_h();

                if(debug) System.out.println("F: " + f + " > " + NeighborNode.getValue_f());

				// if currentNode exists in openNodeList and f(currentNode) > f(neighbor)
				if(found == true && f >= NeighborNode.getValue_f()) {
                    if(debug) System.out.println("continue");
                    continue;
				}
				else {
					NeighborNode.predecessor = currentNode;
                    NeighborNode.predecessor.linkIDToPredecessor = currentNode.links[i];
                    NeighborNode.predecessor.angleFrom = nd.getNorthAngleFrom(currentNode.links[i]);
                    NeighborNode.predecessor.angleTo = nd.getNorthAngleTo(currentNode.links[i]);
					NeighborNode.setValue_g(g);
					NeighborNode.setValue_f(f);

                    if(found != true) {
                        NodePriorityQueue.add(NeighborNode);
                        if(debug) System.out.println("Add to Queue: " + NeighborNode.crossingID + " - Predecessor: " + NeighborNode.predecessor.crossingID);
                    }
				}
			}
			else {
				if(debug) System.out.println("Neighbor closed: " + crossingIDTo);
			}
		}
	}

    private static double getDistancePerLink(int linkID) {
        return (double) nd.getLengthMeters(linkID);
    }

    private static double getMaxSpeedPerLink(Node currentNode, int linkID) {

        int lsiClassNr = nd.getLSIclass(currentNode.crossingID);
        LSIClass lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);

        speed = (double) nd.getMaxSpeedKMperHours(linkID);

        // if no default speed limit is set
        if(speed == 0) {
            String token = lsiClass.classToken;
            speed = mapDefaultSpeed.get(token);
        }
        return speed;
    }

	// costs in seconds between two crossings
    private static double getLinkCostsInSeconds(double distance, double speed) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // time = distance/speed => 70m/s : 8,333m/s = 8,4s
        return (distance / ((speed * 1000) / 3600));
    }
}

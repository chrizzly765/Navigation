import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.io.*;
import java.lang.*;
import java.util.*;

// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

// javac -cp .;nav.jar Navigate.java

// route from Chiemsee to Norddeich
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 47.889674 12.417799 53.612192 07.150162
// java -cp .;nav.jar pp.dorenda.client2.testapp.TestActivity -m webservice;geosrv.informatik.fh-nuernberg.de -c pp.dorenda.client2.additional.UniversalPainter -a Route.txt;s

public class Navigate {

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

    public static NavData nd;
    public static Node lastNode;
    public static Node currentNode;

    public static PriorityQueue<Node> NodePriorityQueue;
    private static Route route;

    public static void main(String[] args) {

        // minimium 5 arguments
        if (args.length < 5) {
            System.out.println("error! usage: Navigate <navcache file> <lat_start> <lon_start> <lat_stop> <lon_stop>");
            System.exit(1);
        }

        try {

            // convert coords into an int by multiplying with a faktor
            start_lat_d = Double.parseDouble(args[1]);
            start_lat = Helper.convertCoordToInt(start_lat_d);
            start_lon_d = Double.parseDouble(args[2]);
            start_lon = Helper.convertCoordToInt(start_lon_d);
            stop_lat_d = Double.parseDouble(args[3]);
            stop_lat = Helper.convertCoordToInt(stop_lat_d);
            stop_lon_d = Double.parseDouble(args[4]);
            stop_lon = Helper.convertCoordToInt(stop_lon_d);

            nd = new NavData(args[0], true);

            // start timer
            long startTime = System.currentTimeMillis();

            // in case of speed is not given by the geo db
            Helper.setDefaultSpeed();

            // get start and destination id
            int crossingIdStart = nd.getNearestCrossing(start_lat, start_lon);
            int crossingIdStop = nd.getNearestCrossing(stop_lat, stop_lon);

            Node nodeStart =       new Node(crossingIdStart, start_lat_d, start_lon_d, stop_lat_d, stop_lon_d);
            Node nodeDestination = new Node(crossingIdStop,  stop_lat_d,  stop_lon_d,  stop_lat_d, stop_lon_d);

            if(A_Star(nodeStart, nodeDestination)) {

                route = new Route();
                route.getNodeCount(currentNode);
                route.reverseRoute(nodeStart,currentNode);

                route.printRoute();
                route.printTurns();

                // stop timer
                long stopTime = System.currentTimeMillis();
                long elapsed = ((stopTime - startTime));

                System.out.println("Zeit: " + elapsed + "ms");
            }
            else {
                System.out.println("no route possible!");
            }
        }
        catch (NumberFormatException nfe) {
            System.out.println("wrong format!");
        }
        catch (Exception e) {
            System.out.println("unexpectetd error!");
        }
    }

    private static boolean A_Star(Node start, Node destination){
        // check if crossingID is invalid
        if(start.crossingID == -1 || destination.crossingID == -1) {
			if(start.crossingID == -1) {
				System.out.println("no crossingID found for starting point!");
			}
			if(destination.crossingID == -1) {
				System.out.println("no crossingID found for destination!");
			}
            System.exit(1);
        }

        currentNode = null;
        Node [] openNodeList = new Node [nd.getCrossingCount()];
        boolean [] closedNodeList = new boolean [nd.getCrossingCount()];

        // use PriorityQueue to provide a structure sorted ascending by f
        NodePriorityQueue = new PriorityQueue<Node>(nd.getCrossingCount(), start);
        NodePriorityQueue.add(start);

        openNodeList[start.crossingID] = start;

        do {
            // assign least element to currentNode and remove from queue
            currentNode = NodePriorityQueue.remove();

            if (currentNode.crossingID == destination.crossingID) {
                return true;
            }
            expand(openNodeList, closedNodeList, currentNode);
            closedNodeList[currentNode.crossingID] = true;
        }
        while(NodePriorityQueue.size() > 0);
        return false;
    }

    private static void expand (Node [] openNodeList, boolean [] closedNodeList, Node currentNode) {

        currentNode.links = nd.getLinksForCrossing(currentNode.crossingID);
        boolean found;
        Node NeighborNode = null;
        int crossingIDTo;
        double c;
        double g;
        double f;

        // Neighbors
        for (int i = 0; i< currentNode.links.length; i++) {

            found = false;
            crossingIDTo = nd.getCrossingIDTo(currentNode.links[i]);

            if(nd.isIsolatedCrossiong(crossingIDTo)) {
                continue;
            }

            if(nd.goesCounterOneway(currentNode.links[i])) {
                continue;
            }

            // is neighbor already closed
            if(closedNodeList[crossingIDTo] == false) {

                // is neighbor already open
                if(openNodeList[crossingIDTo] != null) {
                    NeighborNode = openNodeList[crossingIDTo];
                    found = true;
                }

                // if neighbor not open yet, create node
                if(found == false) {
                    neighborLat = nd.getCrossingLatE6(crossingIDTo);
                    neighborLon = nd.getCrossingLongE6(crossingIDTo);
                    neighborLat_d = Helper.convertCoordToDouble(neighborLat);
                    neighborLon_d = Helper.convertCoordToDouble(neighborLon);

                    NeighborNode = new Node(crossingIDTo, neighborLat_d, neighborLon_d, stop_lat_d, stop_lon_d);
                    NeighborNode.links = nd.getLinksForCrossing(crossingIDTo);
                    openNodeList[crossingIDTo] = NeighborNode;
                }

                c = NeighborNode.c(currentNode, currentNode.links[i]);
                g = NeighborNode.g(currentNode, currentNode.links[i]);
                f = NeighborNode.f(currentNode, currentNode.links[i]);

                // if currentNode exists in openNodeList and f(currentNode) > f(neighbor)
                if(found == true && f >= NeighborNode.getValue_f()) {
                    continue;
                }
                else {
                    //new setPredecessor
                    NeighborNode.setPredecessor(currentNode, nd.getReverseLink(currentNode.links[i]), c, g, f);

                    if(found != true) {
                        NodePriorityQueue.add(NeighborNode);
                    }
                }
            }
        }
    }
}

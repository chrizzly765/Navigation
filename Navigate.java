import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.io.*;
import java.lang.*;
import java.util.*;

// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

// Earth Radius
//import fu.geo.Spherical;

// compile
// javac -cp .;nav.jar Navigate.java

// run
// route from Grünreutherstrasse to Kesslerplatz
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.453025 11.093324

// coordinates from route.txt
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.48431 11.197552 49.474915 11.122614

// route from Laufamholzstrasse to Moritzbergstrasse
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.465152 11.152112

//route Erlangen Berlin
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 49.594275 11.001648 52.416415 13.502733

//route bug fix test
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 50.098458 11.697518 50.079110 11.661637

// route from Chiemsee to Norddeich
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 47.889674 12.417799 53.612192 07.150162

// route from Bodensee to Rügen
// java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC.CAC 47.598265 7.650614 54.560074 13.63268

// 04.05. ca. 200 Sekunden
// 06.05. ca. 115 Sekunden (+PriorityQueue, openNodeList == closedNodeList)
// 06.05. ca. 0,5 Sekunden (+spherical.greatCircleMeters, no outputs!)
// 13.05. ca. 1,6 Sekunden (+fixed bug, +write route.txt)
// 15.05. ca. 17 Sekunden ()

// draw map
// java -cp .;nav.jar pp.dorenda.client2.testapp.TestActivity -m webservice;geosrv.informatik.fh-nuernberg.de -c pp.dorenda.client2.additional.UniversalPainter -a Route.txt;s

/* TODO:
- write turns.txt
- performance: try to avoid explicit casting
- Roth: MAX_SPEED_FOR_LINEAR_DISTANCE?
- Roth: calculate f with costs of turns?
- Roth: default speed as a reason of different turns?
*/

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

  // public static double beeLine;
  //public static double distance;
  //public static double speed;

  public static NavData nd;
  //public static Spherical spherical;

  public static Node lastNode;
  public static Node currentNode;

  public static PriorityQueue<Node> NodePriorityQueue;

  private static Route route;

  // debug
  // public static boolean debug = false;
  // public static String log = "";
  // private final static String LOG_TXT = "Log.txt";
  // public static String eol = System.getProperty("line.separator");
  // public static PrintWriter pwLog;

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
      Helper.setDefaultSpeed();

      // convert coords into an int by multiplying with a faktor
      start_lat_d = Double.parseDouble(args[1]);
      start_lat = Helper.convertCoordToInt(start_lat_d);
      start_lon_d = Double.parseDouble(args[2]);
      start_lon = Helper.convertCoordToInt(start_lon_d);
      stop_lat_d = Double.parseDouble(args[3]);
      stop_lat = Helper.convertCoordToInt(stop_lat_d);
      stop_lon_d = Double.parseDouble(args[4]);
      stop_lon = Helper.convertCoordToInt(stop_lon_d);

      // debug mode
      // if(args.length > 5 && args[5].contains("debug")) debug = true;

      // get start and destination id
      int crossingIdStart = nd.getNearestCrossing(start_lat, start_lon);
      int crossingIdStop = nd.getNearestCrossing(stop_lat, stop_lon);

      Node nodeStart =       new Node(crossingIdStart, start_lat_d, start_lon_d, stop_lat_d, stop_lon_d);
      Node nodeDestination = new Node(crossingIdStop,  stop_lat_d,  stop_lon_d, stop_lat_d, stop_lon_d);

      if(A_Star(nodeStart, nodeDestination)) {

        // System.out.println("... Route found");

        route = new Route();
        route.getNodeCount(currentNode);
        //System.out.println("Count: " + route.nodeCount);
        route.reverseRoute(nodeStart,currentNode);

        // write route.txt
      //   if(route.printRoute()) {
      //     System.out.println("... Route printed");
      //   }
      //   else {
      //     System.out.println("--- Route NOT printed");
      //   }
      //
      //   // write turns.txt
      //   if(route.printTurns()) {
      //     System.out.println("... Turns printed");
      //   }
      //   else {
      //     System.out.println("--- Turns NOT printed");
      //   }
      }
      // else {
      //   System.out.println("No route found!");
      // }

      long stopTime = System.currentTimeMillis();
      double elapsed = ((stopTime - startTime));

      // log += "Elapsed Time: " + stopTime + " - " + startTime + " = " + elapsed + "ms";
      System.out.println("Elapsed Time: " + stopTime + " - " + startTime + " = " + elapsed + "ms");
      // ##########################################
      // stop timer

      // if(debug) {
      //     pwLog = new PrintWriter(LOG_TXT);
      //     pwLog.println(log);
      //     pwLog.close();
      // }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static boolean A_Star(Node start, Node destination){

    currentNode = null;
    Node [] openNodeList = new Node [nd.getCrossingCount()];
    boolean [] closedNodeList = new boolean [nd.getCrossingCount()];

    // use PriorityQueue to provide a structure sorted ascending by f
    NodePriorityQueue = new PriorityQueue<Node>(nd.getCrossingCount(), start);
    NodePriorityQueue.add(start);

    openNodeList[start.crossingID] = start;

    int expandCalls = 0;
    do {

      // assign least element to currentNode and remove from queue
      currentNode = NodePriorityQueue.remove();
      // if(debug) {
      //     log += "+++++ Open Current: " + currentNode.crossingID +
      //             " ##### Current lat/lon: " + Helper.convertCoordToDouble(currentNode.lat) +
      //             " " + Helper.convertCoordToDouble(currentNode.lon) + eol;
      // }

      if (currentNode.crossingID == destination.crossingID) {
        // if(debug) {
        //     log += eol + "--------------------------------------------------------------------" + eol +
        //     "Count Expand: " + expandCalls + " ##### remainingElementsInQueue: " + NodePriorityQueue.size() + eol;
        // }
        System.out.println("expandCalls "+ expandCalls);
        return true;
      }
      expand(openNodeList, closedNodeList, currentNode);
      closedNodeList[currentNode.crossingID] = true;
      expandCalls++;

      // if(debug) log += "----- Close Current crossingID: " + currentNode.crossingID + eol + eol;
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

      // domain
      //   if(debug) {
      // int domainID = nd.getDomainID(currentNode.links[i]);
      // if(nd.isDomain(domainID)) {
      //           log += eol + "# Neighbor " + i + ": " + nd.getDomainName(domainID)
      //           + " ##### LinkID:" + currentNode.links[i] + eol + "----------"
      //           + " domainID: " + domainID + eol;
      // }
      // else {
      // 	log += "No Domain!" + eol;
      // }
      //   }

      crossingIDTo = nd.getCrossingIDTo(currentNode.links[i]);

      if(nd.isIsolatedCrossiong(crossingIDTo)) {
        // if(debug) log += "# Isolated " + i + ": To: " + crossingIDTo + eol;
        continue;
      }

      if(nd.goesCounterOneway(currentNode.links[i])) {
        // if(debug) log += "# One Way " + i + ": To: " + crossingIDTo + eol;
        continue;
      }

      // is neighbor already closed
      if(closedNodeList[crossingIDTo] == false) {

        // is neighbor already open
        if(openNodeList[crossingIDTo] != null) {

          // if(debug) log += "# Found Neighbor Node: " + openNodeList[crossingIDTo] + eol;
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

          // if(debug) log += "# Open Neighbor: " + NeighborNode.crossingID + eol;

          // h
          //beeLine = spherical.greatCircleMeters(neighborLat_d,neighborLon_d,stop_lat_d,stop_lon_d);
          //NeighborNode.setValue_h(Helper.getLinkCostsInSeconds(beeLine, Helper.MAX_SPEED_FOR_LINEAR_DISTANCE));
        }

        // if(debug) log += "Neighbor lat/lon: " + Helper.convertCoordToDouble(NeighborNode.lat) + " " + Helper.convertCoordToDouble(NeighborNode.lon) + eol;

        /*
        // c = costs from current to neighbor
        distance = (double) nd.getLengthMeters(currentNode.links[i]);

        speed = (double) nd.getMaxSpeedKMperHours(currentNode.links[i]);
        if(speed == 0) {
        speed = Helper.getDefaultSpeed(currentNode,currentNode.links[i]);

        if(debug) log += "DEFAULTSPEED: " + speed + eol;
      }
      if(debug) log += "Distance/Speed: " + distance + "/" + speed + eol;

      NeighborNode.setValue_c(Helper.getLinkCostsInSeconds(distance, speed));//currentNode.setValue_c(Helper.getLinkCostsInSeconds(distance, speed));
      if(debug) log += "c: " + NeighborNode.getValue_c() + eol;//if(debug) log += "c: " + currentNode.getValue_c() + eol;
      */
      c = NeighborNode.c(currentNode, currentNode.links[i]);
      // if(debug) log += "c: " + c;

      // g
      // if(debug) log += "g: " + currentNode.getValue_g() + " + " + NeighborNode.getValue_c() + eol;
      //if(debug) log += "g: " + currentNode.getValue_g() + " + " + currentNode.getValue_c() + eol;
      //g = currentNode.getValue_g() + NeighborNode.getValue_c();//g = currentNode.getValue_g() + currentNode.getValue_c();
      g = NeighborNode.g(currentNode, currentNode.links[i]);

      // f
      // if(debug) log += "f: " + g + " + " + NeighborNode.getValue_h() + eol;
      //f = g + NeighborNode.getValue_h();
      f = NeighborNode.f(currentNode, currentNode.links[i]);

      // if(debug) log += "if: found=" + found + " && " + f + " > " + NeighborNode.getValue_f() + eol;

      // if currentNode exists in openNodeList and f(currentNode) > f(neighbor)
      if(found == true && f >= NeighborNode.getValue_f()) {
        // if(debug) log += "continue" + eol;
        continue;
      }
      else {
        /*
        //currentNode.domainID = nd.getDomainID(currentNode.links[i]);
        NeighborNode.predecessor = currentNode;
        NeighborNode.linkIDToPredecessor = nd.getReverseLink(currentNode.links[i]);
        NeighborNode.domainID = nd.getDomainID(currentNode.links[i]);
        NeighborNode.setValue_g(g);
        NeighborNode.setValue_f(f);*/

        //new setPredecessor
        NeighborNode.setPredecessor(currentNode, nd.getReverseLink(currentNode.links[i]), c, g, f);

        if(found != true) {
          NodePriorityQueue.add(NeighborNode);
          // if(debug) log += "# Add to Queue: " + NeighborNode.crossingID +
          // " ##### Predecessor: " + NeighborNode.predecessor.crossingID +
          // " ##### domainID: " + NeighborNode.domainID +
          // eol;
        }
      }
    }
    // else {
    //           // if(debug) log += "# Neighbor closed: " + crossingIDTo + " ##### lat/lon: " + Helper.convertCoordToDouble(nd.getCrossingLatE6(crossingIDTo)) + " " + Helper.convertCoordToDouble(nd.getCrossingLongE6(crossingIDTo)) + eol;
    // }
  }
}
}

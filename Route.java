
import java.io.*;

public class Route
{
	int nodeCount = 0;
	Node[] route;
	
	public PrintWriter pwRoute;
    public PrintWriter pwTurns;
	
	private final String TURNS_TXT = "Turns.txt";
	private final String ROUTE_TXT = "Route.txt";
	
	public Route(){}
	
	// count nodes which are concatinated by predecessors to determine what size of array is needed
    public int getNodeCount(Node lastNode) {

        do {
            if(lastNode.predecessor != null) {
                nodeCount++;
                lastNode = lastNode.predecessor;
            }
            else {
                return nodeCount+1;
            }
        } while(true);
    }
	
	// run through nodes and store in array
    // e.g. nodeCount is 150
    // route[150] = lastNode
    // route[149] = lastNode.predecessor
    // ...
    // route[0] = startNode
    public Node[] reverseRoute(Node start, Node lastNode) {

        route = new Node[nodeCount+1];
        int i=nodeCount;
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
                //if(debug) System.out.println("Exit: " + lastNode);
                return null;
            }
        } while(true);
    }

    public boolean printRoute() throws FileNotFoundException {

        pwRoute = new PrintWriter(ROUTE_TXT);
        pwRoute.println("LINE mode=4 col=0,255,0,200 rad=3 startflag=\"Start\" endflag=\"End\" middleflag=\"...Route...\"");

        String strLog = "";
        for (int i=0;i<route.length-1; i++) {
            pwRoute.println(Helper.convertCoordToDouble(route[i].lon) + "," + Helper.convertCoordToDouble(route[i].lat));
        }
        pwRoute.close();
        return true;
    }

    public boolean printTurns() throws FileNotFoundException {

        pwTurns = new PrintWriter(TURNS_TXT);

        String strLog = "";
        for (int i=0;i<route.length-1; i++) {

            int domainID = Navigate.nd.getDomainID(route[i].linkIDToPredecessor);
            if(Navigate.nd.isDomain(domainID)) {

                strLog =
                "crossingID: ->" + route[i].crossingID
                + " crossingIDFrom:" + route[i].linkIDToPredecessor
                + " DomainID:" + domainID
                + " Domain:" + Navigate.nd.getDomainName(domainID)
                + " lat/lon:" + Helper.convertCoordToDouble(route[i].lat) + ", " + Helper.convertCoordToDouble(route[i].lon);
            }
            pwTurns.println(strLog);
        }
        pwTurns.close();
        return true;
    }
}
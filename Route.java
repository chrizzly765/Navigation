
import java.io.*;

public class Route
{
	public int nodeCount = 0;
	private Node[] route;

	private PrintWriter pwRoute;
    private PrintWriter pwTurns;

	private final String TURNS_TXT = "Turns.txt";
	private final String ROUTE_TXT = "Route.txt";

	public Route(){}

	// count nodes which are concatinated by predecessors to determine what size of array is needed
    public boolean getNodeCount(Node lastNode) {

        do {
            if(lastNode.predecessor != null) {
                nodeCount++;
                lastNode = lastNode.predecessor;
            }
            else {
                //return nodeCount+1;
				//nodeCount++;
				return true;
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

		//System.out.println("lat last:" +  lastNode.lat);
        do {
            if(lastNode.predecessor != null) {

                route[--i] = lastNode.predecessor;
				//System.out.println("i:" + i + " Node: " + lastNode.predecessor.lat);
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
		int[] latG;
		int[] lonG;
		int domainPosFrom;
		int domainPosTo;
		int diff;

        for (int i=0;i<route.length; i++) {

			// TODO: 0 = error
			// start node has no domainID
			if(route[i].domainID == 0) {
				continue;
			}

			if(Navigate.nd.isDomain(route[i].domainID)) {

				latG = Navigate.nd.getDomainLatsE6(route[i].domainID);
				lonG = Navigate.nd.getDomainLongsE6(route[i].domainID);
				domainPosFrom = Navigate.nd.getDomainPosNrFrom(route[i].linkIDToPredecessor);
				domainPosTo = Navigate.nd.getDomainPosNrTo(route[i].linkIDToPredecessor);
				diff = domainPosTo - domainPosFrom;

				for (int j=domainPosTo; ; ) {

					pwRoute.println(Helper.convertCoordToDouble(lonG[j]) + "," + Helper.convertCoordToDouble(latG[j]));
					if(j == domainPosFrom) break;

					if(diff > 0) j--;
					else j++;
				}
			}
        }
        pwRoute.close();
        return true;
    }

    public boolean printTurns() throws FileNotFoundException {

        pwTurns = new PrintWriter(TURNS_TXT);

        String strLog = "";

				// for(int i = 0; i<route.length; i++){
				// 	System.out.println("test " + i + " = " + route[i]);
				// }

        for (int i=0;i<route.length; i++) {

            int domainID = Navigate.nd.getDomainID(route[i].linkIDToPredecessor);
						String domainName = Navigate.nd.getDomainName(domainID);
						int nextDomainID;
						String nextDomainName;
						int alpha = Navigate.nd.getNorthAngleTo(domainID);
						int beta;
						//String nextDomainName = Navigate.nd.getDomainName(route[i+1].domainID);

            if(route[i+1] != null) {
								// System.out.println("route[i+1] != null");
								nextDomainID = Navigate.nd.getDomainID(route[i+1].linkIDToPredecessor);
								System.out.println(" nextDomainID "+ nextDomainID);
								System.out.println(" beta "+ Navigate.nd.getNorthAngleFrom(nextDomainID));
								nextDomainName = Navigate.nd.getDomainName(nextDomainID);
								beta = Navigate.nd.getNorthAngleFrom(nextDomainID);
								System.out.println(" beta "+ Navigate.nd.getNorthAngleFrom(nextDomainID));
								System.out.println(i + " alpha "+ alpha + " beta " + beta);


								if (domainName.equals(nextDomainName)) {
                	strLog += "Bleiben Sie auf der " + domainName;
								}
								else if((alpha+beta) >= 180 ) {
									strLog += "Biegen Sie bitte links ab, in die  " + domainName;
								}
								else if((alpha+beta) < 180) {
									strLog += "Biegen Sie bitte rechts ab, in die  " + domainName;
								}
								// else {
								//
								// }
            }

						strLog +=
						"crossingID: ->" + route[i].crossingID
						+ " crossingIDFrom:" + route[i].linkIDToPredecessor
						+ " DomainID:" + domainID
						+ " Domain:" + Navigate.nd.getDomainName(domainID)
						+ " lat/lon:" + Helper.convertCoordToDouble(route[i].lat) + ", " + Helper.convertCoordToDouble(route[i].lon);
            pwTurns.println(strLog);
         }
        pwTurns.close();
        return true;
     }
}
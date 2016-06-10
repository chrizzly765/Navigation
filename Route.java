
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

			//testing TURNS

			//compile
			//javac -cp .;nav.jar Navigate.java

			//abbiegen nach links:
			//java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.466032 11.156306

			//abbiegen nach rechts:
			//java -Xmx3072M -cp .;nav.jar Navigate CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.466716 11.157468

			// draw map
			// java -cp .;nav.jar pp.dorenda.client2.testapp.TestActivity -m webservice;geosrv.informatik.fh-nuernberg.de -c pp.dorenda.client2.additional.UniversalPainter -a Route.txt;s


        pwTurns = new PrintWriter(TURNS_TXT);

        String strLog = "";
				// PrintTest(262250);
				// PrintTest(308539);
				// PrintTest(262254);
				// PrintTest(262256);
				// PrintTest(262210);

        for (int i=1;i<route.length; i++) {

						int linkID = route[i].linkIDToPredecessor;
            int domainID = Navigate.nd.getDomainID(route[i].linkIDToPredecessor);
						String domainName = Navigate.nd.getDomainName(domainID);
						int nextDomainID;
						String nextDomainName;
						int alpha = Navigate.nd.getNorthAngleFrom(linkID);//ich fahre aus dem alten Link raus
						int beta;
						int nextLinkID;
						//int gamma;
						// int lastID = route[route.length-1].linkIDToPredecessor;
						// int afterNextID;

            if((i+1) < route.length) {
								// System.out.println("route[i+1] != null");
								nextLinkID = route[i+1].linkIDToPredecessor;
								nextDomainID = Navigate.nd.getDomainID(route[i+1].linkIDToPredecessor);
								nextDomainName = Navigate.nd.getDomainName(nextDomainID);
								beta = Navigate.nd.getNorthAngleTo(nextLinkID); //und fahre in den Neuen link rein
								strLog += "Durchgang " + i + Navigate.eol;
								if (domainName.equals(nextDomainName)) {
									strLog += "linkID " + linkID + Navigate.eol;
									strLog += "nextLinkID " + nextLinkID + Navigate.eol;
                	strLog += "Bleiben Sie auf der " + nextDomainName + Navigate.eol;
								}
								else if((alpha-beta) > 0 ) {
									strLog += "linkID " + linkID + Navigate.eol;
									strLog += "nextLinkID " + nextLinkID + Navigate.eol;
									strLog += "alpha " + alpha + Navigate.eol;
								strLog += "beta " + beta + Navigate.eol;
									strLog += "alpha + beta " + (alpha-beta) + Navigate.eol;
									strLog += "Biegen Sie bitte links ab, in die " + nextDomainName + Navigate.eol;
								}
								else if((alpha-beta) < 0) {
									strLog += "linkID " + linkID + Navigate.eol;
									strLog += "nextLinkID " + nextLinkID + Navigate.eol;
									strLog += "alpha " + alpha + Navigate.eol;
									strLog += "beta " + beta + Navigate.eol;
									//strLog += "gamma " + gamma + Navigate.eol;
									strLog += "alpha + beta " + (alpha-beta) + Navigate.eol;
									strLog += "Biegen Sie bitte rechts ab, in die " + nextDomainName +Navigate.eol;
									// if((i+3) < route.length){
									// 	afterNextID = route[i+3].linkIDToPredecessor;
									// 	// alpha = Navigate.nd.getNorthAngleTo(nextLinkID);
									// 	gamma = Navigate.nd.getNorthAngleTo(lastID);
									//
									// 	if((alpha+gamma) >= 180 ) {
									// 		strLog += "linkID " + linkID + Navigate.eol;
									// 		strLog += "nextLinkID " + nextLinkID + Navigate.eol;
									// 		strLog += "alpha " + alpha + Navigate.eol;
									// 		strLog += "beta " + beta + Navigate.eol;
									// 		strLog += "gamma " + gamma + Navigate.eol;
									// 		strLog += "alpha + beta " + (alpha+beta) + Navigate.eol;
									// 		strLog += "Biegen Sie bitte links ab, in die " + nextDomainName + Navigate.eol;
									// 	}
									// 	else if ((alpha+gamma) < 180){
									//
									// 	}

									// }

								}
            }

						// strLog +=
						// "crossingID: ->" + route[i].crossingID
						// + " crossingIDFrom:" + route[i].linkIDToPredecessor
						// + " DomainID:" + domainID
						// + " Domain:" + Navigate.nd.getDomainName(domainID)
						// + " lat/lon:" + Helper.convertCoordToDouble(route[i].lat) + ", " + Helper.convertCoordToDouble(route[i].lon);

         }
				 pwTurns.println(strLog);
        pwTurns.close();
        return true;

}
				//Only for Testing
				public void PrintTest (int linkId){
					System.out.println("---------"+linkId+"--------------");

					int crossingIDTo = 	Navigate.nd.getCrossingIDTo(linkId);
					System.out.println("crossingIDTo " + crossingIDTo);
					int latTo = Navigate.nd.getCrossingLatE6(crossingIDTo);
					System.out.println("latTo " + latTo);
					int longTo = Navigate.nd.getCrossingLongE6(crossingIDTo);
					System.out.println("longTo " + longTo);

					int crossingIDFrom = 	Navigate.nd.getCrossingIDFrom(linkId);
					System.out.println("crossingIDFrom " + crossingIDFrom);
					int latFROM = Navigate.nd.getCrossingLatE6(crossingIDFrom);
					System.out.println("latFROM " + latFROM);
					int longFROM = Navigate.nd.getCrossingLongE6(crossingIDFrom);
					System.out.println("longFROM " + longFROM);

					int alpha = Navigate.nd.getNorthAngleFrom(linkId);
					int beta = Navigate.nd.getNorthAngleTo(linkId);
					System.out.println("alphaFROM " + alpha);
					System.out.println("betaTO " + beta);
				}

				// public String LeftOrRight (int alpha, int beta){
				//
				// 	if((alpha+beta) >= 180 ) {
				// 		return "Biegen Sie bitte links ab, in die "
				// 	}
				// 	return "Biegen Sie bitte rechts ab, in die "
				//
				// }
}

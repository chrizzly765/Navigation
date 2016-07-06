
import java.io.*;

public class Route
{
	public int nodeCount = 0;
	private Node[] route;

	private PrintWriter pwRoute;
	private PrintWriter pwTurns;

	private final String TURNS_TXT = "Turns.txt";
	private final String ROUTE_TXT = "Route.txt";

	public static String eol = System.getProperty("line.separator");

	public Route(){}

		// count nodes which are concatinated by predecessors to determine what size of array is needed
		public boolean getNodeCount(Node lastNode) {

			do {
				if(lastNode.predecessor != null) {
					nodeCount++;
					lastNode = lastNode.predecessor;
				}
				else {
					return true;
				}
			} while(true);
		}

		//generates route in correct order
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
			boolean tmp = false;
			int linkID, domainID, nextDomainID, nextLinkID, alpha, beta, differenz, ergebnis;
			String domainName, nextDomainName, txtStaerke, txtLR, ziel;
			ziel = Navigate.nd.getDomainName(Navigate.nd.getDomainID(route[route.length-1].linkIDToPredecessor));

			for (int i=1;i<route.length-1; i++) {

				linkID = route[i].linkIDToPredecessor;
				domainID = Navigate.nd.getDomainID(route[i].linkIDToPredecessor);
				domainName = Navigate.nd.getDomainName(domainID);
				nextDomainID = Navigate.nd.getDomainID(route[i+1].linkIDToPredecessor);
				nextDomainName =  Navigate.nd.getDomainName(nextDomainID);;
				alpha = Navigate.nd.getNorthAngleFrom(linkID);
				nextLinkID = route[i+1].linkIDToPredecessor;
				beta = Navigate.nd.getNorthAngleTo(nextLinkID);
				differenz = 0;
				txtStaerke = "";
				txtLR ="";

				//direction
				ergebnis = beta - alpha;

				if (ergebnis >= 0){
					txtLR = "rechts ab, in ";
					differenz = ergebnis - 180;
				}
				else if (ergebnis < 0){
					txtLR = "links ab, in ";
					differenz = ergebnis + 180;
				}

				//differenz
				if (Math.abs(differenz) >= 0 && Math.abs(differenz) <= 80 ){
					txtStaerke = "scharf ";
				}
				else if (Math.abs(differenz) > 140 && Math.abs(differenz) < 160){
					txtStaerke = "leicht ";
				}
				else {
					txtStaerke = "";
				}

				if (domainName.equals(nextDomainName)) {
					if(!tmp){
							strLog += "Bleiben Sie auf der " + nextDomainName + eol;
							tmp = true;
						}
				}
				else if(Math.abs(differenz) >= 160){
						strLog += "Fahren Sie weiter gerade aus auf die " + nextDomainName + eol;
						tmp = false;
				}
				else {
					strLog += "Biegen Sie bitte " + txtStaerke + txtLR + nextDomainName + eol;
					tmp = false;
				}
			}
			strLog += "Sie haben die " + ziel + " erreicht!";
			pwTurns.println(strLog);
			pwTurns.close();
			return true;

		}
	}

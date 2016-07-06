
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

		// public boolean printTurns() throws FileNotFoundException {
		//
		// 	pwTurns = new PrintWriter(TURNS_TXT);
		//
		// 	String strLog = "";
		// 	boolean temp = false;
		// 	int linkID, domainID, nextDomainID, nextLinkID, alpha, beta, differenz;
		// 	String domainName, nextDomainName, txt;
		//
		// 	//start domain
		// 	strLog += "Sie sind auf der " + Navigate.nd.getDomainName(Navigate.nd.getDomainID(route[1].linkIDToPredecessor)) + eol;
		//
		// 	for (int i=1;i<route.length-1; i++) {
		//
		// 		linkID = route[i].linkIDToPredecessor;
		// 		domainID = Navigate.nd.getDomainID(route[i].linkIDToPredecessor);
		// 		domainName = Navigate.nd.getDomainName(domainID);
		// 		nextDomainID = Navigate.nd.getDomainID(route[i+1].linkIDToPredecessor);
		// 		nextDomainName =  Navigate.nd.getDomainName(nextDomainID);;
		// 		alpha = Navigate.nd.getNorthAngleTo(linkID);
		// 		nextLinkID = route[i+1].linkIDToPredecessor;
		// 		beta = Navigate.nd.getNorthAngleFrom(nextLinkID);
		// 		differenz = Math.abs(Math.abs(alpha)-Math.abs(beta));
		// 		txt = "";
		//
		// 		if (differenz > 10 && differenz <= 50 ){
		// 			txt = "leicht ";
		// 		}
		// 		else if(differenz > 140){
		// 			txt = "scharf ";
		// 		}
		//
		// 		if (domainName.equals(nextDomainName)) {
		// 			if (temp == false){
		// 				strLog += "Bleiben Sie auf der " + nextDomainName + eol;
		// 				temp = true;
		// 			}
		// 		}
		// 		else if(differenz <= 10){
		// 			strLog += "Fahren Sie weiter gerade aus auf die " + nextDomainName + eol;
		// 			temp = false;
		// 		}
		// 		else if((beta < 0 && alpha < 0)||(beta > 0 && alpha > 0)) {
		// 			if(alpha > beta){
		// 				strLog += "Biegen Sie bitte " + txt + "links ab, in " + nextDomainName + eol;
		// 			}
		// 			else if(alpha < beta) {
		// 				strLog += "Biegen Sie bitte " + txt + "rechts ab, in " + nextDomainName + eol;
		// 			}
		// 			temp = false;
		// 		}
		// 		else if(alpha > 0 && beta < 0){
		// 			strLog += "Biegen Sie bitte " + txt + " links ab, in " + nextDomainName + eol;
		// 			temp = false;
		// 		}
		// 		else if (alpha < 0 && beta > 0){
		// 			strLog += "Biegen Sie bitte " + txt + " rechts ab, in " + nextDomainName + eol;
		// 			temp = false;
		// 		}
		// 	}
		// 	strLog += "Sie haben Ihr Ziel erreicht!";
		// 	pwTurns.println(strLog);
		// 	pwTurns.close();
		// 	return true;
		//
		// }
		public boolean printTurns() throws FileNotFoundException {

			pwTurns = new PrintWriter(TURNS_TXT);

			String strLog = "";
			boolean temp = false;
			int linkID, domainID, nextDomainID, nextLinkID, alpha, beta, differenz;
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

				//Richtung bestimmen
				if (beta > 0) txtLR = "links ab, in ";
				else txtLR = "rechts ab, in ";

				//Differenz bestimmen
				//wenn gleiche vorzeichen
				if ((beta < 0 && alpha < 0)||(beta > 0 && alpha > 0)){
					differenz = 360 - (Math.abs(alpha) + (180 - Math.abs(beta)));
				}
				//wenn unterschiedliche vorzeichen
				else if ((alpha > 0 && beta < 0)||(alpha < 0 && beta > 0)){
					differenz = 360 - (Math.abs(alpha) + Math.abs(beta));
				}
				//differenz
				if (differenz > 10 && differenz <= 50 ){
					txtStaerke = "leicht";
				}
				else if (differenz > 140 && differenz < 150){
					txtStaerke = "scharf";
				}
				//zwischen 50 und 140
				else {
					txtStaerke = "";
				}


				if (domainName.equals(nextDomainName)) {
							strLog += "Bleiben Sie auf der " + nextDomainName + eol;
						}
				else if(differenz <= 10){
						strLog += "Fahren Sie weiter gerade aus auf die " + nextDomainName + eol;
						// temp = false;
				}
				else {
					strLog += "Biegen Sie bitte " + txtStaerke + " " + txtLR + " " + nextDomainName + eol;
				}


				// strLog += "---------TEST--------" + eol;
				// strLog += " alpha " + alpha + " beta " + beta + " differenz " + differenz + eol;
				// strLog +=" txtStaerke: " + txtStaerke + " txtLR " + txtLR + " Straße: " + nextDomainName + eol;
				// strLog += "---------TEST--------" + eol;

			}
			strLog += "Sie haben die " + ziel + " erreicht!";
			pwTurns.println(strLog);
			pwTurns.close();
			return true;

		}
	}

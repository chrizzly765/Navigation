// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

import java.io.*;
import java.util.*;

public final class Helper
{
	public final static int FACTOR = 1000000;
	private final static String TURNS_TXT = "Turns.txt";
	public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();
	public static PrintWriter pw;

	public static void setSpeedLimits(){
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
	}

	public static int convertCoordToInt(double coordinate) {
		return (int)(coordinate*FACTOR);
	}

	public static double convertCoordToDouble(int coordinate) {
		return (double)(coordinate/FACTOR);
	}

	public static double getLinkCostsInSeconds(double distance, double speed) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // time = distance/speed => 70m/s : 8,333m/s = 8,4s
        return (distance / ((speed * 1000) / 3600));
    }

	public static double getSpeedFromType(int crossingID){

		int lsiClassNr = Navigate.nd.getLSIclass(crossingID);
		LSIClass lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);

		String token = lsiClass.classToken;
		return mapDefaultSpeed.get(token);

	}

	// CS: added
	public static void drivingCommands(Node[] route) throws FileNotFoundException {

		// TODO: save link in node
	    pw = new PrintWriter(TURNS_TXT);
		String domainName = "";

		// walk through Nodes
		for (Node section : route) {

			int lsiClassNr = Navigate.nd.getLSIclass(section.crossingID);
 		    LSIClass lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);

			int domainID = Navigate.nd.getDomainID(section.linkId);
			if(Navigate.nd.isDomain(domainID)) {
				domainName = Navigate.nd.getDomainName(domainID);
			}
			else {
				System.out.println("No Domain");
			}

			System.out.println("Angle from: " + Navigate.nd.getNorthAngleFrom(section.linkId));
			System.out.println("Angle to: " + Navigate.nd.getNorthAngleTo(section.linkId));

			String cmdTurn = " - ";

			String row = cmdTurn + lsiClass.className + " " + domainName;
			System.out.println("Section: " + row);
 		    pw.println(row);

		}
			// ignore text in ()
			// check if turn left/right or straight on by using angle
			// if straight on, check if domainName changed
			// format:
			// Biegen Sie [(scharf)/links/rechts] ab auf [className] [domainName]
			// Fahren Sie weiter geradeaus auf [className] [domainName]

	   	pw.close();
	}
}

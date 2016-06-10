 import java.util.*;

// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

 public final class Helper
{
	public final static double FACTOR = 1000000.0;
	public final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 130;
	public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();

	public static int convertCoordToInt(double coordinate) {
		return (int)(coordinate*FACTOR);
	}

	public static double convertCoordToDouble(int coordinate) {
		return (double)(coordinate/FACTOR);
	}

	// initialize map with default speed
    public static void setDefaultSpeed() {

        mapDefaultSpeed.put("BAUSTELLE_VERKEHR", 30);
        mapDefaultSpeed.put("AUTOBAHN", MAX_SPEED_FOR_LINEAR_DISTANCE); //130
        mapDefaultSpeed.put("KRAFTFAHRSTRASSE", 100);//80
        mapDefaultSpeed.put("LANDSTRASSE", 100); //100
        mapDefaultSpeed.put("BUNDESSTRASSE", 100); //50
        mapDefaultSpeed.put("LANDSTRASSE_SEKUNDAER", 90);
        mapDefaultSpeed.put("LANDSTRASSE_TERTIAER", 60);
        mapDefaultSpeed.put("LANDSTRASSE_UNKLASSIFIZIERT", 40);
        mapDefaultSpeed.put("INNERORTSTRASSE", 50); //50
        mapDefaultSpeed.put("VERKEHRSBERUHIGTER_BEREICH", 15);
        mapDefaultSpeed.put("ANSCHLUSSSTELLE_AUTOBAHN", 80);
        mapDefaultSpeed.put("ANSCHLUSSSTELLE_KRAFTFAHRSTRASSE", 50);
        mapDefaultSpeed.put("ANSCHLUSSSTELLE_BUNDESSTRASSE", 50);
        mapDefaultSpeed.put("ANSCHLUSSSTELLE_SEKUNDAER", 30);
        mapDefaultSpeed.put("ANSCHLUSSSTELLE_TERTIAER", 20);
        mapDefaultSpeed.put("KREISVERKEHR", 20);
    }

	public static double getDefaultSpeed(Node currentNode, int linkID) {

        int lsiClassNr = Navigate.nd.getLSIclass(linkID);
        LSIClass lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);

        String token = lsiClass.classToken;
        //if(debug) log += "Streettype: " + token + eol;

        return mapDefaultSpeed.get(token);
    }

    public static double getLinkCostsInSeconds(double distance, double speed) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // time = distance/speed => 70m/s : 8,333m/s = 8,4s
        //return (distance / ((speed * 1000) / 3600));
		return ((distance * 3600) / (speed * 1000));
    }
}

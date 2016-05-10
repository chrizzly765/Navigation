// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

import java.util.*;

public final class Helper
{
	public final static int FACTOR = 1000000;	
	public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();
	
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
}
import java.util.*;

// Street Types
import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

public final class Helper
{
  public final static double FACTOR = 1000000.0;
  public final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 140;
  public static Map<String, Integer> mapDefaultSpeed = new HashMap<String, Integer>();

  public static int lsiClassNr;
  public static LSIClass lsiClass;

  public static int convertCoordToInt(double coordinate) {
    return (int)(coordinate*FACTOR);
  }

  public static double convertCoordToDouble(int coordinate) {
    return (double)(coordinate/FACTOR);
  }

  /* 
   * initialize map with default speed
  */
  public static void setDefaultSpeed() {

    mapDefaultSpeed.put("BAUSTELLE_VERKEHR", 30);
    mapDefaultSpeed.put("AUTOBAHN", MAX_SPEED_FOR_LINEAR_DISTANCE); //130
    mapDefaultSpeed.put("KRAFTFAHRSTRASSE", 100);//80
    mapDefaultSpeed.put("LANDSTRASSE", 100); //100
    mapDefaultSpeed.put("BUNDESSTRASSE", 100); //50
    mapDefaultSpeed.put("LANDSTRASSE_SEKUNDAER", 70);
    mapDefaultSpeed.put("LANDSTRASSE_TERTIAER", 50);
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
  
  public static double getDefaultSpeed(int linkID) {

    lsiClassNr = Navigate.nd.getLSIclass(linkID);
    lsiClass = LSIClassCentre.lsiClassByID(lsiClassNr);

    return mapDefaultSpeed.get(lsiClass.classToken);
  }

  public static double getLinkCostsInSeconds(double distance, double speed) {
    return ((distance / speed) *  (3600 /1000));
  }
}

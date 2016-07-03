import java.util.*;

// Street Types
import fu.keys.LSIClass;

public final class Helper {

    public final static double FACTOR = 1000000.0;
    public final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 140;
    public static int[] mapDefaultSpeed;

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

        // ADDED
        mapDefaultSpeed = new int[34176001];
        mapDefaultSpeed[32711000] = 30;
        mapDefaultSpeed[34110000] = MAX_SPEED_FOR_LINEAR_DISTANCE;
        mapDefaultSpeed[34120000] = 100;
        mapDefaultSpeed[34130000] = 100;
        mapDefaultSpeed[34131000] = 100;
        mapDefaultSpeed[34132000] = 70;
        mapDefaultSpeed[34133000] = 50;
        mapDefaultSpeed[34134000] = 40;
        mapDefaultSpeed[34141000] = 50;
        mapDefaultSpeed[34142000] = 15;
        mapDefaultSpeed[34171000] = 80;
        mapDefaultSpeed[34172000] = 50;
        mapDefaultSpeed[34173000] = 50;
        mapDefaultSpeed[34174000] = 30;
        mapDefaultSpeed[34175000] = 20;
        mapDefaultSpeed[34176000] = 20;
    }

    public static double getDefaultSpeed(int linkID) {

        lsiClassNr = Navigate.nd.getLSIclass(linkID);
        return mapDefaultSpeed[lsiClassNr];
    }

    public static double getLinkCostsInSeconds(double distance, double speed) {
        return ((distance / speed) *  (3600 /1000));
    }
}

import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;
import java.io.File;
import java.lang.*;

// compile
// javac -cp .;nav.jar Navigation.java

// run
// java -Xmx3072M -cp .;nav.jar Navigation CAR_CACHE_de_noCC.CAC



public class Navigation {

    private final static int START_LAT = 49466250;
    private final static int START_LON = 11157778;
    private final static double EARTH_RADIUS = 6378.388;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 130;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("usage NavDemo <navcache file>");
            System.exit(1);
        }

        try {
            /*NavData nd = new NavData(args[0], true);

            // start timer
            long startTime = System.currentTimeMillis();

            // crossing count 12916236
            // Initialize Array with all Crossings: 10ms
            int crossingCount = nd.getCrossingCount();
            //int crossingCount = 12916236;
            int[] arrCrossings = new int[crossingCount];


            // start position
            int crossingID = nd.getNearestCrossing(START_LAT,START_LON);
            int[] crossingLinkID = nd.getLinksForCrossing(crossingID);

            for (int i=0; i < crossingLinkID.length; i++) {

                // info
                System.out.println("LinkID: " + crossingLinkID[i]);
                System.out.println("LSIClass: " + nd.getLSIclass(crossingLinkID[i]));

                double meter = (double) nd.getLengthMeters(crossingLinkID[i]);
                System.out.println("Meter:" + meter);
                double speed = (double) nd.getMaxSpeedKMperHours(crossingLinkID[i]);
                System.out.println("Max. Speed (km/h):" + speed);

                // calculate costs for each link
                System.out.println("Time (s):" + Navigation.getLinkCostsInSeconds(meter, speed));

                int domainID = nd.getDomainID(crossingLinkID[i]);
                System.out.println("DomainName:" + nd.getDomainName(domainID));
                System.out.println();
            }

            // stop timer
            long stopTime = System.currentTimeMillis();
            double elapsed = ((stopTime - startTime));
            System.out.println(stopTime + " - " + startTime + " = " + elapsed + "ms");*/
            System.out.println(getLinearDistance(49.48431,11.197552,49.474915,11.122614));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static double getLinkCostsInSeconds(double s, double v) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // t = s/v => 70m/s : 8,333m/s = 8,4s
        return (s / ((v * 1000) / 3600));
    }


    private static double getLinearDistance(double latSource, double lonSource, double latDest, double lonDest) {

        latSource = Math.toRadians(latSource);
        lonSource = Math.toRadians(lonSource);
        latDest = Math.toRadians(latDest);
        lonDest = Math.toRadians(lonDest);

        // maps.google: Strasse => 6,3km; Luftlinie => 5,52km
        // cos(g) = cos(90° - lat1) * cos(90° - lat2) + sin(90° - lat1) * sin(90° - lat2) * cos(lon2 - lon1)
        // vereinfacht => cos(g) = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)
        double cosG = Math.sin(latSource) * Math.sin(latDest) + Math.cos(latSource) * Math.cos(latDest) * Math.cos(lonDest - lonSource);
        return EARTH_RADIUS * Math.acos(cosG);
    }


}

import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;
import java.io.File;
import java.lang.*;


// compile
// javac -cp .;nav.jar Navigation.java

// run
// java -Xmx3072M -cp .;nav.jar Navigation CAR_CACHE_de_noCC.CAC 49.46591000 11.15800500 49.94795167 10.07600667
// java -Xmx3072M -cp .;nav.jar Navigation CAR_CACHE_de_noCC_mittelfranken.CAC 49.46591000 11.15800500 49.94795167 10.07600667


public class Navigation {

    private static int start_lat = 49465910;
    private static int start_lon = 11158005;
    private static int stop_lat = 49947951;
    private static int stop_lon = 10076006;
    private final static double EARTH_RADIUS = 6378.388;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 100;


    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("usage NavDemo <navcache file>");
            System.exit(1);
        }

        try {

            NavData nd = new NavData(args[0], true);

            // start timer
            long startTime = System.currentTimeMillis();

            // crossing count 12916236
            // Initialize Array with all Crossings: 10ms
            //int crossingCount = nd.getCrossingCount();
            //int crossingCount = 12916236;
            //int[] arrCrossings = new int[crossingCount];
			
            // start position
            int nearestCrossingID = nd.getNearestCrossing(start_lat,start_lon);

            //int[] linkIDs = nd.getLinksForCrossing(nearestCrossingID);

            /*int startCrossingLat = nd.getCrossingLatE6(nearestCrossingID);
            int startCrossingLon = nd.getCrossingLongE6(nearestCrossingID);
            System.out.println("Start Crossing Lat/Lon: " + startCrossingLat + "/" + startCrossingLon);

            for (int i=0; i < linkIDs.length; i++) {

                System.out.println("LinkID: " + linkIDs[i]);
                System.out.println("LSIClass: " + nd.getLSIclass(linkIDs[i]));
                
                if(nd.goesCounterOneway(linkIDs[i])) {
                    System.out.println("Einbahnstrasse: true");
                }

                // crossing
                int crossingIdTo = nd.getCrossingIDTo(linkIDs[i]);
                int crossingIdFrom = nd.getCrossingIDFrom(linkIDs[i]);
                System.out.println("CrossingID From/To: " + crossingIdFrom + "/" + crossingIdTo);

                // calculate costs for each link
                double meter = (double) nd.getLengthMeters(linkIDs[i]);
                System.out.println("Meter:" + meter);
                double speed = (double) nd.getMaxSpeedKMperHours(linkIDs[i]);
                System.out.println("Max. Speed (km/h):" + speed);
                System.out.println("Time (s):" + getLinkCostsInSeconds(meter, speed));

                // angle
                int angleFrom = nd.getNorthAngleFrom(linkIDs[i]);
                System.out.println("Angle from:" + angleFrom);
                int angleTo = nd.getNorthAngleTo(linkIDs[i]);
                System.out.println("Angle to:" + angleTo);

                // domain
                int domainID = nd.getDomainID(linkIDs[i]);
                if(nd.isDomain(domainID)) {
                    System.out.println("DomainName:" + nd.getDomainName(domainID));
                    *//*int[] domainLats = nd.getDomainLatsE6(domainID);
                    System.out.println("DomainLats:" + domainLats.toString());*//*
                }
                System.out.println();
            }
            
            double linearDistance = getLinearDistanceInMeter(49.48431,11.197552,49.474915,11.122614);
            //double linearDistance = getLinearDistanceInMeter(start_lat,start_lon,stop_lat,stop_lon);
            System.out.println("Linear Distance: " + linearDistance + " Time:" + getLinkCostsInSeconds(linearDistance,MAX_SPEED_FOR_LINEAR_DISTANCE));
            */
            // stop timer
            long stopTime = System.currentTimeMillis();
            double elapsed = ((stopTime - startTime));
            System.out.println(stopTime + " - " + startTime + " = " + elapsed + "ms");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static double getLinkCostsInSeconds(double distance, double speed) {

        // 30km/h = 30000m / 3600s = 8,333m/s
        // time = distance/speed => 70m/s : 8,333m/s = 8,4s
        return (distance / ((speed * 1000) / 3600));
    }

    private static double getLinearDistanceInMeter(double latSource, double lonSource, double latDest, double lonDest) {

        latSource = Math.toRadians(latSource);
        lonSource = Math.toRadians(lonSource);
        latDest = Math.toRadians(latDest);
        lonDest = Math.toRadians(lonDest);

        // maps.google: Strasse => 6,3km; Luftlinie => 5,52km
        // cos(g) = cos(90° - lat1) * cos(90° - lat2) + sin(90° - lat1) * sin(90° - lat2) * cos(lon2 - lon1)
        // vereinfacht => cos(g) = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)
        double cosG = Math.sin(latSource) * Math.sin(latDest) + Math.cos(latSource) * Math.cos(latDest) * Math.cos(lonDest - lonSource);
        return (EARTH_RADIUS * Math.acos(cosG)) * 1000;
    }


}



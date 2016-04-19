import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.io.File;
import java.util.Arrays;

public class Navigate {

	private static int start_lat;
    private static double start_lat_d;
    private static int start_lon;
    private static double start_lon_d;
    private static int stop_lat;
    private static double stop_lat_d;
    private static int stop_lon;
    private static double stop_lon_d;

	private final static double EARTH_RADIUS = 6378.388;
	private final static int FAKTOR = 1000000;
    private final static int MAX_SPEED_FOR_LINEAR_DISTANCE = 100;	
	private final static String TURNS_TXT = "Turns.txt";
	private final static String ROUTE_TXT = "Route.txt";

	public static void main(String[] args) {
	
		if (args.length < 1) {
            System.out.println("usage NavDemo <navcache file>");
            System.exit(1);
        }
		try
		{
		
			NavData  nd = new NavData(args[0],true);		
			Node [] openNodeList = new Node [nd.getCrossingCount()];

			// convert coords into an int value by multiplying with a faktor			
			start_lat_d = Double.parseDouble(args[1]);
			start_lat = (int)(start_lat_d*FAKTOR);
			start_lon_d = Double.parseDouble(args[2]);			
			start_lon = (int)(start_lon_d*FAKTOR);			
			stop_lat_d = Double.parseDouble(args[3]);
			stop_lat = (int)(stop_lat_d*FAKTOR);
			stop_lon_d = Double.parseDouble(args[4]);
			stop_lon = (int)(stop_lon_d*FAKTOR);
			
			////TEST
			Node start = new Node(nd, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			/* start.Value_c() = 20;
		
			Node eins = new Node();
			eins.f = 12;

			Node zwei = new Node();
			zwei.f = 14;
		
			Node drei = new Node();
			drei.f = 2; */
/* 			int test = (int)Math.ceil((double)0/2);
			
			System.out.println(test);
		 */
			// start timer
            long startTime = System.currentTimeMillis();
			// start position
            int nearestCrossingID = nd.getNearestCrossing(start_lat,start_lon);
		 
			/* pushNode(openNodeList, start);
			printArray (openNodeList);
			pushNode(openNodeList, drei);
			printArray (openNodeList);
			pushNode(openNodeList, zwei);
			printArray (openNodeList);
			pushNode(openNodeList, eins);
			printArray (openNodeList);
			
			deleteNode(openNodeList);
			printArray (openNodeList);
			deleteNode(openNodeList);
			printArray (openNodeList);
			deleteNode(openNodeList);
			printArray (openNodeList);
			deleteNode(openNodeList);
			printArray (openNodeList);
			System.out.println("ENDE!"); */
		
			////TEST
			
			// stop timer
            long stopTime = System.currentTimeMillis();
            double elapsed = ((stopTime - startTime));
            System.out.println(stopTime + " - " + startTime + " = " + elapsed + "ms");
 
		}
		
		catch (Exception e) {
		e.printStackTrace();
		}

	}
	
	static void pushNode (Node [] openNodeList, Node newOpenNode){
		System.out.println("ArrayPushSortBinarySearch");
		
		//nächste Freie Position des Arrays ermitteln
		
		int nextFreePos = 0;
		int insertPosition = 0;
	
		while(openNodeList[nextFreePos] != null){
			nextFreePos++;
		}
		
		System.out.println("Free Position: " + nextFreePos);
		
		//INSERT POSITION SUCHEN
		
		//einfügen am letzten Platz falls es gleich größer ist
		
		if (openNodeList[0] == null){
			openNodeList[0] = newOpenNode;
		}
		else if (openNodeList[nextFreePos-1].Value_f() <= newOpenNode.Value_f()){
			openNodeList[nextFreePos] = newOpenNode;
		}
		//ansonsten binäre suche
		else{
			insertPosition = binarySearch(openNodeList, newOpenNode, nextFreePos);
			for (int i = nextFreePos; i > insertPosition; i--){
	
			openNodeList[i] = openNodeList[i-1];
		}
	
		openNodeList[insertPosition] = newOpenNode;
		}
	}
		
	static int binarySearch (Node [] openNodeList, Node newOpenNode, int nextFreePos){
						
			int insertPosition = 0;
			int l = 0;
			int r = nextFreePos-1;
			
			
				do{
					insertPosition = (int)Math.ceil((double)(l + r) / 2);
					
					System.out.println("insertPosition in binarySearch: " + insertPosition);
				
					if(openNodeList[insertPosition].Value_f() <= newOpenNode.Value_f()){
						l = insertPosition+1;
					}
					else {
						r = insertPosition-1;
					}
				}
				while (openNodeList[insertPosition].Value_f() != newOpenNode.Value_f() && l<=r);
				
				// wenn das Element nicht gefunden wurde, dann richtige Einfügeposition überprüfen, an die das Element gehört
				if (openNodeList[insertPosition].Value_f() != newOpenNode.Value_f())
					{
						if (openNodeList[insertPosition].Value_f() < newOpenNode.Value_f())
						{
							insertPosition++;
						}
					}
                // wenn Element gefunden, so lange nach links gehen wie gleicher Wert vorhanden
				else
					{
						// (insertPosition > 0) Stellt sicher, dass wir nicht an content[-1] suchen
						while (insertPosition > 0 && openNodeList[insertPosition].Value_f() == openNodeList[insertPosition - 1].Value_f())
						{
							insertPosition--;
						}	
					}
				
				return insertPosition;
	}
		
	static void deleteNode (Node [] openNodeList){
		
		for (int i = 1; i<= openNodeList.length; i++)
		{
			if(openNodeList[i]!= null){
			openNodeList[i-1] = openNodeList[i];
			}
			else{
				openNodeList[i-1] = null;
				break;
			}
		}
	}
	
	//Für uns zur Testzwecken
	static void printArray (Node [] openNodeList){
		System.out.println("Print ARRAY");
		for (int i = 0; i<= openNodeList.length; i++)
		{
			if (openNodeList[i] != null){
			System.out.println("position im Array: " + i + " vorhandene F: " + openNodeList[i].Value_f());
			}
			else{
				break;
			}
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



	

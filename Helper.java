//import nav.NavData;

 public final class Helper
{
	public final static int FACTOR = 1000000;	
	
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
}
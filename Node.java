import java.util.Comparator;

public class Node implements Comparator<Node>{

	public int [] links;
	public int linkIDToPredecessor;
	public Node predecessor;
	public int crossingID;
	public int lat;
	public int lon;

	// ??
	public int angleTo;
	public int angleFrom;

	private double c;
	private double f;
	private double g;
	private double h;

	public Node(int _crossingID, int _lat, int _lon) {

		this.crossingID = _crossingID;
		this.lat = _lat;
		this.lon = _lon;
		this.c = 0;
		this.f = 0;
		this.g = 0;
		this.h = 0;
	}

	@Override
	public int compare(Node n1, Node n2) {

		 // n1 > n2 = 1 => sort asc
		 if(n1.getValue_f() > n2.getValue_f()) return 1;
		 else if(n1.getValue_f() < n2.getValue_f()) return -1;
		 else return 0;
	}

	// Setter and Getter
	public void setValue_h(double h) {
		this.h = h;
	}

	public double getValue_h() {
		return this.h;
	}

	public void setValue_c(double c) {
		this.c = c;
	}

	public double getValue_c() {
		return this.c;
	}

	public void setValue_g(double g) {
		this.g = g;
	}

	public double getValue_g() {
		return this.g;
	}

	public void setValue_f(double f) {
		this.f = f;
	}

	public double getValue_f() {
		return this.f;
	}

	public String toString() {

		//String eol = System.getProperty("line.separator");
		return "crossingID: " + this.crossingID +
				//" lat: " + this.lat +
				//" lon: " + this.lon +
				" C: " + this.getValue_c() +
				" H: " +this.getValue_h() +
				" G: " +this.getValue_g() +
				" F: " +this.getValue_f();
	}
}

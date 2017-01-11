public class Location {
	private int x;
	private int y;
	private int demand;
	private int index;
	private double degree;

	public Location(int index, int x, int y, int demand) {
		this.x = x;
		this.y = y;
		this.demand = demand;
		this.index = index;
		this.degree = 999;
	}
	
	public Location (Location location){
		this(location.index, location.x, location.y, location.demand);
	}
	
	public double getDegree() {
		return degree;
	}

	public void setDegree(double degree) {
		this.degree = degree;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getDemand() {
		return demand;
	}
	
	public int getIndex() {
		return index;
	}
}

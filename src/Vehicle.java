import java.util.ArrayList;

public class Vehicle {
	private int remainingCapacity;
	private Location location;
	private ArrayList<Location> route;

	public Vehicle(int startX, int startY, int startingLocation, int capacity) {
		this.route = new ArrayList<Location>();
		this.remainingCapacity = capacity;
		this.location = new Location(startingLocation, startX, startY, 0);
		this.route.add(new Location(startingLocation, startY, startY, 0));
	}
	
	public ArrayList<Integer> GetRouteIndexes(){
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < route.size(); i ++)
			indexes.add(route.get(i).getIndex());
		return indexes;
	}
	
	public void setRemaining(int remaining) {
		this.remainingCapacity = remaining;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void addRoute(Location dest) {
		this.route.add(dest);
	}

	public int getRemaining() {
		return remainingCapacity;
	}

	public Location getLocation() {
		return location;
	}

	public ArrayList<Location> getRoute() {
		return route;
	}
}

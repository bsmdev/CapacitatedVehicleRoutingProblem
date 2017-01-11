import java.util.ArrayList;
import java.util.Random;
import java.awt.Point;

public class CVRP {
	private CVRPData data;
	
	private final int FULL_PROBABILITY = 100;
	private Random random;
	
	private ArrayList<Location> currentLocationsList;
	private ArrayList<ArrayList<Integer>> currentPopulation;
	private ArrayList<ArrayList<Integer>> bestRoutes;	
	private double minimalTotalCost;

	private int mutationRate;
	private int crossoverRate;
	private int generationsCount;
	
	public CVRP(CVRPData data){
		this.data = data;
		this.random = new Random();
		this.minimalTotalCost = 0;
		this.bestRoutes = new ArrayList<ArrayList<Integer>>();
		initLocations();
	}
	
	 public static void main(String[] args){
		 	//Read fruitybun250 data
			CVRPData data = new CVRPData();		
		
			//Set genetic parameters and execute the algorithm
			int populationSize = 50;
			int crossoverRate = 95;
			int mutationRate = 3;

			//data.NUM_NODES = 6;
			CVRP cvrp = new CVRP(data);
			cvrp.solve(populationSize, crossoverRate, mutationRate);	
			
			//Print total cost
			System.out.println("cost " + cvrp.minimalTotalCost);
	}
	 
	/* Initialize data into locations and triangulate the angles of the locations into an order*/
	private void initLocations() {
		ArrayList<Location> locations = createLocationsWithAngles();
		this.currentLocationsList = reorderLocationsByAngle(locations);
	}
	
	/* operation over generation */
	public void solve(int populationSize, int crossoverRate,int mutationRate) {
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		
		Point locationDepo = data.getLocation(data.DEPOT_LOCATION);
		Vehicle vehicle;
		
		do {
			//Visit the depot and the other locations with the vehicle
			vehicle = new Vehicle(locationDepo.x, locationDepo.y, data.DEPOT_LOCATION, data.VEHICLE_CAPACITY);
			visitLocations(vehicle, currentLocationsList);
			
			//Exit if it is already into the trivial route
			if (vehicle.getRoute().size() <= 2)
				break;
			
			//Get the indexes of the vehicle route
			ArrayList<Integer> routeIndexes = vehicle.GetRouteIndexes();
			
			//Create a population by mutating the existing route
			populateByMutations(routeIndexes, populationSize);
			
			//Using cross over and mutations choose the best route of the current route population
			ArrayList<Integer> bestRoute = chooseBestRouteInGenerations();
			
			//Add the cost to the total minimal solution
			this.minimalTotalCost += routeLength(bestRoute);
			
			//Save the route
			this.bestRoutes.add(bestRoute);
			
			//Print the found best route
			printBestRoute(bestRoute);
			
		} while (vehicle.getRoute().size() > 2);
	}
	
	public ArrayList<Integer> chooseBestRouteInGenerations(){
		ArrayList<Integer> bestRoute = null;
		double minimalCost = Double.MAX_VALUE;
		int notUpdate = 0;
		long time1 = System.nanoTime();
		
		//When we have many non updates of the 
		while(notUpdate < this.currentPopulation.size() * 80) {
			//Do a cross over of the existing population and then mutate it
			this.currentPopulation = crossOver();
			mutatePopulation();
			//System.out.println(minimalCost);
			for (int j = 0; j < currentPopulation.size(); j++) {
				ArrayList<Integer> routeInPopulation = currentPopulation.get(j); 
				double cost = routeLength(routeInPopulation);
				if (cost < minimalCost) {
					bestRoute = routeInPopulation;
					minimalCost = cost;
					notUpdate = 0;
				}
				else{
					notUpdate++;
				}
			}	
			//System.out.println(minimalCost);
		}
		
		long time2 = System.nanoTime();
		long timeTaken = time2 - time1;  
		//System.out.println("Time taken " + timeTaken + " ns");  
		return bestRoute;
	}

	private ArrayList<Location> createLocationsWithAngles() {
		ArrayList<Location> initialLocations = new ArrayList<Location>();
		
		for (int i = 0; i < data.NUM_NODES; i++) {
			//Fetch location with demand from data
			Point location = data.getLocation(i);
			int demand = data.getDemand(i);
			
			//Add the new location to the locations map with its index
			Location newLocation = new Location(i, location.x, location.y, demand);
			initialLocations.add(newLocation);

			//Calculate the angle between the new location and the depot
			Location depotLocation = initialLocations.get(data.DEPOT_LOCATION);
			double degreeDepotLocation = getAngleOfLocations(newLocation, depotLocation);
		
			//Skip depot and add fixed values dependent on the relation of the depot and location on the quadrants to make them from 0 to 360
			if (i != data.DEPOT_LOCATION) {
				if (newLocation.getX() >= depotLocation.getX() && newLocation.getY() >= depotLocation.getY())
					newLocation.setDegree(degreeDepotLocation);
				
				else if (newLocation.getX() < depotLocation.getX())
					newLocation.setDegree(degreeDepotLocation + 180);
				
				else if (newLocation.getX() >= depotLocation.getX() && newLocation.getY() < depotLocation.getY())
					newLocation.setDegree(degreeDepotLocation + 360);
			}
			//System.out.print(newLocation.getDegree() + " ");
		}
		return initialLocations;
	}

	private ArrayList<Location> reorderLocationsByAngle(ArrayList<Location> locations) {
		ArrayList<Location> reorderedLocations = new ArrayList<Location>();
		
		Location depotLocation = locations.get(data.DEPOT_LOCATION);
		reorderedLocations.add(depotLocation);
		
		while (locations.size() > 1) {
			int minimalAngleIndex = 0;
			double degreeMin = Double.MAX_VALUE;
			Location bestLocation = new Location(0, 0, 0, 0);
			//Skipping the depot
			for (int j = 1; j < locations.size(); j++) {
				Location currentLocation = locations.get(j);
				if (currentLocation.getDegree() <= degreeMin) {
					degreeMin = currentLocation.getDegree();
					minimalAngleIndex = j;
					bestLocation = currentLocation;
				}
			}
			reorderedLocations.add(bestLocation);
			locations.remove(minimalAngleIndex);
		}
		return reorderedLocations;
	}

	/* cross over the population one time */
	private ArrayList<ArrayList<Integer>> crossOver() {
		ArrayList<ArrayList<Integer>> nextGen = new ArrayList<ArrayList<Integer>>();
		
		while (currentPopulation.size() != 0) {
			ArrayList<ArrayList<Integer>> parents = getParents();
			ArrayList<Integer> parent1 = parents.get(0);
			int position1 = random.nextInt(parent1.size() - 2) + 1;
			ArrayList<Integer> child1 = parent1;
			
			ArrayList<Integer> parent2 = parents.get(1);
			int position2 = random.nextInt(parent1.size() - position1 - 1) + position1 + 1;
			ArrayList<Integer> child2 = parent2;			
			
			for (int j = position1; j <= position2; j++) {
				int gene1 = child1.get(j);
				int gene2 = child2.get(j);
				
				child1.set(child1.indexOf(gene2), gene1);
				child1.set(j, gene2);
				
				child2.set(child2.indexOf(gene1), gene2);
				child2.set(j, gene1);
			}
			nextGen.add(child1);
			nextGen.add(child2);
		}
		return nextGen;
	}

	/* get two parents from populatioin for crossover */
	private ArrayList<ArrayList<Integer>> getParents() {
		ArrayList<ArrayList<Integer>> parent = new ArrayList<ArrayList<Integer>>();
		
		double fitness = 0;
		while ((currentPopulation.size() != 0) && (parent.size() < 2)) {
			int prob = random.nextInt(currentPopulation.size());
			if (prob < this.crossoverRate) {
				fitness = random.nextDouble() * getFitnessOfPopulation(currentPopulation);
				if (fitness < getFitness(currentPopulation, prob)) {
					parent.add(new ArrayList<Integer>(currentPopulation.get(prob)));
					currentPopulation.remove(prob);
				}
			}
		}
		return parent;
	}

	/* make a population based on original route, with mutation rate of 50 */
	private void populateByMutations(ArrayList<Integer> route, int populationSize) {
		currentPopulation = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> newRoute = new ArrayList<Integer>();
		for (int i = 0; i < populationSize; i++) {
			newRoute = mutateRoute(route);
			currentPopulation.add(newRoute);
		}
	}

	private void visitLocations(Vehicle vehicle, ArrayList<Location> locations) {
		for (int i = 1; i < locations.size(); i++) {
			Location current = locations.get(i);
			if (current == null)
				continue;
			
			//Check if the vehicle fits the demand
			if (vehicle.getRemaining() < getDemand(i)){
				break;
			}
			else{
				vehicle.setRemaining(vehicle.getRemaining() - getDemand(i));
				vehicle.setLocation(current);
				vehicle.addRoute(current);
				locations.set(i, null);
			}
		}
		
		Location depot = currentLocationsList.get(data.DEPOT_LOCATION);
		vehicle.addRoute(depot);
	}
	
	/* route mutation, which switches the order of locations */
	private void mutatePopulation() {
		for (int i = 0; i < currentPopulation.size(); i++) {
			ArrayList<Integer> currentRoute = currentPopulation.get(i);
			ArrayList<Integer> mutatedRouter = mutateRoute(currentRoute); 
			currentPopulation.set(i, mutatedRouter);
		}
	}

	private ArrayList<Integer> mutateRoute(ArrayList<Integer> route) {
		for (int currentGene = 1; currentGene < route.size() - 1; currentGene++) {
			boolean probability = random.nextInt(FULL_PROBABILITY) <= mutationRate;
			if (probability) {
				int oldGeneValue = route.get(currentGene);
				// To skip the last element and having count from 0 we substract 2 and then add 1 to skip first depot visit
				int randomGene = random.nextInt(route.size() - 2) + 1;
				int newGeneValue = route.get(randomGene);
				
				//Swap current i position of the route with the index position and their corresponding values
				route.set(currentGene, newGeneValue);
				route.set(randomGene, oldGeneValue);
			}
		}
		return route;
	}

	/* calculate degree between two locations */
	private double getAngleOfLocations(Location a, Location b) {
		double tangent = (double) ((a.getY() - b.getY())) / ((double) (a.getX() - b.getX()));
		double artanInRadians = Math.atan(tangent);
		double degree = Math.toDegrees(artanInRadians);
		return degree;
	}
	
	/* calculate the fitness of a chromosome */
	private double getFitness(ArrayList<ArrayList<Integer>> pop, int index) {
		double fitness = 0;
		double totalLength = 0;
		for (int i = 0; i < pop.size(); i++) {
			totalLength += routeLength(pop.get(i));
		}
		fitness = totalLength / routeLength(pop.get(index));
		return fitness;
	}

	/* calculate the fitness of a population */
	private double getFitnessOfPopulation(ArrayList<ArrayList<Integer>> population) {
		double totalLength = 0;
		for (int i = 0; i < population.size(); i++) {
			totalLength += routeLength(population.get(i));
		}
		
		double fitnessTotal = 0;
		for (int i = 0; i < population.size(); i++) {
			fitnessTotal += totalLength / routeLength(population.get(i));
		}
		return fitnessTotal;
	}
	
	private double routeLength(ArrayList<Integer> route) {
		double result = 0;
		for (int i = 0; i < route.size() - 1; i++){
			int locationIndex1 = route.get(i);
			int locationIndex2 = route.get(i + 1);
			result += data.getDistance(locationIndex1, locationIndex2);
		}
		return result;
	}
	
	/* get the demand of a customer by its index */
	private int getDemand(int index) {
		return currentLocationsList.get(index).getDemand();
	}

	public double getDist(Location from, Location to) {
		int x1 = from.getX();
		int y1 = from.getY();
		int x2 = to.getX();
		int y2 = to.getY();
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}
	
	private void printBestRoute(ArrayList<Integer> bestRoute) {
		for(int j = 0; j < bestRoute.size(); j++)
			if(j > 0)
				System.out.print("->" + (bestRoute.get(j) + 1));
			else
				System.out.print((bestRoute.get(j)+ 1));
		System.out.println();
	}
}

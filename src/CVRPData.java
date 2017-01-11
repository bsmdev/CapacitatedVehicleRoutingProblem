import java.io.*;
import java.awt.Point;

/**
 * CVRPData.java
 *
 * This class reads in a CVRP data file and provides "get methods" to
 * access the demand and distance information. It does some
 * parameter checking to make sure that nodes passed to the get
 * methods are within the valid range.
 *
 * It should be initialised by calling readFile(). 
 *
 * @author Martin Ivanov
 */

public class CVRPData {
	private final String DIMENSION_PREFIX = "DIMENSION : ";
    private final String CAPACITY_PREFIX = "CAPACITY : ";
    public static final int DEPOT_LOCATION = 0;
    private static final int X_COORDINATE = 0; // x-axis coordinate is dimension 0 in coords[][]
    private static final int Y_COORDINATE = 1; // y-axis coordinate is dimension 1 in coords[][]
    private static final int NUM_COORDINATE_DIMENSIONS = 2;

    private int [][] coords;
    private int[] demand;
    
    public int VEHICLE_CAPACITY;
    public int NUM_NODES;

    /** This is only used for testing. Normally the class is initialised by calling readFile() */
    
	public CVRPData(){
    	readFile("fruitybun250.vrp");
    }
    
    /** Read the .vrp data file into arrays. This method does minimal format checking on the file */
    public void readFile(String file) {	
        try {
	        BufferedReader reader = new BufferedReader(new FileReader(file));
		    // read first line, which tells us the number of nodes in data file
	        String input = reader.readLine(); 
		    
		    if (!input.startsWith(DIMENSION_PREFIX))
		    	quit("Error: first line must begin with \"" + DIMENSION_PREFIX + "\"");
		    // extract the text which follows the prefix
		    input = input.substring(DIMENSION_PREFIX.length()); 
		    NUM_NODES = Integer.valueOf(input);
	
		    // create arrays to hold the data we will read from the file
		    // +1 because we don't use index 0
		    coords = new int[NUM_NODES][NUM_COORDINATE_DIMENSIONS];
		    demand = new int[NUM_NODES];
		    
		    // read second line, which tells us the capacity each truck has
		    input = reader.readLine(); 
		    if (!input.startsWith(CAPACITY_PREFIX))
			quit("Error: second line must begin with \"" + CAPACITY_PREFIX + "\"");
		    input = input.substring(CAPACITY_PREFIX.length()); 
		    VEHICLE_CAPACITY = Integer.valueOf(input);
		    //System.out.println(VEHICLE_CAPACITY);
		    
		    // read line saying "NODE_COORD_SECTION
		    input = reader.readLine(); 
	
		    // read each node coordinate and add it to coords[][]
		    String[] words;
		    for (int node=0; node< NUM_NODES; node++) {
				input = reader.readLine(); 	 
				words = input.split(" "); // format: node number, x-coordinate, y-coordinate
				coords[node][X_COORDINATE] = Integer.valueOf(words[1]);
				coords[node][Y_COORDINATE] = Integer.valueOf(words[2]);
				//System.out.println(" " + coords[node][X_COORDINATE] + "       " + coords[node][Y_COORDINATE]);
		    }
	
		    // read line saying "DEMAND_SECTION"
		    input = reader.readLine();
		    if (!input.equals("DEMAND_SECTION"))
			quit("I expected to read DEMAND_SECTION but I found: " + input);
	
		    // read the demand for each node
		    for (int node=0; node< NUM_NODES; node++) {
				input = reader.readLine(); 	 
				words = input.split(" "); // format: node number, demand
				demand[node] = Integer.valueOf(words[1]);
				//System.out.println(demand[node]);
		    }
		    reader.close();                         	
		}
        catch(FileNotFoundException e) {
            quit("Error: unable to open file " + file);
        }
        catch(IOException e) {
            quit("Error: an I/O problem occurred attempting to read " + file);
        }   

    }

    /** Return the demand for a given node. */
    public int getDemand(int node) {
		if (!nodeIsValid(node)) {
		    System.err.println("Error: demand for node " + node + 
				       " was requested from getDemand() but only nodes 1.." + NUM_NODES + " exist");
		    System.exit(-1);
		}	    
		return demand[node];
    }
        
    /** Return the Euclidean distance between the two given nodes */
    public double getDistance(int node1, int node2) {
		if (!nodeIsValid(node1)) {
		    System.err.println("Error: distance for node " + node1 + 
				       " was requested from getDistance() but only nodes 1.." + NUM_NODES + " exist");
		    System.exit(-1);
		}	    
		
		if (!nodeIsValid(node2)) {
		    System.err.println("Error: distance for node " + node2 + 
				       " was requested from getDistance() but only nodes 1.." + NUM_NODES + " exist");
		    System.exit(-1);
		}	    
	
		int x1 = coords[node1][X_COORDINATE];
		int y1 = coords[node1][Y_COORDINATE];
		int x2 = coords[node2][X_COORDINATE];
		int y2 = coords[node2][Y_COORDINATE];
	
		// compute Euclidean distance
		return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }    

    //* Return the absolute co-ordinates of the given node */
    public Point getLocation(int node) {
	    if (!nodeIsValid(node)) {
		System.err.println("Error: Request for location of non-existent node " + node + ".");
		System.exit(-1);
      }
      return new Point(coords[node][X_COORDINATE], coords[node][Y_COORDINATE]);
    }

    /** Return true if the given node is within the valid range (1..NUM_NODES), false otherwise */
    private  boolean nodeIsValid(int node) {
		if (node < 0 || node >= NUM_NODES)
		    return false;
		else
		    return true;
    }

    /** Print an error message and shut down. */
    private void quit(String message) {
		System.err.println(message);
		System.exit(-1);
    }
}

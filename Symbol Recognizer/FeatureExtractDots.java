import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Trims the number of points of a vector of sketches stored in a file,
 * and saves it into another file 
 * @author Daniel Sanchez
 *
 */
public class FeatureExtractDots {
	
	public static Vector<Point> points;
	public static Vector<Integer> NN1s;
	public static Vector<Integer> NN2s;
	public static Vector<Double> dist;
	
	
	/**
	 * Trims a sketch
	 * @param s Sketch to be trimmed
	 */
	public static void trimSketch(Sketch s, int N) {
		// Arguments checks
		if (s==null) return;
		if (N < 3) return;
		
		points = new Vector<Point>();
		NN1s = new Vector<Integer>();
		NN2s = new Vector<Integer>();
		dist = new Vector<Double>();
		
		//Extract the points of the sketch
		for (int i=0; i < s.events.size(); i++) {
			if (s.events.elementAt(i) instanceof Point) {
				points.add((Point) s.events.elementAt(i));
			} else continue;
		}
		
		// Algorithm can trim with at least 4 points
		if(points.size() < 4) return;
		
		//Remove equal points
		for (int i=0; i < points.size()-1; i++) {
			for (int j=i+1; j < points.size(); j++) {
				if (points.elementAt(i).equals(points.elementAt(j))) {
					points.removeElementAt(j);
					j--;
				}
			}
		}
		
		//Calculate the 2 nearest neighbors of all the points and fill the vectors
		for (int i=0; i < points.size(); i++) {
			//Fill vectors with garbage
			NN1s.add(null);
			NN2s.add(null);
			dist.add(null);
			calculateNNs(i);
		}
		
		//Iteratively remove points with the least value of dist
		int target;
		int NN1,NN2;
		while (points.size() > N) {
			target=indexOfLeastSignificant();
			points.setElementAt(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), target);
			//Get nearest neighbors
			NN1=NN1s.elementAt(target).intValue();
			NN2=NN2s.elementAt(target).intValue();
			//Update their values
			calculateNNs(NN1);
			calculateNNs(NN2);
			
			//Remove the target
			points.removeElementAt(target);
			NN1s.removeElementAt(target);
			NN2s.removeElementAt(target);
			dist.removeElementAt(target);
		}

		return;
	}
	
	private static void calculateNNs(int n) {
		if ((n < 0)||(n >= points.size())) return;
		
		int NN1, NN2;
		NN1=NN2=0;
		double dNN1, dNN2;
		dNN1=dNN2=Double.MAX_VALUE;
		
		Point p=points.elementAt(n);
		double distance;
		for (int i=0; i<points.size(); i++) {
			distance=p.distance(points.elementAt(i));
			if (distance < dNN1) {
				NN1=i;
				dNN1=distance;
			} else if (distance < dNN2) {
				NN2=i;
				dNN2=distance;
			}
		}
		
		NN1s.setElementAt(new Integer(NN1), n);
		NN2s.setElementAt(new Integer(NN2), n);
		dist.setElementAt(new Double(dNN1+dNN2), n);
	}
	
	private static int indexOfLeastSignificant() {
		int index=0;
		double d=Double.MAX_VALUE;
		
		for (int i=0; i< points.size(); i++) {
			if (dist.elementAt(i).doubleValue() < d) {
				d=dist.elementAt(i).doubleValue();
				index=i;
			}
		}
		return index;
	}
		
	
	/**
	 * Trims the sketches of a file
	 * @param args source file, destination file
	 */
	public static void main(String[] args) {
		//Check correct arguments
		if (args.length != 3) {
			System.out.println("Usage: java ExtractFeatures source_file dest_file max_points");
			System.exit(1);
		}
		
		//Filenames, max number of points
		String source = args[0];
		String destination = args[1];
		int N = Integer.parseInt(args[2]);
		
		//Object that will hold the initial points
		Vector<Sketch> dataset;
		
		//Load the dataset
		try {
			FileInputStream fis = new FileInputStream(source);
			ObjectInputStream in = new ObjectInputStream(fis);
			dataset = (Vector<Sketch>) in.readObject();
			in.close();
		} catch (Exception ex) {
			System.out.println("An exception occurred when reading the" +
					"source file. Exiting...");
			ex.printStackTrace();
			System.exit(1);
			return; // needed to compile, otherwise need to initialize dataset before
		}
		
		//Perform the feature extraction sketch by sketch
		Vector <DotsDatapoint> datapoints = new Vector<DotsDatapoint>();
		for (int i=0; i< dataset.size(); i++) {
			trimSketch(dataset.elementAt(i),N);
			DotsDatapoint s = new DotsDatapoint();	
			s.points.addAll(points);
			s.setLabel(dataset.elementAt(i).getLabel());
			datapoints.add(s);
		}
		
		//Save the trimmed dataset
		try {
			FileOutputStream fos = new FileOutputStream(destination);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(datapoints);
			out.close();
		} catch (IOException ex) {
			System.out.println("An exception occurred when reading the" +
					"destination file. Exiting...");
			ex.printStackTrace();
			System.exit(1);
		}
		System.out.println("Terminated successfully");
	}
}
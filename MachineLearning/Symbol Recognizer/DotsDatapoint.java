import java.util.*;
import java.io.*;

/**
 * Represents a Datapoint for the dots classifier
 * @author Daniel Sanchez
 */
public class DotsDatapoint implements Serializable {
	/**
	 * Necessary to avoid rendering unusable previous saved versions of the object
	 */
	public static final long serialVersionUID=0x1234FEDCL;
	
	/**
	 * Points of the datapoint
	 */
	public Vector<Point> points;
	
	/**
	 * Label of the datapoint
	 */
	private char label;
	
	/**
	 * Creates an empty datapoint
	 */
	DotsDatapoint() {
		points = new Vector<Point>();
	}
	
	/**
	 * Creates a datapoint with the points of the supplied sketch
	 * @param s Supplide sketch
	 */
	DotsDatapoint(Sketch s) {
		this();
		
		//Extract the points of the sketch and add them to the points vector
		for (int i=0; i < s.events.size(); i++) {
			if (s.events.elementAt(i) instanceof Point) {
				points.add((Point) s.events.elementAt(i));
			} else continue;
		}
		//Copy the label
		label = s.getLabel();
	}	
	
	/**
	 * Creates a datapoint by trimming the points of the suppied sketch to a maximum of N
	 * @param s Suppied Sketch
	 * @param N number of points
	 */
	DotsDatapoint(Sketch s, int N) {
		this(s);
		trim(N);
	}
	
	/**
	 * Returns the label of the datapoint
	 * @return Label of the datapoint
	 */
	public char getLabel() {
		return label;
	}
	
	/**
	 * Sets the label of the datapoint
	 * @param l
	 */
	public void setLabel(char l) {
		label=l;
	}
	
	/**
	 * Adds a point to the datapoint
	 * @param p point to be added
	 */
	public void addPoint(Point p) {
		points.add(p);
	}
	
	
	/**
	 * Trims the points in the datapoint, reducing its number to N
	 * @param N maximum number of points
	 */
	public void trim(int N) {
		// Argument check
		if ((N < 3)||(N >= points.size())) return;
		
		//Vectors of nearest neighbors and distances
		Vector<Integer> NN1s = new Vector<Integer>();
		Vector<Integer> NN2s = new Vector<Integer>();
		Vector<Double>dist = new Vector<Double>();

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
		NN1s.setSize(points.size());
		NN2s.setSize(points.size());
		dist.setSize(points.size());
		for (int i=0; i < points.size(); i++) {
			calculateNNs(i, NN1s, NN2s, dist);
		}
		
		//Iteratively remove points with the least value of dist
		int target;
		int NN1,NN2;
		while (points.size() > N) {
			//Find the target (next point to remove)
			//Criterium: Point with smallest dist value
			target=0;
			int index=0;
			double d=Double.MAX_VALUE;
			for (int i=0; i< points.size(); i++) {
				if (dist.elementAt(i).doubleValue() < d) {
					d=dist.elementAt(i).doubleValue();
					target=i;
				}
			}
			
			//Set the target really far apart to avoid interfering the distance measure
			points.setElementAt(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), target);
			
			//Get nearest neighbors
			NN1=NN1s.elementAt(target).intValue();
			NN2=NN2s.elementAt(target).intValue();
			
			//Update their values
			calculateNNs(NN1, NN1s, NN2s, dist);
			calculateNNs(NN2, NN1s, NN2s, dist);
			
			//Remove the target
			points.removeElementAt(target);
			NN1s.removeElementAt(target);
			NN2s.removeElementAt(target);
			dist.removeElementAt(target);
			//Note: Removing the target is not possible until this moment, since
			//the indexes of its nearest neighbors depend on it
		}

		return;
	}
	
	/**
	 * Calculates the nearest neighbors of a point. This method is used by trim.
	 * @param n index of the point in the points vector
	 * @param NN1s Vector of nearest neighbors
	 * @param NN2s Vector of second neares neighbors
	 * @param dist Vector of distances
	 * @see trim(int N)
	 */
	private void calculateNNs(int n , Vector<Integer> NN1s,
			Vector<Integer> NN2s, Vector<Double> dist) {
		
		//Argument check
		if ((n < 0)||(n >= points.size())) return;
		
		//Nearest neighbors and distances
		int NN1, NN2;
		NN1=NN2=0;
		double dNN1, dNN2;
		dNN1=dNN2=Double.MAX_VALUE;
		
		//Calculate nearest neighbors and distances
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
		
		//Update vectors
		NN1s.setElementAt(new Integer(NN1), n);
		NN2s.setElementAt(new Integer(NN2), n);
		dist.setElementAt(new Double(dNN1+dNN2), n);
	}
	
	
	
	/**
	 * Computes the distance between two datapoints. This method is crucial
	 * in the classification procedure, as it defines the distance measure.
	 * @param dp dtapoint to measute the distance with
	 * @return distance
	 */
	public double distance(DotsDatapoint dp) {
		double res=0;
		
		DotsDatapoint dp1, dp2, scratch;
		
		//Set dp1 as the datapoint with more points
		if(dp.points.size() > points.size()) {
			dp1=dp;
			dp2=this;
		} else {
			dp1=this;
			dp2=dp;
		}
		
		//Indicates whether a point of dp2 has been linked to a point of dp1 in the first pass
		boolean[] selpoints=new boolean[dp2.points.size()];
		for (int i = 0; i< selpoints.length; i++) selpoints[i]=false;
		
		//First pass: Link every point of dp1 with its nearest neighbor in dp2
		int minj=0;
		for(int i=0; i < dp1.points.size(); i++) {
			Point p=dp1.points.elementAt(i);
			double minDist=Double.MAX_VALUE;
			for(int j=0; j < dp2.points.size(); j++) {
				double d=p.distance(dp2.points.elementAt(j));
				if(d < minDist) {minDist=d; minj=j;}
			}
			res+=minDist;
			selpoints[minj]=true;
		}
		
		//Second pass: Link the still unlinked points in dp2 to its NN in dp1
		for(int i=0; i < dp2.points.size(); i++) {
			if (selpoints[i]) continue;
			Point p=dp2.points.elementAt(i);
			double minDist=Double.MAX_VALUE;
			for(int j=0; j < dp1.points.size(); j++) {
				double d=p.distance(dp1.points.elementAt(j));
				if(d < minDist) minDist=d;
			}
			res+=minDist;
		}
		
		
		//Second shot: Eliminate points from the biggest neighbor
		// Can be still improved!!
		/*scratch=new DatapointDotsClassifier();
		scratch.points.addAll(dp2.points);
		
		for(int i=0; i < dp1.points.size(); i++) {
			Point p=dp1.points.elementAt(i);
			double minDist=Double.MAX_VALUE;
			int minj=0;
			for(int j=0; j < scratch.points.size(); j++) {
				double d=p.distance(scratch.points.elementAt(j));
				if(d < minDist) {minDist=d; minj=j;}
			}
			res+=minDist;
			scratch.points.remove(minj);
			
			if (scratch.points.size() == 0) {
				scratch.points.addAll(dp1.points);
			}
		}*/	
		
		return res;
	}
}

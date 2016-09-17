import java.util.*;
import java.math.*;


public class Stroke {


	public Point endpoint1;
	public Point endpoint2;
	
	//  [0..1]  0: closed loop 1: straight line	
	public double closure;
	
	// Slope will be stored [-90..90] 
	public double slope;

	// number of found crosses	
	public int crosses;	

	// Two varibles with the maximum distance on both sides
	public double curveMin;
	public double curveMax;

    //traversed is the curving distance travelled while making the stroke
    private double traversed;

    //seperation is the distance between the endpoints
    private double separation;

        /**
            Stroke constructor  .. ....
        
        
         **/
        public Stroke(Vector strokeVector) {
           // Calculate all the class members from the already divided strokeVector
	    FindTraversed(strokeVector);
	    FindEndpoints(strokeVector);
	    FindSeperation();
	    FindSlope();
	    FindCurviness(strokeVector);
	    FindClosure();
	    FindCrosses(strokeVector);
	    //System.out.println(this.toString());
           }

    //This function simply calculates traversed.
    private double FindTraversed(Vector strokeVector) {
	double tempDist;
	traversed = 0;
	if (strokeVector.size() > 1) {
	    for (int i = 1; i < strokeVector.size(); i++) {
		Point p1 = (Point) strokeVector.get(i);
		Point p2 = (Point) strokeVector.get(i-1);
		tempDist = Math.sqrt( Math.pow((p1.x - p2.x), 2) +
			     Math.pow((p1.y - p2.y), 2));
		traversed += tempDist;
	    }
	}
	return traversed;
    }

    //This function simply finds the endpoints.
    private void FindEndpoints(Vector strokeVector) {
	int i = strokeVector.size();
	Point p1 = (Point) strokeVector.get(0);
	Point p2 = (Point) strokeVector.get(strokeVector.size() - 1);
	endpoint1 = new Point(p1.x, p1.y);
	endpoint2 = new Point(p2.x, p2.y);
    }

    //This function simply calculates seperation.
    //FindEndpoints should have been called before this function.
    private double FindSeperation() {
	separation = endpoint1.distance(endpoint2);
	return separation;
    }


    //This function simply calculates slope.
    //FindEndpoints should have been called before this function.
    private double FindSlope() {
	if (endpoint1.x == endpoint2.x) {
	    slope = 90.0;
	}
	else {
	    slope = 180.0 / 3.14159 * Math.atan( (double) (endpoint1.y - endpoint2.y) /
					       (double) (endpoint1.x - endpoint2.x));
	}
	return slope;
    }

    //This function simply calculates closure.
    //FindSeparation and FindTraversed should have been called before this function.
    private double FindClosure() {
	closure = separation / traversed;
	return closure;
    }

    //This function simply calculates curvimax and curvimin
    //FindEndpoints and FindSlope should have been called before this function.
    private void FindCurviness (Vector strokeVector) {
	double x = endpoint1.x;
	double y = endpoint1.y;
	curveMax = curveMin = 0;
	double offBy;
	for (int i = 0; i < strokeVector.size(); i++) {
	    Point p = (Point) strokeVector.get(i);
	    offBy = (p.y - (y + Math.tan(slope * 3.14159 / 180) * (p.x - x))) * 
		Math.cos(slope * 3.14159 / 180);
	    curveMax = Math.max(curveMax, offBy);
	    curveMin = Math.min(curveMin, offBy);
	}
	curveMax = curveMax/separation;
	curveMin = curveMin/separation;
    }

    //This function calculates the number of crosses.
    private int FindCrosses (Vector strokeVector) {
	//********** unimplemented **********
	crosses = 0;
	return crosses;
    }

    public String toString() {
	return ("endpoints: " + endpoint1.toString() + ", " + endpoint2.toString() + "\n" +
	    "slope: " + slope + "\n" +
	    "closure: " + closure + "\n" +
	    "crosses: " + crosses + "\n" +
	    "curviness: " + curveMin + ", " + curveMax + "\n");
    }
	    
		
    /**
       this function is used for finding the distance between the endpoints of two different
       Stroke objects 
       @param Stroke s used to compare to


    */ 
		public double pointDistance(Stroke s) {
			double distance1 = endpoint1.distance(s.endpoint1) + endpoint2.distance(s.endpoint2);
			double distance2 = endpoint1.distance(s.endpoint2) + endpoint1.distance(s.endpoint1);
			return Math.min(distance1, distance2);
		}
		
		//This function takes in another stroke to be compared to the one
		//here
		public double CompareStroke(Stroke s) {
			double overallMatch;
			double closeMax, closeMin;
			double crossMatch, nonCrossMatch;
			double slopeMatch, curveMatch;
			double curveMaxMatch, curveMinMatch;
			boolean switchSlope;
			double myCMax, myCMin, yourCMax, yourCMin;
			closeMax = Math.max(closure, s.closure);
			closeMin = Math.min(closure, s.closure);
			crossMatch = Math.pow(.5, Math.abs(crosses - s.crosses));
			switchSlope = Math.abs(slope - s.slope) > 90;
			if (switchSlope) {
				slopeMatch = 1 - (((double) (Math.abs(slope - s.slope)) - 90.0) / 90.0);
				myCMax = curveMax + 1;
				myCMin = curveMin - 1;
				yourCMax = 1 - s.curveMin;
				yourCMin = - 1 - s.curveMax;
				curveMaxMatch = Math.min(myCMax, yourCMax) / Math.max(myCMax, yourCMax);
				curveMinMatch = Math.min(-myCMin, -yourCMin) / Math.max(-myCMin, -yourCMin);
			}
			else {
				slopeMatch = 1 - (((double) (Math.abs(slope - s.slope))) / 90.0);
				myCMax = curveMax + 1;
				myCMin = curveMin - 1;
				yourCMax = s.curveMax + 1;
				yourCMin = s.curveMin - 1;
				curveMaxMatch = Math.min(myCMax, yourCMax) / Math.max(myCMax, yourCMax);
				curveMinMatch = Math.min(-myCMin, -yourCMin) / Math.max(-myCMin, -yourCMin);
			}
			curveMatch = (curveMaxMatch + curveMinMatch) / 2.0;
			nonCrossMatch = Math.sqrt(curveMatch * slopeMatch);
			overallMatch = closeMin * nonCrossMatch + (1 - closeMax) * crossMatch;
			return overallMatch;
		}	
	

}

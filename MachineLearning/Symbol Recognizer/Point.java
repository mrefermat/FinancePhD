import java.io.Serializable;

/**
 * Simple class that represents a point.
 * @author Daniel Sanchez
 *
 */
public class Point implements Serializable{
	/**
	 * Fixed serial so that the class can be expanded
	 * and still use the same saved classes
	 */ 
	public static final long serialVersionUID=0xAABBCCDDEEFFL;
	
	public int x;
	public int y;
	
	Point(int x, int y) {
		this.x=x;
		this.y=y;
	}
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	public boolean equals(Point p) {
		return (p!=null)&&(p.x==x)&&(p.y==y);
	}
	
	public double distance(Point p){
		return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
	}
}
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

/**
 * Represents a sketch
 * @author Daniel Sanchez
 *
 */
public class Sketch implements Serializable {
	/**
	 * Fixed serial UID so that the class can be expanded
	 * and still use the same saved classes
	 */ 
	public static final long serialVersionUID=0xABCDEFFEDCBAL;
	
	//Constants for the press/release events
	public static final int MOUSE_PRESS=0;
	public static final int MOUSE_RELEASE=1;
	
	/**
	 * Vector with the possible events. Can contain:
	 * - An Integer object when indication press/release events
	 * - A Point object in the other cases
	 */
	public Vector events;
	
	/**
	 * Contains one timestamp for every event 
	 */
	public Vector<Integer> timestamps;
	
	/**
	 * Label associated to the sketch
	 */
	private char label;
	
	// Related to timestamps
	private long startTime;
	
	/**
	 * Constructor
	 */
	Sketch() {
		super();
		events=new Vector();
		timestamps=new Vector<Integer>();
		Date d=new Date();
		startTime=d.getTime();
	}
	
	/**
	 * Adds a point to the sketch
	 * @param p point to be added
	 */
	public void addPoint (Point p) {
		events.add(p);
		Date d= new Date();
		timestamps.add((new Long((d.getTime()-startTime))).intValue());
	}
	
	/**
	 * Adds a mouse press/release event to the sketch
	 * @param me integer representation of the event
	 */
	public void addMouseEvent(int me){
		events.add(new Integer(me));
		Date d= new Date();
		timestamps.add((new Long((d.getTime()-startTime))).intValue());
	}
	
	/**
	 * Returns the label associated with the sketch
	 * @return the label
	 */
	public char getLabel() {
		return label;
	}
	
	/**
	 * Sets the label associated with the sketch
	 * @param l label
	 */
	public void setLabel(char l){
		label=l;
	}
	
	/**
	 * Returns a string that represents the sketch
	 */
	public String toString() {
		String res = new String();
		for (int i=0; i<events.size(); i++) {
			res+= events.elementAt(i) + " @ " + timestamps.elementAt(i) + "\n";
		}
		return res;
	}
	
	/**
	 * Normalizes the sketch, centering and proportionally scaling it
	 */
	public void normalize() {
		int xmin, xmax, ymin, ymax;
		xmin=ymin=255;
		xmax=ymax=0;
		
		Point p;
		//Extract the minimum and maximum values of the coordinates
		for (int i=0; i < events.size(); i++) {
			if (events.elementAt(i) instanceof Point) {
				p= (Point) events.elementAt(i);
			} else continue;
			
			if (p.x < xmin) xmin = p.x;
			if (p.x > xmax) xmax = p.x;
			if (p.y < ymin) ymin = p.y;
			if (p.y > ymax) ymax = p.y;
		}
		
		//Maximum of the height and width of the symbol
		int size;
		
		//Translation amounts to center the sketch
		int xdispl,ydispl;
		
		if((xmax-xmin) > (ymax-ymin)) {
			size = xmax-xmin;
			xdispl=xmin;
			ydispl=(ymin+ymax-size)/2;
		} else {
			size = ymax-ymin;
			xdispl=(xmin+xmax-size)/2;
			ydispl=ymin;
		}
		
		//Check for extraneous results (could happen if empty sketch or one point)
		if (size <= 0) return;
		
		//Scaling factor applied
		double scale=255.0/size;
		
		//Scale and center
		for (int i=0; i < events.size(); i++) {
			if (events.elementAt(i) instanceof Point) {
				p= (Point) events.elementAt(i);
			} else continue;
			
			p.x=(int)Math.round(scale*(p.x-xdispl));
			p.y=(int)Math.round(scale*(p.y-ydispl));
		}
	}
	
}
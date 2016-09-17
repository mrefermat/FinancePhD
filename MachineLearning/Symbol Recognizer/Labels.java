import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Reads and provides access to the contents of a "labels" file.
 * Rules:
 *  - A label must have at least one name
 *  - A label can have more than one name
 *  - Each name must have an associated page
 * @author Daniel Sanchez
 *
 */
public class Labels {
	
	/**
	 * Contains the UTF-16 character code associated to each label.
	 */
	public Vector<Character> codes;
	
	/**
	 * Contains the names of the labels.
	 */
	public Vector <String[]> names;
	
	/**
	 * Contains the pages associated to each name of each object.
	 */
	public Vector <String[]> pages;	
	
	/**
	 * Reads a labels file and initializes the vectors
	 */
	Labels(String filename) {
		codes = new Vector <Character>();
		names = new Vector <String[]>();
		pages = new Vector <String[]>();
		
		try {
			RandomAccessFile input = new RandomAccessFile(filename, "r");
		    while(true) {
		    	//Read a label
		    	String[] parts = input.readLine().trim().split("@");
		    	if (parts[0].equals("#")) break;
		    	//Skip invalid symbol declarations
		    	if (parts.length < 2) continue; 
		  		
		    	//Add the character value to the list
		    	char ch = (char)Integer.parseInt(parts[0],16);
		    	codes.add(new Character(ch));
		    	
		    	//Fill out the names/pages for this character
		    	int numNames=parts.length/2;
		    	String [] nameArray = new String[numNames];
		    	String [] pageArray = new String[numNames];
		    	
		    	String [] temp = nameArray;
		    	int n=0;
		    	for (int i = 1; i < parts.length; i++) {
		    		temp[n]=parts[i];
		    		//System.out.println(temp[n] + numNames);
		    		if (temp == nameArray) {
		    			temp = pageArray;
		    		} else {
		    			temp = nameArray;
		    			n++;
		    		}
		    	}
		    	
		    	names.add(nameArray);
		    	pages.add(pageArray);
		    }
		    input.close();
		}
		catch(IOException e) {
			System.out.println("I/O Error:" + e.toString());
		}
	}
	
	/**
	 * Reads the "labels.txt" file and initializes the vectors
	 */	
	Labels() {
		this("labels.txt");
	}
	
	public int size() {
		return codes.size();
	}
	
	
}

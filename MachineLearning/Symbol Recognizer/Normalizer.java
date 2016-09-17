import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
/**
 * Normalizes a vector of sketches stored in a file,
 * and saves it into another file 
 * @author Daniel Sanchez
 *
 */
public class Normalizer {
	/**
	 * Normalizes the sketches of a file
	 * @param args source file, destination file
	 */
	public static void main(String[] args) {
		//Check correct arguments
		if (args.length != 2) {
			System.out.println("Usage: java Normalizer source_file dest_file");
			System.exit(1);
		}
		
		//Filenames
		String source = args[0];
		String destination = args[1];
		
		//Object that will hold the data
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
		
		//Perform the normalization sketch by sketch
		for (int i=0; i< dataset.size(); i++) {
			dataset.elementAt(i).normalize();
		}
		
		//Save the normalized dataset
		try {
			FileOutputStream fos = new FileOutputStream(destination);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(dataset);
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
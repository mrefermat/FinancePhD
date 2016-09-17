/**
 * Object that a classifier returns when classifying a sketch. It contains:
 *  - An array of possible labels, ordered from most to least likely
 *  - An array with the confidence of each label. The confidences are double
 *    values between 0 and 1. 1 indicates that the object will match the 
 * @author CS540 group 10
 *
 */
public class ClassifierResults {
	/**
	 * Array with the UTF-16 encoding of the results
	 */
	public char labels[];
	
	/**
	 * Array with the confidences of each given result
	 */
	public double confidence[];

	/**
	 * Constructor
	 * @param N number of results
	 */
	ClassifierResults(int N) {
		labels= new char[N];
		confidence= new double[N];
	}
	
	/**
	 * Orders the results from highest to lowest confidence
	 */
	public void orderResults() {
		double tempConf;
		char tempLabel;
		
		for (int i=0; i < labels.length-1; i++) {
			//Find max
			double maxconf=-1;
			int maxj=0;
			for (int j=i+1 ; j < labels.length; j++) {
				if (confidence[j] > maxconf) {
					maxconf=confidence[j];
					maxj=j;
				}
			}
			//Swap!
			if (confidence[i] < maxconf) {
				tempConf=confidence[i];
				tempLabel=labels[i];
			
				confidence[i]=confidence[maxj];
				labels[i]=labels[maxj];
			
				confidence[maxj]=tempConf;
				labels[maxj]=tempLabel;
			}
		}	
		
		return;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i < labels.length; i++) {
			s+= Integer.toHexString((int)labels[i]) + " with confidence " + confidence[i] + '\n';
		}
		return s;
	}
}

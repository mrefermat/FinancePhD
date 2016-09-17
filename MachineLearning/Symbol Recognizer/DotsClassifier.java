import java.util.*;
import java.io.*;


public class DotsClassifier implements Classifier {
	
	private Vector<DotsDatapoint> datapoints;
	private final int NumPoints=30;
	
	private Labels labels;
	
	DotsClassifier() {
		datapoints = new Vector<DotsDatapoint>();
		labels= new Labels();
	}
	
	/**
	 * Adds the contents of a file into the classifier.
	 * Can be used multiple times to add the datapoints of different files.
	 * The classifier can accept files that contain either a Vector of previously
	 * processed datapoints or a Vector of raw sketches. In the latter case, the method will process (normalize and trim) them prior to 
	 * @param trainingSetFile file that contains the datapoints
	 */
	public void addTrainingSet(String trainingSetFile) {
		try {
			FileInputStream fis = new FileInputStream(trainingSetFile);
			ObjectInputStream in = new ObjectInputStream(fis);
			Vector v = (Vector) in.readObject();
			
			Vector<DotsDatapoint> newdata;
			//v can contain either raw sketches or previously processed datapoints
			if (v.elementAt(0) instanceof DotsDatapoint) {
				newdata = v;
			} else if (v.elementAt(0) instanceof Sketch) {
				newdata = new Vector<DotsDatapoint>();
				for (int i=0; i < v.size(); i++) {
					Sketch s = (Sketch) v.get(i);
					s.normalize();
					newdata.add(new DotsDatapoint(s, NumPoints));
				}
			} else {
				throw new Exception("Invalid file format");
			}

			in.close();
			datapoints.addAll(newdata);
		} catch (Exception ex) {
			System.out.println("An exception occurred when reading the" +
					"training set file.");
			ex.printStackTrace();
		}
	}

	/**
	 * Classifies a sketch
	 * @param s Sketch to classify
	 * @param k number of results that the classifier must return
	 * @return a ClassifierResults object with the N possible labels and
	 * 		   the confidences of each label
	 * @see ClassifierResults
	 * @see Sketch
	 */
	public ClassifierResults classify(Sketch s, int N) {
		//Argument check
		if (N <= 0) return null;
		
		//Results
		ClassifierResults res= new ClassifierResults(N);
		
		//Creates the datapoint from the sketch
		s.normalize();
		DotsDatapoint dp = new DotsDatapoint(s, NumPoints);
		
		
		double [] distances = new double[labels.size()];
		
		for (int i=0; i < labels.size(); i++) {
			//distances[i]=0; //If average is used
			distances[i]=Double.MAX_VALUE; //If minimum is used
		}
		
		//Calculate distance, pick minimum
		for (int i=0; i < datapoints.size(); i++) {
			double dist=dp.distance(datapoints.elementAt(i));
			int k = labels.codes.indexOf(new Character(datapoints.elementAt(i).getLabel()));
			
			if (distances[k] > dist) { //If minimum is used
				distances[k]=dist;
			}
			//distances[k]+=dist; //If average is used
		}
		
		double[] dists= new double[N];
		for (int i=0; i < N; i++) {
			dists[i]=Double.MAX_VALUE;
		}
		
		//Pick N nearest points (NOTE: THIS IS 1NN with multiple outputs!!)
		for (int i=0; i < labels.size(); i++) {
			int maxj=0;
			double maxdist=0;
			//Locate maximum
			for (int j=0; j < N; j++) {
				if(dists[j] > maxdist) {
					maxdist=dists[j];
					maxj=j;
				}
			}
			
			//Substitute it!!
			if(distances[i] < maxdist) {
				dists[maxj]=distances[i];
				res.labels[maxj]=labels.codes.elementAt(i).charValue();
			}
			
		}
		
		//Confidence metric
		double avgDist=0;
		double maxDist=Double.MIN_VALUE;
		for (int i=0; i < N; i++) {
			avgDist+=dists[i];
			maxDist=Math.max(dists[i], maxDist);
		}
		avgDist/=N;
		
		for (int i=0; i < N; i++) {
			res.confidence[i]=1-dists[i]/maxDist;
		}
		
		res.orderResults();
			
		return res;
	}
	
	
	public static void main(String args[]) {
		
		//SPAGHETTI CODE, THIS SHOULD BE IMPLEMENTED IN THE EVALUATOR CLASS
		
		DotsClassifier classifier = new DotsClassifier();
		//classifier.addTrainingSet("Daniel1.trs");
		//classifier.addTrainingSet("phil.trs");
		classifier.addTrainingSet("MikeSet1.trs");
		//classifier.addTrainingSet("MarkNormalized_completed.trs");
		int n=4;
		
		Vector<Sketch> testSet;
		
		try {
			//Read the dataset
			FileInputStream fis = new FileInputStream("Daniel0.trs");
			ObjectInputStream in = new ObjectInputStream(fis);
			testSet = (Vector<Sketch>) in.readObject();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}		
		
		Labels slabels = new Labels();
		int ok=0;
		int inresults=0;
		int total=0;
		for (int i=0; i < testSet.size(); i++) {
			int index = slabels.codes.indexOf(new Character(testSet.elementAt(i).getLabel()));
			
			System.out.println("Classifying " +
					 (Integer.toHexString((int)testSet.elementAt(i).getLabel())) +
					 " (" + slabels.names.get(index)[0] + ")");
			ClassifierResults cr = classifier.classify(testSet.elementAt(i), n);
			System.out.println(cr);
			if (testSet.elementAt(i).getLabel()==cr.labels[0]) ok++;
			for (int j=0; j < n; j++) {
				if (testSet.elementAt(i).getLabel()==cr.labels[j]){
					inresults++;
					break;
				}
			}
			total++;
		}
		System.out.println("Accuracy: " + ok +"/" + total + "("+(((double)ok*100)/total)+"%)");
		System.out.println("In results: " + inresults +"/" + total + "("+(((double)inresults*100)/total)+"%)");
	}
		
	
}

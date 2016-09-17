/**
 * Interface for the classifiers
 * @author CS540 group 10
 */
interface Classifier {

	/**
	 * Adds the contents of a file into the classifier.
	 * Can be used multiple times to add the datapoints of different files.
	 * The format of the files depends on the specific classifier.
	 * @param trainingSetFile file that contains the datapoints
	 */
	public void addTrainingSet(String trainingSetFile);

	/**
	 * Classifies a sketch
	 * @param s Sketch to classify
	 * @param N number of results that the classifier must return
	 * @return a ClassifierResults object with the N possible labels and
	 * 		   the confidences of each label
	 * @see ClassifierResults
	 * @see Sketch
	 */
	public ClassifierResults classify(Sketch s, int N);

}

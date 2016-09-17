import java.io.*;
import java.util.*;

/**
 * Computes the accuracy of the given classifier
 * @author Phillip Hill
 *
 */

public class Evaluator 
{
	// Global Variables	
	private static String[] folds;
	private static String name;
	private static int classifierNum;
	private static int numOfPoints;
	private static int numOfFolds;
	private static Report report;
	static final String DOTS = "DotsClassifier";
	static final String VELOCITY = "VelocityClassifier";
	static final String STROKES = "StrokeClassifier";
	static double overallAccuracy = 0;
	static double overallListedAccuracy = 0;
	static final String WIKI = "http://en.wikipedia.org/wiki/";
	
	
	/**
	 * main Method
	 * @param args
	 */
	public static void main(String[] args) 
	{		
		/* Step 1: Setup the evaluator */
		Setup(args);
		
		/* Setp 2: Create the report */
		report = new Report(name);
		
		/* Step 3: Evaluate each fold */
		for(int i = 0; i < numOfFolds; i++){ evaluate(i); }
		
		/* Step 4: Print the report */
		report.print(name, overallAccuracy, overallListedAccuracy, numOfFolds);
		
		System.out.println("Evalutation and Report Completed Succesfully!");	
	}
	
	private static void Setup(String[] args)
	{
		int count = 0;
		// Check for proper number of parameters
		// 		# of folds
		// 		folds
		//		classifier
		//		# of test Points
		if(args.length < 5)
			System.out.println("Evaluator usage: 'number of folds' '...folds...' 'classifier to test' 'number of points to test'");
		
		// Check that number of folds is valid
		numOfFolds = Integer.parseInt(args[count]);
		if(numOfFolds < 2)
		{
			System.out.println("The number of folds must be greater than 1.");
			System.exit(0);
		}		
		count++;
		
		// Check if the "folds" exist
		folds = new String[numOfFolds];
		BufferedReader file;
		int track = 0;
		for(int i = count; i < (count + numOfFolds); i++)
		{
			try
			{
				file = new BufferedReader(new FileReader(args[i]));
				file.close();
			}
			catch(Exception e)
			{
				System.out.println(args[i] + " does not exist.");
				System.exit(0);
			}
			folds[track] = args[i];
			track++;
		}
		count = count + numOfFolds;
		
		// Check if a valid classifer has been selected
		if(args[count].equalsIgnoreCase(DOTS))
		{
			// Make the classifier a dots classifier
			name = DOTS;
			classifierNum = 1;
		}
		else if(args[count].equalsIgnoreCase(VELOCITY))
		{
			// Make the classifier a velocity classifier
			name = VELOCITY;
			classifierNum = 2;
		}
		else if(args[count].equalsIgnoreCase(STROKES))
		{
			// Make the classifier a strokes classifier
			name = STROKES;
			classifierNum = 3;
		}
		else
		{
			System.out.println(args[count] + " is not a valid classifier.");
			System.exit(0);
		}			
		count++;
		
		// Check that the number of points is valid
		numOfPoints = Integer.parseInt(args[count]);
		if(numOfPoints < 1)
		{
			System.out.println("The number of points must be greater than 0.");
			System.exit(0);
		}
	}
	
	private static void evaluate(int foldNum)
	{
		System.out.println("Testing fold: " + folds[foldNum]);
		// Make a new classifier
		Classifier classifier;
		if(classifierNum == 1)
			classifier = new DotsClassifier();
		else if(classifierNum == 2)
			classifier = new DotsClassifier(); // For test
		else
			classifier = new StrokeClassifier();
		
		// Add all folds to classifier except one to be evaluated
		for(int i = 0; i < numOfFolds; i++)
			if(!(i == foldNum))
				classifier.addTrainingSet(folds[i]);
		
		// Get test sketches
		Vector<Sketch> testSet = getTestSketches(folds[foldNum]);
		testSet.trimToSize();
		
		// Classify each sketch
		ClassifierResults[] results = new ClassifierResults[testSet.size()];;
		int correct = 0, wrong = 0;
		char[] trueLabels = new char[testSet.size()];
		boolean[] isCorrect = new boolean[testSet.size()];
		for(int i = 0; i < testSet.size(); i++)
		{
			// Test the sketch
			trueLabels[i] = testSet.get(i).getLabel();
			results[i] = classifier.classify(testSet.get(i), numOfPoints);
			
			// Ensure correct ordering of labels
			results[i].orderResults();			
			
			// Correct?
			if(trueLabels[i] == results[i].labels[0])
			{
				isCorrect[i] = true;
				correct++;
			}
			else
			{
				isCorrect[i] = false;
				wrong++;
			}			
		}
		
		System.out.println("Correct = " + correct);
		System.out.println("Wrong = " + wrong);
		
		// Build Stats
		String stats = "";
		String confidences = "";
		int inLabels = 0;
		double acc1, acc2;
		Labels codes = new Labels();
		acc1 = ((double)correct/(double)testSet.size()) * 100;
		stats = stats + "Overall Accuracy: " + acc1 + "%\n";
		for(int i = 0; i < testSet.size(); i++)
		{
			if(isCorrect[i] == false)
			{
				if(inResults(results[i].labels, trueLabels[i]))
					inLabels++;
				confidences = confidences + "Labels with confidences for symbol " 
					+ codes.codes.get(codes.codes.indexOf(trueLabels[i])) + ":\n" + results[i].toString() + "\n";
			}
		}
		acc2 = ((double)inLabels/(double)wrong) * 100;
		stats = stats + confidences;
		stats = stats + "% of time correct label was found for incorrectly labeled symbols: " 
			+ acc2 + "%\n";
		
		// Add to final stats
		overallAccuracy = overallAccuracy + acc1;
		overallListedAccuracy = overallListedAccuracy + acc2;
		report.addStats(folds[foldNum], stats);
		
	}
	
	@SuppressWarnings("unchecked")
	private static Vector<Sketch> getTestSketches(String filename)
	{
		Vector<Sketch> testSet = null;

		try 
		{		    
		    FileInputStream file = new FileInputStream(filename);
		    ObjectInputStream in = new ObjectInputStream(file);
		    testSet = (Vector<Sketch>) in.readObject();
		    in.close();		    
		} 
		catch (Exception e) 
		{
			System.out.println(e);
		}	
		return testSet;
	}
	
	private static boolean inResults(char[] labels, char label)
	{
		for(int i = 0; i < labels.length; i++)
		{
			if(label == labels[i])
				return true;
		}
		return false;
	}
	
	static void testFunc()
	{
		Labels labels = new Labels();
		String[] name;
		String[] page;		
		for(int i = 0; i < labels.size(); i++)
		{
			name = new String[labels.names.get(i).length];
			page = new String[labels.pages.get(i).length];
			char symbol = labels.codes.get(i);	
			for(int j = 0; j < labels.names.get(i).length; j++)
			{
				name[j] = labels.names.get(i)[j];
				page[j] = labels.pages.get(i)[j];
				System.out.println(name[j] + " - " + symbol + ": " + WIKI + page[j]);
			}					
						
		}
	}
}

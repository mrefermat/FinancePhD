import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Class Name: Report
 * Description: This class outputs a file that contains statistics on a 
 * given classifier and training sets
 */
public class Report
{
	// Report Variables
	PrintWriter out;
	String finalStats;
	final String SEPERATOR = "-------------------------------------------------------------------------------";

	
	public Report(String name)
	{
		try
		{
			out = new PrintWriter(new FileWriter(name + " Accuracy Report"));
			finalStats = "-------------------- Accuracy Report for " + name + "	--------------------\n";
			finalStats = finalStats + SEPERATOR + "\n";
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	public void addStats(String foldName, String stats)
	{
		finalStats = finalStats + "Accuracy for fold " + foldName + "\n" 
			+ stats + "\n";
		finalStats = finalStats + SEPERATOR + "\n" + "	 \n";
	}
	
	public void print(String name, double acc1, double acc2, int numFolds)
	{
		// Add overall stats
		finalStats = finalStats + "Total Overall Accuracy: " + acc1 / numFolds + "\n";
		finalStats = finalStats + "Total Listed Accuracy: " + acc2 / numFolds + "\n";
		
		// End Report
		finalStats = finalStats + "-------------------- End of Report --------------------";
		out.println(finalStats);
		out.close();
		System.out.println("Report in file " + name + " Accuracy Report");
	}	
}
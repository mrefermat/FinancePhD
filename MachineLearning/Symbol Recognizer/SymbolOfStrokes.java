import java.util.*;

public class SymbolOfStrokes {



 
   // Vector of Stroke Objects
   public Vector<Stroke> myStrokes; 
  
   public char SymbolLabel;
    
    public SymbolOfStrokes() {
		myStrokes = new Vector<Stroke>();
		SymbolLabel = 0;
  
      
   }
     
   
   
   // Possibly stroke to stroke comparison
   //   taking in either one stroke or entire SymbolOfStrokes
   
   
   // Adds a stroke to the vector of strokes
    
    public void addStroke(Stroke st)  {
       myStrokes.add(st);
    } 

    /**

    This function does the actual calcuation of confidence.  It must decide which strokes
    are nearest to each stroke.  Then compute the difference between the strokes.  Then 
    find out some or any type of confidence rating system.
    */
    public double distance(SymbolOfStrokes sym) {
       if (sym.myStrokes.size() == 0) {
          return 0;
       }	
		
		Stroke curr;
		double strokeDistance[] = new double[sym.myStrokes.size()];
		int best = 0;
		double bestDistance, temp;

		
		for(int i = 0 ; i < myStrokes.size() ; i++)  {
			curr = (Stroke) myStrokes.get(i);
			bestDistance = 999999;
                        // This loop finds the best matched Stroke
                        for (int j = 0; j < sym.myStrokes.size(); ++j) {
				temp = curr.pointDistance( (Stroke)  sym.myStrokes.get(j));
				if (temp < bestDistance) {
					bestDistance = temp;
					best = j;
	                        }
                        }
                        // resets value
			bestDistance = 99999;
                        //System.out.print("Best: " + best);
                           //System.out.print(sym.myStrokes.size() + ":");
                           Stroke km =(Stroke) sym.myStrokes.get(best);
                           double temp3 = curr.CompareStroke(km);
                           try {
                           strokeDistance[i] = temp3;
                           
                           			
                           //System.out.print(strokeDistance[i] + " ");
                           } catch (Exception e) {//System.out.println("size: " + strokeDistance.length);
                           }
			   Stroke tempe = sym.myStrokes.get(best);

       
			//System.out.println("First: " + curr.toString() + "\nNext: " + tempe.toString());

					
		}
                double total = 0;
                for (int i= 0 ; i < strokeDistance.length ; ++i) {
                   total += strokeDistance[i];
                   }
				if (sym.myStrokes.size() != myStrokes.size()) {
					int min = Math.min(sym.myStrokes.size(), myStrokes.size());
					int max = Math.max(sym.myStrokes.size(), myStrokes.size());
					total = total * (min/(double) max);
				}

                  
                   return total/(double) strokeDistance.length;  
    } 
	public String toString() {
		return "Number of Strokes:  " + myStrokes.size() + " in Label: " + SymbolLabel;
	}
      
}   




import java.util.*;
import java.io.*;






public class StrokeClassifier implements Classifier {

      
   /*
         Public constructor takes in the entire training set and stores them as SymbolOfStrokes objects  
  
   */    

    Vector<SymbolOfStrokes> trainingSet;







   public StrokeClassifier()  {
      trainingSet = new Vector<SymbolOfStrokes>();
      }

      
      
      
      /**
      This function breaks the entire sketch up into different strokes
      @param Vector<Integer> timestamps
      @param Vector events
      
      @return SymbolOfStrokes
      
       */
      
      public SymbolOfStrokes strokeBreaker( Vector<Integer> timestamps, Vector events)  {
         
         SymbolOfStrokes fullSymbol = new SymbolOfStrokes();
         
         Vector tempStroke = new Vector();
		// System.out.println("Size of Symbol: " + events.size());
         

	// // This arrary stores the distance between two points
	// double distance[] = new double[events.size()];
	 // This stores the difference in time
	// int timeDiff[] = new int[timestamps.size()];
	 // This stores the velocities
	 double velocity[] = new double[events.size()];
	 for(int i = 0; i < velocity.length; ++i) 
		 velocity[i] = 0;
	 
	 
	 

	 int lastTime= 0;	 
	 int thisTime;
	 //String className = "Point";
	 Point last = new Point(0,0);
	 int flag_new_stroke = 1;
	 for (int i = 0 ; i < events.size() ; ++i) {

	     Object tempObj = events.get(i);
	     thisTime = timestamps.get(i);
		 Point curr = null;
		 try {
	     curr = (Point) tempObj;
		 } catch (Exception e){
			 int mouseEvent = (Integer) tempObj;
			 if (mouseEvent == 1) {
				 if (tempStroke.size() < 5 ) {
					 tempStroke.clear();
					 flag_new_stroke = 1;
					 continue;
				 }
				 else {
					 Stroke temp = new Stroke(tempStroke);
					 fullSymbol.addStroke(temp);
					 tempStroke.clear();
					 flag_new_stroke = 1;
					 //System.out.print("!");
					 continue;
				 }
			 }
			 continue;
			 
				 
		 }
		 if(flag_new_stroke == 1) {
			 lastTime = thisTime;
			 last = curr;
			 flag_new_stroke = 0;
			 continue;
		 }
		 
	     double speed = Math.abs((curr.distance(last)/(lastTime - thisTime)));
		 //System.out.println("Speed: " + speed + "at point: " + curr.toString());
		 
		 velocity[i] = speed;
	     lastTime = thisTime;				
	 
	 }	 
	 // this is the maximum speed
	 double maxSpeed = velocity[findMaxSpeed(velocity)];
	 for (int i = 0 ; i < velocity.length; i++) {
		 Point curr = new Point(0,0);
		 
		 Object tempObj = events.get(i);
		 try {
			 curr = (Point) tempObj;
		 } catch (Exception e){
			 int mouseEvent = (Integer) tempObj;
			 if (mouseEvent == 1) {
				 if (tempStroke.size() < 5 && events.size() > 25) {
					 tempStroke.clear();
					 //flag_new_stroke = 1;
					 continue;
				 }
			 }
		 }
		 //////THRESHOLD
		 if (velocity[i] > (maxSpeed*.006) || events.size() < 25) {
			 
			 tempStroke.add(curr);
		 }
	     else {
			 //System.out.print (tempStroke.size());
			 if (tempStroke.size() < 5) {
					// System.out.print("size before cut: " + tempStroke.size());
					 tempStroke.clear();
				 //System.out.print("|");
			 }
			 else {
				 Stroke temp = new Stroke(tempStroke);
				 fullSymbol.addStroke(temp);
				 tempStroke.clear();
				 //System.out.print(".");
				 
			 }
	     }
	 
	 }

	 if (events.size() < 25) {
		 tempStroke.remove(0);
		 tempStroke.remove(tempStroke.size()-1);
	 }
	 //System.out.println("Size: " + tempStroke.size());
	 if (tempStroke.size() > 5 ) {
		 if (tempStroke.size() != 0) {
			 //System.out.println("Number of items in Stroke: " + tempStroke.size());
			 //System.out.println("First: " + tempStroke.get(1).toString());
			 Stroke temp = new Stroke(tempStroke);
			 fullSymbol.addStroke(temp);
		 }
	 }
	 //System.out.println("Number of strokes: " + fullSymbol.myStrokes.size());
	 if (fullSymbol.myStrokes.size() == 0) {
		 //System.out.println(" ^ I have nothing ^");
		 tempStroke.clear();
		 for (int i=0; i < events.size(); i++) {
			 Point curr = new Point(0,0);
			 Object tempObj = events.get(i);
			 try {
				 curr = (Point) tempObj;
			 } catch (Exception e){}
			 tempStroke.add(curr);
		 }
		 Stroke st = new Stroke(tempStroke);
		 fullSymbol.addStroke(st);
	 }
		 
	 return fullSymbol;
      }

   
   
   
   /**
	   This function breaks the entire sketch up into different strokes
	@param Vector<Integer> timestamps
	@param Vector events
	
	@return SymbolOfStrokes
	
	
   
   public SymbolOfStrokes strokeBreakerRelative( Vector<Integer> timestamps, Vector events)  {
	   
	   SymbolOfStrokes fullSymbol = new SymbolOfStrokes();
	   
	   Vector tempStroke = new Vector();
	   // System.out.println("Size of Symbol: " + events.size());
	   
	   
	   // // This arrary stores the distance between two points
	   // double distance[] = new double[events.size()];
	   // This stores the difference in time
	   // int timeDiff[] = new int[timestamps.size()];
	   // This stores the velocities
	   double velocity[] = new double[events.size()];
	   for(int i = 0; i < velocity.length; ++i) 
		   velocity[i] = 0;
	   
	   
	   
	   
	   int lastTime= 0;	 
	   int thisTime;
	   //String className = "Point";
	   Point last = new Point(0,0);
	   int flag_new_stroke = 1;
	   for (int i = 0 ; i < events.size() ; ++i) {
		   
		   Object tempObj = events.get(i);
		   thisTime = timestamps.get(i);
		   Point curr = null;
		   try {
			   curr = (Point) tempObj;
		   } catch (Exception e){
			   int mouseEvent = (Integer) tempObj;
			   if (mouseEvent == 1) {
				   if (tempStroke.size() < 5 ) {
					   tempStroke.clear();
					   flag_new_stroke = 1;
					   continue;
				   }
				   else {
					   Stroke temp = new Stroke(tempStroke);
					   fullSymbol.addStroke(temp);
					   tempStroke.clear();
					   flag_new_stroke = 1;
					   //System.out.print("!");
					   continue;
				   }
			   }
			   continue;
			   
			   
		   }
		   if(flag_new_stroke == 1) {
			   lastTime = thisTime;
			   last = curr;
			   flag_new_stroke = 0;
			   continue;
		   }
		   
		   double speed = Math.abs((curr.distance(last)/(lastTime - thisTime)));
		  // System.out.println("Speed: " + speed + "at point: " + curr.toString());
		   
		   velocity[i] = speed;
		   lastTime = thisTime;				
		   
	   }	 
	   // this is the maximum speed
	   //double maxSpeed = velocity[findMaxSpeed(velocity)];
	   for (int i = 2 ; i < velocity.length; i++) {
		   Point curr = new Point(0,0);
		   Object tempObj = events.get(i);
		   try {
			   curr = (Point) tempObj;
		   } catch (Exception e){
			   int mouseEvent = (Integer) tempObj;
			   if (mouseEvent == 1) {
				   if (tempStroke.size() < 5 && events.size() > 25) {
					   tempStroke.clear();
					   //flag_new_stroke = 1;
					   continue;
				   }
			   }
		   }
		   
		   if (//velocity[i-2] < velocity[i-1] ||
			   velocity[i] < velocity[i-1] || 
			   events.size() < 25) 
		   {
			   // add stroke to the system
			   tempStroke.add(curr);
		   }
		   else {
			   //System.out.print (tempStroke.size());
			   if (tempStroke.size() < 5) {
				   // System.out.print("size before cut: " + tempStroke.size());
				   tempStroke.clear();
				   //System.out.print("|");
			   }
			   else {
				   Stroke temp = new Stroke(tempStroke);
				   fullSymbol.addStroke(temp);
				   tempStroke.clear();
				   //System.out.print(".");
				   
			   }
		   }
	   }
	   
	   if (events.size() < 25) {
		   tempStroke.remove(0);
		   tempStroke.remove(tempStroke.size()-1);
	   }
	   //System.out.println("Size: " + tempStroke.size());
	   if (tempStroke.size() > 5 ) {
		   if (tempStroke.size() != 0) {
			   //System.out.println("Number of items in Stroke: " + tempStroke.size());
			   //System.out.println("First: " + tempStroke.get(1).toString());
			   Stroke temp = new Stroke(tempStroke);
			   fullSymbol.addStroke(temp);
		   }
	   }
	   //System.out.println("Number of strokes: " + fullSymbol.myStrokes.size());
	   return fullSymbol;
	   
   }
   */
         
      
      	/**
       * Adds the contents of a file into the classifier.
       * Can be used multiple times to add the datapoints of different files.
       * The format of the files depends on the specific classifier.
       * @param trainingSetFile file that contains the datapoints
         */
      public void addTrainingSet(String trainingSetFile)  {

	  /**
	     For this we first need to figure out how to convert that trainingSetFile


	     Once we get it, we take each item and break it into SymbolOfStrokes and it's lable

	     

	  */
		  Vector<Sketch> trainingSketch = new Vector<Sketch>();
			try {
			  FileInputStream inStream = new FileInputStream(trainingSetFile);
			  ObjectInputStream in = new ObjectInputStream(inStream);
			  trainingSketch = (Vector<Sketch>) in.readObject();
			  in.close();
		  } catch (Exception ex) {
			  ex.printStackTrace();
		  }
		  
		  // This loop creates the SymbolOfStrokes training objects
			int num_of_zeros =0;
		  for (int i = 0; i < trainingSketch.size(); ++i) {
			  Sketch sk = (Sketch) trainingSketch.get(i);
			  SymbolOfStrokes trainedSOS =  strokeBreaker(sk.timestamps, sk.events);
			  trainedSOS.SymbolLabel = sk.getLabel();
			  //System.out.println(trainedSOS.toString());
			  trainingSet.add(trainedSOS);
			  if (trainedSOS.myStrokes.size() == 0) 
				  num_of_zeros++;
			
		  }
		  //System.out.println("Number of zeros: " + num_of_zeros);

      }

      
      
      
      
      
	/**
       * Classifies a sketch
       * @param s Sketch to classify
       * @param N number of results that the classifier must return
       * @return a ClassifierResults object with the N possible labels and
       * 		   the confidences of each label
       * @see ClassifierResults
       * @see Sketch
         */
      public ClassifierResults classify(Sketch s, int N) 
      {

         ClassifierResults results = new ClassifierResults(N);        
	 //double distance[] = new distance[trainingSet.size()];
	 double test; 
	 for (int i = 0 ; i < N ; ++i) {
	     results.confidence[i] = 0;
	 }

	 SymbolOfStrokes toBeClassified = strokeBreaker(s.timestamps, s.events);
	 

	 // This loop calculates the best N distances
	 for(int i = 0; i < trainingSet.size() ; ++i) {
		 SymbolOfStrokes temp = (SymbolOfStrokes) trainingSet.get(i);
	     test = toBeClassified.distance( temp);
	     for (int j = 0 ; j < N ; j++) {
		 if (test > results.confidence[j]) {
			 // do the multiple item check here
			 // it's another loop
			 for (int m = 0 ; m < N ; m++) {
				 if (temp.SymbolLabel == results.labels[m]) {
					 if (results.confidence[m] <= test) {
						 j=N;
						 break;
					 }
					 else {
						 // This is where if there is a double it takes the max
						 
						 //System.out.print("CHANGED: " + results.labels[m] + " test: " + results.confidence[m]);
						 for (int k = m; k < (N-1); k++) {
							 results.confidence[k] = results.confidence[k+1];
							 results.labels[k] = results.labels[k+1];
						 }
						 j=N;
						 i--;
						 break;
						 
					 }
				 }
				 
			 }
			 if (j==N)
				 break;
			for (int k = (N-1) ; k > j ; k--) {
				 results.confidence[k] = results.confidence[k-1];
				 results.labels[k] = results.labels[k-1];
		     }
		     results.confidence[j] = test;
		     results.labels[j] = temp.SymbolLabel;
		     break;
		 }
	     }
	 }
	 /*
	 toBeClassified = strokeBreakerRelative(s.timestamps, s.events);

	 
	 // This loop calculates the best N distances
	 for(int i = 0; i < trainingSet.size() ; ++i) {
		 SymbolOfStrokes temp = (SymbolOfStrokes) trainingSet.get(i);
	     test = toBeClassified.distance( temp);
	     for (int j = 0 ; j < N ; j++) {
			 if (test > results.confidence[j]) {
				 // do the multiple item check here
				 // it's another loop
				 for (int m = 0 ; m < N ; m++) {
					 if (temp.SymbolLabel == results.labels[m]) {
						 if (results.confidence[m] < test) {
							 j=N;
							 break;
						 }
						 else {
							 //TODO:  figure out a way to update the cost
							 //        this would mean that you have to  update the array as well
							 
							 //System.out.print("HEY HEY!!!!");
							 //kill this code::
							 j=N;
							 break;
							 
						 }
					 }
					 
				 }
				 if (j==N)
					 break;
				 for (int k = (N-1) ; k > j ; k--) {
					 results.confidence[k] = results.confidence[k-1];
					 results.labels[k] = results.labels[k-1];
				 }
				 results.confidence[j] = test;
				 
				 results.labels[j] = temp.SymbolLabel;
				 break;
			 }
	     }
	 }
	 */
	 double totalScore = 0;
	 for (int i = 0 ; i < N; ++i) 
		 totalScore += results.confidence[i];
	 for (int i = 0 ; i < N; ++i)
		 results.confidence[i] = results.confidence[i]/totalScore;
	 
	 
	 
	 
	 //System.out.print("Results ");
	 //for (int i = 0 ; i < N ; i++) {
		// System.out.print("(" + results.labels[i] + "," + results.confidence[i] +") ");
		 //System.out.print(results.labels[i] + " " );
	// }
	 //System.out.println("");
         return results;
         
      }
    
   static int findMaxSpeed(double s[]) {
	   int max = 0;
	   int position = 0;
	   for (int i = 0 ; i < s.length ; i++) {
		   if (s[i] > max) {
			   max = position;
			   position = i;
		   }
	   }
	   return position;
   }
   






























}

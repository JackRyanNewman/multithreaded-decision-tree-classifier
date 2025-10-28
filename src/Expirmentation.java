//Jack Newman
//Date: 2024-11-1, Assigment 2 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

//Expirementation: 
//Description: This is where I simulated the search algorthimn before I harded coded it into my descion tree. I wanted to quickly plug and play with different types of varibles
//and dataStructures as well dig into doing paralization in java, In this file, I tested out using streams, mutplie levels of paralization, and using that with hashmaps for the 
//atomtic integer arrrays, arrays for imporrtance calculations, testing data caching, and upfront costs of streams and parailization, and the cost of using structures tied to nodes. 
//I also played around with linkedLists vs ArrrayLists, and countless other ideas, there used to atleast 5 or 6 more methods. IN this I also made a very nice printing program to compare
//mutplie methods at a time.


public class Expirmentation {
	//==============================================================
	//Expirementation controll and print controlls
	static final boolean printRecInfo = false, PrintNext = false, printAll = true, printNone = false;
	static int MaxTrials=100, CurrTrial=0, SkipRate = 40, TrialSkipCount=SkipRate, c=0;
	static int Rows = 6000, Cols = 22, trimRate =200, min = Rows/Cols;
	static char[][] uniques = {
		    {'A', 'B', 'C'}, // Group 1
		    {'D', 'E', 'F'}, // Group 2
		    {'G', 'H', 'I'}, // Group 3
		    {'J', 'K', 'L'}, // Group 4
		    {'M', 'N', 'O'}, // Group 5
		    {'P', 'Q', 'R'}, // Group 6
		    {'S', 'T', 'U'}, // Group 7
		    {'V', 'W', 'X'}, // Group 8
		    {'Y', 'Z', ' '}, // Group 9
		    {'a', 'b', 'c'}, // Group 10
		    {'d', 'e', 'f'}, // Group 11
		    {'g', 'h', 'i'}, // Group 12
		    {'j', 'k', 'l'}, // Group 13
		    {'m', 'n', 'o'}, // Group 14
		    {'p', 'q', 'r'}, // Group 15
		    {'s', 't', 'u'}, // Group 16
		    {'v', 'w', 'x'}, // Group 17
		    {'y', 'z', '0'}, // Group 18
		    {'1', '2', '3'}, // Group 19
		    {'4', '5', '6'}, // Group 20
		    {'7', '8', '9'}, // Group 21
		    {'a', 'z', 'd'}, // Group 22
	};
	//==============================================================
   //Under lying data for replicate recursion
	static ArrayList<Attribute>attrs = new ArrayList<>(Cols);
	static ArrayList<char[]> rows =  new ArrayList<char[]>(Rows);
	static ArrayList<char[]> tmpr =  new ArrayList<char[]>(Rows);
	static ArrayList<Attribute>tmpa = new ArrayList<>(Cols);
	static ArrayList<char[]>[] Lazy = new ArrayList[3];
	static boolean switchR = true, switchA = true;; 
	static final double log2 = Math.log(2); 
	//Stastics======================================================
	static double[] averages = {0,0,0,0};
	static long totalCount = 0;
	
	
	
   public static void main(String[] args) {
   	if(PrintNext)TrialSkipCount=0; 
   	init();
   	 for(;CurrTrial < MaxTrials; CurrTrial++,TrialSkipCount++) { 
   		 long start = System.nanoTime(); 
   		 runController(trimRate);   
   		 long end = (System.nanoTime() - start) / 1_000_000; 
   		 if(printNone) System.out.printf("Trial %d completed in %d ms%n", CurrTrial, end/4);
   		 if(SkipRate == TrialSkipCount) TrialSkipCount =0;
   		 totalCount++;
   		 
   	 }
   	 System.out.printf("Avgs:| For: %5.4f ms | PS: %5.4f ms | BP: %5.4f ms | Stream: %5.4f ms%n",
             (double) averages[0] / totalCount,
             (double) averages[2] / totalCount,
             (double) averages[3] / totalCount,
             (double) averages[1] / totalCount);
	 }
  
   
   //================================================================================
   //Init
       
    private static void init() {
   	 char[][] array = new char[Rows][Cols];
		 Random random = new Random();
		  for (int i = 0; i < Rows; i++) {
		      for (int j = 0; j < Cols; j++) {
		          // Randomly select one of the three unique characters for each column
		          array[i][j] = uniques[j][random.nextInt(3)];
		      }
		  }
		 for (char[] row : array) rows.add(row);
		 for (int i = 0; i < Cols; i++) {
		     Attribute attribute = new Attribute(i);
		     attribute.initializeKeys(uniques[i]); // Unique characters for each column
		    attrs.add(attribute);
		 }
		 for (int i = 0; i < Lazy.length; i++) {
          Lazy[i] = new ArrayList<char[]>();
      }
    }
 
    static class Attribute {
       short index;
       char[] keys;
       float gain = 0;

       HashMap<Character, Integer> hashMap = new HashMap<>();
   
       
       public Attribute(int index) {
           this.index = (short) index;
       }

       
       public void initializeKeys(char[] uniqueCharacters) {
           for (char character : uniqueCharacters) {
               hashMap.put(character, 0); // Initialize each character key to 0
               
           }
           keys = uniqueCharacters;
       }
   }
    
   
    //================================================================================
    //Running
    
    public static void runController(final int r){
   	 for(int i=0; i < Cols; i++, c++) {
   		 for(int j=0; j < Cols; j++, c++) {
   			 if(!switchR && tmpr.isEmpty()) switchR=true;
   	   	 if(!switchA &&  tmpa.isEmpty()) switchA=true;
   			 run(c, r);
   		 }
   	 }
   		
    }
    
    
    //================================================================================
    //Testing methods
    @SuppressWarnings("unused")
	public static void run(int c,int trimR){
   	 	for(int i=0; i<3; i++) Lazy[i] = rows;
   	 	
   	 	long start = System.nanoTime(); tmpArrFor();
        	long durrFor = (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds

        	// Start measuring time for the stream version
        	start = System.nanoTime(); Streams();
        	long durrS = (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds

        	// Start measuring time for the parallel stream version
        	start = System.nanoTime(); multiParallel();
        	long durrPS = (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds
        	
      	start = System.nanoTime(); basicParallel();
        	long durrBP = (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds
        	
        	averages[0]+=durrFor;
        	averages[1]+=durrS;
        	averages[2]+=durrPS;
        	averages[3]+=durrBP;
        	if(!printNone && (printAll || SkipRate == TrialSkipCount)){
        		if(!printRecInfo)System.out.printf("Test:%3d|For:%2dms|PS:%2dms|BP:%2dms|Stream:%2dms%n",c, durrFor, durrPS, durrBP, durrS);
         	else{ 
         		System.out.printf(
   	        	    "Test:%3d| Rows:%7d-%5d|Attr: %2d-%2d| For:%2dms|PS:%2dms|BP:%2dms|Stream:%2dms%n",
   	        	    c, rows.size(), tmpr.size(), attrs.size(), tmpa.size(), durrFor, durrPS, durrBP, durrS
         		);
          	}
        	}
   	 
       start = System.nanoTime(); multiParallel();
   	 if(switchR){ //Keep removin trimR size every call, until limit reached, or size is rows/cols. 
   		 for(int r=0; r< trimR && rows.size()>min; r++) {
	   		 	tmpr.add(rows.get(0)); 
	   			rows.remove(0); 
	   	 }
   	 	 if(!(rows.size()>min)) switchR=false;
   	 }
   	 else{
   		 for(int i=min; i!=1 && !tmpr.isEmpty(); i--) {
				rows.add(tmpr.get(0));
				tmpr.remove(0);
   	 }}
   	 if(switchA) {
   		 tmpa.add(attrs.get(0));  
   		 attrs.remove(0); 
   		 if( attrs.size() == 1) switchA = false;
   	 }
   	 else {
   		 attrs.add(tmpa.get(0)); 
   		 tmpa.remove(0);
   	 }
   	 long adj = (System.nanoTime() - start) / 1_000_000;
//   	 averages[0]+=adj;
//   	 averages[1]+=adj;
//       averages[2]+=adj;
//       averages[3]+=adj;
   	 
    }

    private static void tmpArrFor() {
   		int[] mimic = {1,2,3}; 
      	float[] Gain = {0}, s0= {0}; //varibles 		
   		int subLen = 3;
        	int sum = Arrays.stream(mimic).sum();
       	for(int i=0; i < 3; i++) s0[0]+=(mimic[i]/sum)*(Math.log(mimic[i]/sum)/log2);
       	s0[0]*=-1;
   	 
   	 for (Attribute attribute : attrs) {
   			float remainder = 0; 
    			short index = attribute.index;
 				int[] totalV = new int[123]; 
 				int[][] subEntropy = new int[subLen][123];
          
 				
 				for (int run = 0; run < 3; run++) {
 		          for (char[] row : Lazy[run]) {  
 		        	  	char val = row[index];
 		        		subEntropy[run][val]++;
 		        		totalV[val]++; //Increments total class. 
 		           }
 		         }
 				
	  	    	for(char key: attribute.keys) {
	  	    		float entropy = 0; 
	  	    		float valueCT =  totalV[key];
	  	    		if(valueCT != 0) { //Nothing was caculuated in this class. 
	  	    			for(int c=0; c<subLen; c++) {
	  	    				float pk =  subEntropy[c][key] / valueCT;
	  	    				if(pk != 0) entropy+=(pk*(Math.log(pk)/Math.log(2)));
	  	    			}
		     	   remainder+=((valueCT/sum)*(-entropy));
	  	   	 }
	  	    }
      }
  }
    
    private static void basicParallel() {
   	int[] mimic = {1,2,3}; 
   	float[] Gain = {0}, s0= {0}; //varibles 		
		int subLen = 3;
     	int sum = Arrays.stream(mimic).sum();
    	for(int i=0; i < 3; i++) s0[0]+=(mimic[i]/sum)*(Math.log(mimic[i]/sum)/log2);
    	s0[0]*=-1;
    	attrs.parallelStream().forEach(attribute -> {
    		 	float remainder = 0; 
    			short index = attribute.index;
 				int[] totalV = new int[123]; 
 				int[][] subEntropy = new int[subLen][123];
			 for (int run = 0; run < 3; run++) {
	          for (char[] row : Lazy[run]) {  
	        	  	char val = row[index];
	        		subEntropy[run][val]++;
	        		totalV[val]++; //Increments total class. 
	           }
	         }
	  	    for(char key: attribute.keys) {
	  	   	 float entropy = 0; 
	  	   	 float valueCT =  totalV[key];
	  	   	 if(valueCT != 0) { //Nothing was caculuated in this class. 
	  	   		 for(int c=0; c<subLen; c++) {
	  	   			 float pk =  subEntropy[c][key] / valueCT;
		  	   		 if(pk != 0) entropy+=(pk*(Math.log(pk)/Math.log(2)));
		  	   	 }
		     	    remainder+=((valueCT/sum)*(-entropy));
	  	   	 }
	  	    }
   	});	
    }
    
    private static void Streams() {
   		int[] mimic = {1,2,3}; 
      	float[] Gain = {0}, s0= {0}; //varibles 		
   		int subLen = 3;
        	int sum = Arrays.stream(mimic).sum();
       	for(int i=0; i < 3; i++) s0[0]+=(mimic[i]/sum)*(Math.log(mimic[i]/sum)/log2);
       	s0[0]*=-1;
   	 
   	 attrs.stream().forEach(attribute -> {
   			float remainder = 0; 
    			short index = attribute.index;
 				int[] totalV = new int[123]; 
 				int[][] subEntropy = new int[subLen][123];
				IntStream.range(0, subLen).forEach(subSet->{Lazy[subSet].forEach(row -> {
	           char val = row[index];
	           subEntropy[subSet][val]+=1;  //Updates, attr x, values y, count for output x 
	           totalV[val]+=1; 				 //Updates, attr x, values y, for all outputs. 
	       	});
			 }); 
	  	    for(char key: attribute.keys) {
	  	   	 float entropy = 0; 
	  	   	 float valueCT =  totalV[key];
	  	   	 if(valueCT != 0) { //Nothing was caculuated in this class. 
	  	   		 for(int c=0; c<subLen; c++) {
	  	   			 float pk =  subEntropy[c][key] / valueCT;
		  	   		 if(pk != 0) entropy+=(pk*(Math.log(pk)/Math.log(2)));
		  	   	 }
		     	    remainder+=((valueCT/sum)*(-entropy));
	  	   	 }
	  	    }
   	});	
  }
    
    
    //===============================================================================
    //Irrevalant
    
    
    private static void multiParallel() {
   	 int s0 = 0;
       int[] mimic = {1, 2, 3};
       int sum = Arrays.stream(mimic).sum();
       for (int i = 0; i < 3; i++) s0 += (mimic[i] / sum) * (Math.log(mimic[i] / sum) / log2);

       attrs.parallelStream().forEach(attribute -> {
           int index = attribute.index;
           char[] keys = attribute.keys;
           AtomicIntegerArray[] subEntropy = new AtomicIntegerArray[3];
           AtomicIntegerArray localTotalCounts = new AtomicIntegerArray(123); // Use AtomicIntegerArray for local counts
           for (int i = 0; i < 3; i++) subEntropy[i] = new AtomicIntegerArray(123); // 123 is the length for char keys
           
          

           // Parallel processing of Lazy arrays
           IntStream.range(0, 3).parallel().forEach(run -> {
               Lazy[run].forEach(row -> {
                   char val = row[index];
                   subEntropy[run].incrementAndGet(val); // Atomically increments the value at index `val`
                   localTotalCounts.incrementAndGet(val); // Atomically increments the localTotalCounts for `val`
               });
           });

           int classTotal = 3; 
           double remainder = 0; 
           for (char key : keys) {
               double entropy = 0; 
               int valueCT = localTotalCounts.get(key); // Get the current count for key
               for(int Class=0; Class<3; Class++) {
                   double pk = valueCT == 0 ? 0 : subEntropy[Class].get(key) / (double) valueCT;
                   entropy += (pk * Math.log(pk) / log2);
               }
               remainder += (valueCT / (double) classTotal * (entropy));
           }
           attribute.gain = (float) remainder;
       });
    }
    

	//	 IntStream.range(0, subLen).forEach(subSet->{subSets[subSet].forEach(row -> {
	//  char val = row[index];
	//  subEntropy[subSet][val]+=1;  //Updates, attr x, values y, count for output x 
	//  totalV[val]+=1; 				 //Updates, attr x, values y, for all outputs. 
	//	});
	//}); 
	//
    
 

//		for(char vKey: node.Attr.QKeys) {
//			ArrayList<char[]>[] cSets =  new ArrayList[subSets.length];
//			TreeNode child = handleChild(vKey,node, subSets, cSets, attrs);  //Creates DataSet, and determines if node should be split or not. 
//			CountDownLatch latch = new CountDownLatch(1);
//			new Thread(() -> {
//           for (int i = 0; i < subSets.length; i++) subSets[i].removeIf(Objects::isNull); // Bulk remove reference
//           latch.countDown(); // Indicate that the resizing is complete
//       }).start();
//			if(child != null) split(child, cSets, attrs);					    				 //I split it only if its needed. Else I prit its outcome. 
//			latch.await(); // This will block until countDown() is called
//		}
//		
    
//int totalSize = Arrays.stream(subSets).mapToInt(ArrayList::size).sum(); //This may be very innfiecnet but its cool! 
//for(int i=0; i < subSets.length; i++) subSets[i].removeIf(Objects::isNull); //Then I bulk remove references. For downSizing. 
    

//    private static void HashParallel() {
//  	 attrs.parallelStream().forEach(attribute -> {
//         int index = attribute.index;
//         ConcurrentHashMap<Character, Integer> x = attribute.map;
//         int[] localCounts = new int[123]; // Assuming maximum size is 120
//
//         // Run the counting process 3 times
//         IntStream.range(0, 3).forEach(run -> {
//             rows.parallelStream().forEach(row -> {
//                 localCounts[row[index]]++; // Increment count based on the character's index
//             });
//         });
//         x.keySet().forEach(key -> { x.put(key, localCounts[key]); });
//     });
//  } 

//		private boolean baseCase(TreeNode node){
//		if(node.Leaf != '\0'){ 
//			if(V>2) {
//				System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+"): ");
//				if(node.AllPure) System.out.print("Node Is Pure|Unexpected\n");
//				else if(DL_On && node.Depth >= DL) System.out.print("Node is at max depth|Unexpected\n");	
//				else System.out.print("No data left to split, prediction chosen based off majority|Unexpected\n");
//				
//			}
//			return true;
//		}
//		else if(V>2) System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+"): Node is splittable\n");
//		if(ExtraP) System.out.println("‹›base Case Reached");
//		return false;
//	}    
    
    
   
   
   
}

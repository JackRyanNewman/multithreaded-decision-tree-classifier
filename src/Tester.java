//Jack Newman
//Date: 2024-11-1, Assigment 2 

//========================================================================================================================
/*
 * TESTING FILE: 
 * 	Used to generate random perumations of all inputs used and files given. Gives you multplie options and 
 * 	and ways to test out the learning tree.  
 */
//========================================================================================================================

public class Tester {

	static String[] fp = { 									   // -f <FILENAME>    Name of the file to read data from
	        "../457-ML-02-JN/a02-data/mushroom_data_small.txt",
	        "../457-ML-02-JN/a02-data/mushroom_data.txt",
	        "../457-ML-02-JN/a02-data/simple-spam.txt",
	        "../457-ML-02-JN/a02-data/titanic-data.txt"
	    };  															
	    static String[] BGS = {"10"};         			// -b <INTEGER>     Base training group size; init = 10
	    static String[] INC = {"-1", "100", "200"};   	// -i <INTEGER>     Increment rate for training group size; init = base group size
	    static String[] LIM = {"-1", "20", "30"};     	// -l <INTEGER>     Limit for training group size; init = base group size
	    static String[] TRI = {"1", "2", "3"};         // -t <INTEGER>     Num trials performed at each training group size; default is 1
	    static String[] MDP = {"-1", "3", "5", "7"}; 	// -d <INTEGER>     Max depth for decision tree; default = no limit
	    static String[] VRB = {"1", "2", "3", "4"};  	//-v <INTEGER>      Verbosity level for output; default is 1
	    static String r = "-r";								//-p <boolean>     Toggles printing of decision tree built in the last trial; default is disabled
	    static String p = "-p";								//-r <boolean>     Enables randomization of data for selection into the training set; default is false
	
	 	public static void main(String[] args) throws Exception {		
	 		
	 		String[] tmpArgs = null;
//	 		for(short option =1; option<9; option++) {
//	 			switch(option) {
//		 		case 0: tmpArgs = "-f ../457-ML-02-JN/a02-data/mushroom_data.txt -b 50 -i 100 -l 300 -t 10".split(" "); break;
//		 	   case 1: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 400 -t 3 -r 2".split(" "); break; //-r
//		 	   case 2: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -t 3 -r -v 2 -d 3".split(" "); break;
//		 	   case 3: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -d 2 -v 3".split(" "); break;
//		 	   case 4: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -d 2 -v 4".split(" "); break;
//		 	   case 5: tmpArgs = "-f ../457-ML-02-JN/a02-data/mushroom_data_small.txt -b 150 -p -d 3 -v 4".split(" "); break;
//		 		
//		 	   case 6: tmpArgs =  "-f ../457-ML-02-JN/a02-data/simple-spam.txt -b 2 -d 1 -v 3".split(" "); break;		//spam
//		 		case 7: tmpArgs =  "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -d 2 -v 4".split(" ");break; //custom
//		 		case 8: tmpArgs =  "-f ../457-ML-02-JN/a02-data/patrons -b 12 -v 4".split(" "); break;							//class
//	 		}
//	 			String tmp = String.join(" ", tmpArgs); System.out.println(tmp);
//		 		SearchController.main(tmpArgs);
//	 			
//	 		}
	 		short option = 9;
	 			switch(option) {
		 		case 0: tmpArgs = "-f ../457-ML-02-JN/a02-data/mushroom_data.txt -b 50 -i 100 -l 300 -t 10".split(" "); break;
		 	   case 1: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 100 -t 3 -r 2".split(" "); break; //-r
		 	   case 2: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -t 3 -r -v 2 -d 3".split(" "); break;
		 	   case 3: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -d 2 -v 3".split(" "); break;
		 	   case 4: tmpArgs = "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -d 2 -v 4".split(" "); break;
		 	   case 5: tmpArgs = "-f ../457-ML-02-JN/a02-data/mushroom_data_small.txt -b 150 -p -d 3 -v 4".split(" "); break;
		 		
		 	   case 6: tmpArgs =  "-f ../457-ML-02-JN/a02-data/simple-spam.txt -b 2 -d 1 -v 3".split(" "); break;		//spam
		 		case 7: tmpArgs =  "-f ../457-ML-02-JN/a02-data/titanic-data.txt -b 200 -d 2 -v 3 -p".split(" ");break; //custom
		 		case 8: tmpArgs =  "-f ../457-ML-02-JN/a02-data/patrons -b 12 -v 4".split(" "); break;		
		 		case 9: tmpArgs =  "-f ../457-ML-02-JN/a02-data/mushroom_data.txt -t 10000 -b 1000 -v 1".split(" ");
		 	
	 		}
	 			 
	 			
	 		String tmp = String.join(" ", tmpArgs); System.out.println(tmp);
	 		SearchController.main(tmpArgs);
	 		//genPermutations(); 
	 	
	 	}
	
	 	 public static void genPermutations() throws Exception {
	        for (String filePath : fp) {
	            for (String baseGroupSize : BGS) {
	                for (String increment : INC) {
	                    for (String limit : LIM) {
	                        for (String trial : TRI) {
	                            for (String maxDepth : MDP) {
	                                for (String verbosity : VRB) {
	                                    // Construct the args array
	                                    String[] ta = {
	                                        "-f", filePath,        
	                                        "-b", baseGroupSize,  
	                                        "-i", increment,      
	                                        "-l", limit,          
	                                        "-t", trial,         
	                                        "-d", maxDepth,      
	                                        "-v", verbosity,       
	                                    };

	                                    // Call the main method of SearchController with generated args
	                                    SearchController.main(ta);
	                                   
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }
	
	
}

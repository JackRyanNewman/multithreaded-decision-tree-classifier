//Jack Newman
//Date: 2024-11-1, Assigment 2
	//Assgiment Descripition: 
		//This prorgram generates learning curves for a decision tree classifier on specefied dataSets, and computes accuracry scores for both training and validtion sets. https://en.wikipedia.org/wiki/Decision_tree
		//My solution is uniqued optimized version, that makes use of parailization, that loosely follows the books and proffesors solution, and I was able to achieve at minuim 3, to maxium possible 100's, as it 
 	   //expoetinally speeds up when on larger trials, espcially after the first trial. To achieve this, I did big O anylsis, heavy expirmentation(See expirmentation java, I deleted alot in it since), 
		//parallelization, and in respect to how it might break down in asmebly, while all also trying to balance readability. 
	

//Search Controller
//Description: This class, takes in all command arguments to run the program, it creates a desicionTree class object to intilize all the data structures and needed global varibles for the tree creation.
//This class then iteratively creates desicions trees based off various sizes of data(Intinally based on the baseGroupSize), the data can be samppled in order from the file, or randomized. 
//For each size of data you can also generate multplie trials for each groupsize, and test different permutations of randomized data to see how different sizes do. It handle the some basic verbosity printing. 
//However the descion trees StatsInfo Object does all the informational printing. 


public class SearchController {  
	static boolean timeTrialingActive = true; //Turn true, if you want to see how the how long the program takes, starting from creating the first subSets of data, 
															//and to also print out all long each group size takes for x trials

	
	static public void main(String[] args) throws Exception {
		String fileName = "";       	//-f <FILENAME>    Name of the file to read data from
		int baseGroupSize = 10;    	//-b <INTEGER>     Base training group size; 		  		 	init = 10
		int increment = -1;         	//-i <INTEGER>     Increment rate for training group size	init = base group size
		int limit = -1;             	//-l <INTEGER>     Limit for training group size	 			init = base group size
		int trials = 1;             	//-t <INTEGER>     Num trials performed at each training group size default is 1
		
		int maxDepth = -1;         	//-d <INTEGER>     Max depth for decision tree; default = no limit. 
																		 //Splitting at rote node is depth 0, child 1 = depth 1. 
		byte tmpV = 1;          		//-v <INTEGER>     Verbosity level for output; init = 1
		boolean tmpR = false;  	     	//-p <boolean>     Toggles printing of decision tree built in the last trial; default is disabled
		boolean tmpPT = false;  	  	//-r <boolean>     Enables randomization of data for selection into the training set; default is false
		
		for (int i = 0; i < args.length; i += 2) {
		    String tmp = "";
		    switch (args[i]) {
		        case "-f": {
		            fileName = args[i + 1];
		            break;  
		        }
		        case "-b": {
		            baseGroupSize = Integer.parseInt(args[i + 1]);
		            break;  
		        }
		        case "-i": {
		            increment = Integer.parseInt(args[i + 1]);
		            break;  
		        }
		        case "-l": {
		            limit = Integer.parseInt(args[i + 1]);
		            break;  
		        }
		        case "-t": {
		            trials = Integer.parseInt(args[i + 1]);
		            break; 
		        }
		        case "-d": {
		            maxDepth = Integer.parseInt(args[i + 1]);
		            break; 
		        }
		        case "-v": {
		            tmpV = Byte.parseByte(args[i + 1]);
		            break; 
		        }
		        case "-p": {
		      	  	tmpPT = true;
		            i -= 1;
		            break;  
		        }
		        case "-r": {
		      	  	tmpR = true;
		            i -= 1;
		            break;  
		        }
		    }
		}

		
		if (!(fileName.equals(""))) {
			double i = 0;
			double totalTime = 0;
			final byte v = tmpV;
			final boolean r = tmpR;  	
			final boolean pt = tmpPT;  
			if(limit==-1) limit = baseGroupSize;
			if(increment==-1) increment =  baseGroupSize;
			DecisionTree Search = new DecisionTree(fileName, maxDepth,v,trials);    //Intilization of Desicsion tree. 
			limit = Search.FullSet.length > limit? Search.FullSet.length: limit; 
			
			
			for(int cgs=baseGroupSize; cgs < limit; cgs+=increment, i++){ 				//This loop controlls, the dataSet size
				System.out.println("----------------------------------"); 
				System.out.println("* Using Training Group of size " +cgs);
				if(!r) Search.createSubData(cgs);
				for(int t=1; t <= trials; t++) {													//This loop controlls how many trials you do. 
					 if(r) Search.randomize(cgs);
					 else Search.Stats.StartT = System.nanoTime();
					 if(v>1)System.out.println("  * Trial "+t+":");
					 if(v>2)System.out.println("    * Beginning decision tree learning");
					 Search.decisionTreeLearn();												//This is where I run a desciion tree. 
					 if(v>2) System.out.println("    * Learned tree has learned "+(Search.NodeCount+1)+""); 
					 if(v>1) Search.Stats.printTrialAcc(); 
				}
				totalTime += Search.Stats.printGroupAvg(timeTrialingActive);
			}
			if(pt) Search.Stats.pt();
			if(timeTrialingActive) System.out.printf("\n\n•›Total average time taken for all trials for all group sizes: %.5fs%n", totalTime/i);
		}
	}
		
}

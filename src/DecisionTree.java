//Jack Newman
//Date: 2024-11-1, Assigment 2 
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;



/*
 * Desicion Tree: This class is a masssive file, that has 3 main purposes, and 3 subclasses, and they are organized into three sections. I would reccomond folding methods for better navigation.
  		•› Section 1: Data structures: Handles intilization and creation of the main dataStructure, attributes, global varibles, and creation of validation/SubDataSets. 
  		•› Section 2: Decision Tree Algorithm: My unique solution to handeling desicion trees. The most optimization occurs in the importance file.
		•› Section 3: Stats and printing: This contains the StatsInfo class, and my methods for printing, and accurarcy calcuations.
*/


public class DecisionTree {
	static final double log2 = Math.log(2);  //This block of varibles, are frequently called varibles, made globals, to reduce clutter, and to optimization by using keyword final.  
	static final boolean ExtraP = false;     //This varible allows you to see the choice of gain at each importance calcuclation, and the size of the data it was based off. 
	final boolean DL_On; //Depth-Limit_On = DL_ON
	final int DL; 			//Depth-Limit
	final byte V;			//Verbosity
	
	StatInfo Stats;			//This block of varibles, are non data structurres varibles that are reset during various stages of the alogirthmn
	TreeNode root;		
	int NodeCount;
	
	
	int YPos;					//This block of varibles, are for all my dataStructures, that are used during search, or things related to them.  row[YPos] is the always the output column!   
	Attribute OPClass;      //YPos represents the column postion of the output in the FullSet, OPClass represents, Output class which is translated into a attribute
	Attribute[] Attrs;		//All attributes
	char[][]  FullSet;		//An array of all data 
	ArrayList<char[]> ValSet;    //Validation set
	ArrayList<char[]> TrainSet;  //Training set
	ArrayList<char[]>[] SubSets; //This is a collection of the trainset but its split up and organized based off its outuput, this is useful for searching.    
	
	

   //================================================================================
   //Data structures and classes. 
 	
		//Intilizaton of my dataStructures. 
 		public DecisionTree(String file, final int md, final byte v, int trials) throws Exception{
 			DL = md == -1? 2147483647: md;
 			DL_On = md != -1? true: false; 
 			Stats = new StatInfo(trials);
 			V = v;
 			
 			String line = "";  
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			do line = br.readLine(); while(line.equals("")||!Character.isDigit(line.charAt(0))); //Skip until attribute size found. 
 			YPos = Integer.parseInt(line);												   					 //I mark down output value indice, and attribute list size. 
 			Attrs = new Attribute[YPos];
 					
 			
 			for(short i=0; i<YPos; i++) Attrs[i] = new Attribute( i, br.readLine().split(":"), false);  //I set  each attribute
 			do line = br.readLine(); while(line.equals("")||line.charAt(0) != ':');			 			   //I skip to output class
 			OPClass = new Attribute((short)YPos, line.split(":"), true);									//I set my outPut class
 			SubSets = new ArrayList[OPClass.QKeys.length]; 

 			ArrayList<String> parser = new ArrayList<String>(10000);  //I make a temp data structure to keep track of all Records
 			do line = br.readLine(); while(line.isEmpty() || !Character.isLetterOrDigit(line.charAt(0))); //I skip to records.  
 			do parser.add(line.replaceAll("\\s+", "")); while((line = br.readLine()) != null);   //Then I read all datum. 
 			if(parser.get(parser.size()-1).equals("")) parser.remove(parser.size()-1);
 			
 		
 			FullSet = new char[parser.size()][YPos];	//Then I intilize my orginal dataSet			
 			for(int i=0; i < parser.size(); i++) FullSet[i] = parser.get(i).toCharArray(); //Then I make a char array out of it. 		
 			br.close();
 		}
 		
 		public void createSubData(int cgs) {
 			Stats.StartT = System.nanoTime();
 			Arrays.setAll(SubSets, i -> new ArrayList<char[]>(cgs/OPClass.QKeys.length)); // Create a new ArrayList for each element
 			ValSet  = new ArrayList<char[]>(Arrays.asList(FullSet).subList(cgs, FullSet.length));	////////////////
 			TrainSet = new ArrayList<char[]>(Arrays.asList(FullSet).subList(0, cgs));		
 			TrainSet.sort(Comparator.comparing(row -> row[YPos]));  
 	
 			for(int i=0, c=0; c<cgs && i < OPClass.QKeys.length; i++) {
 				char opKey = TrainSet.get(c)[YPos]; //outPut
 				if(!(opKey == OPClass.QKeys[i])) continue;
 				while (c < cgs && TrainSet.get(c)[YPos] == opKey) {
 					SubSets[i].add(TrainSet.get(c)); 
 					c++;
 				}
 			} 
 			for (ArrayList<char[]> list : SubSets) list.trimToSize(); 
 		}
 		
 		public void randomize(int gp) {
 			Stats.StartT = System.nanoTime();
 			Collections.shuffle(Arrays.asList(FullSet));
 			Arrays.setAll(SubSets, i -> new ArrayList<char[]>(gp/OPClass.QKeys.length)); // Create a new ArrayList for each element
 			ValSet  = new ArrayList<char[]>(Arrays.asList(FullSet).subList(gp, FullSet.length));	
 		   TrainSet = new ArrayList<char[]>(Arrays.asList(FullSet).subList(0, gp));	//PRoven to better than making a tmp 2d char array! 
 			TrainSet.sort(Comparator.comparing(row -> row[YPos]));  
 			
 			for(int i=0, c=0; c <gp && i < OPClass.QKeys.length ; i++) { 
 				char opKey = TrainSet.get(c)[YPos]; //outPut
 				if(!(opKey == OPClass.QKeys[i])) continue;
 				while (c < gp && TrainSet.get(c)[YPos] == opKey) {
 					SubSets[i].add(TrainSet.get(c)); 
 					c++;
 				}
 			}
 			for (ArrayList<char[]> list : SubSets) list.trimToSize(); 
 			
 		}
 		
 		private class Attribute{
 			short Index; 
 			String Name;
 			char[] QKeys;
 			HashMap<Character, String> Meaning; 	
 					
 			public Attribute(short index, String[] parsed, boolean OP){
 				if(parsed[1].charAt(0)==' ')parsed[1]=parsed[1].substring(1); 
 				Index = index;
 				Name = OP?"O/P":parsed[0];
 				parsed = parsed[1].split(" ");
 				Meaning = new HashMap<Character, String>(parsed.length+2);
 				String tmp = "";
 				
 				for(String item: parsed) {
 					String meaning = item.length() > 1? item.substring(2): item.charAt(0)+"";
 					Meaning.put(item.charAt(0), meaning);			
 					tmp+=item.charAt(0);
 				}
 				QKeys = tmp.toCharArray();
 				if(Name.equals("O/P")) Arrays.sort(QKeys);
 			}
 		}
 		
   //================================================================================
   //Decision Tree Algorithm
 		private class TreeNode{
 			int Depth;  								   //Keep track of nodes depth
 			Attribute Attr; 								//The nodes attribute
 			HashMap<Character, TreeNode> Children; //Keeps track of each option, and their repsective next path. 
 		
 			
 			char Leaf; 				//Base value, if changed it indicates its a leaf. 
 			boolean AllPure;
 			boolean IsLeaf;
 			
 			
 			public TreeNode() { //special constructor for root.
 				NodeCount = 0;
 				Depth = 0; 
 				Children = new HashMap<>();
 				NodeCount = 0;
 				
 			}
 			
 			public TreeNode(int depth){ //For nodes undecided its a branch or not 
 				NodeCount++;
 				Attr = null; 
 				Depth = depth;
 				Children = new HashMap<>();
 				
 			}
 			
 			public TreeNode(char leaf, boolean pure, int depth) { //Nodes that are spefically leafs
 				NodeCount++;
 				Leaf = leaf;
 				AllPure = pure;
 				Depth = depth; 
 				
 				IsLeaf =true;
 				Children = null;
 			}
 	}
 		
 		public void decisionTreeLearn(){
 			ArrayList<Attribute> attrs = new ArrayList<>(Arrays.asList(Attrs));
 			split(root = new TreeNode(), SubSets, attrs);
 			Stats.calcAcc();
 		}

 		//This handles all the splitting, and it has various subMethods that it calls. The most different thing about my alogorhtimn, is I pre prune recursion in 3 ways.
 		//One I end rearly if they are all pure, or if there is no data left, or its on last attribute, if its not any of those conditions, then I check 
 		//Each child/branch for purity, no data, and depth limit as well. I make use of parallel arrrays, and also my subSets arraylist contains
 		//The data in different subsets based off thier output type for max effeiency of telling purity, and easy mapping for importance. 
 		//I also found many connections and ways to save calcuatlions in a good 
 		
		private void split(TreeNode node, ArrayList<char[]>[] subSets, ArrayList<Attribute> attrs) {
			if(V>2) System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+"): Node is splittable\n");
			float sum = maxProbality(node, subSets); 			 //Determines if nodes children are pure, finds largest output, and cacluates sum total of them.  
			if(node.AllPure) allPure(node, attrs.get(0));   //This assumes that the data has to end, and all data is pure. 
			else { 
				int[][] fullCount = importance(node, subSets, attrs, sum); //Finds most important node, removes its from list, and returns the counting of all the most important attribute
				if(attrs.isEmpty()||(DL_On && DL <= node.Depth)) handleProbality(node, fullCount); //With fullCount, I can, easily and quickly split up all the data if it needs to end 
				else {																									  ///I do this cache optimization, and reduced stack calls
					fullCount = null; //Deference for garabage colleciton. 
					ArrayList<char[]>[] cSets =  new ArrayList[subSets.length];
					for(char vKey: node.Attr.QKeys) {
						TreeNode child = handleChild(vKey,node, subSets, cSets);  //Creates childs DataSet, and determines if node should be split or not. 
						if(child != null) split(child, cSets, attrs);					    				 //I split it only if its needed. Else I prit its outcome. 
					}
				}
				attrs.add(node.Attr); //I add the attribute that previously removed.			
			}
		}
					
		
		//Determines if nodes children are pure, finds largest output, and cacluates sum total of them
		private float maxProbality(TreeNode node, ArrayList<char[]>[] subSets) {
			float sum = 0; 
			short pureCount = 0; 
			ArrayList<char[]> globalMaxLoc = subSets[0];
			for(int i=0; i < subSets.length; i++) { 
				ArrayList<char[]> subSet = subSets[i];
				int currSize = subSet.size();
				if(globalMaxLoc.size() < currSize) globalMaxLoc =  subSet;
				if(currSize != 0) pureCount++;
				sum += currSize;
			}
			if(pureCount == 1) node.AllPure = true; //I mark down if its pure. 
			node.Leaf = globalMaxLoc.get(0)[YPos];  //I premityevely mark down the most likely outcome, incase of any prencodintions. 
			return sum;
		}
		
		//This assumes that the data has to end, and all data is pure. 
		private void allPure(TreeNode node, Attribute attr){
			node.Attr = attr;
			for(char vKey: attr.QKeys) { 
				node.Children.put(vKey, new TreeNode(node.Leaf, true,node.Depth+1));
				if(V>2) System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+1+"): Node Is Pure\n");
			}
		}
		
		//Finds most important node, removes its from list, and returns a use dataStruct. *PARALIZATION*
		private int[][] importance(TreeNode node, ArrayList<char[]>[] subSets, ArrayList<Attribute> attrs, float sum) {
			int subLen = subSets.length;
			float[] Gain = {-1f}, s0= {0f}; //array varibles, for parallelization safety. 
			int[][][] fullCount = new int[1][][];
			for(ArrayList<char[]> subSet: subSets) {
				int size = subSet.size();
				if(size != 0) s0[0]+=(size /sum)*(Math.log(size/sum)/log2);
			}
			s0[0]*=-1;
		
			attrs.parallelStream().forEach(attribute -> { //In parrallel I look at every attribute and read columns wise from each of subSets
				float remainder = 0;  
				int index = attribute.Index;
	    	   int[][] subEntropy = new int[subLen][123]; //Any hashmap of size 10 or more is larger than this, and runs risks of collisions. Furthermore access time is inferior to this.	    	   
	    	   for(int subIndex=0; subIndex < subLen; subIndex++) { //2 calculations, 1 array access to pointer, pointer to data structure data, then O(1) search, then pointer access to int. vs 1 array access. 
	    	   	int[] currEntropy = subEntropy[subIndex];
	    	   	ArrayList<char[]> subSet =  subSets[subIndex]; 
	    	   	for(char[] row: subSet)  currEntropy[row[index]]++;  //Updates, attr x, values y, count for output x 
	    	   }
	    	   
  	   		for(char vKey: attribute.QKeys) {	//Basic calcuations. 
  	   			float entropy = 0, valueCT = 0; //Updates, attr x, values y, for all outputs.
		  	   	for(int[] subTotal: subEntropy) valueCT+=subTotal[vKey];
		  	   	if(valueCT != 0) { //Nothing was caculuated in this class. 
	  	   		 for(int[] subSet: subEntropy) {
	  	   			float pk =  subSet[vKey] / valueCT;
	  	   			if(pk != 0) entropy+=(pk*(Math.log(pk)/log2));
	  	   		 }
		     	    remainder+=((valueCT/sum)*(-entropy));
		  	   	}
		  	   	
  	   		}
		  	   synchronized(node) {							//Lock for thread safety.
		  	   	remainder=s0[0]-remainder;
		  	   	if(V==4)System.out.printf("         Gain=%.4f with split on [%s]\n", remainder,attribute.Name);
		  	   	if(Gain[0] < remainder){
		  	   		Gain[0] = remainder;
		  	   		node.Attr = attribute;
		  	   		fullCount[0] = subEntropy;
		       }
		  	   }
	       });
			if(ExtraP) {
				System.out.print("         •› size of data ="+sum+"\n");
				System.out.printf("         •›[%s] Gain = %.4f\n", node.Attr.Name,Gain[0]);
			}
			attrs.remove(node.Attr);
			return fullCount[0];
		}
				
		//Handles filtering the data for each child 
		private void handleProbality(TreeNode node, int[][] fullCount){
			for(char vKey: node.Attr.QKeys) {
				short pureCount = 0; 
				int subSetLoc = -1; 
				int subMaxVal = -1;
				
				for(int i=1; i< fullCount.length; i++){
					int valueSize = fullCount[i][vKey];
					if(subMaxVal < valueSize) {
						subMaxVal = valueSize;
						subSetLoc = i;
						pureCount++; 
					}
				}	
				
				
				if(pureCount == 1) {//Pure is found. 
					node.Children.put(vKey, new TreeNode(OPClass.QKeys[subSetLoc], true,node.Depth+1));
					if(V>2) System.out.println("       Examining node "+NodeCount+" (depth="+(node.Depth+1)+"): Node is Pure…");
				}
				else {
					if(subSetLoc == -1) { //No values were found, pick values found in marjoirty regardless of type. 
						node.Children.put(vKey, new TreeNode(node.Leaf, false, node.Depth+1));
					}
					else { //If pure node not found, there no attr left, and there only x choices left. 
						node.Children.put(vKey, new TreeNode(OPClass.QKeys[subSetLoc], false,node.Depth+1));
					}
					if(V>2) {
						System.out.println("       Examining node "+NodeCount+" (depth="+(node.Depth+1)+"): ");
						if(DL_On && node.Depth + 1 >= DL) System.out.print("Node is at max depth…\n");	
						else System.out.print(" No data left to split, prediction chosen based off majority…\n");
					}
				}
			}
		}
	
		private TreeNode handleChild(char key, TreeNode parent, ArrayList<char[]>[] subSets, ArrayList<char[]>[] cSets){
			//ArrayList<char[]>[] newsubSets = new ArrayList[3];
			int index = parent.Attr.Index;
			int subSetLoc = -1, subMaxVal = 0;
			int pureCount = 0;
			
			for(int subIndex=0; subIndex < subSets.length; subIndex++) { 
				int subMax = 0;
				ArrayList<char[]> subSet = subSets[subIndex];	
				cSets[subIndex] = new ArrayList<char[]>(subSet.size());
				//newsubSets[subIndex] = new ArrayList<char[]>(subSet.size());
				
				for(char[] row: subSet) {
					if(row[index]==key) {
						cSets[subIndex].add(row);
						//subSets[subIndex].remove(subIndex);
						subMax++;		 
					}
				}
				if(subMax!=0) pureCount++;
    	   	if(subMaxVal < subMax) {
    	   		subMaxVal = subMax;
					subSetLoc = subIndex;
				}
    	   }
			
			if(pureCount == 1){ //Regardless of max depth has been reached or not, this will end the search for this node. 
				parent.Children.put(key, new TreeNode(OPClass.QKeys[subSetLoc], true,parent.Depth+1)); //Insert pure
				if(V>2) System.out.println("       Examining node "+NodeCount+" (depth="+(parent.Depth+1)+"): Node is Pure.");
				return null; //Make it so it does not split. 
			}
			else if(DL_On && parent.Depth+1 >= DL){ //Due to depth and/or no data, pick one highest count.
				if(subSetLoc == -1) parent.Children.put(parent.Leaf, new TreeNode(parent.Leaf, false, parent.Depth+1)); //no subdata, pick highest count in current state
				else parent.Children.put(key, new TreeNode(OPClass.QKeys[subSetLoc], false, parent.Depth+1)); 			  //Pick highest count of the subSets of dat. 
				if(V>2) System.out.println("       Examining node "+NodeCount+" (depth="+(parent.Depth+1)+"): Node is at max depth.");	
				return null;
			}
			else if(subSetLoc == -1) { //If no subData, pick highest global. 
				parent.Children.put(key, new TreeNode(parent.Leaf,false, parent.Depth+1));
				if(V>2) System.out.println("       Examining node "+NodeCount+"(depth="+(parent.Depth+1)+"): No Data Left: Picked Highest Probality.");
				return null; 
			}
			//subSets = newsubSets;
			TreeNode child = new TreeNode(parent.Depth+1);
			parent.Children.put(key, child);	
			return child;
		}
		
  //================================================================================
  //Stats and printing
		//Stats info just handles holding the trial time info, and accuracy info, does the calling for the printTree Method, and predict method. 
		
		public class StatInfo{
			double StartT;
			double Time;
			double[][] Acc; //0= train%trainav, 1=val&valAvg. Row 0 represents amount of predictions right 
 			int Trials;		//in most recent trial, and the its 2nd colum is sum of it. 2 row is the same but for vali
			
 			public StatInfo(int trials){
 				Acc = new double[2][2];
 				Trials = trials;
 				Time = 0;
 				StartT=0;
 			}
				
 			public void calcAcc() {
 				Acc[0][0] = predict(root, TrainSet,0); 
 				Acc[0][1]+=Acc[0][0];
 				
 							
 				Acc[1][0] = predict(root, ValSet, 0); 
 				Acc[1][1]+=Acc[1][0];
 				
 				Time+=((System.nanoTime()-StartT)/1_000_000_000);
 			}
 						
 			public void printTrialAcc() {
 				double trnError = (Acc[0][0]) / ((double) TrainSet.size());
 				double valError = (Acc[1][0]) / ((double) ValSet.size());
 				System.out.printf("      Training and validation accuracy:       %.5f %-5.5f\n",trnError,valError);
 			}
 			
 			public double printGroupAvg(boolean printTime){
 				double trnError = (Acc[0][1]) / ((float) (TrainSet.size() * Trials));
 				double valError = (Acc[1][1]) / ((float) (ValSet.size() * Trials));
 				System.out.print("  * Average accuracy across "+Trials+" trials\n");
 				System.out.printf("      Training and validation accuracy:       %.5f %-5.5f",trnError,valError);
 				if(printTime) System.out.printf("\n\n•› All Trials completed in %.5fs%n", Time);
 				System.out.println("");
 				double totalTime = Time;
 				Acc = new double[2][2];
 				Time = 0;
 				
 				return totalTime;
 			}
 			
 		
 			public void pt(){
 				System.out.println("----------------------------------"); 
			   System.out.println("* Final decision tree");
			   System.out.printf("Node: Split on [%s]\n", root.Attr.Name);
				printTree(root, 2);
 			}
 		}
		
		//This premenetively prunes recursion. 
		
		private void printTree(TreeNode node, int s) {
			  String attrName = node.Attr.Name;
			  HashMap<Character, String> lookUp = node.Attr.Meaning;
	        for (Map.Entry<Character, TreeNode> entry : node.Children.entrySet()) { //I check each branch and see if its a leaf or not. 
	            String meaning = lookUp.get(entry.getKey());
	            TreeNode child = entry.getValue(); 
	            System.out.printf("%"+s+"s Branch [%s]=[%s]\n", "", attrName, meaning); 	
	            if (child.Children != null) {
	            	System.out.printf("%"+(s+2)+"s Node: Split on [%s]\n", "", child.Attr.Name);
	            	printTree(child, s+4);
	            }
	            else System.out.printf("%"+(s+2)+"s Leaf: Predict [%s]\n", "", OPClass.Meaning.get(child.Leaf));  
	        }
			}
					
		private double predict(TreeNode node, ArrayList<char[]> set, double count) {
			if(node.IsLeaf) {
				char leaf = node.Leaf;
         	for(char[] row: set) if(row[YPos] == leaf) count++; //I count only dataum that is correctly classfied. row[YPos] is the always the output column!    
			}
			else {
				short aPos = node.Attr.Index;  
		      for(Map.Entry<Character, TreeNode> entry : node.Children.entrySet()) {
		      	char key = entry.getKey().charValue();
            	ArrayList<char[]> tmp = new ArrayList<char[]>();
					for(char[] row: set) if(row[aPos]==key) tmp.add(row); //prune dataset to only consists of data that has attribute with that specific key
            	if(tmp.size() != 0) count+=predict(entry.getValue(), tmp,0); //Then only recurse if its not empty. 
		      }
			}
			return count;
		}
		
		
		//This is a combination of both printTree and prediciton, that i used for debugging, and shows the amount right and wrong, and how many dataSets exist at each leaf. 
		//May not fully actaully calculate the right sum. 
		private double printTreePredictA(TreeNode node, ArrayList<char[]> set, int s, double count) {
			if(node.IsLeaf) {
				char leaf = node.Leaf;
				int right = 0, wrong = 0;
				System.out.printf("%"+(s+2)+"s Leaf: Predict [%s]\n", "", OPClass.Meaning.get(leaf)+""+leaf);        	
         	for(char[] row: set) {
         		if(row[YPos] == leaf) {
         			count++; right++;
         		}
         	}
         	for(char[] row: set) if(row[YPos] != leaf) wrong++;
         	System.out.printf("%"+(s+3)+"s Right=%d\n","", right);
         	System.out.printf("%"+(s+3)+"s Wrong=%d\n", "",wrong);	
			}
			else {
				short aPos = node.Attr.Index;  
				String attrName = node.Attr.Name;
				HashMap<Character, String> lookUp = node.Attr.Meaning;
		      for(Map.Entry<Character, TreeNode> entry : node.Children.entrySet()) {
		      	String meaning = lookUp.get(entry.getKey());
		      	char key = entry.getKey().charValue();
		      	TreeNode child = entry.getValue();
		         System.out.printf("%"+s+"s Branch [%s]=[%s]\n", "", attrName, meaning); 	
		         if(child.IsLeaf) System.out.printf("%"+(s+2)+"s Leaf: Predict [%s]\n", "", OPClass.Meaning.get(key)+""+key);     
		         else System.out.printf("%"+(s+2)+"s Node: Split on [%s]\n", "", child.Attr.Name);
            	ArrayList<char[]> tmp = new ArrayList<char[]>();
					for(char[] row: set) if(row[aPos]==key) tmp.add(row);
            	if(tmp.size() != 0) {
            		count+=printTreePredictA(child, tmp, s+4, 0);
            		System.out.printf("%"+(s+3)+"s return amount %f\n","",count); 
            	}
		      }
			}
			return count;
		}
		
		
		
		
	
		

		
		
		
		
		
}

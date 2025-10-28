import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.Random;
import java.util.stream.Collectors;

//Optional Optimizations. Transpose the data, so you read input vertically, and organize by output column. 
//This would allow you to parrel process this, and you would only have to keep track of where the outputs start and stop within the data. 



public class DecisionTree2 {
	static final double log2 = Math.log(2);
	static final boolean ExtraP = false;
	final boolean DL_On; 
	final int DL; 
	final byte V;
	TreeNode root;
	
	 Attribute[] Attrs;
	 Attribute OPClass;
	 int NodeCount;
	 
	  	 
	 int YPos;
	 char[][] FullSet;
	 ArrayList<char[]> valSet; 
	 LinkedList<char[]>[] SubSets;   //This dataset used in search, = full unless r, and is you navigate it by bounds of x. 
	 

  //================================================================================
  //Data structures for data and tree calculations.  
	 
	
		public DecisionTree2(String file, final int md, final byte v) throws Exception{
			DL = md == -1? 2147483647: md;
			DL_On = md != -1? true: false; 
			V = v;
			
			String line = "";  
			BufferedReader br = new BufferedReader(new FileReader(file));
			do line = br.readLine(); while(line.equals("")||!Character.isDigit(line.charAt(0))); //Skip until attribute size found. 
			YPos = Integer.parseInt(line);												   					 //I mark down output value indice, and attribute list size. 
			Attrs = new Attribute[YPos];
					
			
			for(short i=0; i<YPos; i++) Attrs[i] = new Attribute( i, br.readLine().split(":"), false);  //I set  each attribute
			do line = br.readLine(); while(line.equals("")||line.charAt(0) != ':');			 			   //I skip to output class
			OPClass = new Attribute((short)YPos, line.split(":"), true);									//I set my outPut class
			SubSets = new LinkedList[OPClass.Meaning.size()]; 

			ArrayList<String> parser = new ArrayList<String>(10000);  //I make a temp data structure to keep track of all Records
			do line = br.readLine(); while(line.isEmpty() || !Character.isLetterOrDigit(line.charAt(0))); //I skip to records.  
			do parser.add(line.replaceAll("\\s+", "")); while((line = br.readLine()) != null);    										 //Then I read all datum. 
			
		
			FullSet = new char[parser.size()][YPos];	//Then I intilize my orginal dataSet			
			for(int i=0; i< parser.size(); i++) FullSet[i] = parser.get(i).toCharArray(); //Then I make a char array out of it. 		
			br.close();
		}
		
		public void createSubData(int cgs) {
			ArrayList<char[]> org =new ArrayList<char[]>(Arrays.asList(FullSet).subList(0, cgs));		
			
			org.sort(Comparator.comparing(row -> row[YPos]));  	//I sort all the datum by their outputs
			Arrays.setAll(SubSets, i -> new LinkedList<char[]>()); // Create a new ArrayList for each element
			
			valSet = new ArrayList<char[]>((FullSet.length-cgs)+1);
			
			for(int i=0, c=0; c<cgs; i++) {
				char opKey = org.get(c)[YPos]; //outPut
				if(!(opKey == OPClass.QKeys[i])) continue;
				for(;c < cgs;c++) {
					if(org.get(c)[YPos] != opKey) break;
					else SubSets[i].add(org.get(c)); 
				}
			}
			for(int i=cgs; i< FullSet.length;i++) valSet.add(FullSet[i]);
		}
		
		public void randomize(int gp) {
			int dataSetLength = FullSet.length;								//Data row num
			Arrays.setAll(SubSets, i -> new LinkedList<char[]>()); 	// Create a new ArrayList for each element
			valSet = new ArrayList<char[]>((FullSet.length-gp)+1);   //Reintilization my valSet. 
			ArrayList<char[]> org = new ArrayList<char[]>();   //I make a new SubSets. 
			boolean[] prevSelected = new boolean[dataSetLength];  //This keeps track of if I previously selected the data. 
			Random rand = new Random();
			int r=0, rIndex = 0;
	
			while(r< gp) {
				rIndex = rand.nextInt(dataSetLength);
				if(!prevSelected[rIndex]) {
					prevSelected[rIndex] = true; 
					org.add(FullSet[rIndex]);
					
					r++; 
				}
			}

			
			org.stream()
		   	.filter(row -> row.length > YPos)  
		   	.sorted(Comparator.comparing(row -> row[YPos]))
		   	.collect(Collectors.toList());
			
			//org.sort(Comparator.comparing(row -> row[YPos])); //I sort all the datum by their outputs
		
			for(int i=0, c=0; c < gp && i < OPClass.QKeys.length ; i++) { 
				char opKey = org.get(c)[YPos]; //outPut
				if(!(opKey == OPClass.QKeys[i])) continue;
		   	for(; c < gp; c++) {
					if(org.get(c)[YPos] != opKey) break;
					else SubSets[i].add(org.get(c)); 
		   	}
			}
			for(int i=0; i< dataSetLength; i++) if(!prevSelected[i]) valSet.add(FullSet[i]);
			
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
				for(String item: parsed) {
					String meaning = item.length() > 1? item.substring(1): "";
					Meaning.put(item.charAt(0), meaning);			
				}
				int loc = 0;
				QKeys = new char[Meaning.size()];
				for(char key: Meaning.keySet()) QKeys[loc++] = key;
				if(Name.equals("O/P")) Arrays.sort(QKeys);
			}
		}
		
		private class TreeNode{
			int Depth;  								   //Keep track of nodes depth
			Attribute Attr; 								//The nodes attribute
			HashMap<Character, TreeNode> Children; //Keeps track of each option, and their repsective next path. 
			
			char Leaf; 				//Base value, if changed it indicates its a leaf. 
			boolean AllPure;
			
			public TreeNode() { //special constructor for root, boolean is passed through, so I have a pure null constructor for children. 
				NodeCount = 0;
				Depth = 0; 
				Children = new HashMap<>();
			}
			
			public TreeNode(int depth){
				Attr = null; 
				Depth = depth;
				Children = new HashMap<>();
				NodeCount++;
			}
			
			public TreeNode(char leaf, boolean pure, int depth) {
				Leaf = leaf;
				AllPure = pure;
				Depth = depth; 
				NodeCount++;
				Children = null;
			}
	}
		
		
  //================================================================================
  //Creating tree. 
	
		public void decisionTreeLearn() {
			ArrayList<Attribute> attrs = new ArrayList<>(Arrays.asList(Attrs));
			split(root = new TreeNode(), SubSets, attrs);	
			
			
			//reset node count
		}
				
		private void split(TreeNode node, LinkedList<char[]>[] subSets, ArrayList<Attribute> attrs) {
			if(V>2) System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+"): Node is splittable\n");
			float sum = maxProbality(node, subSets); 			 //Determines if nodes children are pure, finds largest output, and cacluates sum total of them.  
			if(node.AllPure) allPure(node, attrs.get(0));   //This assumes that the data has to end, and all data is pure. 
			else { 
				int[][] fullCount = importance(node, subSets, attrs, sum); //Finds most important node, removes its from list, and returns a use dataStruct
				if(attrs.isEmpty()||(DL_On && DL <= node.Depth)) handleProbality(node, fullCount); //I do this cache optimization, and reduced stack calls
				else {
					fullCount = null; //Deference for garabage colleciton. 
					for(char vKey: node.Attr.QKeys) {
						LinkedList<char[]>[] cSets =  new LinkedList[subSets.length];
						TreeNode child = handleChild(vKey,node, subSets, cSets, attrs);  //Creates DataSet, and determines if node should be split or not. 
						if(child != null) split(child, cSets, attrs);					    				 //I split it only if its needed. Else I prit its outcome. 
					}
				}
				attrs.add(node.Attr); //I add the attribute that previously removed.			
			}
		}
					
		private float maxProbality(TreeNode node, LinkedList<char[]>[] subSets) {
			float sum = 0; 
			short pureCount = 0; 
			LinkedList<char[]> globalMaxLoc = subSets[0];
			for(int i=0; i < subSets.length; i++) { 
				LinkedList<char[]> subSet = subSets[i];
				int currSize = subSet.size();
				if(globalMaxLoc.size() < currSize) globalMaxLoc =  subSet;
				if(currSize != 0) pureCount++;
				sum += currSize;
			}
			if(pureCount == 1) node.AllPure = true; 
			node.Leaf = globalMaxLoc.get(0)[YPos];
			return sum;
		}
		
		private int[][] importance(TreeNode node, LinkedList<char[]>[] subSets, ArrayList<Attribute> attrs, float sum) {
			int subLen = subSets.length;
			float[] Gain = {-1}, s0= {0}; //array varibles, for parallelization safety. 
			int[][][] fullCount = new int[1][][];
			for(LinkedList<char[]> subSet: subSets) s0[0]+=(subSet.size() /sum)*(Math.log(subSet.size()/sum)/log2);
			s0[0]*=-1;
		
			attrs.parallelStream().forEach(attribute -> {
				float remainder = 0;  
				int index = attribute.Index;
	    	   int[][] subEntropy = new int[subLen][123]; //Any hashmap of size 10 or more is larger than this, and runs risks of collisions. Furthermore access time is inferior to this.	    	   
	    	   for(int subIndex=0; subIndex < subLen; subIndex++) { //2 calculations, 1 array access to pointer, pointer to data structure data, then O(1) search, then pointer access to int. vs 1 array access. 
	    	   	int[] currEntropy = subEntropy[subIndex];
	    	   	LinkedList<char[]> subSet =  subSets[subIndex]; 
	    	   	for(char[] row: subSet) currEntropy[row[index]]++;  //Updates, attr x, values y, count for output x 
	    	   }
	    	   
  	   		for(char vKey: attribute.QKeys) {
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
		  	   synchronized(node) {
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
				
		private void allPure(TreeNode node, Attribute attr){
			node.Attr = attr;
			for(char vKey: attr.QKeys) { 
				node.Children.put(vKey, new TreeNode(node.Leaf, true,node.Depth+1));
				if(V>2) System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+1+"): Node Is Pure\n");
			}
		}
		
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
	
		private TreeNode handleChild(char key, TreeNode parent, LinkedList<char[]>[] subSets, LinkedList<char[]>[] cSets, ArrayList<Attribute> attrs){
			int index = parent.Attr.Index;
			int subSetLoc = -1, subMaxVal = 0;
			int pureCount = 0;
			
			for (int subIndex = 0; subIndex < subSets.length; subIndex++) { 
			    int subMax = 0;
			    cSets[subIndex] = new LinkedList<char[]>();
			    LinkedList<char[]> subSet = subSets[subIndex];            
			    
			    // Use an iterator to safely remove elements
			    Iterator<char[]> iterator = subSet.iterator();
			    while (iterator.hasNext()) {
			        char[] row = iterator.next();
			        if (row[index] == key) {
			            cSets[subIndex].add(row);
			            iterator.remove(); // Remove using the iterator
			            subMax++;		 
			        }
			    }

			    if (subMax != 0) {
			        pureCount++;
			    }
			    if (subMaxVal < subMax) {
			        subMaxVal = subMax;
			        subSetLoc = subIndex;
			    }
			}
			
			if(pureCount == 1){ //Regardless of max depth has been reached or not, this will end the search for this node. 
				parent.Children.put(key, new TreeNode(OPClass.QKeys[0], true,parent.Depth+1)); //Insert pure
				if(V>2) System.out.println("       Examining node "+NodeCount+" (depth="+(parent.Depth+1)+"): Node is Pure.");
				return null; //Make it so it does not split. 
			}
			else if(DL_On && parent.Depth+1 >= DL){ //Due to depth and/or no data, pick one highest count.
				if(subSetLoc == -1) parent.Children.put(key, new TreeNode(parent.Leaf, false, parent.Depth+1)); //no subdata, pick highest count in current state
				else parent.Children.put(key, new TreeNode(OPClass.QKeys[subSetLoc], false, parent.Depth+1)); 			  //Pick highest count of the subSets of dat. 
				if(V>2) System.out.println("       Examining node "+NodeCount+" (depth="+(parent.Depth+1)+"): ‹Node is at max depth.");	
				return null;
			}
			else if(subSetLoc == -1) { //If no subData, pick highest global. 
				parent.Children.put(key, new TreeNode(parent.Leaf,false, parent.Depth+1));
				if(V>2) System.out.println("       Examining node "+NodeCount+"(depth="+(parent.Depth+1)+"): No Data Left: Picked Highest Probality.");
				return null; 
			}
			TreeNode child = new TreeNode(parent.Depth+1);
			parent.Children.put(key, child);	
			return child;
		}
		
		
  //================================================================================
  //

		private void calculateAcc(){
			//int totalSize = Arrays.stream(SubSets).mapToInt(ArrayList::size).sum(); //This may be very innfiecnet but its cool! 
			float y=0;
			
			
			for(LinkedList<char[]> subSet: SubSets) {
				TreeNode tmp = root;
				
				
				for(char[] row: subSet) {
					TreeNode curr = tmp; 
					while(tmp.Children != null) curr = tmp.Children.get(row[tmp.Attr.Index]);
					
					
			
					
					
				}
				
			}
		
			
			
		}
		
		private void printTree(){
			
		}
		
		
		
		
//		private boolean baseCase(TreeNode node){
//			if(node.Leaf != '\0'){ 
//				if(V>2) {
//					System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+"): ");
//					if(node.AllPure) System.out.print("Node Is Pure|Unexpected\n");
//					else if(DL_On && node.Depth >= DL) System.out.print("Node is at max depth|Unexpected\n");	
//					else System.out.print("No data left to split, prediction chosen based off majority|Unexpected\n");
//					
//				}
//				return true;
//			}
//			else if(V>2) System.out.print("       Examining node "+NodeCount+" (depth="+node.Depth+"): Node is splittable\n");
//			if(ExtraP) System.out.println("‹›base Case Reached");
//			return false;
//		}


		
		
		
		//int totalSize = Arrays.stream(SubSets).mapToInt(ArrayList::size).sum(); //This may be very innfiecnet but its cool! 
		
		
		
		
		
}

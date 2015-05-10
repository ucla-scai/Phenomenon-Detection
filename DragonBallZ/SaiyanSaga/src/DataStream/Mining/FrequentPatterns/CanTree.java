package DataStream.Mining.FrequentPatterns;

/* Based on the paper: 
 * Carson Kai-Sang Leung, Quamrul I Khan, Tariqul Hoque  (2005) 
 * Cantree: A tree structure for efficient incremental mining of frequent patterns. 
 * In: Proceeding of the 2005 ICDM
 * 
 * Implemented by:
 * @author: Ariyam Das
 * Date: 2015-04-02
 * 
 * Description:
 * - 	CanTree is similar to FP-Tree except that ordering is done on lexicographic order.
 * 
 * -	Since, the ordering is already known, we will construct this CanTree
		by scanning the transaction database (provided as an input file) only once.
		Discarding infrequent items not done here in the CanTree construction.
		All items in a transaction are retained.
 *  
 * -	Most of the implementation is common to FP-Tree code base.
 * -	However, mining frequent patterns have some differences. 
 * 		1) In the prefix to the root, infrequent single ton items are discarded.
 * 		2) Avoid sorting the prefix again (because they are already sorted).
 * 
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Arrays;


public class CanTree {
	private ArrayList<FPTreeHeaderElement> header_table;
	private FPTreeNode cantree_root;
	private int support_threshold;
	private Hashtable<String,Integer> item_support;
	private boolean to_be_sorted;
	
	//performance measurement counters
	static int cantree_construction_calls = 0;		//CanTree construction from file
	static int cond_cantree_construction_calls = 0;	//conditional CanTree constructions
	static int cantree_mining_calls = 0;			//CanTree mining call
	static int single_path_mining_calls = 0;		//direct mining from single paths
	static int num_of_single_paths = 0;				//number of single paths encountered
	
	public CanTree() 
	{
		header_table = new ArrayList<FPTreeHeaderElement>();
		cantree_root = null;
		support_threshold = 0;
		item_support = new Hashtable<String,Integer>();
		to_be_sorted = false;
	}
	
	//This is to create the CanTree from an input file containing all the transactions
	public CanTree(File inputfile, int support)
	{
		//initializations
		this.header_table = new ArrayList<FPTreeHeaderElement>();
		this.cantree_root = new FPTreeNode();
		this.support_threshold = support;
		this.item_support = new Hashtable<String,Integer>();
		to_be_sorted = false;
				
		constructCanTree(inputfile);	//from all transactions in the input file
	}
	
	//This is to create the CanTree from an input file with all transactions starting at line no [start_at] and ending at line no [end_at]
	public CanTree(File inputfile, int support, int start_at, int end_at)
	{
		//initializations
		this.header_table = new ArrayList<FPTreeHeaderElement>();
		this.cantree_root = new FPTreeNode();
		this.support_threshold = support;
		this.item_support = new Hashtable<String,Integer>();
		to_be_sorted = false;
						
		constructCanTree(inputfile, start_at, end_at);	//from specific range of transactions in the input file
	}
	
	//This is to create the CanTree from a conditional pattern base
	public CanTree(ArrayList<String> cond_pattern_base, int support)
	{
		//initializations
		this.header_table = new ArrayList<FPTreeHeaderElement>();
		this.cantree_root = new FPTreeNode();
		this.support_threshold = support;
		this.item_support = new Hashtable<String,Integer>();
		to_be_sorted = false;
				
		constructCanTree(cond_pattern_base);
	}
	
	public FPTreeHeaderElement getHeaderElement(String item) 
	{
		FPTreeHeaderElement ret = null;
		for(int i=0; i<header_table.size(); i++)
			if(header_table.get(i).getItem().equals(item))
			{
				ret = header_table.get(i);
				break;
			}
		return ret;
	}
	
	public FPTreeHeaderElement getHeaderElement(int i) 
	{
		FPTreeHeaderElement ret = null;
		if(header_table.size()>i)
			ret = header_table.get(i);
		return ret;
	}
	
	public ArrayList<FPTreeHeaderElement> getFPTreeHeaderTable()
	{
		return this.header_table;
	}
	
	public FPTreeNode getRoot() 
	{
		return this.cantree_root;
	}
	
	public int getSupportThreshold()
	{
		return this.support_threshold;
	}
	
	public Hashtable<String,Integer> getItemSupport()
	{
		return this.item_support;
	}
	
	/*
	 * Construct CanTree from all transactions in the file.
	 */
	public void constructCanTree(File inputfile) 
	{		
		try 
		{
			firstScan(inputfile); //one and only scan
		} 
		catch(IOException ioe) 
		{
			System.out.println("Error! Scan for CanTree construction failed !");
			System.out.println(ioe.toString());
		}		
		cantree_construction_calls++;
	}
	
	/*
	 * Construct CanTree from specific range of lines (transaction_ids) in the input file.
	 */
	public void constructCanTree(File inputfile, int start_at, int end_at) 
	{		
		try 
		{
			firstScan(inputfile, start_at, end_at); //one and only scan
		} 
		catch(IOException ioe) 
		{
			System.out.println("Error! Scan for CanTree construction failed !");
			System.out.println(ioe.toString());
		}
		cantree_construction_calls++;
	}
	
	/* 
	 * CanTree construction from conditional pattern base.
	 */
	public void constructCanTree(ArrayList<String> cond_pattern_base) 
	{		
		firstScan(cond_pattern_base); 		//one and only scan	
		cond_cantree_construction_calls++;
	}	

	//traverses the pointers starting from the node link of the header element to reach the last node containing the item
	public FPTreeNode getLastFPTreeNode(String item)
	{
		FPTreeNode fptn = null;
		FPTreeHeaderElement elem = getHeaderElement(item);
		if(elem!=null)
			fptn = elem.getNodeLink();
		if(fptn!=null)
			while(fptn.getNextNode()!=null)
				fptn = fptn.getNextNode();
							
		return fptn;
	}
	
	/*
	 * Following function inserts a prefix into CanTree with the corresponding count.
	 * The prefix is provided as an array to the method.
	 * The prefix is already sorted. No need to sort it again. 
	 */
	public void insertIntoCanTree(String[] itm, int count)
	{
    	FPTreeNode tmp = cantree_root;	
    	for(int i=0; i<itm.length; i++)	//adding the prefix in CanTree now
    	{
    		String f_item = itm[i];		//get the frequent item
    		
    		if(!item_support.containsKey(f_item))					//when the item is encountered for the first time
    		{
    			header_table.add(new FPTreeHeaderElement(f_item));	//add it to the header table
    			item_support.put(f_item, count);					//add it to item support table as well
    			to_be_sorted = true;
    		}
    		else
    		{
    			int old_supp = item_support.get(f_item);
    			item_support.put(f_item,  old_supp+count);			//support value incremented by count
    		}
    		/* @P.S:
    		 * Note, header_table may be out of order! 
    		 * But it's OK! We will sort it before mining any frequent item sets from the CanTree
    		 */
    		
    		tmp.incrementFrequency(count);		//increment support of parent
    		if(tmp.isChild(f_item)==false) 		//if child not present, add it & update the node links from the header table
    		{
    			FPTreeNode new_child = tmp.addChild(f_item,0);	//child added
    			FPTreeNode last_node = getLastFPTreeNode(f_item);
    			if(last_node==null)				//first time this item is encountered
    			{
    				FPTreeHeaderElement hdrelem = getHeaderElement(f_item);
    				if(hdrelem!=null)
    					hdrelem.setNodeLink(new_child);
    			}
    			else
    				last_node.setNextNode(new_child);	//pointers updated, new child pointed by next node pointer
    		}
    		tmp = tmp.getChild(f_item);       		
    	}
    	if(tmp.getItem().equals("root")==false)
    		tmp.incrementFrequency(count);	//increment frequency of the last child
	}
	
	/*
	 * Following function removes a prefix from CanTree with the corresponding count.
	 * The prefix is provided as an array to the method.
	 * The prefix is already sorted. No need to sort it again. 
	 */
	public void removeFromCanTree(String[] itm, int count)
	{
    	FPTreeNode tmp = cantree_root;	
    	for(int i=0; i<itm.length; i++)	//removing the prefix from CanTree now
    	{
    		String f_item = itm[i];		//get the frequent item
    		
    		int old_supp = item_support.get(f_item);
    		item_support.put(f_item,  old_supp-count);		//support value decremented
    		
    		/* @P.S:
    		 * Note, we will not remove elements from header_table even when the frequency is 0. 
    		 * Similarly, CanTree nodes with 0 support are also not deleted.
    		 */
    		
    		tmp.incrementFrequency(-count);		//decrement support of parent
    		tmp = tmp.getChild(f_item);       	//get the immediate child	
    	}
    	if(tmp.getItem().equals("root")==false)
    		tmp.incrementFrequency(-count);		//decrement frequency of the last child
	}
	
	/*
	 * Return <total support> for individual item by looking up the hash table.
	 */
	public int getIndividualItemSupport(FPTreeHeaderElement elem) 
	{		
		return item_support.get(elem.getItem());
	}
		
	/* 	
	 * The only scan for CanTree construction.
	 */
	public void firstScan(File inputfile) throws IOException
	{       
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
        String transaction = null;
                
        while((transaction = br.readLine()) != null) //read input file (transaction database)
        {
        	String []items = transaction.split("\\s");					//split transaction into individual items        	
        	Arrays.sort(items);											//lexicographically sort items
        	
        	insertIntoCanTree(items,1);				//sorted items get added to the CanTree with support as 1
        }
        br.close(); //file reading complete 
	}

	//overloaded method - file reading done between lines [start_at] and [end_at]
	public void firstScan(File inputfile, int start_at, int end_at) throws IOException
	{       
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
        String transaction = null;
        int line_no = 0;
        
        while((transaction = br.readLine()) != null) //read input file (transaction database) from lines [start_at] & [end_at] (both inclusive)
        {
        	line_no++;
        	if(line_no < start_at)
        		continue;
        	else if(line_no <= end_at)
        	{
        		String []items = transaction.split("\\s");					//split transaction into individual items        	
            	Arrays.sort(items);											//lexicographically sort items
            	
            	insertIntoCanTree(items,1);				//sorted items get added to the CanTree with support as 1
        	}
        	else
        		break;       	
        }       
        br.close(); //file reading complete   
	}
			
	/* 
	 * Scan of transactions entered as conditional pattern base 
	 * to create the Can-Tree and its header table.
	 */
	public void firstScan(ArrayList<String> cond_pattrn_base)
	{       
		for(int n=0; n<cond_pattrn_base.size(); n++)
		{
			String prefx = cond_pattrn_base.get(n);
			String []tokens = prefx.split(":");			
			
			if(tokens.length != 2)	return;	//error-handling
			
			int count = Integer.parseInt(tokens[0]);	//frequency of occurrence of the prefix
			
			String pattern = tokens[1];					//get the actual pattern -> Now this pattern is already in lexicographic order
			String items[] = pattern.split("\\s");		//Just get the individual items, in the same lexicographic order (no sorting required)
			insertIntoCanTree(items, count);			//insert into Can-Tree in the same order
		}   
	}
	
	/*
	 * Trace the prefix from its immediate parent to the root (containing all the items separated by space).
	 * Also include its support. 
	 * The path traversed is upwards (reverse lexicographic order).
	 * So reverse the traced path.
	 * The returned string will be in the format <support>:<prefix>
	 * Also ignore infrequent items in the prefix.
	 */
	public String getPrefixEndingWith(FPTreeNode fptn)
	{
		String prefix = fptn.getFrequency()+":" ;	//colon as delimiter between support and prefix (list of items separated by space)
		
		String tmp;
		ArrayList<String> holder = new ArrayList<String>();
		
		fptn = fptn.getParent();
		tmp = fptn.getItem();
		
		if(tmp.equals("root"))
			return null;
		
		while(fptn != null && !(tmp.equals("root")))
		{
			if(item_support.get(tmp) >= support_threshold)	//we will only consider frequent items
				holder.add(tmp);
			fptn = fptn.getParent();
			tmp = fptn.getItem();
		}
		
		for(int i=holder.size()-1; i>=0; i--)
			prefix += holder.get(i) + " ";		//reversing the traced out path -> will be in lexicographic order
		
		return prefix;
	}
	
	/* 
	 * For a given header element, form its conditional pattern base by getting all its possible prefixes.
	 * Return all the prefixes in a array list.
	 */
	public ArrayList<String> getConditionalPatternBase(FPTreeHeaderElement elem) 
	{
		ArrayList<String> cond_patt_base;
		FPTreeNode fptn = elem.getNodeLink();
		
		if(fptn==null)	
			return null;
		else
			cond_patt_base = new ArrayList<String>();
		
		while(fptn!=null)
		{
			String prefix = getPrefixEndingWith(fptn);
			if(prefix!=null)
				cond_patt_base.add(prefix);
			fptn = fptn.getNextNode();
		}
		
		return cond_patt_base;
	}
	
	//following function returns true, if a given node and its descendants form a single branch
	public boolean isSingleBranch(FPTreeNode fptn)
	{
		int child_cnt;
		while((child_cnt = fptn.getChildrenCount()) == 1)
			fptn = fptn.getChild(0); 	//get the first and only child
		if(child_cnt>0)		//if the last node has more than 1 children, then not a single path
			return false;
		else
			return true;	//if last node is a leaf, then this is a single path
	}
	
	/* 
	 * Generate all possible combinations as frequent items from a single path
	 * E.g. a:3 -> b:2 -> c:1 will yield a:3, b:2, c:1, ab:2, ac:1, bc:1, abc:1 (2^n -1 items for n item path)
	 */	
	public void genAllCombinations(FPTreeNode fptn, String curr)
	{
		int freq = fptn.getFrequency();
		
		if(freq >= support_threshold)
		{
			String prev = curr;
			curr = prev + " " + fptn.getItem();
			
			System.out.print(curr + " : " + freq + "\n");	//print combination with support
			
			if(fptn.getChildrenCount()==1)
			{
				genAllCombinations(fptn.getChild(0), prev);		//combinations not including the item at this index
				genAllCombinations(fptn.getChild(0), curr);		//combinations including the item at this index
			}	
		}		
		single_path_mining_calls++;
	}
	

	/*
	 * Similar to the FP-Growth method for mining frequent patterns.
	 * Frequent Item sets with their respective supports will be printed through this method.
	 * 
	 * The frequent item sets mined from the CanTree will have support 
	 * greater than or equal to the support threshold of the CanTree (provided as argument in the constructor).
	 */
	public void minePatterns(String curr)
	{
		if(to_be_sorted)			//if new elements added or removed, sort them in the header list
		{
			Collections.sort(header_table);
			to_be_sorted = false;
		}
		
		if(this.isSingleBranch(cantree_root))	//if tree contains a single path, then generate all possible combinations as frequent item sets
		{
			if(cantree_root.getChildrenCount()==1)	
			{
				this.genAllCombinations(cantree_root.getChild(0), curr);	//leave out the root, start with child which has the 1st item	
				num_of_single_paths++;
			}
		}
		else
		{
			for(int i=header_table.size()-1; i>=0; i--)
			{
				FPTreeHeaderElement elmnt = header_table.get(i);
				int suppt = this.getIndividualItemSupport(elmnt);
				
				if(suppt<support_threshold)
					continue;
				
				String itemset = curr + " " + elmnt.getItem() + " : " + suppt;
				System.out.print(itemset + "\n");	//print frequent item set containing this element
				
				ArrayList<String> conditional_pattern_base = this.getConditionalPatternBase(elmnt);	//get the conditional pattern base
				CanTree subtree = new CanTree(conditional_pattern_base, support_threshold);	//create CanTree from this conditional pattern base
				String pattern = curr + " " + elmnt.getItem();	
				subtree.minePatterns(pattern);	//recursively mine the subtree
			}
		}		
		cantree_mining_calls++;
	}
	
	/*
	 * Overloaded method: When we want to mine the CanTree again with different support threshold.
	 */
	public void minePatterns(String curr, int new_support)
	{
		support_threshold = new_support;
		minePatterns(curr);	
	}

	
	//For Debugging => Pre-Order traversal of CanTree (node first and then all of its children)
	public void traverseCanTree(FPTreeNode node, int depth) 
	{
		for(int i=0; i<depth; i++)	//tabs printed as per the depth
			System.out.print("\t");
		System.out.print("-" + node + "\n");	//print node : its support : no. of children
		for(int i=0; i<node.getChildrenCount(); i++)
		{
			FPTreeNode tmp = node.getChild(i);
			traverseCanTree(tmp, depth+1);	//recursively print its child
		}
	}
		
	//For Debugging => Single Linked-List traversal of all elements in the header table, one by one
	public void traverseCanTreeHeaderTable()
	{
		for(int i=0; i<header_table.size(); i++)
		{
			System.out.print(header_table.get(i).getItem()+": "); //print item
			FPTreeNode tmp = header_table.get(i).getNodeLink();
			while(tmp!=null)
			{
				System.out.print("-> "+tmp);
				tmp = tmp.getNextNode();
			}
			System.out.print("\n");
		}	
	}
	

	public void printFunctionCallStats() {
		System.out.print("\nDirect CanTree Construction: \t" + cantree_construction_calls);
		System.out.print("\nConditional CanTree Constructions: \t" + cond_cantree_construction_calls);
		System.out.print("\nMining Frequent Itemsets calls: \t" + cantree_mining_calls);
		System.out.print("\nDirect mining from single path calls: \t" + single_path_mining_calls);
		System.out.print("\nNo. of single path: \t" + num_of_single_paths);
	}
	
	public void refreshFunctionCallStats() {
		
		cantree_construction_calls = 0;
		cond_cantree_construction_calls = 0;
		cantree_mining_calls = 0;
		single_path_mining_calls = 0;
		num_of_single_paths = 0;
	}
	
	public static void main(String args[]) 
	{
		File file = new File("data/T10I4D100K.dat");
		
		//long startTime, endTime, totalTime;  
		
		//startTime = System.currentTimeMillis();
		CanTree fpt = new CanTree(file,150,90001,100000);
		//endTime   = System.currentTimeMillis();
		//totalTime = endTime - startTime;
		//System.out.println(totalTime);
		
		//startTime = System.currentTimeMillis();
		fpt.minePatterns("");
		//endTime   = System.currentTimeMillis();
		//totalTime = endTime - startTime;
		//System.out.println(totalTime);
		
		//fpt.traverseCanTree(fpt.getRoot(), 0);
		//fpt.traverseCanTreeHeaderTable();
		//fpt.refreshFunctionCallStats();
		//fpt.printFunctionCallStats();
	}	
}

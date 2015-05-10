package DataStream.Mining.FrequentPatterns;

/* 
 * Generate the stream of transaction events.
 * Use EPL to query the streaming data. 
 * Add listener to receive the query results.
 * Analyze the results and perform necessary actions. 
 */

import com.espertech.esper.client.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class CanTreeItemsetMining extends BaselineItemsetMining {
	
	private CanTree cantree;
	
	public CanTreeItemsetMining(String filename, int window_size, int slide_size, int supp, int tag)
    {
    	super(filename, window_size, slide_size, supp, tag);	//super constructor of BaselineItemsetMining
    }
			
	/*
	 * Lexicographically sort the items in the input transaction.
	 * Return the sorted transaction.
	 */
	public String lexSortTransaction(String transaction)
	{
		String output = "";
		String []tokens = transaction.split("\\s+");
		Arrays.sort(tokens);
		for(int i=0; i<tokens.length; i++)
			output += tokens[i] + " ";
		
		return output;
	}
	
	/*
	 * We will override the existing method with only 1 slight change.
	 * 	- Transactions will be sorted in lexicographic order and then added to the window.
	 * 	- This will avoid re-sorting of the transaction again, 
	 * 	  when they are to be removed from the CanTree as the window slides over. 
	 */
	@Override
	public void initializeWindow(File file, int start_at, int end_at)
	{
		try 
    	{
    		FileInputStream fis = new FileInputStream(file);
    		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            int line_no = 0;
            
            while ((line = br.readLine()) != null) 
            {
            	line_no++;
            	if(line_no < start_at) 
            		continue;
            	else if(line_no <= end_at)
            		window_content.add(lexSortTransaction(line));	//add sorted transaction to the queue at the end
            	else
            		break;
            }	
            br.close();
            
            setWindowLength(end_at-(start_at-1));	//update window length
    	}
    	catch(IOException ioe)
    	{
    		System.out.println("Error! Cannot initialize window ! "+ioe.toString());
    	}	
	}
	
	/*
     * Overriding original method with one change
     * - CanTree construction is done after window initialization
     * The support will be set in the constructor, during class initialization before this method is invoked.
     * But anyways, we will call CanTree support mining by explicitly providing the support value again.
     * 
     */
	@Override
    public void streamInputData(EPRuntime cepRT, File file, int start_at, int end_at)
    {
    	initializeWindow(file, start_at, end_at);
    	cantree = new CanTree(file, support_val, start_at, end_at);
    	generateTransactions(cepRT, file, start_at, end_at);
    }
    
    /*
     * Overloaded method likewise modified. 
     */
	@Override
    public void streamInputData(EPRuntime cepRT, File file, int window_size)
    {
    	initializeWindow(file, 1, window_size);
    	cantree = new CanTree(file, support_val, 1, window_size);
    	generateTransactions(cepRT, file, 1, window_size);
    }
	
    @Override
	public void slideWindow(EventBean[] newData)
    { 	
    	String oldval, newval;
    	
    	for(int i=0; i<newData.length; i++)		// scan the new batch of arrived transactions
    	{
    		String recent_transaction = newData[i].getUnderlying().toString();	//format is {<item_list>=<name of items>}
    		recent_transaction = recent_transaction.replace("{", "");	//remove braces
    		recent_transaction = recent_transaction.replace("}", "");
    		String components[] = recent_transaction.split("=");		//extract only the names of the items
    		
    		if(!window_content.isEmpty())
    			oldval = window_content.poll();					// retrieve and delete the head (first element) of the queue
    		else
    			oldval = "";
    		
    		if(components.length == 2)
    			newval = lexSortTransaction(components[1]);		 // proper list of items, sorted in lexicographic order
    		else
    			newval = "";									//empty string
    		
    		window_content.add(newval);				// add newly arrived list of items, sorted in lexicographic order
    		
    		String []olditems = oldval.split("\\s+");
    		cantree.removeFromCanTree(olditems, 1);				//old prefix removed from CanTree
    		
    		String []newitems = newval.split("\\s+");
    		cantree.insertIntoCanTree(newitems, 1);				//new prefix added into the CanTree	
    	}
    	
    	cantree.minePatterns("", support_val);			//mine frequent patterns from the CanTree
    	
    	System.out.println("*** \t *** \t ***");		//window sliding completed   
    }
    
    public static void main(String[] args) throws IOException 
    {
    	int runs = 2;
    	for(int i=0; i<runs; i++)
    	{
    	/*
    	 * 1st argument: Input file
    	 * 2nd argument: Window Length
    	 * 3rd argument: Slide Length
    	 * 4th argument: Support
    	 */
    		new CanTreeItemsetMining("data/T10I4D100K.dat",20000,5000,1000,i);
    	}									
    } 
} 


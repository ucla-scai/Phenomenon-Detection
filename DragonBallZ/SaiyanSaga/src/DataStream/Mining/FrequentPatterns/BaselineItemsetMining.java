package DataStream.Mining.FrequentPatterns;

/* 
 * Generate the stream of transaction events.
 * Use EPL to query the streaming data. 
 * Perform necessary actions (baseline frequent item set mining through repeated FPTree constructions) on the query results. 
 */

import com.espertech.esper.client.*;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class BaselineItemsetMining {
	
	//We will extend this class and we want this attributes to be used by its children classes.
	protected LinkedList<String> window_content;
	protected int window_length;
	protected int slide_length;
	protected int support_val;
	
	public int getWindowLength()
	{
		return window_length;
	}
	
	public int getSlideLength()
	{
		return slide_length;
	}
	
	public int getSupportVal()
	{
		return support_val;
	}
	
	public void setWindowLength(int val)
	{
		this.window_length = val;
	}
	
	public void setSlideLength(int val)
	{
		this.slide_length = val;
	}
	
	public void setSupportVal(int val)
	{
		this.support_val = val;
	}
	
	public boolean isWindowFull()
	{
		return (window_length > 0 && window_length == window_content.size());
	}
	
	/*
	 * Window will contain transactions from file starting at line no #[start_at] and ending at line no #[end_at]
	 */
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
            		window_content.add(line);	//add to the queue at the end
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
	
	/* Method to generate data stream of transactions from the input file.
	 * Leave out specific range of lines from the input file; do not stream those lines.
	 */
    public void generateTransactions(EPRuntime cepRT, File file, int start_at, int end_at)
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
            	if(line_no >= start_at && line_no <= end_at)
            		continue;
            	else
            	{
            		Transaction tr = new Transaction(line_no, line);
            		cepRT.sendEvent(tr);
            	}
            }
            br.close();
    	}
    	catch(IOException ioe)
    	{
    		System.out.println("Error! Cannot stream transactions! "+ioe.toString());
    	}
    }
    
    /*
     * All transactions are within an input file resident on the disk.
     * Following method first initializes the window with the transactions between [start_at, end_at]
     * It streams rest of the transactions.
     * The window will slide as the new transactions arrive.
     * Physical window considered all throughout this project.
     */
    public void streamInputData(EPRuntime cepRT, File file, int start_at, int end_at)
    {
    	initializeWindow(file, start_at, end_at);
    	generateTransactions(cepRT, file, start_at, end_at);
    }
    
    /*
     * Overloaded method: 
     * 	- We initialize the window with the transactions at the beginning of the file.
     * 	- We stream all the remaining transactions afterwards.
     */
    public void streamInputData(EPRuntime cepRT, File file, int window_size)
    {
    	initializeWindow(file, 1, window_size);
    	generateTransactions(cepRT, file, 1, window_size);
    }
    
    /*
     * Window slides over as new transactions arrive.
     */
    public void slideWindow(EventBean[] newData)
    {   	
    	String newval = null;
    	
    	for(int i=0; i<newData.length; i++)		// scan the new batch of arrived transactions
    	{
    		String recent_transaction = newData[i].getUnderlying().toString();	//format is {<item list>=<name of items>}
    		recent_transaction = recent_transaction.replace("{", "");			//remove braces
    		recent_transaction = recent_transaction.replace("}", "");
    		String components[] = recent_transaction.split("=");				//we will extract only the names of the items
    		
    		if(!window_content.isEmpty())
    			window_content.remove();			//delete the head (first element) of the queue
    		
    		if(components.length == 2)
    			newval = components[1];				//proper list of items
    		else
    			newval = "";						//empty string
    		
    		window_content.add(newval);				//add newly arrived list of items
    	}  	
    	
    	trivialBaselineItemsetMining(support_val);	//trivial baseline item set mining done repeatedly at each window  	
    	
    	System.out.println("*** \t *** \t ***");	//print end of the slide
    }
 
    /*
     * The following method trivially constructs a FP-Tree from scratch whenever the window slides.
     * Every time a new FP-Tree is constructed, as the window slides and the frequent item sets are mined and returned from it.
     * This is the most trivial approach and forms the baseline.
     * Support value is provided as argument to this function.
     */
    public void trivialBaselineItemsetMining(int supp)
    {
    	if(!isWindowFull())		//error-handling, if the window is not yet full, then skip
    		return;
    	
    	ArrayList<String> transaction_base = new ArrayList<String>(window_content); 
    	for(int i=0; i<window_length; i++)
    	{
    		String tmp = "1:" + transaction_base.get(i);	//we will be using FP-Tree construction from conditional pattern base, hence support added as prefix
    		transaction_base.set(i,tmp);
    	}
    	
    	FPTree fpt_win = new FPTree(transaction_base, supp);
    	fpt_win.minePatternsByFPGrowth("");	
    }
    
    /*
     * Run the following EPL query:
     * SELECT item_list
     * FROM Transaction.win:length_batch([slide size])
     */
    public String getEPLQuery(int slide_size)
    {
    	setSlideLength(slide_size);
    	
    	String query = "SELECT item_list FROM " + Transaction.class.getName() + ".win:length_batch(" + slide_length + ")";
    	return query;
    }
    
    //overloaded method, without any argument
    public String getEPLQuery()
    { 	    	
    	String query = "SELECT item_list FROM " + Transaction.class.getName() + ".win:length_batch(" + slide_length + ")";
    	return query;
    }
    
    public void initConfig(String filename, int window_size, int slide_size, int supp, int tag)
    {
    	//The Configuration is meant only as an initialization-time object.
    	Configuration cepConfig = new Configuration();
        cepConfig.addEventType(Transaction.class.getName(), Transaction.class.getName());
        EPServiceProvider cep = EPServiceProviderManager.getProvider("CEPEngine"+tag, cepConfig);	//make sure CEPEngine name is unique
        
        EPRuntime cepRT = cep.getEPRuntime(); 	//runtime environment
        EPAdministrator cepAdm = cep.getEPAdministrator();	//system configuration
        
        File inputfile = new File(filename);
        setSupportVal(supp);		//support threshold is set for mining frequent item sets
                
        String output_filename = filename + "_" + window_size + "_" + slide_size + "_" + supp + ".result";
        EPStatement cepStatement = cepAdm.createEPL(getEPLQuery(slide_size));	//this will internally call setSlideLength(slide_size)
        cepStatement.addListener(new CEPListener(this,output_filename));
        
        streamInputData(cepRT,inputfile,window_size);		//this will internally call setWindowLength(window_size)
    }
    
    //overloaded method
    public void initConfig(String filename, int start_at, int end_at, int slide_size, int supp, int tag)
    {
    	//The Configuration is meant only as an initialization-time object.
    	Configuration cepConfig = new Configuration();
        cepConfig.addEventType(Transaction.class.getName(), Transaction.class.getName());
        EPServiceProvider cep = EPServiceProviderManager.getProvider("CEPEngine"+tag, cepConfig);	//make sure CEPEngine name is unique
        
        EPRuntime cepRT = cep.getEPRuntime(); 	//runtime environment
        EPAdministrator cepAdm = cep.getEPAdministrator();	//system configuration
        
        File inputfile = new File(filename);
        setSupportVal(supp);		//support threshold is set for mining frequent item sets
        
        String output_filename = filename + "_" + start_at+"-"+end_at + "_" + slide_size + "_" + supp + ".result";
        EPStatement cepStatement = cepAdm.createEPL(getEPLQuery(slide_size));	//this will internally call setSlideLength(slide_size)
        cepStatement.addListener(new CEPListener(this,output_filename));
        
        streamInputData(cepRT,inputfile,start_at,end_at);		//this will internally call setWindowLength(window_size)
    }
    
    public BaselineItemsetMining(String filename, int window_size, int slide_size, int supp, int tag)
    {
    	window_content = new LinkedList<String>();   	
    	initConfig(filename,window_size,slide_size,supp,tag);
    }
    
    //overloaded constructor
    public BaselineItemsetMining(String filename, int start_at, int end_at, int slide_size, int supp, int tag)
    {
    	window_content = new LinkedList<String>();
    	initConfig(filename,start_at,end_at,slide_size,supp,tag);
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
    		new BaselineItemsetMining("experiments/T10I4D100K",20000,5000,1000,i);
    	}
    }
}



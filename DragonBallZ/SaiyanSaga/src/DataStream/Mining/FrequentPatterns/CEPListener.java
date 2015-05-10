package DataStream.Mining.FrequentPatterns;

/*  
 * Add listener to receive the EPL query results.
 * Call method to analyze the results and perform necessary actions. 
 */

import com.espertech.esper.client.*;
import java.io.*;

/* 
 * Listener for the incoming data stream
 */
public class CEPListener implements UpdateListener {

	BaselineItemsetMining db_engine;
	long slide_no;
	long running_time;
	String output_filename;
	
	public CEPListener(BaselineItemsetMining db_engine, String filename)
	{
		this.db_engine = db_engine;
		slide_no = 0;
		running_time = 0;
		output_filename = filename;
	}
	
	public void logData(String text)
	{
    	try 
    	{
    		BufferedWriter out = new BufferedWriter(new FileWriter(output_filename,true));
    		out.append(text);
    		out.close();
    	}
    	catch ( IOException e )
    	{
    		e.printStackTrace();
    	}
	}
	
    public void update(EventBean[] newData, EventBean[] oldData) 
    { 
    	slide_no++;				//new slide -> so increment slide no.
    	
    	/*
    	 * Calculate time to process new slide.
    	 */
    	long startTime = System.currentTimeMillis();
    	db_engine.slideWindow(newData);
    	long endTime = System.currentTimeMillis();
    	
    	running_time += (endTime - startTime);
    	
    	logData(slide_no+" "+running_time+"\n");	//log computed time
    }   
}
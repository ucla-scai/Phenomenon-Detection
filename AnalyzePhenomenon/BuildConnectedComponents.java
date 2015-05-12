import java.util.*;
import java.io.*;

/*
 - java BuildConnectedComponents [input filename located at data/itemsets] [output file to be generated at data/phenomenon]
 - input file:  This file will contain all the list of frequent itemsets.
	        Each frequent item set is written in a single line with their respective support in parentheses.  
 - output file: Prints all the hashtags related to a phenomenon. 
		All related hashtags for a phenomenon appear in a single line.
		Line ends with an asterisk.
*/

public class BuildConnectedComponents {

	public static void main(String args[]) {

		if(args.length !=2 )
		{
			System.out.println("Two arguments required - Input filename & Output filename");
			System.exit(1);
		}

		final int ubound = 5000; 		//assuming input FI file will never contain more than 'ubound' lines

		String phendir = "data/phenomenon/";		
		try {
			File opdir = new File(phendir);
			opdir.mkdirs();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		} 
		
		String inputfile = "data/itemsets/" + args[0];
		String outputfile = phendir + args[1];

		Hashtable<String,Integer> ht = new Hashtable<String,Integer>();
		String tags[] = new String[ubound];
		int comps[] = new int[ubound];
		int comp_id=0;	//component id	

		try 
    		{
    			FileInputStream fis = new FileInputStream(new File(inputfile));
    			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            		String rec = null;

			while ((rec = br.readLine()) != null) 
            		{
				if(rec.length()==0)	continue;	//newline

				String txt[] = rec.split("\\s+");

				comps[comp_id] = comp_id;	
				String unseen_tags = "";

				for(int i=0; i<txt.length-1; i++) 	//leave out last token, do not consider the support at the end 
				{					
					if(!ht.containsKey(txt[i]))	//if hashtag not seen before
					{
						ht.put(txt[i],comp_id);		//add it with current component id
						unseen_tags+=txt[i]+" ";	//append unseen hashtag
					}
					else
					{
						int old_comp_id = (int)ht.get(txt[i]);
						while(old_comp_id != comps[old_comp_id])
							old_comp_id = comps[old_comp_id];
						comps[old_comp_id] = comp_id;
					}					
				}
				tags[comp_id] = unseen_tags;
				comp_id++;
			}	
            		br.close();
                }
    		catch(Exception e)
    		{
			e.printStackTrace();
			System.exit(1);
    		}

		Hashtable<Integer,String> comp_distr = new Hashtable<Integer,String>();

		for(int i=comp_id-1; i>=0; i--)
		{
			if(comps[i] == i)
			{
				String tmp = i+" ";
				comp_distr.put(i,tmp);
			}
			else
			{
				int x = comps[i];
				int y = comps[x];
				comps[i] = y;
				String tmp = (String)comp_distr.get(y);
				tmp += i+" ";
				comp_distr.put(y,tmp);
			}
		}

		try {
            		File file = new File(outputfile);
            		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

			for (Integer key : comp_distr.keySet())
	 		{
    				String e[] = (comp_distr.get(key)).split("\\s+");
				for(int i=0; i<e.length; i++)
				{
					int j = Integer.parseInt(e[i]);
					bw.append(tags[j]);
				}
				bw.append("*\n");
			}
            		bw.close();

		} catch(IOException ioe) {
			ioe.printStackTrace();
		}				
	}
}

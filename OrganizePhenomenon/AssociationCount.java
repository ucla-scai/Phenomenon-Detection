import java.util.*;
import java.io.*;

/*
 - java AssociationCount
	[input file path containing consolidated phenomenon] 
	[input file containing all hashtags over the same period as consolidated phenomenon]
	[output dir]
 - in global consolidated phenomenon file, we are bothered with phenomenon having length > 1
*/

public class AssociationCount {

	public static void main(String args[]) {

		if(args.length != 3 )
		{
			System.out.println("Improper number of arguments !");
			System.exit(1);
		}

		Hashtable<String,Integer> single_tag = new Hashtable<String,Integer>();
		Hashtable<String,Integer> biword_tag = new Hashtable<String,Integer>();

		// 1. Read global consolidated phenomenon
		try 
    		{
    			FileInputStream fis = new FileInputStream(new File(args[0]));
    			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            		String rec = null;

			while ((rec = br.readLine()) != null) 
            		{
				String txt[] = rec.split("\\s+");

				for(int i=0; i<txt.length; i++) 	
					single_tag.put(txt[i],0);		

				for(int i=0; i<txt.length; i++) 	
				for(int j=0; j<txt.length; j++) 	
				{
					if(i==j) continue;
					String t = txt[i] + "-" + txt[j];
					biword_tag.put(t,0);
				}
			}	
            		br.close();
                }
    		catch(Exception e)
    		{
			e.printStackTrace();
			System.exit(1);
    		}

		// 2. Read all tweets with hashtags
		try 
    		{
    			FileInputStream fis = new FileInputStream(new File(args[1]));
    			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            		String rec = null;

			ArrayList<String> elem = new ArrayList<String>();

			while ((rec = br.readLine()) != null) 
            		{
				String txt[] = rec.split("\\s+");

				elem.clear();
				for(int i=0; i<txt.length; i++) 	
					if(single_tag.containsKey((txt[i])))
						elem.add((txt[i]));

				int cnt = elem.size();
				for(int i=0; i<cnt; i++) 
				{
					boolean flag = true;
					for(int j=0; j<cnt; j++) 
					{
						if(i==j) continue;
						String t = elem.get(i) + "-" + elem.get(j);
						if(biword_tag.containsKey(t))
						{
							flag = false;
							int val = biword_tag.get(t);
							biword_tag.put(t,val+1);
						}
					}

					if(flag==true)
					{
						int val = single_tag.get((elem.get(i)));
						single_tag.put((elem.get(i)),val+1);
					}
				}
			}	
            		br.close();
                }
    		catch(Exception e)
    		{
			e.printStackTrace();
			System.exit(1);
    		}
		
		// 3. Generate bi-word association file
		try {
			Enumeration<String> keys = biword_tag.keys();

            		File file = new File(args[2] + "/" + "biword_assoc.dat");
            		BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));

        		while(keys.hasMoreElements())
			{
            			String key = keys.nextElement();
            			bw.write(key + " " + biword_tag.get(key) + "\n");
        		}

            		bw.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

		// 4. Generate monoword association file
		try {
			Enumeration<String> keys = single_tag.keys();

            		File file = new File(args[2] + "/" + "monoword_assoc.dat");
            		BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));

        		while(keys.hasMoreElements())
			{
            			String key = keys.nextElement();
            			bw.write(key + " " + single_tag.get(key) + "\n");
        		}

            		bw.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

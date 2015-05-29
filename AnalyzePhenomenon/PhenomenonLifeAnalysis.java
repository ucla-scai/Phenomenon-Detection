import java.util.*;
import java.io.*;

/*
 - java PhenomenonLifeAnalysis 
	[input file path containing consolidated phenomenon] 
	[input dir comprising local daily phenomenon]
	[output dir path where the analysis results will be stored]
 - in global consolidated phenomenon file, we are bothered with phenomenon having length > 1
*/

public class PhenomenonLifeAnalysis {

	public static void main(String args[]) {

		if(args.length != 3 )
		{
			System.out.println("Three arguments required !");
			System.exit(1);
		}

		String infile_consol_phenm, daily_phenm, opdir;
		infile_consol_phenm = "" + args[0];
		daily_phenm = "" + args[1];

		opdir = "" + args[2];
		try {
			File opd = new File(opdir);
			opd.mkdirs();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		} 

		int num_phenm = 0;
		
		Hashtable<String,Integer> phenm_trck = new Hashtable<String,Integer>();
		Hashtable<String,Boolean> discovered_hashtag = new Hashtable<String,Boolean>();

		int phen_evol[][] = new int[900][91]; //for tracking evolution of max 900 phenomenon over 90 days
		for(int i=0; i<900; i++)	//phenomenon i
		for(int j=0; j<91; j++)		//day j
			phen_evol[i][j]=-1;	//-1 will represent that this phenomenon has not even started
						//0 will indicate that old hashtags for this phenomenon was encountered
						//>0 indicates number of new hashtags encountered

		try 
    		{
    			FileInputStream fis = new FileInputStream(new File(infile_consol_phenm));
    			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            		String rec = null;

			while ((rec = br.readLine()) != null) 
            		{
				String txt[] = rec.split("\\s+");
				num_phenm++;

				for(int i=0; i<txt.length; i++) 	
				{
					phenm_trck.put(txt[i],num_phenm);		
					discovered_hashtag.put(txt[i],false);
				}
			}	
            		br.close();
                }
    		catch(Exception e)
    		{
			e.printStackTrace();
			System.exit(1);
    		}

		for(int i=1; i<=90 ;i++)
		{
			String infile = daily_phenm + "/" + i + ".dat";
			try 
    			{
    				FileInputStream fis = new FileInputStream(new File(infile));
    				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            			String rec = null;

				while ((rec = br.readLine()) != null) 
            			{
					String txt[] = rec.split("\\s+");

					if(!phenm_trck.containsKey(txt[0]))	
						continue;	//single element phenomenon to be ignored

					int ph_id = phenm_trck.get(txt[0]); 
					int cnt=0;
	
					for(int k=0; k<txt.length-1; k++) //leave out last asterisk
					{
						boolean bool = discovered_hashtag.get(txt[k]);
						if(bool==false)
						{
							discovered_hashtag.put(txt[k],true);
							cnt++;
						}
					}

					phen_evol[ph_id][i] = cnt;
				}	
            			br.close();
                	}
    			catch(Exception e)
    			{
				e.printStackTrace();
				System.exit(1);
    			}
		}

		try {
			String opfile1 = opdir + "/" + "phen_lifetime.dat";
			String opfile2 = opdir + "/" + "delta_phen.dat";
            		File file1 = new File(opfile1);
            		File file2 = new File(opfile2);
            		BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1, true));
            		BufferedWriter bw2 = new BufferedWriter(new FileWriter(file2, true));

			for(int p=1; p<=num_phenm; p++)
			{
				bw1.append(getLifetime(phen_evol,p));	
				bw2.append(deltaPhen(phen_evol,p));	
			}

            		bw1.close();
            		bw2.close();

		} catch(IOException ioe) {
			ioe.printStackTrace();
		}				
	}

	// following method performs lifetime analysis of a given phenomenon
	public static String getLifetime(int arr[][], int x)
	{
		int lastday=-1, firstday=100;

		for(int col=90; col>=1; col--)
			if(arr[x][col]>=0)
			{
				if(col>lastday)
					lastday=col;
				if(col<firstday)
					firstday=col;
			}

		String ret = "Id:"+x+" FDay:"+firstday+" LDay:"+lastday+" Dur:"+(int)(lastday-firstday)+"\n";
		return ret;
	}

	// following method checks which are the days (after day 0), when new hashtags (and how many) were added to a given phenomenon
	public static String deltaPhen(int arr[][], int x)
	{
		String ret="";

		int firstday=100;
		for(int col=1; col<=90; col++)
			if(arr[x][col]>0)
			{
				if(col<firstday)
					firstday=col;
				else
				{
					ret += "Id:"+x+" Day:"+(int)(col-firstday)+" Len:"+arr[x][col]+"\n";	
				}
			}

		return ret;
	}
}

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

/*
 - java SplitByTime [start time] [slide window length]
 - java SplitByTime 2012/10/01-12:10:25 1-12-30-45 
 - means slide length of 1 day 12 hrs 30 mins and 45 secs
*/
public class SplitByTime {

	public static void main(String args[]) {
		int days, hrs, mins, secs;
		long slide_window_length=0L, endpoint=0L;

		SimpleDateFormat starttimeformat = new SimpleDateFormat("yyyy/MM/dd-hh:mm:ss");
		SimpleDateFormat recformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		if(args.length != 2) 
		{
			System.out.println("Improper number of arguments!");
			System.exit(1);
		}
		else
		{
			Date datetmp = null;
			try {
				datetmp = starttimeformat.parse(args[0]);
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			String val[] = (args[1]).split("-"); 		
			days = Integer.parseInt(val[0]);
			hrs = Integer.parseInt(val[1]);
			mins = Integer.parseInt(val[2]);
			secs = Integer.parseInt(val[3]); 

			slide_window_length = days*24*60*60 + hrs*60*60 + mins*60 + secs;

			endpoint = datetmp.getTime()/1000 + slide_window_length;
		}

		String timesplitdir = "data/timesplit/"+args[1]+"/";		
		try {
			File opdir = new File(timesplitdir);
			opdir.mkdirs();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		} 

		String inputfile = "data/input/tweets_hashtags.dat";
		try 
    		{
    			FileInputStream fis = new FileInputStream(new File(inputfile));
    			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            		String rec = null;
			int slides = 1;
            		
			while ((rec = br.readLine()) != null) 
            		{
				String txt[] = rec.split("\\s+");

				Date dt = recformat.parse(txt[0]+" "+txt[1]);

				if(dt.getTime()/1000 > endpoint)
				{
					slides++;
					endpoint+=slide_window_length;
				}

				String outputfile = timesplitdir+slides+".dat";
				appendToFile(outputfile, txt);					
	            	}
	
            		br.close();

			System.out.println("total files created: "+ slides);
                }
    		catch(Exception e)
    		{
			e.printStackTrace();
			System.exit(1);
    		}		
	}

	public static void appendToFile(String filename, String text[]) {
		try {
            		File file = new File(filename);
            		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			for(int i=2; i<text.length; i++)
            			bw.append(((text[i]).substring(1)).toLowerCase()+" ");
			if(text.length>2)
				bw.append("\n");
            		bw.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

package libs;

import java.util.ArrayList;

public class HaarCoefs {
	
	private int levels;
	public ArrayList<Long> readingShares;
	public ArrayList<ArrayList<Long>> haHaarCoefs;
	public ArrayList<ArrayList<Long>> gaHaarCoefs;
	
	/*
	 * 
	 */
	public HaarCoefs(int pLevel, ArrayList<Long> pReadingShares)
	{
		levels = pLevel;
		readingShares = pReadingShares;
		haHaarCoefs = new ArrayList<ArrayList<Long>>();
		gaHaarCoefs = new ArrayList<ArrayList<Long>>();
	
	}
	
	/*
	 * By default the coefficients upto 3 levels will be considered by default
	 */
	public HaarCoefs(ArrayList<Long> pReadingShares)
	{
		this(3, pReadingShares);
	}
	
	/*
	 * This function computes the coefficients and populate the respective fields
	 */
	public void computeHaarCoefs()
	{
		int index = 0;
		ArrayList<Long> tempList = readingShares;
		ArrayList<Long> haTempList = null;
		ArrayList<Long> gaTempList = null;
		double tempValue = 0.0;
		long finalValue = 0;
		
		for(int levelCount = 0; levelCount < levels; levelCount++)
		{
			haTempList = new ArrayList<Long>();
			gaTempList = new ArrayList<Long>();
			
			haHaarCoefs.add(haTempList);
			gaHaarCoefs.add(gaTempList);
			
			for (int i = 0; i < (tempList.size()/2);i++)
			{
				tempValue = (double)(tempList.get(2 * i) + tempList.get(2 * i + 1));
				finalValue = (long)Math.round(tempValue);
				haTempList.add(finalValue);				

				tempValue = (double)(tempList.get(2 * i) - tempList.get(2 * i + 1));
				finalValue = (long)Math.round(tempValue);
				gaTempList.add(finalValue);
			}
			
			tempList = gaHaarCoefs.get(index);
			index++;
		}
	}

}

package messages;

import java.util.BitSet;
import java.util.ArrayList;

public class DInfo extends MessageInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<BitSet> dForMTs;
	public int maxPackSize;
	
	public DInfo()
	{
		dForMTs = new ArrayList<>();
		maxPackSize = 0;
	}
	
	public DInfo(ArrayList<BitSet> pDForMTs, int pMaxPackSize)
	{
		dForMTs = pDForMTs;
		maxPackSize = pMaxPackSize;
	}
}
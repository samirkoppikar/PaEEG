package messages;

import java.math.BigInteger;
import java.util.ArrayList;

public class DforMTInfo extends MessageInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<BigInteger> dForMTs;
	public int maxPackSize;
	
	public DforMTInfo()
	{
		dForMTs = new ArrayList<>();
		maxPackSize = 0;
	}
	
	public DforMTInfo(ArrayList<BigInteger> pDForMTs, int pMaxPackSize)
	{
		dForMTs = pDForMTs;
		maxPackSize = pMaxPackSize;
	}
}

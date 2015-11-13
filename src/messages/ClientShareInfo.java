package messages;

import java.util.ArrayList;

public class ClientShareInfo extends MessageInfo {
	public ArrayList<Long> shares;
	public int bitLength;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ClientShareInfo()
	{
		shares = new ArrayList<>();
		bitLength = 28;
	}
	
	public ClientShareInfo(ArrayList<Long> pShares, int pBitLength)
	{
		shares = pShares;
		bitLength = pBitLength;
	}

}

package messages;

import java.math.BigInteger;
import java.util.ArrayList;

public class EFSharesInfo extends MessageInfo {

	public ArrayList<BigInteger> eShares;
	public ArrayList<BigInteger> fShares;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EFSharesInfo()
	{
		
	}
	
	public EFSharesInfo(ArrayList<BigInteger> pESHares, ArrayList<BigInteger> pFShares)
	{
		eShares = pESHares;
		fShares = pFShares;
	}
	
}

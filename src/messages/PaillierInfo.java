package messages;

import libs.Paillier;

public class PaillierInfo extends MessageInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Paillier p;
	
	public PaillierInfo()
	{
		
	}
	
	public PaillierInfo(Paillier pP)
	{
		p = pP;
	}
}

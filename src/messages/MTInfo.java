package messages;

import java.util.ArrayList;

import classes.Triple;

public class MTInfo extends MessageInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Triple> encTripleList;
	
	public MTInfo()
	{
		
	}
	
	public MTInfo(ArrayList<Triple> pEncTripleList)
	{
		encTripleList = pEncTripleList;
	}
}

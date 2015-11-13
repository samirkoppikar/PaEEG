package messages;


import java.io.Serializable;

import constants.MESSAGE_TYPE;



public class MessageInfo implements Serializable {
	public long bytesTransferred;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int transactionNo;
	private MESSAGE_TYPE msgType;
	
	public MessageInfo()
	{
		
	}
	
	public MessageInfo(int pTransactionNo)
	{
		this.transactionNo = pTransactionNo;
	}
	
	//getters	
	public int getTransactionNo()
	{
		return transactionNo;
	}
	
	public MESSAGE_TYPE getMessageType()
	{
		return msgType;
	}
	
	
	//setters	
	public void setMessageType(MESSAGE_TYPE pMsgType)
	{
		this.msgType = pMsgType;
	}
	
}


package constants;


import java.io.Serializable;

public enum MESSAGE_TYPE implements Serializable {
	DistanceCalculationBegin (0),
	DistanceCalculationEnd (1),
	Exit (2),
	Result (3),
	Sync (4),
	ArithmeticMTBegin (5),
	ArithmeticMTEnd (6),
	Paillier (7),
	ASharingBegin (8),
	ASharingEnd (9),
	BitLength (10);
	
	private int val;
	
	private MESSAGE_TYPE(int pVal)
	{
		this.val = pVal;
	}
	
	public int getVal()
	{
		return val;
	}
	
	public static MESSAGE_TYPE parseMESSAGE_TYPE(String pVal) throws Exception
	{
		if(pVal.equalsIgnoreCase("0"))
		{
			return MESSAGE_TYPE.DistanceCalculationBegin;
		}
		else if(pVal.equalsIgnoreCase("1"))
		{
			return MESSAGE_TYPE.DistanceCalculationEnd;
		}
		else if(pVal.equalsIgnoreCase("2"))
		{
			return MESSAGE_TYPE.Exit;
		}
		else if(pVal.equalsIgnoreCase("3"))
		{
			return MESSAGE_TYPE.Result;
		}
		else if(pVal.equalsIgnoreCase("4"))
		{
			return MESSAGE_TYPE.Sync;
		}
		else if(pVal.equalsIgnoreCase("5"))
		{
			return MESSAGE_TYPE.ArithmeticMTBegin;
		}
		else if(pVal.equalsIgnoreCase("6"))
		{
			return MESSAGE_TYPE.ArithmeticMTEnd;
		}
		else if(pVal.equalsIgnoreCase("7"))
		{
			return MESSAGE_TYPE.Paillier;
		}
		else if(pVal.equalsIgnoreCase("8"))
		{
			return MESSAGE_TYPE.ASharingBegin;
		}
		else if(pVal.equalsIgnoreCase("9"))
		{
			return MESSAGE_TYPE.ASharingEnd;
		}
		else if(pVal.equalsIgnoreCase("10"))
		{
			return MESSAGE_TYPE.BitLength;
		}
		else
		{
			throw new Exception("Unknown MESSAGE_TYPE");
		}
	}
}


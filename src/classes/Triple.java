package classes;

import java.io.Serializable;
import java.math.BigInteger;

public class Triple implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BigInteger a;
	public BigInteger b;
	public BigInteger c;
	public BigInteger rand;
	
	public Triple()
	{
		a = BigInteger.ZERO;
		b = BigInteger.ZERO;
		c = BigInteger.ZERO;
		rand = BigInteger.ZERO;
	}
	
	public Triple(BigInteger pA, BigInteger pB, BigInteger pC)
	{
		this.a = pA;
		this.b = pB;
		this.c = pC;
		rand = BigInteger.ZERO;
	}
}

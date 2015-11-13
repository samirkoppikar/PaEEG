package classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;

import constants.MESSAGE_TYPE;
import libs.Communicator;
import libs.HaarCoefs;
import libs.Paillier;
import messages.ClientShareInfo;
import messages.DforMTInfo;
import messages.EFSharesInfo;
import messages.MTInfo;
import messages.MessageInfo;
import messages.PaillierInfo;

/*
 * This is the server or third party without packing
 */
public class iParty {
	
	public static long clientSharesBytes;
	public static long MTCalculationProtocolSentBytes;
	public static long MTCalculationProtocolRecvBytes;
	public static long multiplicationProtocolSentBytes;
	public static long multiplicationProtocolRecvBytes;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BigInteger distanceShare = BigInteger.ZERO;
		int partyType = 0; //1 - Server, 2 - Third Party
		String signUpSharesHaCoefsPath = null;
		BigInteger biMod = BigInteger.ZERO;
		int syncRound = 1;
		int bitLength = 0;
		ArrayList<Long> allCoefsFromSignUp = null;
		ArrayList<Long> allCoefsForAuth = null;
		ArrayList<Long> readingShares = null;
		ArrayList<Triple> mtList = null;
		LocationInfo source = null, destination = null, clientInfo = null;
		int sigma = 0, totalReadings = 0, levels = 0;
		long mod = 0;
		long distCalcStart = 0, distCalcEnd = 0, mtCalcStart = 0, mtCalcEnd = 0;
		int paillierModBitLength = 0;
		long shareRecvStart = 0, shareRecvEnd = 0;
				
		if(args.length < 8)
		{
			System.out.println("Expected arguments");
			System.out.println("1. Server (1), Third Party (2)?");
			System.out.println("2. Self Location Info (IPAddress:PortNo ex. 127.0.0.1:65000)");
			System.out.println("3. Other Location Info (IPAddress:PortNo ex. 127.0.0.1:65000)");
			System.out.println("4. Ga Coefficients calculated during sign up file path");
			System.out.println("5. Ha Coefficients calculated during sign up file path");
			System.out.println("6. Bit Length");
			System.out.println("7. Sigma for calculating MT");
			System.out.println("8. Levels for calculating Haar Coefficients");
			System.out.println("9. Client Location Info (IPAddress:PortNo ex. 127.0.0.1:65000)");
			System.out.println("10. Paillier Mod BitLength");
			return;
		}
		
		partyType = Integer.parseInt(args[0]);
		//get the location info
		source = Communicator.deriveLocationInfo(args[1]);
		destination = Communicator.deriveLocationInfo(args[2]);
		signUpSharesHaCoefsPath = args[4];
		bitLength = Integer.parseInt(args[5]);	
		sigma = Integer.parseInt(args[6]);	
		levels = Integer.parseInt(args[7]);		
		//get the location info
		clientInfo = Communicator.deriveLocationInfo(args[8]);	
		paillierModBitLength = Integer.parseInt(args[9]);
		
		//calculate the value of mod
		mod = (long)Math.pow(2, bitLength);
		biMod = BigInteger.valueOf(mod);
		
		//get the shares from client
		shareRecvStart = System.currentTimeMillis();
		
		readingShares = getSharesFromClient(source);
		
		shareRecvEnd = System.currentTimeMillis();
		
		/*
		 * At every level readings are halved. The levels start from 0, hence we take levels + 1
		 * At the end, we have equal number h[n] and g[n] coefficients that are combined hence, 
		 * we multiply by 2
		 */
		totalReadings = (readingShares.size() / (2 * (levels + 1))) * 2;
		
		//get start time for MT calculation
		mtCalcStart = System.currentTimeMillis();
		
		//precompute the MTs
		mtList = calcArithmeticMT(partyType, sigma, totalReadings, bitLength, biMod, source, destination, paillierModBitLength);
		
		mtCalcEnd = System.currentTimeMillis();
		
		//read the ha coefs during signup
		allCoefsFromSignUp = readData(signUpSharesHaCoefsPath);
		
		//get coefficients from the shares
		HaarCoefs coefs = new HaarCoefs(readingShares);
		coefs.computeHaarCoefs();

		//collect all these coefficients in one list
		allCoefsForAuth = coefs.haHaarCoefs.get(coefs.haHaarCoefs.size() - 1);
		
		distCalcStart = System.currentTimeMillis();
		
		distanceShare = calcDistanceShare(partyType, source, destination, 
				readingShares, allCoefsFromSignUp, allCoefsForAuth, mtList, biMod);
		
		distCalcEnd = System.currentTimeMillis();
		
		System.out.println("Final dist share: "+distanceShare);
		
		System.out.println("Time Stats:");
		System.out.println("Share Recv: "+(shareRecvEnd - shareRecvStart)+" milliseconds");
		System.out.println("MT Calculation: "+(mtCalcEnd - mtCalcStart)+" milliseconds");
		System.out.println("Distance Calculation: "+(distCalcEnd - distCalcStart)+" milliseconds");
		System.out.println("Bandwidth Stats:");
		System.out.println("Share Recv: ");
		System.out.println("Sent: "+0+" B");
		System.out.println("Recv: "+clientSharesBytes+" B");
		System.out.println("Total (Sent + Recv): "+(0 + clientSharesBytes)+" B");
		
		System.out.println("MT Calculation: ");
		System.out.println("Sent: "+MTCalculationProtocolSentBytes+" B");
		System.out.println("Recv: "+MTCalculationProtocolRecvBytes+" B");
		System.out.println("Total (Sent + Recv): "+(MTCalculationProtocolSentBytes + MTCalculationProtocolRecvBytes)+" B");
		
		System.out.println("Distance Calculation: ");
		System.out.println("Sent: "+multiplicationProtocolSentBytes+" B");
		System.out.println("Recv: "+multiplicationProtocolRecvBytes+" B");
		System.out.println("Total (Sent + Recv): "+(multiplicationProtocolSentBytes + multiplicationProtocolRecvBytes)+" B");
	}
	
	/*
	 * This function recieves its shares from the client
	 */
	public static ArrayList<Long> getSharesFromClient(LocationInfo locInfo)
	{
		ArrayList<Long> readingShares = null;
		ClientShareInfo shareMsg = null;
		
		shareMsg = (ClientShareInfo) Communicator.receiveMessage(locInfo);
		clientSharesBytes = shareMsg.bytesTransferred;
		
		System.out.println("Big Length Info from client: "+shareMsg.bitLength);
		
		readingShares = shareMsg.shares;
		
		return readingShares;
	}
	
	/*
	 * This function will execute the protocol to compute the Arithmetic Multiplication Triple
	 */
	public static ArrayList<Triple> calcArithmeticMT(int partyType, int sigma, int totalReadings, int modBitLength,
			BigInteger biMod, LocationInfo source, LocationInfo destination, int paillierModBitLength)
	{
		ArrayList<Triple> mtList = new ArrayList<>(), encMTList = new ArrayList<>();
		Triple triple = null, encTriple = null;		
		Paillier p = null;
		PaillierInfo pMsg = null;
		MTInfo mtMsg = null;
		DforMTInfo dMsg = null;
		int totalMTs = 0;
		BigInteger d;
		ArrayList<BigInteger> dList = new ArrayList<>();
		
		/*
		 * For each distance calculation we require 3 multiplications. And we have say x number of readings
		 * i.e., Ga + Ha coefficients. So we will need 3x MTs
		 */
		totalMTs = totalReadings * 3;
		
		
		//the server will create the Paillier object and send it to the TP
		if(partyType == 1)
		{
			//create the Paillier object
			p = new Paillier(paillierModBitLength, 64);
			
			pMsg = new PaillierInfo(p);			
			pMsg.setMessageType(MESSAGE_TYPE.Paillier);
			
			/*
			 * In reality the encryption of random numbers of the server and the random number generated
			 * by the third party will be encrypted using the public key of the server and then using
			 * HE the triple for the server will be computed
			 */
			Communicator.sendMessage(pMsg, destination);
		}
		else
		{
			pMsg = (PaillierInfo) Communicator.receiveMessage(source);
			
			p = pMsg.p;
		}
		
		/*
		 * now execute the protocol as follows:
		 * 
		 */
	
		int j = 0;
		
		for (int i = 0; i < totalMTs; i++) {
			
			//generate two random numbers
			if(j == 1)
			{
				triple = generateNumbers(partyType, 0, modBitLength, biMod, sigma);	
			}
			else
			{
				triple = generateNumbers(partyType, 1, modBitLength, biMod, sigma);			
			}
			
			mtList.add(triple);					

			//server will send its encrypted random numbers to TP
			
			if(partyType == 1)
			{
				encTriple = new Triple(
							p.Encryption(triple.a),
							triple.b.compareTo(BigInteger.ZERO) == 0 ? triple.b : p.Encryption(triple.b),
							BigInteger.ZERO
						);
				
				encMTList.add(encTriple);
			}
			
			j++;
			
			if(j == 3)
			{
				j = 0;
			}
		}
		

		
		/*
		 * TP will generate three random numbers and the third one will be used to generate the
		 * multiplication triple at the server 
		 */
		if(partyType == 1)
		{
			
			mtMsg = new MTInfo(encMTList);
			
			Communicator.sendMessage(mtMsg, destination);
			MTCalculationProtocolSentBytes = mtMsg.bytesTransferred;
			
			//server will wait for a message from TP for generating the MT
			dMsg = (DforMTInfo)Communicator.receiveMessage(source);
			MTCalculationProtocolRecvBytes = dMsg.bytesTransferred;
			
			ArrayList<BigInteger> encDList = dMsg.dForMTs;
			
			//calculate the triple using the received value of d
			getMul(p, encDList, mtList, biMod);
		}
		else
		{
			dMsg = new DforMTInfo();
			
			//get the encrypted random numbers from server
			mtMsg = (MTInfo)Communicator.receiveMessage(source);
			MTCalculationProtocolRecvBytes = mtMsg.bytesTransferred;
			
			for(int i = 0; i < mtMsg.encTripleList.size(); i++)
			{			
				encTriple = mtMsg.encTripleList.get(i);
				triple = mtList.get(i);
				
				//TP will generate a number d that is required by the server for calculating the MT
				d = calculateD(
						encTriple.a, 
						encTriple.b, 
						triple,
						p);
				
				dList.add(d);
			}
			
			
			dMsg.dForMTs = dList;
			
			//send this to the server
			Communicator.sendMessage(dMsg, destination);
			MTCalculationProtocolSentBytes = dMsg.bytesTransferred;
		}
		
		
		return mtList;
	}
	
	public static BigInteger calculateD(BigInteger a0, BigInteger b0, Triple t, Paillier p)
	{
		BigInteger encRand = p.Encryption(t.rand); 
		BigInteger result;
		BigInteger aoPowB1;
		BigInteger b0PowA1;
		
		if(b0.compareTo(BigInteger.ZERO) != 0)
		{
			aoPowB1 = a0.modPow(t.b, p.nsquare); // a0 * b1
			b0PowA1 = b0.modPow(t.a, p.nsquare); // b0 * a1
			
			result = aoPowB1.multiply(b0PowA1).mod(p.nsquare); // a0 * b1 + b0 * a1
			result = result.multiply(encRand).mod(p.nsquare); // a0 * b1 + b0 * a1 + r
		}
		else
		{
			aoPowB1 = a0.modPow(t.b, p.nsquare); // a0 * b1
			result = aoPowB1.multiply(encRand).mod(p.nsquare); // a0 * b1 + b0 * a1 + r -> b0 is 0 hence that part will be zero
		}
		
		return result;
	}
	

	
	/*
	 * if calcFor is 0 then two random numbers will be generated (a and b) else only one will be generated ie a
	 * and the other will be set to 0
	 * @param calcFor: Will indicate if the multiplication is for (x1.x2 or y1.y2) or (x.y)
	 */
	public static Triple generateNumbers(int partyType, int calcFor, int modBitLength, BigInteger biMod, int sigma)
	{
		Triple triple = new Triple();

		if(partyType == 1)
		{
			triple.a = getRandomNum(modBitLength);	
			if(calcFor == 0)
			{	
				triple.b = getRandomNum(modBitLength);	
			}
			else
			{
				triple.b = BigInteger.ZERO;
			}
		}
		else if(partyType == 2)
		{
			int modRandBitLength = 0;
			
			triple.b = getRandomNum(modBitLength);
			if(calcFor == 0)
			{	
				triple.a = getRandomNum(modBitLength);
			}
			else
			{
				triple.a = BigInteger.ZERO;
			}

			//calculate the value of modRandBitLength which is 2 * modBitLength + 1 + sigma (security parameter)
			modRandBitLength = 2 * modBitLength + 1 + sigma;
			
			triple.rand = getRandomNum(modRandBitLength);

			triple.c = triple.a.multiply(triple.b).subtract(triple.rand).mod(biMod);

			//if no less than 0 then take no % mod	
			triple.c = triple.c.mod(biMod);

		}

		return triple;
	}	
	
	public static void getMul(Paillier p, ArrayList<BigInteger> encDList, ArrayList<Triple> mtList, BigInteger biMod)
	{		
	BigInteger decD = null;
	Triple tmpTriple = null;
	
	for(int i = 0 ; i < mtList.size(); i++)
	{
		tmpTriple = mtList.get(i);
		
		decD = p.Decryption(encDList.get(i));
		
		if(tmpTriple.b.compareTo(BigInteger.ZERO) != 0)
		{
			tmpTriple.c = tmpTriple.a.multiply(tmpTriple.b).add(decD).mod(biMod);
		}
		else
		{
			tmpTriple.c = decD;
		}
	}
}
	
	public static BigInteger getRandomNum(int pBitLength)
	{
		BigInteger rand = new BigInteger(pBitLength, new SecureRandom());		
		return rand;
	}
	
	/*
	 * This function calculates the Euclidean distance between the features
	 */
	public static BigInteger calcDistanceShare(int partyType, LocationInfo source, LocationInfo destination, 
			ArrayList<Long> readingShares, ArrayList<Long> allCoefsFromSignUp, ArrayList<Long> allCoefsForAuth,
			ArrayList<Triple> mtList, BigInteger biMod)
	{
		BigInteger distanceShare = BigInteger.ZERO, mulShare = BigInteger.ZERO;
		int iValue = 0;
		
		//identify if running as server of TP
		if(partyType == 1)
		{
			iValue = 0;
		}
		else
		{
			iValue = 1;
		}
		
		//compute the Euclidean distance
		/*
		 * We need to compute Sum [(x - y)^2] = Sum [ (x1 + x2)^2 - 2.x.y + (y1 + y2)^2 ]
		 * = Sum[ (x1^2 + 2.x1.x2 + x2^2) - 2.x.y + (y1^2 + 2.y1.y2 + y2^2) ]
		 * = Sum[ (x1^2 + y1^2) + 2.x1.x2 - 2.x.y + 2.y1.y2 + (x1^2 + y1^2) ]
		 * 1. (x1^2 + y1^2) & (x1^2 + y1^2) can be computed locally. So first compute this using the
		 * allCoefsFromSignUp & allCoefsForAuth
		 * 2. 2.x1.x2 use the MT which has one of the terms 0 i.e., for server b0 is 0 and for TP a1 is 0
		 * and calculate the product of the share
		 * 3. (- 2.x.y ) use the MT
		 * 4. 2.y1.y2 use the MT which has one of the terms 0 i.e., for server b0 is 0 and for TP a1 is 0
		 * and calculate the product of the share
		 * 5. Add the result of each operation and at the end we will have share of the distance
		 */
		
		//Step 1
		distanceShare = lCalcSquareAndSum(allCoefsFromSignUp, biMod);
		distanceShare = distanceShare.add(lCalcSquareAndSum(allCoefsForAuth, biMod));
		distanceShare = distanceShare.mod(biMod);
		System.out.println("Step 1 dist: "+distanceShare);	
		
		//Step 2, 3, 4 will be executed by the function
		mulShare = execMultiplicationProtocol(partyType, iValue, allCoefsForAuth, allCoefsFromSignUp, mtList, source, destination, biMod);
		
		//Step 5
		distanceShare = distanceShare.add(mulShare);
		distanceShare = distanceShare.mod(biMod);
		
		System.out.println("Step 5 dist: "+distanceShare);
		
		return distanceShare;
	}
	
	/*
	 * This function executes the multiplication protocol
	 */
	public static BigInteger execMultiplicationProtocol(int partyType, int iValue, ArrayList<Long> allCoefsForAuth, 
			ArrayList<Long> allCoefsFromSignUp, ArrayList<Triple> mtList, LocationInfo source, LocationInfo destination,
			BigInteger biMod)
	{
		BigInteger distanceShare = BigInteger.ZERO, eShare = BigInteger.ZERO, fShare = BigInteger.ZERO, mulShare = BigInteger.ZERO;
		int multiplier = 0, k = 0;
		long actualShareX = 0, actualShareY = 0;
		Triple tempTriple = null;
		MessageInfo message = null;
		EFSharesInfo efSharesMsg = null;
		int syncRound = 1;
		ArrayList<BigInteger> eShares = new ArrayList<>(), fShares = new ArrayList<>(), mulShares = null;
		ArrayList<Integer> multiplierList =  new ArrayList<>();
	
		
		//for distance between each coefficient (i.e., feature) we will have to perform three multiplications
		for(int i = 0; i < allCoefsForAuth.size(); i++)
		{
			for(int j = 0; j < 3; j++)
			{
				//use the precomputed triple
				tempTriple = mtList.get(k);

				multiplier = 2;
				if (j== 0)
				{
					//this is multiplication of x1.x2
					/*
					 * Here depending upon partyType either of actualShareX
					 * or actualShareY will be 0
					 */
					if(partyType == 1)
					{
						actualShareX = allCoefsFromSignUp.get(i);
						actualShareY = 0;
					}
					else if(partyType == 2)
					{
						actualShareX = 0;
						actualShareY = allCoefsFromSignUp.get(i);
					}
				}
				else if (j == 2)
				{
					//this is multiplication of y1.y2
					/*
					 * this is multiplication of x1.x2. Here depending upon partyType either of actualShareX
					 * or actualShareY will be 0
					 */
					if(partyType == 1)
					{
						actualShareX = allCoefsForAuth.get(i);
						actualShareY = 0;
					}
					else if(partyType == 2)
					{
						actualShareX = 0;
						actualShareY = allCoefsForAuth.get(i);
					}
				}
				else
				{
					//this multiplication of x.y
					multiplier = -2;
					
					actualShareX = allCoefsFromSignUp.get(i);
					actualShareY = allCoefsForAuth.get(i);
				}
				
				multiplierList.add(multiplier);
								
				//calc eshare
				eShare = calcEFShare(
							BigInteger.valueOf(actualShareX), 
							tempTriple.a
						);
				
				eShares.add(eShare);
				
				//calc fshare
				fShare = calcEFShare(
							BigInteger.valueOf(actualShareY), 
							tempTriple.b
						);
				
				fShares.add(fShare);
				
				//increment round count
				syncRound++;
				eShare = fShare = BigInteger.ZERO;
				k++;
			}
		}		

		
		message = new EFSharesInfo(eShares, fShares);
		
		//if the party is server then it will send the first message else it will wait for the first message
		if(partyType == 1)
		{		
			Communicator.sendMessage(message, destination);
			multiplicationProtocolSentBytes = message.bytesTransferred;
	
			efSharesMsg = (EFSharesInfo) Communicator.receiveMessage(source);
			multiplicationProtocolRecvBytes = efSharesMsg.bytesTransferred;
		}
		else if(partyType == 2)
		{
			efSharesMsg = (EFSharesInfo) Communicator.receiveMessage(source);
			multiplicationProtocolRecvBytes = efSharesMsg.bytesTransferred;
			
			Communicator.sendMessage(message, destination);
			multiplicationProtocolSentBytes = message.bytesTransferred;
		}
		
		//reconstruct e and f		
		for(int i = 0; i < eShares.size(); i++)
		{
			eShares.set(i, eShares.get(i).add(efSharesMsg.eShares.get(i)).mod(biMod));
			fShares.set(i, fShares.get(i).add(efSharesMsg.fShares.get(i)).mod(biMod));
		}
		
		mulShares = calcMulProtocolShare(
				iValue, eShares, fShares, 
				mtList
			);
		
		
		for(int i = 0 ; i < multiplierList.size(); i++)
		{		
			mulShare = mulShare.add(mulShares.get(i).multiply(BigInteger.valueOf(multiplierList.get(i)))).mod(biMod);
		}
		
		distanceShare = distanceShare.add(mulShare);
		distanceShare = distanceShare.mod(biMod);
		
		return distanceShare;
	}
	
	public static ArrayList<Long> readData(String filePath)
	{
		ArrayList<Long> returnList = new ArrayList<>();		
		
		try (InputStream stream = new FileInputStream(new File(filePath)))
		{
			try(Scanner s = new Scanner(stream))
			{
				String data = s.useDelimiter("\\A").hasNext() ? s.next() : ""; 
				
				String[] arrData = data.trim().split(",");
				
				for(String d : arrData)
				{
					if(d!= null && d.length() > 0)
					{
						returnList.add(Long.parseLong(d));
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnList;
	}
	
	public static ArrayList<Triple> readMTData(String filePath)
	{
		ArrayList<Triple> returnList = new ArrayList<>();		
		String data = null;
		String[] arrData = null;
		String[] arrSubData = null;
		
		try (InputStream stream = new FileInputStream(new File(filePath)))
		{
			try(Scanner s = new Scanner(stream))
			{
				data = s.useDelimiter("\\A").hasNext() ? s.next() : ""; 
				
				arrData = data.trim().split("\n");
								
				for(String d : arrData)
				{
					if(d!= null && d.length() > 0)
					{
						arrSubData = d.split(",");
						
						if(arrSubData != null && arrSubData.length == 3)
						{
							returnList.add(new Triple(
										new BigInteger(arrSubData[0]),
										new BigInteger(arrSubData[1]),
										new BigInteger(arrSubData[2])
									));
						}
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnList;
	}
	public static BigInteger calcEFShare(BigInteger arg1, BigInteger arg2) 
	{
		BigInteger result = null;
		
		result = arg1.subtract(arg2);
		
		return result;
	}
	
	//public static BigInteger calcMulProtocolShare(int i, BigInteger e, BigInteger f, BigInteger shareA, BigInteger shareB, BigInteger shareC)
	public static ArrayList<BigInteger> calcMulProtocolShare(int i, ArrayList<BigInteger> e, ArrayList<BigInteger> f, 
			ArrayList<Triple> tripleList)
	{
		ArrayList<BigInteger> resultList = new ArrayList<>();
		BigInteger eShare, fShare, aShare, bShare, cShare;
		
		eShare = fShare = aShare = bShare = cShare = BigInteger.ZERO;
		
		for(int j = 0; j < e.size(); j++)
		{

			eShare = e.get(j);
			fShare = f.get(j);
			aShare = tripleList.get(j).a;
			bShare = tripleList.get(j).b;
			cShare = tripleList.get(j).c;
			
			resultList.add(
					(BigInteger.valueOf(i).multiply(eShare).multiply(fShare))
						.add(
								(fShare.multiply(aShare))
						)
						.add(
								(eShare.multiply(bShare))
						)
						.add(cShare)
					);
		}
		
		return resultList;
	}
	
	public static BigInteger iCalcSquareAndSum(ArrayList<Integer> coefs, BigInteger mod)
	{
		BigInteger result = BigInteger.ZERO;
		Integer isquare = 0;
		
		for(Integer i : coefs)
		{
			isquare = i * i;
			result = result.add(BigInteger.valueOf(isquare).mod(mod));
			result = result.mod(mod);
		}
		
		return result;
	}
	
	public static BigInteger lCalcSquareAndSum(ArrayList<Long> coefs, BigInteger mod)
	{
		BigInteger result = BigInteger.ZERO;
		Long lsquare = 0L;
		
		for(Long l : coefs)
		{
			lsquare = l * l;
			result = result.add(BigInteger.valueOf(lsquare).mod(mod));
			result = result.mod(mod);
		}
		
		return result;
	}
}
package classes;

import java.util.ArrayList;

import libs.Communicator;
import libs.Preprocessor;
import messages.ClientShareInfo;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int readings = 8, levels = 3;
		int bitLength = 6;
		String filePath = "EEG Sample Data.txt";
		ArrayList<Long> serverShares = null, ttpShares = null;
		ClientShareInfo sharesMsg = null;
		LocationInfo clientInfo = null, serverInfo = null, tpInfo = null;
		
		if(args.length < 6)
		{
			System.out.println("Expected arguments");
			System.out.println("1. EEG sample file path");
			System.out.println("2. Readings count");
			System.out.println("3. Level count");
			System.out.println("4. Client Location Info (IPAddress:PortNo ex. 127.0.0.1:65000)");
			System.out.println("5. Server Location Info (IPAddress:PortNo ex. 127.0.0.1:65000)");
			System.out.println("6. Trusted Third Party Location Info (IPAddress:PortNo ex. 127.0.0.1:65000)");
			return;
		}
		
		filePath = args[0];
		readings = Integer.parseInt(args[1]);
		levels = Integer.parseInt(args[2]);
		
		//get the location info
		clientInfo = Communicator.deriveLocationInfo(args[3]);
		serverInfo = Communicator.deriveLocationInfo(args[4]);
		tpInfo = Communicator.deriveLocationInfo(args[5]);
				
		Preprocessor temp = new Preprocessor();
		temp.readRada(filePath);
		
		temp.processData();
		
		if(temp.readings.size() != readings)
		{
			System.out.println("Readings count mismatch. Expected: "+readings+" Received: "+temp.readings.size());
			return;
		}
		System.out.println("Readings Stats. Expected: "+readings+" Received: "+temp.readings.size());
		
		bitLength = calcBitLength(temp.abs_max, readings, levels);
		
		System.out.println("Bit Length: "+bitLength);
		
		serverShares = new ArrayList<>();
		ttpShares = new ArrayList<>();
		
		calculateShares(bitLength, temp.readings, serverShares, ttpShares);
				
		//send shares to respective parties
		
		//construct msg for server
		serverShares.trimToSize();
		sharesMsg = new ClientShareInfo(serverShares, bitLength);
		//send
		Communicator.sendMessage(sharesMsg, serverInfo);
		
		System.out.println("Bytes Sent: "+sharesMsg.bytesTransferred +" B");
		
		//construct message for ttp		
		ttpShares.trimToSize();
		sharesMsg = new ClientShareInfo(ttpShares, bitLength);
		//send
		Communicator.sendMessage(sharesMsg, tpInfo);
		System.out.println("Bytes Sent: "+sharesMsg.bytesTransferred +" B");
	}
	
	public static int calcBitLength(int absMax, int totalReadings, int levels)
	{
		int bitLength = 0;
		long temp = 0;
		
		int cntFinalReadings = totalReadings / (2 * (levels + 1));	
		
		temp = absMax;
		temp = temp * (long)Math.pow(2, levels);
		temp = temp * temp;
		temp = temp * (cntFinalReadings * 2);
		
		bitLength = (int)Math.ceil((Math.log10(temp) / Math.log10(2)));
		
		//to handle negative values add 1
		return bitLength + 1;
	}
	
	public static long calculateMod(int bitLength)
	{
		long mod = (long)(Math.pow(2, bitLength));
		
		return mod;
	}
	
	public static void calculateShares(int bitLength, ArrayList<Integer> readings, ArrayList<Long> serverShares, ArrayList<Long> ttpShares)
	{
		long rand = 0;
		long otherNum = 0;
		
		if(bitLength > 63)
		{
			System.out.println("Bitlength too long");
			return;
		}
		
		long mod = calculateMod(bitLength);
		
		for(int x : readings)
		{
			rand = java.util.concurrent.ThreadLocalRandom.current().nextLong((mod * -1) - 1,mod + 1);
			
			otherNum = x - rand;
			
			if(rand < 0)
			{
				rand = rand + mod;
			}
			
			serverShares.add(rand);

			while(otherNum < 0)
			{
				otherNum = otherNum + mod;
			}			
			
			ttpShares.add(otherNum);
		}
	}
}

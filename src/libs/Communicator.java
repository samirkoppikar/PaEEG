package libs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;

import classes.LocationInfo;
import messages.MessageInfo;


/*
 * This class handles the communication
 */
public class Communicator {
	
	
	public static long sendMessage(MessageInfo message, LocationInfo destination)
	{
		boolean isConnected = false;
		Socket clientSocket = null;
		
		long bytesTransferred = 0;
		
		while(!isConnected)
		{
			//create socket
			try
			{	
				
				clientSocket = new Socket(destination.getIpAddress(), destination.getPort());
							
				isConnected = true;
				
				ByteArrayOutputStream baStream = new ByteArrayOutputStream();
				
				ObjectOutputStream ooStream = new ObjectOutputStream(baStream);
				
				ooStream.writeObject(message);
				
				CountingOutputStream coStream = new CountingOutputStream(clientSocket.getOutputStream());
				
				coStream.write(baStream.toByteArray());
				
				message.bytesTransferred = bytesTransferred = coStream.getByteCount();
				
				coStream.close();
				
			}
			catch(SocketException e)
			{
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try {
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		
		return bytesTransferred;
	}
	
	public static MessageInfo receiveMessage(LocationInfo serverInfo)
	{
		MessageInfo message = null;

		//create socket
		try(ServerSocket serverSocket = new ServerSocket(serverInfo.getPort()))
		{	
			message = receiveMessage(serverSocket.accept());
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return message;
	}
	
	public static MessageInfo receiveMessage(Socket commSocket)
	{
		MessageInfo message = null;
		
		try
		{			
			
			ByteArrayOutputStream baStream = new ByteArrayOutputStream();
			
			CountingInputStream ciStream = new CountingInputStream(commSocket.getInputStream());
			
			byte[] buff = new byte[1024];
			int bytesRead = 0;
			
			while((bytesRead = ciStream.read(buff, 0, buff.length)) != -1)
			{
				baStream.write(buff, 0, bytesRead);
			}
			
			baStream.flush();
			
			ByteArrayInputStream baiStream = new ByteArrayInputStream(baStream.toByteArray());
			ObjectInputStream oiStream = new ObjectInputStream(baiStream);
			message = (MessageInfo) oiStream.readObject();
			message.bytesTransferred = ciStream.getByteCount();
			
			ciStream.close();
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return message;
	}
	
	public static LocationInfo deriveLocationInfo(String info)
	{
		LocationInfo locInfo = null;
		
		String[] arrInfo = info.trim().split(":");
		
		if(arrInfo != null && arrInfo.length == 2)
		{
			locInfo = new LocationInfo("", arrInfo[0], Integer.parseInt(arrInfo[1]));
		}
		else
		{
			System.out.println("Location Information format incorrect. It should be IPAddress:PortNo ex. 127.0.0.1:65000");
		}
		
		return locInfo;
	}
}

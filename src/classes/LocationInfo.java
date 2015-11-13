package classes;

import java.io.Serializable;

public class LocationInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String ipaddress;
	private Integer port;
	
	public LocationInfo()
	{
		
	}
	
	public LocationInfo(String pName, String pIpaddress, Integer pPort)
	{
		this.name = pName;
		this.ipaddress = pIpaddress;
		this.port = pPort;
	}
	
	//getters
	public String getName()
	{
		return name;
	}
	
	public String getIpAddress()
	{
		return ipaddress;
	}
	
	public Integer getPort()
	{
		return port;
	}
	
	//setters
	public void setName(String pName)
	{
		this.name = pName;
	}
	
	public void setIpAddress(String pIpaddress)
	{
		this.ipaddress = pIpaddress;
	}
	
	public void setPort(Integer pPort)
	{
		this.port = pPort;
	}
	
}

package libs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Preprocessor {
	public String data = null;
	public ArrayList<Integer> readings = null;
	public Integer min = Integer.MAX_VALUE, max = Integer.MIN_VALUE; 
	public Integer abs_max = 0;
	
	public void readRada(String path)
	{		
		try (InputStream stream = new FileInputStream(new File(path)))
		{
			try(Scanner s = new Scanner(stream))
			{
				data = s.useDelimiter("\\A").hasNext() ? s.next() : ""; 
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Boolean processData()
	{
		Boolean success = false;
		String strReadings[] = null;
		Double tempVal = 0.0;
		Integer intVal = 0;
		if(data != null)
		{

			//trim the input text
			data = data.trim();
					
			strReadings = data.split("\t");
						
			if(strReadings != null && strReadings.length > 0)
			{
				//check if readings are a power of 2
				if((strReadings.length & -strReadings.length) == (strReadings.length))
				{
					readings = new ArrayList<Integer>();
					
					for(String r : strReadings)
					{
						tempVal = Double.parseDouble(r);
						intVal = (int)Math.round(tempVal);
						
						readings.add(intVal);
						
						if(intVal > max)
						{
							max = intVal;
						}
						
						if(intVal < min)
						{
							min = intVal;
						}			
						
					}
					
					abs_max = max > Math.abs(min) ? max : Math.abs(min);
				}
				else
				{
					System.out.println("Readings aren't a power of 2");
				}
			}
			else
			{
				System.out.println("Incorret data format");
			}
		}
		else
		{
			System.out.println("No data read");
		}
		return success;
	}
}

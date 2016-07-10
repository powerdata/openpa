package com.powerdata.openpa.wirecable;

import java.io.IOException;
import java.util.Arrays;

import com.powerdata.openpa.tools.SimpleCSV;

public class PoleConstruction 
{
	
	private SimpleCSV _pole;
	private float [] _distance=new float[10];
	private String[] _sdistance;
	private float distance_hor=0,distance_ver=0;
	
	
	public PoleConstruction (String input_directory) throws IOException
	{
		_pole=new SimpleCSV(input_directory);
	}
	
	private String[] getdata(String header)
	{
		return  _pole.get(header);
		
	}
	
	private float pythagorus(float[] points)
	{
		distance_hor=points[0]-points[1];
		distance_ver=points[2]-points[3];
		//System.out.println("HOR DIS"+distance_hor+" VER_DIS"+distance_ver);
		return (float) (Math.sqrt(distance_hor*distance_hor+ distance_ver*distance_ver));
	}
	public float[] calcdistance (String header)
	{
		float[] points = new float[4]; 
		
			_sdistance=_pole.get(header);
		//	System.out.println(Arrays.toString(_sdistance));
			Arrays.fill(_distance, (-1));
		
		
		
	
		
		/** First one for start of horizontal point 
		 * Second place for horizontal end 
		 * Third place for start of vertical point 
		 * Forth place for end of vertical point 
		 * */
		 
		
		try
		{
			// Calculating Dab
			if (_sdistance[0].length() !=0 && _sdistance[2].length() !=0 )
			{
				points[0]=Float.parseFloat(_sdistance[0]); 
				points[1]=Float.parseFloat(_sdistance[2]);
				points[2]=Float.parseFloat(_sdistance[1]);
				points[3]=Float.parseFloat(_sdistance[3]);
				_distance[0]=pythagorus(points);
			}
			// Calculating Dan
			if (_sdistance[0].length() !=0 && _sdistance[6].length() !=0 )
			{
				points[0]=Float.parseFloat(_sdistance[0]); 
				points[1]=Float.parseFloat(_sdistance[6]);
				points[2]=Float.parseFloat(_sdistance[1]);
				points[3]=Float.parseFloat(_sdistance[7]);
				_distance[3]=pythagorus(points);
			}
			// Calculating Dbc
			if (_sdistance[2].length() !=0 && _sdistance[4].length() !=0 )
			{
				points[0]=Float.parseFloat(_sdistance[2]); 
				points[1]=Float.parseFloat(_sdistance[4]);
				points[2]=Float.parseFloat(_sdistance[3]);
				points[3]=Float.parseFloat(_sdistance[5]);
				_distance[1]=pythagorus(points);
			}
			// Calculating Dbn
			if (_sdistance[2].length() !=0 && _sdistance[6].length() !=0 )
			{
				points[0]=Float.parseFloat(_sdistance[2]); 
				points[1]=Float.parseFloat(_sdistance[6]);
				points[2]=Float.parseFloat(_sdistance[3]);
				points[3]=Float.parseFloat(_sdistance[7]);
				_distance[4]=pythagorus(points);
			}
			// Calculating Dca
			if (_sdistance[4].length() !=0 && _sdistance[0].length() !=0 )
			{
				points[0]=Float.parseFloat(_sdistance[4]); 
				points[1]=Float.parseFloat(_sdistance[0]);
				points[2]=Float.parseFloat(_sdistance[5]);
				points[3]=Float.parseFloat(_sdistance[1]);
				_distance[2]=pythagorus(points);
			}
			// Calculating Dcn
			if (_sdistance[4].length() !=0 && _sdistance[6].length() !=0 )
			{
				points[0]=Float.parseFloat(_sdistance[4]); 
				points[1]=Float.parseFloat(_sdistance[6]);
				points[2]=Float.parseFloat(_sdistance[5]);
				points[3]=Float.parseFloat(_sdistance[7]);
				_distance[5]=pythagorus(points);
			}
		}
			catch (Exception e)
			{
				System.out.println("Pole Construction Not Available");
			}
			
		return _distance;
	}
	
	
	
	public static void main(String[] args) throws Exception
	{
		String input_directory="C:/Users/shamm/Dropbox/ASU/6bus/6bus/PoleConstruction.csv";
		PoleConstruction mat=new PoleConstruction(input_directory);
		float[] res=mat.calcdistance("100"); 
		System.out.println(Arrays.toString(res));
				
	}
}

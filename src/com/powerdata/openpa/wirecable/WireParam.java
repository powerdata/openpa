package com.powerdata.openpa.wirecable;

import java.io.IOException;
import java.util.HashMap;

import com.powerdata.openpa.tools.SimpleCSV;

public class WireParam 
{
		private float _gmr=0;
		private float _r=0;
		private float _gmrn=0;
		private float _rn=0;
		private SimpleCSV wireparam;
		
		public WireParam(double gmr,double r,double gmrn,double rn)
		{
			_gmr=(float) gmr;
			_r=(float)r;
			_gmrn=(float)gmrn;
			_rn=(float)rn;
		}
		
		public WireParam(String file_directory) throws IOException
		{
			wireparam=new SimpleCSV(file_directory);
			
		}
		
		
		
		public float getGMR() {return _gmr;}
		public float getR()   {return _r;}
		
		public float getGMRn() {return _gmrn;}
		public float getRn()   {return _rn;}
		
		public void setGMR(float gmr) {_gmr=gmr;}
		public void setR(float r) {_r=r;}
		
		public void setGMRn(float gmrn) {_gmrn=gmrn;}
		public void setRn(float rn) {_rn=rn;}
		
		public float[] getwireparam()
		{
			return new float[] {_gmr,_r,_gmrn,_rn};
		}
		
		public float[] getwireparam(String header)
		{
			return wireparam.getFloats(header);
		}
		
		public static void main(String[] args) 
		{
			

		}

}

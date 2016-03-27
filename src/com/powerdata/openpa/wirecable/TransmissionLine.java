package com.powerdata.openpa.wirecable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.powerdata.openpa.tools.Complex;

public class TransmissionLine 
{
	private PoleConstruction _pole;
	private String _polecode; 
	//private WireParam _wire;
	private float[] _wire;
	private int[] _phase;
	private float _length;
	private float _real,_imag;
	
	private ArrayList<Complex> ZKron=new ArrayList<Complex>();
	
	private Complex Znn=Complex.Zero;
	private Complex Zs=Complex.Zero;
	private Complex Zab=Complex.Zero;
	private Complex Zbc=Complex.Zero;
	private Complex Zca=Complex.Zero;
	private Complex Zan=Complex.Zero;
	private Complex Zbn=Complex.Zero;
	private Complex Zcn=Complex.Zero;
	
	
	
	//public TransmissionLine(float length,int[] phase,WireParam wire,PoleConstruction pole,String polecode)
	public TransmissionLine(float length,int[] phase,float[] wire,PoleConstruction pole,String polecode)
	{
		_pole=pole;
		_wire=wire;
		_length=length;
		_phase=phase.clone();
		_polecode=polecode;
		/**for (int i=0;i<4;i++)
		{
			ZMatrixThreeWire.add(i, Complex.Zero);
		}
		for (int i=0;i<3;i++)
		{
			ZMatrixNeutral.add(i, Complex.Zero);
		}*/
		for (int i=0;i<6;i++)
		{
			ZKron.add(i, Complex.Zero);
		}
		
	}
	/**
	 * This following method calcualtes the KRON matrix 
	 */
	public void setZMatrix()
	{
		Complex temp=Complex.Zero;
		float[] geometry=_pole.calcdistance(_polecode);
		//Zs=SelfImpedance(_wire.getR(),_wire.getGMR());
		Zs=SelfImpedance(_wire[1],_wire[0]);
		Znn=SelfImpedance(_wire[3],_wire[2]);
		
		
		if(geometry[0]>0)
			Zab=MutualImpedance(geometry[0]);
		if(geometry[1]>0)
			Zbc=MutualImpedance(geometry[1]);
		if(geometry[2]>0)
			Zca=MutualImpedance(geometry[2]);
		if(geometry[3]>0)
			Zan=MutualImpedance(geometry[3]);
		if(geometry[4]>0)
			Zbn=MutualImpedance(geometry[4]);
		if(geometry[5]>0)
			Zcn=MutualImpedance(geometry[5]);
		
		if(_phase[0]==1)
		{
			temp=Zan.mult(Zan);
			temp=temp.div(Znn);
			temp=Zs.sub(temp);
			ZKron.set(0, temp);
			
			if(_phase[1]==2)
			{
				temp=Zan.mult(Zbn);
				temp=temp.div(Znn);
				temp=Zab.sub(temp);
				ZKron.set(3, temp);
			}
		}
			
		
		
		if(_phase[1]==2)
		{
			temp=Zbn.mult(Zbn);
			temp=temp.div(Znn);
			temp=Zs.sub(temp);
			ZKron.set(1, temp);
			
			if(_phase[2]==3)
			{
				temp=Zbn.mult(Zcn);
				temp=temp.div(Znn);
				temp=Zbc.sub(temp);
				ZKron.set(5, temp);
			}
		}
		
		
		
		if(_phase[2]==3)
		{
			temp=Zcn.mult(Zcn);
			temp=temp.div(Znn);
			temp=Zs.sub(temp);
			ZKron.set(2, temp);
			
			if(_phase[0]==1)
			{
			temp=Zcn.mult(Zan);
			temp=temp.div(Znn);
			temp=Zca.sub(temp);
			ZKron.set(4, temp);
			}
		}
		System.out.println();
		

	}
	
	public ArrayList<Complex> GetZMatrix() {return ZKron;}
	
	private Complex SelfImpedance (float r, float gmr)
	{
		_real=(float) (r+0.09530);
		_imag=(float) (0.12134*(7.93402+Math.log(1/gmr)));
		return (new Complex(_real,_imag));
	}
	
	
	private Complex MutualImpedance (float distance)
	{
		_real=(float) 0.09530;
		_imag=(float) (0.12134*(7.93402+Math.log(1/distance)));
		return (new Complex(_real,_imag));
	}

	

	public  static void main(String[] args) throws IOException
	{
		String input_directory="C:/Users/shamm/Dropbox/ASU/6bus/6bus/PoleConstruction.csv";
		String wire_directory="C:/Users/shamm/Dropbox/ASU/6bus/6bus/wireparam.csv";
		PoleConstruction pole=new PoleConstruction(input_directory);
		WireParam wire=new WireParam(wire_directory);
		int[] phase_ABCN={1,2,3}; // 1 2 3 corresponds to Phase A , Phase B, Phase C, if a specific phase is absent put zero
		
		TransmissionLine line01=new TransmissionLine(1000,phase_ABCN,wire.getwireparam("1"),pole,"500");
		// The pole class object is to be sent unless there is no way to figure out where is the input file located 
		line01.setZMatrix();
		ArrayList<Complex> data=line01.GetZMatrix();
		
		for(Complex z: data)
		{
			z.show();
		}
		
		//phase_ABCN[2]=0;
		phase_ABCN[1]=0;
		TransmissionLine line02=new TransmissionLine(1000,phase_ABCN,wire.getwireparam("1"),pole,"505AC");
		line02.setZMatrix();
		data=line02.GetZMatrix();
		
		for(Complex z: data)
		{
			z.show();
		}
	}
}

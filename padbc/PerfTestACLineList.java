package com.powerdata.openpa.padbc;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class PerfTestACLineList extends ACLineList
{
	protected int _size;
	protected float[] _data;
	
	public PerfTestACLineList(int size)
	{
		_size = size;
		_data = new float[size];
		Random rand = new Random();
		for (int i=0; i < size; ++i)
			_data[i] = rand.nextFloat()*2F;
			
	}
	
	@Override
	public String getID(int ndx) {return String.valueOf(ndx);}

	@Override
	public int getFromNode(int ndx) {return 0;}

	@Override
	public int getToNode(int ndx) {return 0;}

	@Override
	public float getR(int ndx) {return 0;}

	@Override
	public float getX(int ndx) {return 0;}

	@Override
	public float getFromBChg(int ndx) {return _data[ndx];}

	@Override
	public float getToBChg(int ndx) {return 0;}

	@Override
	public void updateActvPower(int ndx, float p) {}

	@Override
	public void updateReacPower(int ndx, float q) {} 


	
	@Override
	public int size() {return _size;}

	public long testNative()
	{
		float sum = 0;
		long ts = System.currentTimeMillis();
		for(int i=0; i < _size; ++i)
			sum += _data[i];
		return System.currentTimeMillis() - ts;
	}
	
	public long testAccessor()
	{
		float sum = 0;
		long ts = System.currentTimeMillis();
		for(int i=0; i < _size; ++i)
			sum += getFromBChg(i);
		return System.currentTimeMillis() - ts;
	}
	
	public long testObject()
	{
		float sum = 0;
		long ts = System.currentTimeMillis();
		for(int i=0; i < _size; ++i)
			sum += get(i).getFromBChg();
		return System.currentTimeMillis() - ts;
	}
	
	public long testPrettyFor()
	{
		float sum = 0;
		long ts = System.currentTimeMillis();
		for(ACLine n : this)
			sum += n.getFromBChg();
		return System.currentTimeMillis() - ts;
	}
	
	public long testIterator()
	{
		float sum = 0;
		Iterator<ACLine> li = iterator();
		long ts = System.currentTimeMillis();
		while (li.hasNext())
			sum += li.next().getFromBChg();
		return System.currentTimeMillis() - ts;
	}

	public long testListIterator()
	{
		float sum = 0;
		ListIterator<ACLine> li = listIterator();
		long ts = System.currentTimeMillis();
		while (li.hasNext())
			sum += li.next().getFromBChg();
		return System.currentTimeMillis() - ts;
	}

	public void test()
	{
		System.out.println("Native Array: "+testNative());
		System.out.println("Accessor through List: "+testAccessor());
		System.out.println("Accessor through Object: "+testObject());
		System.out.println("Convenient For Loop: "+testPrettyFor());
		System.out.println("Iterator: "+testListIterator());
		System.out.println("List Iterator: "+testListIterator());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		PerfTestACLineList test = new PerfTestACLineList(100000000);
		test.test();
	}


}

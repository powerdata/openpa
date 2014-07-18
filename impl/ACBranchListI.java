package com.powerdata.openpa.impl;

import com.powerdata.openpa.ACBranch;
import com.powerdata.openpa.ACBranchListIfc;
import com.powerdata.openpa.ColumnMeta;

public abstract class ACBranchListI<T extends ACBranch> extends
		TwoTermDevListI<T> implements ACBranchListIfc<T>
{
	protected ACBranchListI()
	{
		super();
	}
	FloatData _r, _x;
	protected ACBranchListI(PAModelI model, int[] keys, ACBranchEnum le)
	{
		super(model, keys, le);
		setFields(le);
	}
	protected ACBranchListI(PAModelI model, int size, ACBranchEnum le)
	{
		super(model, size, le);
		setFields(le);
	}
	private void setFields(ACBranchEnum le)
	{
		_r = new FloatData(le.r());
		_x = new FloatData(le.x());
	}
	@Override
	public float getR(int ndx)
	{
		return _r.get(ndx);
	}
	@Override
	public void setR(int ndx, float r)
	{
		_r.set(ndx, r);
	}
	@Override
	public float[] getR()
	{
		return _r.get();
	}
	@Override
	public void setR(float[] r)
	{
		_r.set(r);
	}
	@Override
	public float getX(int ndx)
	{
		return _x.get(ndx);
	}
	@Override
	public void setX(int ndx, float x)
	{
		_x.set(ndx, x);
	}
	@Override
	public float[] getX()
	{
		return _x.get();
	}
	@Override
	public void setX(float[] x)
	{
		_x.set(x);
	}

	interface ACBranchEnum extends TwoTermDevEnum
	{
		ColumnMeta r();
		ColumnMeta x();
	}
}

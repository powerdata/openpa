package com.powerdata.openpa.pwrflow;
/*
 * Copyright (c) 2016, PowerData Corporation, Incremental Systems Corporation
 * All rights reserved.
 * Licensed under the BSD-3 Clause License.
 * See full license at https://powerdata.github.io/openpa/LICENSE.md
 */

import java.util.AbstractList;
import com.powerdata.openpa.Bus;
import com.powerdata.openpa.BusList;
import com.powerdata.openpa.BusRefIndex;
import com.powerdata.openpa.FixedShunt;
import com.powerdata.openpa.FixedShuntListIfc;
import com.powerdata.openpa.PAModelException;
import com.powerdata.openpa.OneTermBaseList.OneTermBase;
import com.powerdata.openpa.tools.PAMath;
import com.powerdata.openpa.tools.matrix.JacobianElement;
import com.powerdata.openpa.tools.matrix.JacobianList;
import com.powerdata.openpa.tools.matrix.JacobianMatrix;

public class FixedShuntCalcList extends AbstractList<com.powerdata.openpa.pwrflow.FixedShuntCalcList.FixedShuntCalc>
{
	public class FixedShuntCalc implements OneTermBase
	{
		int _ndx;
		public FixedShuntCalc(int ndx)
		{
			_ndx = ndx;
		}
		@Override
		public int getIndex()
		{
			return _ndx;
		}

		@Override
		public Bus getBus() throws PAModelException
		{
			return FixedShuntCalcList.this.getBus(_ndx);
		}
		
		public float getQpu() {return FixedShuntCalcList.this.getQpu(_ndx);}
		public FixedShunt getShunt()
		{
			return FixedShuntCalcList.this.getShunt(_ndx);
		}
	}
	
	class FSJacList extends AbstractList<JacobianElement> implements JacobianList
	{
		@Override
		public float getDpda(int ndx) {return 0f;}
		@Override
		public float getDpdv(int ndx) {return 0f;}
		@Override
		public float getDqda(int ndx) {return 0f;}
		@Override
		public float getDqdv(int ndx) {return _jac[ndx];}
		@Override
		public void setDpda(int ndx, float v) {}
		@Override
		public void setDpdv(int ndx, float v) {}
		@Override
		public void setDqda(int ndx, float v) {}
		@Override
		public void setDqdv(int ndx, float v) {_jac[ndx] = v;}
		@Override
		public void reset() {}
		@Override
		public JacobianElement get(int index)
		{
			return new JacobianList.Element(this, index);
		}
		@Override
		public int size() {return _src.size();}
	}

	FixedShuntListIfc<? extends FixedShunt> _src;
	BusList _buses;
	int[] _buslist;
	float[] _q, _b, _jac;
	//TODO: handle SBASE more intelligently
	float _sbase = 100f;
	
	public FixedShuntCalcList(FixedShuntListIfc<? extends FixedShunt> src,
			BusRefIndex bri) throws PAModelException
	{
		_src = src;
		_buses = bri.getBuses();
		_buslist = bri.get1TBus(src);
		_b = PAMath.mva2pu(src.getB(), _sbase);
	}
	
	public JacobianList getJacobianList() {return new FSJacList();}
	
	public FixedShunt getShunt(int ndx)
	{
		return _src.get(ndx);
	}

	public Bus getBus(int ndx) throws PAModelException
	{
		return _buses.get(_buslist[ndx]);
	}

	public float getQpu(int ndx)
	{
		return _q[ndx];
	}

	public FixedShuntCalcList calc(float[] vmpu) throws PAModelException
	{
		int n = size();
		_q = new float[n];
		_jac = new float[n];
		for(int i=0; i < n; ++i)
		{
			float vm = vmpu[_buslist[i]];
			float t = _b[i] * vm;
			_q[i] = t * vm;
			_jac[i] = t * 2f;
		}
		return this;
	}

	public void applyJacobian(JacobianMatrix jm)
	{
		int n = size();
		JacobianList jlist = getJacobianList();
		for(int i=0; i < n; ++i)
		{
			int b = _buslist[i];
			jm.subValue(b, b, jlist.get(i));
		}
	}
	
	public void applyMismatches(Mismatch qmm) throws PAModelException
	{
		applyMismatches(qmm.get());
	}
	public void applyMismatches(float[] mm) throws PAModelException
	{
		int n = size();
		for(int i=0; i < n; ++i)
		{
			mm[_buslist[i]] -= _q[i]; 
		}
	}

	@Override
	public int size()
	{
		return _src.size();
	}

	@Override
	public FixedShuntCalc get(int index)
	{
		return new FixedShuntCalc(index);
	}

	public void update() throws PAModelException
	{
		_src.setQ(PAMath.pu2mva(_q, _sbase));
	}
	
}

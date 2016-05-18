package com.powerdata.openpa.se;
/*
 * Copyright (c) 2016, PowerData Corporation, Incremental Systems Corporation
 * All rights reserved.
 * Licensed under the BSD-3 Clause License.
 * See full license at https://powerdata.github.io/openpa/LICENSE.md
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import com.powerdata.openpa.Bus;
import com.powerdata.openpa.BusList;
import com.powerdata.openpa.BusRefIndex;
import com.powerdata.openpa.PAModel;
import com.powerdata.openpa.PAModelException;
import com.powerdata.openpa.PflowModelBuilder;
import com.powerdata.openpa.TwoTermDev;
import com.powerdata.openpa.impl.EmptyLists;
import com.powerdata.openpa.pwrflow.BusType;
import com.powerdata.openpa.pwrflow.BusTypeUtil;
import com.powerdata.openpa.se.MeasMgr.MeasType;
import com.powerdata.openpa.tools.PAMath;
import com.powerdata.openpa.tools.matrix.FloatMatrix;
import com.powerdata.openpa.tools.matrix.MatrixDebug;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Estimate power network state using Given's Rotation
 * 
 * @author chris@powerdata.com
 *
 */
public class StateEstimator
{
	PAModel _model;
	BusRefIndex _bri;
	BusTypeUtil _btu;
	boolean _dbg1=true;
	
	public StateEstimator(PAModel m, BusRefIndex bri) throws PAModelException
	{
		_model = m;
		_bri = bri;
		_btu = new BusTypeUtil(m, bri);
		System.err.format("Ref: %s\n", Arrays.toString(_btu.getBuses(BusType.Reference)));
	}

	
	public void runSE() throws PAModelException
	{
		try
		{
		boolean nconv = true;
		MeasMgr mmgr = new MeasMgr(_model, _bri);
//		mmgr.setNoisy(true);
		BusList buses = _bri.getBuses();
		final int nbus = buses.size();
		float[] vm = PAMath.vmpu(buses);
		float[] va = PAMath.deg2rad(buses.getVA());
		int[] ref = _btu.getBuses(BusType.Reference);
		while (nconv)
		{
		FloatMatrix hhx = mmgr.getGainMatrix(vm, va);
		final int[] map = new int[hhx.getColumnCount() - ref.length * 2];
		FloatMatrix hh = new FloatMatrix()
		{
			
			{
				TIntSet rset = new TIntHashSet();
				for (int x : ref)
				{
					rset.add(x);
					rset.add(x + nbus);
				}
				Arrays.sort(ref);
				int nbus = buses.size();
				int n = 0;
				for (int i = 0; i < nbus * 2; ++i)
				{
					if (!rset.contains(i))
					{
						map[n++] = i;
					}
				}
			}
			@Override
			public int getRowCount()
			{
				return hhx.getRowCount();
			}
			@Override
			public int getColumnCount()
			{
				return map.length;
			}
			@Override
			public void setValue(int row, int column, float value)
			{
				hhx.setValue(row, map[column], value);
			}
			@Override
			public float getValue(int row, int column)
			{
				return hhx.getValue(row, map[column]);
			}
			@Override
			public void addValue(int row, int column, float value)
			{
				hhx.addValue(row, map[column], value);
			}
			@Override
			public void multValue(int row, int column, float value)
			{
				hhx.multValue(row, map[column], value);
			}
		};
		final String[] measnm = new String[mmgr.getMeasCount()];
		final MeasType[] meastype = new MeasType[mmgr.getMeasCount()];
		mmgr.iterateMeasVector((r,o,t,v)->{measnm[r]=o.getID();meastype[r]=t;});
		PrintWriter wmeas = new PrintWriter(new BufferedWriter(new FileWriter("/run/shm/measjac.csv")));
		FloatMatrix.wrap(hh).dump(wmeas, new MatrixDebug<Float>(){

			@Override
			public String getRowID(int ir) throws PAModelException
			{
				return String.valueOf(String.format("%s,%s", measnm[ir], meastype[ir].toString()));
			}

			@Override
			public String getColumnID(int col) throws PAModelException
			{
				return String.valueOf(map[col]+1);
			}});
		wmeas.close();
		float[] dz = mmgr.getDz(vm, va);
		int nstate = hh.getColumnCount();
		int nmeas = mmgr.getMeasCount();
		for (int i = 0; i < nstate; ++i)
		{
			for (int j = i + 1; j < nmeas; ++j)
			{
				float zpivot = dz[i];
				float ztarget = dz[j];
				float hhji = hh.getValue(j, i);
				float hhii = hh.getValue(i, i);
				float hpt = (float) Math.hypot(hhji, hhii);
				if (hpt != 0)
				{
					float c = hhii / hpt;
					float s = hhji / hpt;
					for (int k = 0; k < nstate; ++k)
					{
						float hpivotk = hh.getValue(i, k);
						float htargetk = hh.getValue(j, k);
						float newival = hpivotk * c + htargetk * s;
						float newjval = hpivotk * -s + htargetk * c;
						hh.setValue(i, k, newival);
						hh.setValue(j, k, newjval);
					}
					dz[i] = zpivot * c + ztarget * s;
					dz[j] = zpivot * -s + ztarget * c;
				}
			}
		}
		
		if(_dbg1)
		{
			_dbg1 = false;
		PrintWriter pwd = new PrintWriter(new BufferedWriter(new FileWriter("/run/shm/Udbg.csv")));
		FloatMatrix.wrap(hh).dump(pwd, new MatrixDebug<Float>()
		{
			@Override
			public String getRowID(int ir) throws PAModelException
			{
				return String.valueOf(ir+1);
			}

			@Override
			public String getColumnID(int col) throws PAModelException
			{
				return String.valueOf(col+1);
			}});
		pwd.close();
		}
		
		float[] dx = new float[nstate];
		int i = nstate-1;
		float tii = hh.getValue(i, i);
		if (tii != 0f)
			dx[i] = dz[i]/tii;
		--i;
		for(; i >= 0; --i)
		{
			float summ = 0;
			for(int j = i+1; j < nstate; ++j)
				summ += hh.getValue(i, j) * dx[j];
			tii = hh.getValue(i, i);
			if (tii != 0f) // System.err.format("0 div at %d %d\n", i, i);
				dx[i] = (dz[i] - summ) / tii;
		}
		
		float normdx = 0f;
		int nmap = map.length;
		for(int im=0; im < nmap; ++im)
		{
			float n = Math.abs(dx[im]);
			if (n > normdx) normdx = n;
		}
		
		if(normdx < .0001f)
		{
			System.out.format("Converged: %f\n", normdx);
			nconv = false;
		}
		if(nconv)
		{
		System.out.format("normdx %f\n", normdx);
		
		int n = nmap/2;
		for(int im=0; im < n; ++im)
			va[map[im]] += dx[im];
		for(int im=n; im <nmap; ++im)
			vm[map[im]-nbus] += dx[im];
		}

//		nconv = false;
		}
		
			PrintWriter rw = new PrintWriter(
				new BufferedWriter(new FileWriter("/run/shm/se.csv")));
			rw.println("row,id,type,bus,meas,est");
			int nmeas = mmgr.getMeasCount();
			float[] z = new float[nmeas];
			mmgr.iterateMeasVector((r,o,t,v)->z[r]=v);
			mmgr.iterateEstVector(va, vm, (r,o,t,v)->
			{
				Bus b = null;
				if(TwoTermDev.class.isInstance(o))
				{
					TwoTermDev ttd = (TwoTermDev)o;
					b = (t == MeasType.EstFromP || t == MeasType.EstFromQ) ? ttd.getFromBus() : ttd.getToBus(); 
				}
				rw.format("%d,%s,%s,%s,%f,%f\n",
					r, o.getID(), t.toString(), (b == null)? "" : b.getID(), z[r], v); 
			});
			rw.close();
		}
		catch(IOException ioe) {ioe.printStackTrace();}
		
	}	
	public static void main(String...args) throws PAModelException
	{
		/** The URI is a subsystem-independent way to specify how to access source model data */
		String uri = null;
		/** Output directory for any debug or report files */
		File outdir = new File(System.getProperty("user.dir"));
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "uri":
					uri = args[i++];
					break;
				case "outdir":
					outdir = new File(args[i++]);
					break;
			}
		}
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri "
					+ "[ --outdir output_directory (deft to $CWD ]\n");
			System.exit(1);
		}
		if (!outdir.exists()) outdir.mkdirs();
		
		/**A ModelBuilder object is used to build one or more models */
		PflowModelBuilder bldr = PflowModelBuilder.Create(uri);
		bldr.enableFlatVoltage(true);
		/** load (build) the model */
		PAModel m = bldr.load();
		
		/**
		 * This tool allows for a uniform interface to access buses related to a
		 * device regardless of topology. We are choosing a single-bus topology in this
		 * case.
		 */
		BusRefIndex bri = BusRefIndex.CreateFromSingleBuses(m);
		
		 StateEstimator se = new StateEstimator(m, bri);
		 se.runSE();
		 

	}
	
}

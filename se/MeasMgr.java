package com.powerdata.openpa.se;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.powerdata.openpa.ACBranch;
import com.powerdata.openpa.ACBranchList;
import com.powerdata.openpa.BaseList;
import com.powerdata.openpa.BaseObject;
import com.powerdata.openpa.Bus;
import com.powerdata.openpa.BusList;
import com.powerdata.openpa.BusRefIndex;
import com.powerdata.openpa.FixedShunt;
import com.powerdata.openpa.FixedShuntList;
import com.powerdata.openpa.Gen;
import com.powerdata.openpa.Line;
import com.powerdata.openpa.Load;
import com.powerdata.openpa.PAModel;
import com.powerdata.openpa.PAModelException;
import com.powerdata.openpa.PflowModelBuilder;
import com.powerdata.openpa.SubLists;
import com.powerdata.openpa.Transformer;
import com.powerdata.openpa.pwrflow.ACBranchFlows;
import com.powerdata.openpa.pwrflow.ACBranchFlows.ACBranchFlow;
import com.powerdata.openpa.tools.PAMath;
import com.powerdata.openpa.tools.matrix.ArrayJacobianMatrix;
import com.powerdata.openpa.tools.matrix.FloatArrayMatrix;
import com.powerdata.openpa.tools.matrix.FloatMatrix;
import com.powerdata.openpa.tools.matrix.JacobianElement;
import com.powerdata.openpa.tools.matrix.JacobianMatrix;
import com.powerdata.openpa.tools.matrix.MatrixDebug;
import com.powerdata.openpa.pwrflow.ACBranchFlowsI;
import com.powerdata.openpa.pwrflow.ACBranchJacobianList;
import com.powerdata.openpa.pwrflow.ACBranchJacobianList.ACBranchJacobian;
import com.powerdata.openpa.pwrflow.FixedShuntCalcList;
import com.powerdata.openpa.pwrflow.FixedShuntCalcList.FixedShuntCalc;

public class MeasMgr
{
	@FunctionalInterface
	public interface FloatMeasConsumer
	{
		void accept(int row, BaseObject obj, MeasType mtype, float value) throws PAModelException;
	}
	@FunctionalInterface
	public interface JacobianConsumer
	{
		void accept(int row,  BaseObject obj, MeasType mtype, JacobianElement value) throws PAModelException;
	}
	@FunctionalInterface
	interface MeasGen<T>
	{
		int generate(T obj, int row) throws PAModelException;
	}
	@FunctionalInterface
	private interface IdGen<T>
	{
		String getid(T obj) throws PAModelException;
	}
	
	
	private class BusInjection
	{
		float p,q;
		BusInjection(float p, float q)
		{
			this.p = p;
			this.q = q;
		}
		float getP(){return p;}
		float getQ(){return q;}
	}
	
	private abstract class MeasGenHolder
	{
		int bofs = _nbr*2+_nbus;
		FloatMeasConsumer flc;
		MeasGenHolder(FloatMeasConsumer flc) {this.flc = flc;}
		abstract MeasGen<ACBranch> getBranchGen();
		abstract MeasGen<Bus> getInjGen();
	};

	class MeasFromData extends MeasGenHolder
	{
		MeasFromData(FloatMeasConsumer flc)
		{
			super(flc);
		}
		@Override
		MeasGen<ACBranch> getBranchGen()
		{
			return new MeasGen<ACBranch>()
			{
				@Override
				public int generate(ACBranch obj, int row) throws PAModelException
				{
					int r2 = row + _nbr, r3 = r2 + _nbr + _nbus, r4 = r3 + _nbr;
					flc.accept(row++, obj, MeasType.TelemFromP,
						PAMath.mva2pu(obj.getFromP(), _sbase));
					flc.accept(r2, obj, MeasType.TelemToP,
						PAMath.mva2pu(obj.getToP(), _sbase));
					flc.accept(r3, obj, MeasType.TelemFromQ,
						PAMath.mva2pu(obj.getFromQ(), _sbase));
					flc.accept(r4, obj, MeasType.TelemToQ,
						PAMath.mva2pu(obj.getToQ(), _sbase));
					return row;
				}
			};
		}
		@Override
		MeasGen<Bus> getInjGen()
		{
			return new MeasGen<Bus>()
			{
				@Override
				public int generate(Bus obj, int row) throws PAModelException
				{
					BusInjection inj = getMeasBusInj(obj);
					int r2 = row + bofs;
					flc.accept(row++, obj, MeasType.TelemBusInjP,
						PAMath.mva2pu(inj.getP(), _sbase));
					flc.accept(r2, obj, MeasType.TelemBusInjQ,
						PAMath.mva2pu(inj.getQ(), _sbase));
					return row;
				}
			};
		}
	};	

	class NoisyMeas extends MeasGenHolder
	{
		Random _rand = new Random(System.nanoTime());
		NoisyMeas(FloatMeasConsumer flc)
		{
			super(flc);
		}
		@Override
		MeasGen<ACBranch> getBranchGen()
		{
			return new MeasGen<ACBranch>()
			{
				@Override
				public int generate(ACBranch obj, int row) throws PAModelException
				{
					int r2 = row + _nbr, r3 = r2 + _nbr + _nbus, r4 = r3 + _nbr;
					flc.accept(row++, obj, MeasType.TelemFromP,
						((float) _rand.nextGaussian()) * PAMath.mva2pu(obj.getFromP(), _sbase));
					flc.accept(r2, obj, MeasType.TelemToP,
						((float) _rand.nextGaussian()) * PAMath.mva2pu(obj.getToP(), _sbase));
					flc.accept(r3, obj, MeasType.TelemFromQ,
						((float) _rand.nextGaussian()) * PAMath.mva2pu(obj.getFromQ(), _sbase));
					flc.accept(r4, obj, MeasType.TelemToQ,
						((float) _rand.nextGaussian()) * PAMath.mva2pu(obj.getToQ(), _sbase));
					return row;
				}
			};
		}
		@Override
		MeasGen<Bus> getInjGen()
		{
			return new MeasGen<Bus>()
			{
				@Override
				public int generate(Bus obj, int row) throws PAModelException
				{
					BusInjection inj = getMeasBusInj(obj);
					int r2 = row + bofs;
					flc.accept(row++, obj, MeasType.TelemBusInjP,
						((float) _rand.nextGaussian()) * PAMath.mva2pu(inj.getP(), _sbase));
					flc.accept(r2, obj, MeasType.TelemBusInjQ,
						((float) _rand.nextGaussian()) * PAMath.mva2pu(inj.getQ(), _sbase));
					return row;
				}
			};
		}
	};	
	
	int _nmeas;
	int _nbr;
	int _nbus;
	private PAModel _model;
	private BusRefIndex _bri;
	private List<ACBranchList> _branches;
	private List<ACBranchFlows> _flows;
	private List<ACBranchJacobianList> _jac;
	private List<FixedShuntCalcList> _fscalc;
	private List<int[]> _index;
	float[] _conf;
	final float _sbase;
	MeasGenHolder _mgen;
	boolean _noise = false;
	
	public MeasMgr(PAModel m, BusRefIndex bri) throws PAModelException
	{
		_model = m;
		_sbase = m.getSBASE();
		_bri = bri;
		
		/* make a list of in-service offsets for sublists later on */
		List<ACBranchList> branches = m.getACBranches();
		
		int nbrlist = branches.size();
		_branches = new ArrayList<>(nbrlist);
		_flows = new ArrayList<>(nbrlist);
		_jac = new ArrayList<>(nbrlist);
		_index = new ArrayList<>(nbrlist);
		for(ACBranchList list : branches)
		{
			_index.add(SubLists.getInServiceIndexes(list));
			_branches.add(list);
			ACBranchFlows flows = new ACBranchFlowsI(list, bri);
			_flows.add(flows);
			_jac.add(new ACBranchJacobianList(flows));
		}
		
		List<FixedShuntList> fshlists = m.getFixedShunts();
		_fscalc = new ArrayList<>(fshlists.size());
		for(FixedShuntList list : fshlists)
			_fscalc.add(new FixedShuntCalcList(list, _bri));
			
		
		_nbr = _branches.stream().mapToInt(ACBranchList::size).sum();
		_nbus = bri.getBuses().size();
		_nmeas = _nbr * 4 + _nbus * 2;
		_conf = new float[_nmeas];
		Arrays.fill(_conf, 0.01f);
		
		
	}
	
	public void setNoisy(boolean noisy) {_noise = noisy;}
	public boolean getNoisy() {return _noise;}
	
	public int getMeasCount() {return _nmeas;}

	private <T> void iterateVector(List<? extends List<T>> lists, MeasGen<T> lgen, MeasGen<Bus> busgen) throws PAModelException
	{
		int row = 0;
		int nlist = lists.size();
		for(int li=0; li < nlist; ++li)
		{
			int[] idx = _index.get(li);
			int nidx = idx.length;
			List<T> list = lists.get(li);
			for(int ii=0; ii < nidx; ++ii)
			{
				T elem = list.get(idx[ii]);
				row = lgen.generate(elem, row);
			}
		}
		row += _nbr;
		for(Bus b : _bri.getBuses())
		{
			row = busgen.generate(b, row);
		}
	}
	
	public FloatMatrix getGainMatrix(float[] vm, float[] va) throws PAModelException
	{
		FloatMatrix h = new FloatArrayMatrix(_nmeas, _nbus*2);
		JacobianMatrix jm = new ArrayJacobianMatrix(_nbus, _nbus);
		for(ACBranchJacobianList l : _jac) l.calc(vm, va).apply(jm);
		for(FixedShuntCalcList l : _fscalc) l.calc(vm).applyJacobian(jm);

		iterateVector(_jac, 
		new MeasGen<ACBranchJacobian>()
		{
			@Override
			public int generate(ACBranchJacobian j, int row) throws PAModelException
			{
				int r2 = row + _nbr, r3 = r2 + _nbr + _nbus,r4 = r3 + _nbr; 
				JacobianElement jacfs = j.getFromSelf();
				JacobianElement jacfm = j.getFromMutual();
				int fbus = j.getFromBus().getIndex();
				int tbus = j.getToBus().getIndex();
				float conf = _conf[row];
				h.addValue(row, fbus, jacfs.getDpda()/conf);
				h.addValue(row, tbus, jacfm.getDpda()/conf);
				h.addValue(row, fbus+_nbus, jacfs.getDpdv()/conf);
				h.addValue(row, tbus+_nbus, jacfm.getDpdv()/conf);
				
				JacobianElement jacts = j.getToSelf();
				JacobianElement jactm = j.getToMutual();
				conf = _conf[r2];
				h.addValue(r2, fbus, jactm.getDpda() / conf);
				h.addValue(r2, tbus, jacts.getDpda() / conf);
				h.addValue(r2, fbus + _nbus, jactm.getDpdv() / conf);
				h.addValue(r2, tbus + _nbus, jacts.getDpdv() / conf);

				conf = _conf[r3];
				h.addValue(r3, fbus, jacfs.getDqda() / conf);
				h.addValue(r3, tbus, jacfm.getDqda() / conf);
				h.addValue(r3, fbus + _nbus, jacfs.getDqdv() / conf);
				h.addValue(r3, tbus + _nbus, jacfm.getDqdv() / conf);

				conf = _conf[r4];
				h.addValue(r4, fbus, jactm.getDqda() / conf);
				h.addValue(r4, tbus, jacts.getDqda() / conf);
				h.addValue(r4, fbus + _nbus, jactm.getDqdv() / conf);
				h.addValue(r4, tbus + _nbus, jacts.getDqdv() / conf);
				
				return row+1;
			}
		},
		
		new MeasGen<Bus>()
		{
			int ofs = _nbr*2+_nbus;
			@Override
			public int generate(Bus obj, int row) throws PAModelException
			{
				int r2 = row + ofs;
				float cr1 = _conf[row], cr2 = _conf[r2];
				for(int i=0; i < _nbus; ++i)
				{
					int i2 = i+_nbus;
					JacobianElement e = jm.getValue(obj.getIndex(), i);
					h.addValue(row, i, e.getDpda()/cr1);
					h.addValue(row, i2, e.getDpdv()/cr1);
					h.addValue(r2, i, e.getDqda()/cr2);
					h.addValue(r2, i2, e.getDqdv()/cr2);
				}
				return row+1;
			}
		});
		
		return h;
	}
	
	public void iterateMeasVector(FloatMeasConsumer flc) throws PAModelException
	{
		MeasGenHolder mh = _noise ? new NoisyMeas(flc) : new MeasFromData(flc);
		iterateVector(_branches, mh.getBranchGen(), mh.getInjGen()); 
	}
	
	public void iterateEstVector(float[] va, float[] vm, FloatMeasConsumer flc) throws PAModelException
	{
		//TODO:  should we move the calculations outside for more control?
		for(ACBranchFlows list : _flows) list.calc(vm, va);
		float[] pmm = new float[_nbus], qmm = new float[_nbus];
		for(FixedShuntCalcList list : _fscalc)
			list.calc(vm).applyMismatches(qmm);
		iterateVector(_flows,
		new MeasGen<ACBranchFlow>()
		{
			@Override
			public int generate(ACBranchFlow obj, int row) throws PAModelException
			{
				int r2 = row + _nbr, r3 = r2 + _nbr + _nbus,r4 = r3 + _nbr; 
				int f = obj.getFromBus().getIndex();
				int t = obj.getToBus().getIndex();
				float fp = obj.getFromPpu(), tp = obj.getToPpu();
				float fq = obj.getFromQpu(), tq = obj.getToQpu();
				pmm[f] += fp;
				pmm[t] += tp;
				qmm[f] += fq;
				qmm[t] += tq;
				ACBranch br = obj.getBranch();
				flc.accept(row++, br, MeasType.EstFromP, fp);
				flc.accept(r2, br, MeasType.EstToP, tp);
				flc.accept(r3, br, MeasType.EstFromQ, fq);
				flc.accept(r4, br, MeasType.EstToQ, tq);
				return row;
			}
		},
		new MeasGen<Bus>()
		{
			int ofs = _nbr*2+_nbus;
			@Override
			public int generate(Bus obj, int row) throws PAModelException
			{
				int r2 = row + ofs;
				int idx = obj.getIndex();
				flc.accept(row++, obj, MeasType.EstBusInjP, pmm[idx]);
				flc.accept(r2, obj, MeasType.EstBusInjQ, qmm[idx]);
				return row;
			}
		});
	}

	private BusInjection getMeasBusInj(Bus b) throws PAModelException
	{
		float p = 0f, q = 0f;
		for(Gen g : b.getGenerators())
		{
			if(g.isInService())
			{
				p += g.getP();
				q += g.getQ();
			}
		}
		for(Load g : b.getLoads())
		{
			if(g.isInService())
			{
				p += g.getP();
				q += g.getQ();
			}
		}
//		for(FixedShuntList list : b.getFixedShunts())
//		{
//			for(FixedShunt sh : list)
//			{
//				if (sh.isInService())
//					q += sh.getQ();
//			}
//		}
		//TODO:  add SVC's
		return new BusInjection(p, q);
	}
	
	float[] getDz(float[] vm, float[] va) throws PAModelException
	{
		int nmeas = getMeasCount();
		float[] rv = new float[nmeas];
		for(FixedShuntCalcList l : _fscalc)
			l.calc(vm).update();
		iterateMeasVector((r,o,t,v)-> rv[r] = v);
		iterateEstVector(va, vm, (r,o,t,v)-> rv[r] -= v);
		for(int i=0; i < nmeas; ++i)
			rv[i] /= _conf[i];
		return rv;
	}
	
	enum MeasType
	{
		TelemFromP, TelemToP, TelemFromQ, TelemToQ, TelemBusInjP, TelemBusInjQ,
		EstFromP, EstToP, EstFromQ, EstToQ, EstBusInjP, EstBusInjQ;
	}
	
	public static void main(String...args) throws Exception
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
		PAModel model = PflowModelBuilder.Create(uri).load();
		BusRefIndex bri = BusRefIndex.CreateFromSingleBuses(model);
		
		MeasMgr mmgr = new MeasMgr(model, bri);
		String[] rowid = new String[mmgr.getMeasCount()];
		PrintWriter zcm = new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir,"newz.csv"))));
		BusList buses = bri.getBuses();
		int nbus = buses.size();
		float[] vm = PAMath.vmpu(buses);
		float[] va = PAMath.deg2rad(buses.getVA());
		mmgr.iterateMeasVector((r, o, t, v) -> 
		{
			zcm.format("%d,%s,%s,%f\n", r, o.getID(), t.toString(), v);
			rowid[r] = o.getID();
		});
		zcm.close();
		PrintWriter zecm = new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir,"newzest.csv"))));
		mmgr.iterateEstVector(va, vm,
			(r,o,t,v) -> zecm.format("%d,%s,%s,%f\n", r, o.getID(), t.toString(), v));
		zecm.close();
		
		PrintWriter hw = new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, "newh.csv"))));
		FloatMatrix.wrap(mmgr.getGainMatrix(vm, va)).dump(hw, new MatrixDebug<Float>()
		{
			@Override
			public String getRowID(int ir) throws PAModelException
			{
				return rowid[ir];
			}

			@Override
			public String getColumnID(int col) throws PAModelException
			{
				if (col >= nbus) col -= nbus;
				return buses.get(col).getID();
			}});
		hw.close();
	}
	
}

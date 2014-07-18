package com.powerdata.openpa;

import com.powerdata.openpa.impl.LoadListI;

public interface LoadList extends OneTermDevListIfc<Load>
{

	static final LoadList Empty = new LoadListI();

	float getMaxP(int ndx);

	void setMaxP(int ndx, float mw);

	float[] getMaxP();
	
	void setMaxP(float[] mw);
	
	float getMaxQ(int ndx);

	void setMaxQ(int ndx, float mvar);
	
	float[] getMaxQ();
	
	void setMaxQ(float[] mvar);

}

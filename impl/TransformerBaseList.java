package com.powerdata.openpa.impl;

import com.powerdata.openpa.ACBranchListIfc;

public interface TransformerBaseList<T extends TransformerBase> extends ACBranchListIfc<T>
{

	float getGmag(int ndx);

	void setGmag(int ndx, float g);
	
	float[] getGmag();
	
	void setGmag(float[] g);

	float getBmag(int ndx);

	void setBmag(int ndx, float b);
	
	float[] getBmag();
	
	void setBmag(float[] b);

	float getShift(int ndx);

	void setShift(int ndx, float sdeg);
	
	float[] getShift();
	
	void setShift(float[] sdeg);

	float getFromTap(int ndx);

	void setFromTap(int ndx, float a);
	
	float[] getFromTap();
	
	void setFromTap(float[] a);

	float getToTap(int ndx);

	void setToTap(int ndx, float a);
	
	float[] getToTap();
	
	void setToTap(float[] a);

}

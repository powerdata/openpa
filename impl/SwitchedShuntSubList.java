package com.powerdata.openpa.impl;

import com.powerdata.openpa.ListMetaType;
import com.powerdata.openpa.SwitchedShunt;
import com.powerdata.openpa.SwitchedShuntList;
/* TODO:  move this out of the "PSR" or one-term device category */
public class SwitchedShuntSubList extends ShuntSubList<SwitchedShunt> implements SwitchedShuntList
{

	public SwitchedShuntSubList(SwitchedShuntList src, int[] ndx)
	{
		super(src, ndx);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SwitchedShunt get(int index)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListMetaType getMetaType()
	{
		return ListMetaType.SwitchedShunt;
	}
	
}

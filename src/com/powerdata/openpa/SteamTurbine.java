package com.powerdata.openpa;
/*
 * Copyright (c) 2016, PowerData Corporation, Incremental Systems Corporation
 * All rights reserved.
 * Licensed under the BSD-3 Clause License.
 * See full license at https://powerdata.github.io/openpa/LICENSE.md
 */


public class SteamTurbine extends AbstractBaseObject
{
	public enum SteamSupply {Unknown, BWR, PWR, Coal};

	SteamTurbineList _list;
	public SteamTurbine(SteamTurbineList list, int ndx)
	{
		super(list, ndx);
		_list = list;
	}
	
	SteamSupply getSteamSupply() throws PAModelException
	{
		return _list.getSteamSupply(_ndx);
	}
	void setSteamSupply(int ndx, SteamSupply v) throws PAModelException
	{
		_list.setSteamSupply(_ndx, v);
	}

}

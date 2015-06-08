package com.powerdata.openpa.tools.psmfmt;

public enum RatioTapChanger implements VersionedDoc
{
	ID, Name, TransformerWinding, TapNode, MinTap, MaxTap, NeutralTap, StepSize, MinKV, MaxKV, NeutralKV;
	
	@Override
	public String getVersion() {return "1.10";}
}

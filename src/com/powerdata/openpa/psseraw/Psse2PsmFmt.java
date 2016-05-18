package com.powerdata.openpa.psseraw;
/*
 * Copyright (c) 2016, PowerData Corporation, Incremental Systems Corporation
 * All rights reserved.
 * Licensed under the BSD-3 Clause License.
 * See full license at https://powerdata.github.io/openpa/LICENSE.md
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class converts a PSS/e raw file to the PowerSimulator Model and Case
 * formats
 * 
 */
public class Psse2PsmFmt extends PsseProcessor 
{
	@FunctionalInterface
	private interface ToolConstructor
	{
		PsseRecWriter apply(PsseClass pc, PsseRepository rep) throws IOException;
	}
	
	@FunctionalInterface
	private interface ToolConstructorBus
	{
		PsseRecWriter apply(PsseClass pc, File dir, Map<String,String> busMap) throws IOException;
	}
	
	private Map<String,PsseRecWriter> _wmap = new HashMap<>();
	PsseRepository _rep;
	static PsseRecWriter _defw = new PsseRecWriter()
	{
		@Override
		public void writeRecord(PsseClass pclass, String[] record) throws PsseProcException {}
	};
	
	/**
	 * Construct a Psse2PsmFmt object to convert PSS/e raw files to
	 * PowerSimulator CSV formats
	 * 
	 * @param rawpsse
	 *            The input PSS/e raw file
	 * @param specversion
	 *            Override the version for PSS/e files that may not have it in
	 *            the headers
	 * @param outdir
	 *            output directory for CSV files. If this directory does not
	 *            exist, it is created
	 * @throws IOException
	 * @throws PsseProcException
	 */
	public Psse2PsmFmt(Reader rawpsse, String specversion, File outdir) throws IOException,
			PsseProcException 
	{
		super(rawpsse, specversion);
		if(!outdir.exists()) outdir.mkdirs();
		_rep = new PsseRepository(outdir);
		setupWriters();
	}

	private void setupWriters() throws IOException
	{
		PsseClassSet pcs = getPsseClassSet();

		addToMap(pcs.getAreaInterchange(),		PsseAreaTool::new);
		addToMap(pcs.getOwner(),				PsseOwnerTool::new);
		addToMap(pcs.getLoad(),					PsseLoadTool::new);
		addToMap(pcs.getBus(),					PsseBusTool::new);
		addToMap(pcs.getGenerator(),			PsseGenTool::new);
		addToMap(pcs.getNontransformerBranch(),	PsseLineTool::new);
		addToMap(pcs.getSwitchedShunt(), 		PsseSwitchedShuntTool::new);
		addToMap(pcs.getTransformer(),			PsseTransformerTool::new);
	}

	private void addToMap(PsseClass psseClass, ToolConstructor tc) throws IOException
	{
		_wmap.put(psseClass.getClassName(), tc.apply(psseClass, _rep));
	}
	
	/**
	 * Test routine and command-line access
	 * 
	 * @param args
	 *            <p>
	 *            <table>
	 *            <tr>
	 *            <td class='cmdparmname'>--dir</td>
	 *            <td class='cmdparmdesc'>Output directory for CSV files. This
	 *            directory is created if it does not already exist</td>
	 *            </tr>
	 *            <tr>
	 *            <td class='cmdparmname'>--psse</td>
	 *            <td>Input PSS/e raw file</td>
	 *            </tr>
	 *            </table>
	 *            </p>
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		File outdir = null;
		File psse = null;
		
		int narg = args.length;
		for (int i = 0; i < narg;)
		{
			String a = args[i++];
			if (a.startsWith("-"))
			{
				int idx = (a.charAt(1) == '-') ? 2 : 1;
				switch (a.substring(idx))
				{
					case "d":
					case "dir":
						outdir = new File(args[i++]);
						System.out.println("[main] outdir = "+outdir.getName());
						break;
					case "p":
					case "psse":
						psse = new File(args[i++]);
						System.out.println("[main] psse = "+psse.getName());
						break;
					default:
						System.out.println("parameter " + a + " not understood");
				}
			}
		}
		
		if (psse == null)
		{
			System.err.println("Unable to locate PSS/E file");
		}
		
		if (!outdir.exists()) outdir.mkdirs();
		
		Reader psseReader = new BufferedReader(new FileReader(psse));
		Psse2PsmFmt toPsm = new Psse2PsmFmt(psseReader, "30", outdir);
		
		toPsm.process();
		psseReader.close();
		toPsm.cleanup();
		
	}

	/**
	 * Cleanup and flush any resources
	 */
	public void cleanup()
	{
		_rep.cleanup();
	}

	@Override
	protected PsseRecWriter getWriter(String psseClassName)
	{
		PsseRecWriter w = _wmap.get(psseClassName);
		if (w == null)
		{
			System.err.format("No writer found for %s\n", psseClassName);
			w = _defw;
		}
		return w;
	}
}
	

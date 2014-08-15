/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.console.events;

import thobe.logfileviewer.kernel.plugin.console.SubConsole;

/**
 * @author Thomas Obenaus
 * @source CEvt_DestroySubConsole.java
 * @date Aug 15, 2014
 */
public class CEvt_DestroySubConsole extends ConsoleEvent
{
	private SubConsole	subConsole;

	public CEvt_DestroySubConsole( SubConsole subConsole )
	{
		super( ConsoleEventType.DESTROY_SUBCONSOLE );
		this.subConsole = subConsole;
	}

	public SubConsole getSubConsole( )
	{
		return subConsole;
	}
}

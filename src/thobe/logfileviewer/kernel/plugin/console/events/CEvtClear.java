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

/**
 * @author Thomas Obenaus
 * @source CEvtClear.java
 * @date 25.06.2014
 */
public class CEvtClear extends ConsoleEvent
{
	public CEvtClear( )
	{
		super( ConsoleEventType.CLEAR );
	}
}

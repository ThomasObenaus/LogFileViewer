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
 * @source CEvnt_ScrollToLast.java
 * @date Jun 18, 2014
 */
public abstract class CEvt_Scroll extends ConsoleEvent
{
	public CEvt_Scroll( ConsoleEventType type )
	{
		super( type );
	}
}

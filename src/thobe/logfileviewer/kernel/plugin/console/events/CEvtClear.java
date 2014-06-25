/*
 *  Copyright (C) 2014, j.umbel. All rights reserved.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    j.umbel
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

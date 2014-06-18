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
 * @source CEvt_SetAutoScrollMode.java
 * @date Jun 18, 2014
 */
public class CEvt_SetAutoScrollMode extends CEvt_Scroll
{
	private boolean	enable;

	public CEvt_SetAutoScrollMode( boolean enable )
	{
		super( ConsoleEventType.SET_AUTOSCROLL_MODE );
		this.enable = enable;
	}

	public boolean isEnable( )
	{
		return enable;
	}
}

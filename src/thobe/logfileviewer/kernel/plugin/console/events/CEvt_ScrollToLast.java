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
 * @source CEvt_ScrollToLast.java
 * @date Jun 18, 2014
 */
public class CEvt_ScrollToLast extends CEvt_Scroll
{
	public CEvt_ScrollToLast( )
	{
		super( ConsoleEventType.SCROLL_TO_LAST );
	}
}

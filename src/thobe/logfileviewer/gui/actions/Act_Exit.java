/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui.actions;

import java.awt.event.ActionEvent;

import thobe.logfileviewer.LogFileViewerInfo;
import thobe.logfileviewer.gui.MainFrame;
import thobe.widgets.action.AbstrAction;

/**
 * @author Thomas Obenaus
 * @source Act_Exit.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class Act_Exit extends AbstrAction
{
	public static final String	KEY	= "EXIT";
	private MainFrame			mainframe;

	public Act_Exit( MainFrame mainframe )
	{
		super( "Exit", "Exit", "Exits " + LogFileViewerInfo.getAppName( ), "Exits " + LogFileViewerInfo.getAppName( ), null, null );
		this.mainframe = mainframe;
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		this.mainframe.exit( );
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

}

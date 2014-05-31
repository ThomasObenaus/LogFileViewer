/*
 *  Copyright (C) 2014,Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.gui.actions;

import java.awt.event.ActionEvent;

import thobe.logfileviewer.gui.MainFrame;
import thobe.widgets.action.AbstrAction;

/**
 * @author Thomas Obenaus
 * @source Act_OpenFile.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class Act_OpenFile extends AbstrAction
{
	public static final String	KEY	= "OPEN_FILE";
	private MainFrame			mainframe;

	public Act_OpenFile( MainFrame mainframe )
	{
		super( "Open File", "Open File", "Open a log-file", "Open a log-file", null, null );
		this.mainframe = mainframe;
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

}

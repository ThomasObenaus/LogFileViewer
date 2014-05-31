/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.kernel.LogFileViewerApp;
import thobe.logfileviewer.kernel.source.DataSource;
import thobe.logfileviewer.kernel.source.IpSource;
import thobe.logfileviewer.kernel.source.TraceSourceException;
import thobe.widgets.action.AbstrAction;
import thobe.widgets.action.ActionRegistry;

/**
 * @author Thomas Obenaus
 * @source Act_OpenConnection.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class Act_OpenConnection extends AbstrAction
{
	public static final String	KEY	= "OPEN_CONNECTION";
	private MainFrame			mainframe;

	public Act_OpenConnection( MainFrame mainframe )
	{
		super( "Open Connection", "Open Connection", "Open a connection", "Open a connection", null, null );
		this.mainframe = mainframe;
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		try
		{
			LogFileViewerApp app = this.mainframe.getApp( );
			DataSource dataSource = app.getDataSource( );
			dataSource.open( new IpSource( "127.0.0.1", 15000 ) );

			ActionRegistry.get( ).getAction( Act_Close.KEY ).setEnabled( true );
			ActionRegistry.get( ).getAction( Act_OpenConnection.KEY ).setEnabled( false );
			ActionRegistry.get( ).getAction( Act_OpenFile.KEY ).setEnabled( false );
		}
		catch ( TraceSourceException e )
		{
			JOptionPane.showMessageDialog( this.mainframe, "Unable to connect: " + e.getLocalizedMessage( ), "Connection failed", JOptionPane.ERROR_MESSAGE );
		}
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

}

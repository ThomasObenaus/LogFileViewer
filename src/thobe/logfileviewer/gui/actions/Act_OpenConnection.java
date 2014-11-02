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

import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.gui.dialogs.Dlg_OpenIpConnection;
import thobe.logfileviewer.kernel.LogFileViewerApp;
import thobe.logfileviewer.kernel.preferences.SourcePrefs;
import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.listeners.ILogStreamStateListener;
import thobe.widgets.action.AbstrAction;
import thobe.widgets.action.ActionRegistry;

/**
 * @author Thomas Obenaus
 * @source Act_OpenConnection.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class Act_OpenConnection extends AbstrAction implements ILogStreamStateListener
{
	public static final String	KEY	= "OPEN_CONNECTION";
	private MainFrame			mainframe;

	public Act_OpenConnection( MainFrame mainframe )
	{
		super( "Open Connection", "Open Connection", "Open a connection", "Open a connection", null, null );
		this.mainframe = mainframe;
		LogFileViewerApp app = this.mainframe.getApp( );
		LogStream logStream = app.getLogStream( );
		logStream.addLogStreamStateListener( this );
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		LogFileViewerApp app = this.mainframe.getApp( );
		SourcePrefs sourcePrefs = app.getPreferences( ).getSourcePreferences( );

		Dlg_OpenIpConnection dlg = new Dlg_OpenIpConnection( this.mainframe, sourcePrefs, app.getLogStreamConnector( ) );
		dlg.setVisible( true );
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return "Act_OpenConnection";
	}

	@Override
	public void onEOFReached( )
	{}

	@Override
	public void onOpened( )
	{
		ActionRegistry.get( ).getAction( Act_OpenConnection.KEY ).setEnabled( false );
	}

	@Override
	public void onClosed( )
	{
		ActionRegistry.get( ).getAction( Act_OpenConnection.KEY ).setEnabled( true );
	}

}

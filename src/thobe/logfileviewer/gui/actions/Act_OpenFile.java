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

import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.kernel.LogFileViewerApp;
import thobe.logfileviewer.kernel.source.logstream.LogStream;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamStateListener;
import thobe.widgets.action.AbstrAction;

/**
 * @author Thomas Obenaus
 * @source Act_OpenFile.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class Act_OpenFile extends AbstrAction implements ILogStreamStateListener
{
	public static final String	KEY	= "OPEN_FILE";
	private MainFrame			mainframe;

	public Act_OpenFile( MainFrame mainframe )
	{
		super( "Open File", "Open File", "Open a log-file", "Open a log-file", null, null );
		this.mainframe = mainframe;
		LogFileViewerApp app = this.mainframe.getApp( );
		LogStream logStream = app.getLogStream( );
		logStream.addLogStreamStateListener( this );
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		long elapsed = System.currentTimeMillis( );
		System.gc( );
		System.out.println( "Time For GC: " + ( ( System.currentTimeMillis( ) - elapsed ) / 1000.0 ) + "s" );
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return "Act_OpenFile";
	}

	@Override
	public void onEOFReached( )
	{}

	@Override
	public void onOpened( )
	{
		//		ActionRegistry.get( ).getAction( Act_OpenFile.KEY ).setEnabled( false );
	}

	@Override
	public void onClosed( )
	{
		//		ActionRegistry.get( ).getAction( Act_OpenFile.KEY ).setEnabled( true );
	}

}

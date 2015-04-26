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

import javax.swing.JOptionPane;

import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.kernel.LogFileViewerApp;
import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.logstream.LogStream;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamStateListener;
import thobe.widgets.action.AbstrAction;
import thobe.widgets.action.ActionRegistry;

/**
 * @author Thomas Obenaus
 * @source Act_Close.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class Act_Close extends AbstrAction implements ILogStreamStateListener
{
	public static final String	KEY	= "CLOSE";
	private MainFrame			mainframe;

	public Act_Close( MainFrame mainframe )
	{
		super( "Close", "Close", "Closes the open resource (file/connection)", "Closes the open resource (file/connection)", null, null );
		this.mainframe = mainframe;
		LogFileViewerApp app = this.mainframe.getApp( );
		LogStream logStream = app.getLogStream( );
		logStream.addLogStreamStateListener( this );
		this.setEnabled( false );
	}

	@Override
	public void actionPerformed( ActionEvent arg0 )
	{
		try
		{
			LogFileViewerApp app = this.mainframe.getApp( );
			LogStream logStream = app.getLogStream( );
			logStream.close( );
		}
		catch ( LogStreamException e )
		{
			JOptionPane.showMessageDialog( this.mainframe, "Failed to close: " + e.getLocalizedMessage( ), "Close failed", JOptionPane.ERROR_MESSAGE );
		}
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return "Act_Close";
	}

	@Override
	public void onEOFReached( )
	{}

	@Override
	public void onOpened( )
	{
		ActionRegistry.get( ).getAction( Act_Close.KEY ).setEnabled( true );
	}

	@Override
	public void onClosed( )
	{
		ActionRegistry.get( ).getAction( Act_Close.KEY ).setEnabled( false );
	}

}

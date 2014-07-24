/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.performance;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;

import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogLine;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;

/**
 * @author Thomas Obenaus
 * @source PerformanceMonitor.java
 * @date Jul 7, 2014
 */
public class PerformanceMonitor extends Plugin implements LogStreamDataListener
{
	public static final String	FULL_PLUGIN_NAME	= "thobe.logfileviewer.performance.Performance";

	private Logger				log;

	public PerformanceMonitor( )
	{
		super( FULL_PLUGIN_NAME, FULL_PLUGIN_NAME );
		this.log = Logger.getLogger( FULL_PLUGIN_NAME );
	}

	@Override
	public JComponent getVisualComponent( )
	{
		// TODO Auto-generated method stub
		return new JButton( "Performance" );
	}

	@Override
	public boolean onStarted( )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onRegistered( IPluginAccess pluginAccess )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLogStreamOpened( ILogStreamAccess logStreamAccess )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrepareCloseLogStream( ILogStreamAccess logStreamAccess )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onLogStreamClosed( )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnRegistered( )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onStopped( )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPluginDescription( )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCurrentMemory( )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void freeMemory( )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewLine( LogLine line )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewBlockOfLines( List<LogLine> blockOfLines )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getLineFilter( )
	{
		return ".*";
	}

	protected Logger LOG( )
	{
		return this.log;
	}

}
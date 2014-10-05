/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.taskview;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.IPluginUIComponent;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.logline.LogLine;

/**
 * @author Thomas Obenaus
 * @source TaskView.java
 * @date Jul 7, 2014
 */
public class TaskView extends Plugin
{
	public static final String	FULL_PLUGIN_NAME	= "thobe.logfileviewer.plugin.TaskView";

	private Logger				log;
	private TaskViewUI			uiComponent;

	public TaskView( )
	{
		super( FULL_PLUGIN_NAME, FULL_PLUGIN_NAME );
		this.log = Logger.getLogger( FULL_PLUGIN_NAME );
		this.uiComponent = new TaskViewUI( );
	}

	@Override
	public IPluginUIComponent getUIComponent( )
	{
		return this.uiComponent;
	}

	@Override
	public boolean onStarted( )
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onRegistered( IPluginAccess pluginAccess )
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onLogStreamOpened( ILogStreamAccess logStreamAccess )
	{}

	@Override
	public void onPrepareCloseLogStream( ILogStreamAccess logStreamAccess )
	{}

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
	public void freeMemory( )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewLine( LogLine line )
	{}

	@Override
	public void onNewBlockOfLines( List<LogLine> blockOfLines )
	{
		// TODO Auto-generated method stub
		//for ( LogLine ll : blockOfLines )
		//System.out.println( ll.getData( ) );
	}

	@Override
	public Pattern getLineFilter( )
	{
		return Pattern.compile( ".*_TSK\\].*" );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	@Override
	public long getMemory( )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNameOfMemoryWatchable( )
	{
		return FULL_PLUGIN_NAME;
	}

}

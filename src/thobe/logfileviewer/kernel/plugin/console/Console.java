/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.plugin.console;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogLine;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;

/**
 * @author Thomas Obenaus
 * @source Console.java
 * @date May 29, 2014
 */
public class Console extends Plugin implements LogStreamDataListener
{
	public static final String	FULL_PLUGIN_NAME			= "thobe.logfileviewer.plugin.Console";
	private static long			MAX_TIME_PER_BLOCK_IN_MS	= 1000;
	private static long			MAX_LINES_PER_BLOCK			= 100;

	private Deque<LogLine>		lineBuffer;
	private ConsoleTableModel	tableModel;
	private JPanel				pa_logPanel;
	private long				memInLineBuffer;

	public Console( )
	{
		super( FULL_PLUGIN_NAME, FULL_PLUGIN_NAME );
		this.memInLineBuffer = 0;
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.buildGUI( );
	}

	private void buildGUI( )
	{

		this.pa_logPanel = new JPanel( new BorderLayout( ) );
		this.tableModel = new ConsoleTableModel( );

		JTable ta_logTable = new JTable( this.tableModel );
		this.pa_logPanel.add( ta_logTable.getTableHeader( ), BorderLayout.NORTH );
		JScrollPane scrpa_main = new JScrollPane( ta_logTable );
		this.pa_logPanel.add( scrpa_main, BorderLayout.CENTER );
		
		// adjust column-sizes
		ta_logTable.getColumnModel( ).getColumn( 0 ).setMinWidth( 110 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setMaxWidth(  110 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 110 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setResizable( false );
	}

	@Override
	public JComponent getVisualComponent( )
	{
		return this.pa_logPanel;
	}

	@Override
	public boolean onRegistered( IPluginAccess pluginAccess )
	{
		LOG( ).info( this.getPluginName( ) + " registered." );
		return true;
	}

	@Override
	public boolean onStarted( )
	{
		LOG( ).info( this.getPluginName( ) + " started." );
		return false;
	}

	@Override
	public void onLogStreamOpened( ILogStreamAccess logStreamAccess )
	{
		LOG( ).info( this.getPluginName( ) + " LogStream opened." );
		logStreamAccess.addLogStreamDataListener( this );
	}

	@Override
	public void onPrepareCloseLogStream( ILogStreamAccess logStreamAccess )
	{
		LOG( ).info( this.getPluginName( ) + " prepare to close LogStream." );
		logStreamAccess.removeLogStreamDataListener( this );
	}

	@Override
	public void onLogStreamClosed( )
	{
		LOG( ).info( this.getPluginName( ) + " LogStream closed." );
	}

	@Override
	public boolean onStopped( )
	{
		LOG( ).info( this.getPluginName( ) + " stopped." );
		return true;
	}

	@Override
	public void onUnRegistered( )
	{
		LOG( ).info( this.getPluginName( ) + " unregistered." );
	}

	@Override
	public String getPluginName( )
	{
		return FULL_PLUGIN_NAME;
	}

	@Override
	public String getPluginDescription( )
	{
		return "A simple console displaying the whole logfile";
	}

	@Override
	public void run( )
	{
		List<LogLine> block = new ArrayList<>( );
		while ( !this.isQuitRequested( ) )
		{
			long startTime = System.currentTimeMillis( );
			boolean timeThresholdHurt = false;
			boolean blockSizeThresholdHurt = false;
			block.clear( );

			// collect some lines
			while ( ( !this.lineBuffer.isEmpty( ) ) && !timeThresholdHurt && !blockSizeThresholdHurt )
			{
				LogLine ll = this.lineBuffer.pollFirst( );
				this.memInLineBuffer -= ll.getMem( );
				block.add( ll );
				blockSizeThresholdHurt = block.size( ) > MAX_LINES_PER_BLOCK;
				timeThresholdHurt = ( System.currentTimeMillis( ) - startTime ) > MAX_TIME_PER_BLOCK_IN_MS;
			}// while ( ( !this.lineBuffer.isEmpty( ) ) && !timeThresholdHurt && !blockSizeThresholdHurt ).

			// Add the block if we have collected some lines
			if ( !block.isEmpty( ) )
			{
				this.tableModel.addBlock( block );
			}// if ( !block.isEmpty( ) ).

			// sleep ... 
			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}
		}// while ( !this.isQuitRequested( ) ).
	}

	@Override
	public void onNewLine( LogLine line )
	{
		this.lineBuffer.add( line );
		this.memInLineBuffer += line.getMem( );
	}

	@Override
	public String getLineFilter( )
	{
		return ".*";
	}

	@Override
	public long getCurrentMemory( )
	{
		return this.memInLineBuffer + this.tableModel.getMem( );
	}
}

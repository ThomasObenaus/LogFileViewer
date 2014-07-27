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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import thobe.logfileviewer.kernel.plugin.IPlugin;
import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.IPluginUI;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.console.events.CEvtClear;
import thobe.logfileviewer.kernel.plugin.console.events.ConsoleEvent;
import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogLine;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;

/**
 * Implementation of the {@link Console} {@link IPlugin}.
 * @author Thomas Obenaus
 * @source Console.java
 * @date May 29, 2014
 */
public class Console extends Plugin implements LogStreamDataListener, ISubConsoleFactoryAccess
{
	public static final String			FULL_PLUGIN_NAME			= "thobe.logfileviewer.plugin.Console";
	/**
	 * Max time spent waiting for completion of the next block of {@link LogLine}s (in MS)
	 */
	private static long					MAX_TIME_PER_BLOCK_IN_MS	= 1000;

	/**
	 * Max amount of {@link LogLine} waiting for completion of one block until the block will be drawn.
	 */
	private static long					MAX_LINES_PER_BLOCK			= 100;

	/**
	 * Queue for misc console-events.
	 */
	private Deque<ConsoleEvent>			eventQueue;

	/**
	 * Queue containing all incoming {@link LogLine}s
	 */
	private Deque<LogLine>				lineBuffer;

	/**
	 * The internal {@link TableModel}
	 */
	private ConsoleTableModel			tableModel;

	/**
	 * The plugin-panel (returned by {@link IPluginUI#getVisualComponent()}.
	 */
	private JPanel						pa_logPanel;

	/**
	 * Semaphore for the internal event-main-loop
	 */
	private Semaphore					eventSemaphore;

	private Set<ConsoleDataListener>	consoleDataListeners;

	private Pattern						pattern;

	public Console( )
	{
		super( FULL_PLUGIN_NAME, FULL_PLUGIN_NAME );
		this.pattern = Pattern.compile( ".*" );
		this.eventSemaphore = new Semaphore( 1, true );
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.eventQueue = new ConcurrentLinkedDeque<>( );
		this.consoleDataListeners = new HashSet<>( );
		this.buildGUI( );
	}

	@Override
	public SubConsole createNewSubConsole( String parentConsolePattern, String pattern )
	{
		LOG( ).info( "Create new console for filter '" + pattern + "'" );

		// create a new instance of a sub-console
		SubConsole newSubConsoleUI = new SubConsole( parentConsolePattern, pattern, this, LOG( ) );

		return newSubConsoleUI;
	}

	@Override
	public void registerSubConsole( SubConsole subConsole, boolean registerVisualComponent )
	{
		LOG( ).info( "Register new console for filter '" + subConsole + "'" );

		if ( registerVisualComponent )
		{
			// register the window/panel of the new sub-console 
			this.getPluginWindowManagerAccess( ).registerComponent( this, subConsole.getLogPanel( ) );
		}

		// register the new console as listener -> enable retrieval of log-lines
		synchronized ( this.consoleDataListeners )
		{
			this.consoleDataListeners.add( subConsole );
		}// synchronized ( this.consoleDataListeners )		

		// start the new sub-console
		subConsole.start( );

		LOG( ).info( "New console '" + subConsole + "' is now running" );
	}

	private void buildGUI( )
	{
		// create and regiter the first sub-console
		SubConsole consoleUI = createNewSubConsole( null, ".*" );
		this.registerSubConsole( consoleUI, false );
		this.pa_logPanel = consoleUI.getLogPanel( );
	}

	public void clear( )
	{
		this.eventQueue.add( new CEvtClear( ) );
		this.eventSemaphore.release( );
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
		this.eventSemaphore.release( );
	}

	@Override
	public void onPrepareCloseLogStream( ILogStreamAccess logStreamAccess )
	{
		LOG( ).info( this.getPluginName( ) + " prepare to close LogStream." );
		logStreamAccess.removeLogStreamDataListener( this );
		this.lineBuffer.clear( );
		this.eventSemaphore.release( );
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
		this.eventSemaphore.release( );
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
			// process misc events
			processEvents( );

			long startTime = System.currentTimeMillis( );
			boolean timeThresholdHurt = false;
			boolean blockSizeThresholdHurt = false;
			block.clear( );

			// collect some lines
			synchronized ( this.lineBuffer )
			{
				while ( ( !this.lineBuffer.isEmpty( ) ) && !timeThresholdHurt && !blockSizeThresholdHurt )
				{
					LogLine ll = this.lineBuffer.pollFirst( );
					block.add( ll );
					blockSizeThresholdHurt = block.size( ) > MAX_LINES_PER_BLOCK;
					timeThresholdHurt = ( System.currentTimeMillis( ) - startTime ) > MAX_TIME_PER_BLOCK_IN_MS;
				}// while ( ( !this.lineBuffer.isEmpty( ) ) && !timeThresholdHurt && !blockSizeThresholdHurt ).
			}// synchronized ( this.lineBuffer ).

			// Add the block if we have collected some lines
			if ( !block.isEmpty( ) )
			{
				this.fireNewBlockOfLogLines( block );
			}// if ( !block.isEmpty( ) ).

			try
			{
				this.eventSemaphore.tryAcquire( 2, TimeUnit.SECONDS );
			}
			catch ( InterruptedException e )
			{
				LOG( ).severe( "Exception caught in console-plugin main-loop: " + e.getLocalizedMessage( ) );
			}
		}// while ( !this.isQuitRequested( ) ).
	}

	private void fireNewBlockOfLogLines( List<LogLine> block )
	{
		synchronized ( this.consoleDataListeners )
		{
			for ( ConsoleDataListener cdl : this.consoleDataListeners )
			{
				cdl.onNewData( block );
			}
		}// synchronized ( this.consoleDataListeners )
	}

	private void processEvents( )
	{
		ConsoleEvent evt = null;
		synchronized ( this.eventQueue )
		{

			while ( ( evt = this.eventQueue.poll( ) ) != null )
			{
				switch ( evt.getType( ) )
				{
				case CLEAR:
					this.tableModel.clear( );
					break;
				default:
					LOG( ).warning( "Unknown event: " + evt );
					break;
				}// switch ( evt.getType( ) ) .
			}// while ( ( evt = this.eventQueue.poll( ) ) != null ) .
		}// synchronized ( this.eventQueue ) .
	}

	@Override
	public void onNewLine( LogLine line )
	{
		this.lineBuffer.add( line );
		this.eventSemaphore.release( );
	}

	@Override
	public Pattern getLineFilter( )
	{
		return this.pattern;
	}

	@Override
	public long getCurrentMemory( )
	{
		/*long memInLineBuffer = 0;
		for ( LogLine ll : this.lineBuffer )
			memInLineBuffer += ll.getMem( ) + SizeOf.REFERENCE + SizeOf.HOUSE_KEEPING_ARRAY;
		long memInEventQueue = this.scrollEventQueue.size( ) * SizeOf.REFERENCE * SizeOf.HOUSE_KEEPING;
		memInEventQueue += SizeOf.REFERENCE + SizeOf.HOUSE_KEEPING_ARRAY;

		return memInLineBuffer + this.tableModel.getMem( ) + memInEventQueue;*/
		return 0;
	}

	@Override
	public void onNewBlockOfLines( List<LogLine> blockOfLines )
	{
		this.lineBuffer.addAll( blockOfLines );
		this.eventSemaphore.release( );
	}

	@Override
	public void freeMemory( )
	{
		synchronized ( this.lineBuffer )
		{
			this.tableModel.clear( );
			this.lineBuffer.clear( );
		}
		this.eventSemaphore.release( );
	}
}

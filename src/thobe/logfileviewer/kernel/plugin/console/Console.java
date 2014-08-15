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

import thobe.logfileviewer.kernel.plugin.IPlugin;
import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.IPluginUI;
import thobe.logfileviewer.kernel.plugin.IPluginUIComponent;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.SizeOf;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_DestroySubConsole;
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
	private static long					MAX_LINES_PER_BLOCK			= 1000;

	/**
	 * Queue for misc console-events.
	 */
	private Deque<ConsoleEvent>			eventQueue;

	/**
	 * Queue containing all incoming {@link LogLine}s
	 */
	private Deque<LogLine>				lineBuffer;

	/**
	 * The {@link IPluginUIComponent} (returned by {@link IPluginUI#getUIComponent()}.
	 */
	private SubConsole					firstConsolePanel;

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
	public SubConsole createNewSubConsole( String parentConsolePattern, String pattern, boolean closeable )
	{
		LOG( ).info( "Create new console for filter '" + pattern + "'" );

		// create a new instance of a sub-console
		SubConsole newSubConsoleUI = new SubConsole( parentConsolePattern, pattern, this, LOG( ), closeable );

		return newSubConsoleUI;
	}

	@Override
	public void unRegisterSubConsole( final SubConsole subConsole )
	{
		LOG( ).info( "Unregister console for filter '" + subConsole + "'" );

		this.eventQueue.add( new CEvt_DestroySubConsole( subConsole ) );
		this.eventSemaphore.release( );
	}

	@Override
	public void registerSubConsole( SubConsole subConsole, boolean registerVisualComponent )
	{
		LOG( ).info( "Register new console for filter '" + subConsole + "'" );

		if ( registerVisualComponent )
		{
			// register the window/panel of the new sub-console 
			this.getPluginWindowManagerAccess( ).registerVisualComponent( this, subConsole );
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
		this.firstConsolePanel = createNewSubConsole( null, ".*", false );
		this.registerSubConsole( this.firstConsolePanel, false );
	}

	@Override
	public IPluginUIComponent getUIComponent( )
	{
		return this.firstConsolePanel;
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
				case DESTROY_SUBCONSOLE:
					destroySubConsole( ( ( CEvt_DestroySubConsole ) evt ).getSubConsole( ) );
					break;
				default:
					LOG( ).warning( "Unknown event: " + evt );
					break;
				}// switch ( evt.getType( ) ) .
			}// while ( ( evt = this.eventQueue.poll( ) ) != null ) .
		}// synchronized ( this.eventQueue ) .
	}

	private void destroySubConsole( final SubConsole subConsole )
	{
		// unregister the console as listener -> disable retrieval of log-lines
		synchronized ( this.consoleDataListeners )
		{
			this.consoleDataListeners.remove( subConsole );
		}// synchronized ( this.consoleDataListeners )

		// start a thread that destroys the subconsole
		SubConsoleDestroyer destroyer = new SubConsoleDestroyer( subConsole );
		destroyer.start( );
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
		long memInLineBuffer = 0;
		for ( LogLine ll : this.lineBuffer )
			memInLineBuffer += ll.getMem( ) + SizeOf.REFERENCE + SizeOf.HOUSE_KEEPING_ARRAY;
		long memInEventQueue = this.eventQueue.size( ) * SizeOf.REFERENCE * SizeOf.HOUSE_KEEPING;

		long memoryOfAttachedDataListeners = 0;
		synchronized ( this.consoleDataListeners )
		{
			for ( ConsoleDataListener cdl : this.consoleDataListeners )
				memoryOfAttachedDataListeners += cdl.getCurrentMemory( );
		}

		return memInLineBuffer + memInEventQueue + memoryOfAttachedDataListeners;
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
		// free internal memory 
		synchronized ( this.lineBuffer )
		{
			this.lineBuffer.clear( );
		}

		// free memory of attached listeners
		synchronized ( this.consoleDataListeners )
		{
			for ( ConsoleDataListener cdl : this.consoleDataListeners )
				cdl.freeMemory( );
		}

		this.eventSemaphore.release( );
	}

	@Override
	public String createTitle( SubConsole subConsole )
	{
		return "Console {" + subConsole.getFullPattern( ) + "}";
	}

	@Override
	public String createDescription( SubConsole subConsole )
	{
		return "Console window " + this.getPluginDescription( ) + ", filter={" + subConsole.getFullPattern( ) + "}";
	}

	private class SubConsoleDestroyer extends Thread
	{
		private static final int	limit		= 3000;
		private static final int	maxRetries	= 20;

		private SubConsole			subConsole;

		public SubConsoleDestroyer( SubConsole subConsole )
		{
			super( "Destroyer for subConsole '" + subConsole + "'" );
			this.subConsole = subConsole;
		}

		@Override
		public void run( )
		{

			boolean success = false;

			// try to destroy the tread
			for ( int i = 0; ( i < maxRetries ) && ( !success ); ++i )
			{
				try
				{
					subConsole.quit( );
					subConsole.join( limit );
					success = !subConsole.isAlive( );
					if ( success )
						LOG( ).info( "Console '" + subConsole + "' is now unregistered and stopped." );
					else LOG( ).severe( "Unable to stop/interrupt the console for filter '" + subConsole + "' within " + limit + " ms. Retry ... " + ( i + 1 ) + "/" + maxRetries );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace( );
				}
			}
		}
	}

}

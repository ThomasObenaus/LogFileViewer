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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import thobe.logfileviewer.kernel.plugin.IPlugin;
import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.IPluginUI;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.SizeOf;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_Scroll;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_ScrollToLast;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_SetAutoScrollMode;
import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogLine;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;

/**
 * Implementation of the {@link Console} {@link IPlugin}.
 * @author Thomas Obenaus
 * @source Console.java
 * @date May 29, 2014
 */
public class Console extends Plugin implements LogStreamDataListener
{
	public static final String	FULL_PLUGIN_NAME			= "thobe.logfileviewer.plugin.Console";
	/**
	 * Max time spent waiting for completion of the next block of {@link LogLine}s (in MS)
	 */
	private static long			MAX_TIME_PER_BLOCK_IN_MS	= 1000;

	/**
	 * Max amount of {@link LogLine} waiting for completion of one block until the block will be drawn.
	 */
	private static long			MAX_LINES_PER_BLOCK			= 100;

	/**
	 * Queue containing all scroll-events
	 */
	private Deque<CEvt_Scroll>	scrollEventQueue;

	/**
	 * Queue containing all incoming {@link LogLine}s
	 */
	private Deque<LogLine>		lineBuffer;

	/**
	 * The internal {@link TableModel}
	 */
	private ConsoleTableModel	tableModel;

	/**
	 * The plugin-panel (returned by {@link IPluginUI#getVisualComponent()}.
	 */
	private JPanel				pa_logPanel;

	/**
	 * True if autoscrolling is enabled, false otherwise
	 */
	private boolean				autoScrollingEnabled;

	/**
	 * The table.
	 */
	private JTable				ta_logTable;

	private Semaphore			eventSemaphore;

	public Console( )
	{
		super( FULL_PLUGIN_NAME, FULL_PLUGIN_NAME );
		this.eventSemaphore = new Semaphore( 1, true );
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.scrollEventQueue = new ConcurrentLinkedDeque<>( );
		this.autoScrollingEnabled = true;
		this.buildGUI( );
	}

	private void buildGUI( )
	{

		this.pa_logPanel = new JPanel( new BorderLayout( ) );
		this.tableModel = new ConsoleTableModel( );

		this.ta_logTable = new JTable( this.tableModel );
		this.pa_logPanel.add( ta_logTable.getTableHeader( ), BorderLayout.NORTH );
		final JScrollPane scrpa_main = new JScrollPane( ta_logTable );
		this.pa_logPanel.add( scrpa_main, BorderLayout.CENTER );

		// adjust column-sizes
		ta_logTable.getColumnModel( ).getColumn( 0 ).setMinWidth( 60 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setMaxWidth( 60 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 60 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setResizable( true );
		ta_logTable.getColumnModel( ).getColumn( 1 ).setMinWidth( 110 );
		ta_logTable.getColumnModel( ).getColumn( 1 ).setMaxWidth( 110 );
		ta_logTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 110 );
		ta_logTable.getColumnModel( ).getColumn( 1 ).setResizable( false );

		// listener that is responsible to scroll the table to the last entry 
		this.tableModel.addTableModelListener( new TableModelListener( )
		{
			@Override
			public void tableChanged( final TableModelEvent e )
			{
				if ( e.getType( ) == TableModelEvent.INSERT && autoScrollingEnabled )
				{
					addScrollEvent( new CEvt_ScrollToLast( ) );
				}
			}
		} );

		// listener keeping track of disabling/enabling autoscrolling
		scrpa_main.getVerticalScrollBar( ).addAdjustmentListener( new AdjustmentListener( )
		{
			@Override
			public void adjustmentValueChanged( AdjustmentEvent evt )
			{
				// Disable auto-scrolling if the scrollbar is moved 
				int extent = scrpa_main.getVerticalScrollBar( ).getModel( ).getExtent( );
				int currentScrollPos = scrpa_main.getVerticalScrollBar( ).getValue( ) + extent;

				// bottom is reached in case the current-scrolling pos is greater than 98% of the maximum-scroll position of the table
				// if bottom is reached we want to enable autoscrolling 
				boolean bottomReached = currentScrollPos >= ( scrpa_main.getVerticalScrollBar( ).getMaximum( ) * 0.98 );

				// in case we have changed the autoscrolling-mode (disable/enable) we generate the according event to force the modification
				if ( ( evt.getValueIsAdjusting( ) == true ) && ( evt.getAdjustmentType( ) == AdjustmentEvent.TRACK ) && ( autoScrollingEnabled != bottomReached ) )
				{
					addScrollEvent( new CEvt_SetAutoScrollMode( bottomReached ) );
				}
			}
		} );
	}

	private void addScrollEvent( CEvt_Scroll evt )
	{
		this.scrollEventQueue.add( evt );
		eventSemaphore.release( );
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
			// process next event from the event-queue
			processScrollEvents( );

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
				this.tableModel.addBlock( block );
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

	private void processScrollEvents( )
	{

		CEvt_Scroll evt = null;
		boolean hasScrollToLast = false;
		while ( ( evt = this.scrollEventQueue.poll( ) ) != null )
		{
			switch ( evt.getType( ) )
			{
			case SCROLL_TO_LAST:
				hasScrollToLast = true;
				break;
			case SET_AUTOSCROLL_MODE:
				this.autoScrollingEnabled = ( ( CEvt_SetAutoScrollMode ) evt ).isEnable( );
				break;
			default:
				LOG( ).warning( "Unknown event: " + evt );
				break;
			}
		}

		// scroll to the last line if needed
		if ( hasScrollToLast && this.autoScrollingEnabled )
		{
			SwingUtilities.invokeLater( new Runnable( )
			{
				public void run( )
				{
					int viewRow = ta_logTable.convertRowIndexToView( tableModel.getRowCount( ) );
					ta_logTable.scrollRectToVisible( ta_logTable.getCellRect( viewRow, 0, true ) );
				}
			} );
		}
	}

	@Override
	public void onNewLine( LogLine line )
	{
		this.lineBuffer.add( line );
		this.eventSemaphore.release( );
	}

	@Override
	public String getLineFilter( )
	{
		return ".*";
	}

	@Override
	public long getCurrentMemory( )
	{
		long memInLineBuffer = 0;
		for ( LogLine ll : this.lineBuffer )
			memInLineBuffer += ll.getMem( ) + SizeOf.REFERENCE + SizeOf.HOUSE_KEEPING_ARRAY;
		long memInEventQueue = this.scrollEventQueue.size( ) * SizeOf.REFERENCE * SizeOf.HOUSE_KEEPING;
		memInEventQueue += SizeOf.REFERENCE + SizeOf.HOUSE_KEEPING_ARRAY;

		return memInLineBuffer + this.tableModel.getMem( ) + memInEventQueue;
	}

	@Override
	public void onNewBlockOfLines( List<LogLine> blockOfLines )
	{
		this.lineBuffer.addAll( blockOfLines );
		this.eventSemaphore.release( );
	}

	public void setAutoScrollingEnabled( boolean autoScrollingEnabled )
	{
		this.autoScrollingEnabled = autoScrollingEnabled;
		this.eventSemaphore.release( );
	}
}

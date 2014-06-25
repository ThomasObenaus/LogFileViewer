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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import thobe.logfileviewer.kernel.plugin.IPlugin;
import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.IPluginUI;
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.SizeOf;
import thobe.logfileviewer.kernel.plugin.console.events.CEvtClear;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_Scroll;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_ScrollToLast;
import thobe.logfileviewer.kernel.plugin.console.events.CEvt_SetAutoScrollMode;
import thobe.logfileviewer.kernel.plugin.console.events.ConsoleEvent;
import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogLine;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;
import thobe.widgets.buttons.SmallButton;

/**
 * Implementation of the {@link Console} {@link IPlugin}.
 * @author Thomas Obenaus
 * @source Console.java
 * @date May 29, 2014
 */
public class Console extends Plugin implements LogStreamDataListener
{
	public static final String	FULL_PLUGIN_NAME				= "thobe.logfileviewer.plugin.Console";
	/**
	 * Max time spent waiting for completion of the next block of {@link LogLine}s (in MS)
	 */
	private static long			MAX_TIME_PER_BLOCK_IN_MS		= 1000;

	/**
	 * Max amount of {@link LogLine} waiting for completion of one block until the block will be drawn.
	 */
	private static long			MAX_LINES_PER_BLOCK				= 100;

	/**
	 * Time in ms to wait for the next update of the console plugins display-values
	 */
	private static long			UPDATE_DISPLAY_VALUES_INTERVAL	= 1000;

	/**
	 * Queue containing all scroll-events
	 */
	private Deque<CEvt_Scroll>	scrollEventQueue;

	/**
	 * Queue for misc console-events.
	 */
	private Deque<ConsoleEvent>	eventQueue;

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

	/**
	 * Semaphore for the internal event-main-loop
	 */
	private Semaphore			eventSemaphore;

	private SmallButton			bu_clear;
	private SmallButton			bu_settings;
	private SmallButton			bu_enableAutoScroll;

	private JLabel				l_statusline;

	/**
	 * Timestamp of next time the console's display-values will be updated
	 */
	private long				nextUpdateOfDisplayValues;

	public Console( )
	{
		super( FULL_PLUGIN_NAME, FULL_PLUGIN_NAME );
		this.eventSemaphore = new Semaphore( 1, true );
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.scrollEventQueue = new ConcurrentLinkedDeque<>( );
		this.eventQueue = new ConcurrentLinkedDeque<>( );
		this.autoScrollingEnabled = true;
		this.nextUpdateOfDisplayValues = 0;
		this.buildGUI( );
	}

	private void buildGUI( )
	{
		this.pa_logPanel = new JPanel( new BorderLayout( 0, 0 ) );
		this.tableModel = new ConsoleTableModel( );

		this.ta_logTable = new JTable( this.tableModel );
		final JScrollPane scrpa_main = new JScrollPane( ta_logTable );

		this.pa_logPanel.add( scrpa_main, BorderLayout.CENTER );

		CellConstraints cc_settings = new CellConstraints( );
		JPanel pa_settings = new JPanel( new FormLayout( "3dlu,fill:pref,pref:grow,pref,3dlu", "3dlu,pref,3dlu" ) );
		this.pa_logPanel.add( pa_settings, BorderLayout.NORTH );

		this.l_statusline = new JLabel( "Lines: 0/" + this.tableModel.getMaxNumberOfConsoleEntries( ) );
		pa_settings.add( this.l_statusline, cc_settings.xy( 2, 2 ) );

		JPanel pa_buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT, 0, 0 ) );
		pa_settings.add( pa_buttons, cc_settings.xy( 4, 2 ) );

		this.bu_enableAutoScroll = new SmallButton( "Autoscroll" );
		pa_buttons.add( this.bu_enableAutoScroll );
		this.bu_enableAutoScroll.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				toggleAutoScroll( );
			}
		} );

		this.bu_clear = new SmallButton( "clear" );
		pa_buttons.add( this.bu_clear );
		this.bu_clear.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				clear( );
			}
		} );

		this.bu_settings = new SmallButton( "Settings" );
		pa_buttons.add( this.bu_settings );

		// adjust column-sizes

		ta_logTable.getColumnModel( ).getColumn( 0 ).setMinWidth( 60 );
		ta_logTable.getColumnModel( ).getColumn( 0 ).setWidth( 60 );
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

	public void clear( )
	{
		this.eventQueue.add( new CEvtClear( ) );
		this.eventSemaphore.release( );
	}

	public void toggleAutoScroll( )
	{
		this.addScrollEvent( new CEvt_SetAutoScrollMode( !this.autoScrollingEnabled ) );
		this.eventSemaphore.release( );
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

	private void updateDisplayValues( )
	{
		if ( this.nextUpdateOfDisplayValues <= System.currentTimeMillis( ) )
		{
			this.nextUpdateOfDisplayValues = System.currentTimeMillis( ) + UPDATE_DISPLAY_VALUES_INTERVAL;
			this.l_statusline.setText( "Lines: " + this.tableModel.getRowCount( ) + "/" + this.tableModel.getMaxNumberOfConsoleEntries( ) );
		}// if ( this.nextUpdateOfDisplayValues <= System.currentTimeMillis( ) ) .
	}

	@Override
	public void run( )
	{
		List<LogLine> block = new ArrayList<>( );
		while ( !this.isQuitRequested( ) )
		{
			// process next event from the event-queue
			processScrollEvents( );

			// process misc events
			processEvents( );

			// update display-values
			updateDisplayValues( );

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

	private void processScrollEvents( )
	{
		CEvt_Scroll evt = null;
		boolean hasScrollToLast = false;
		synchronized ( this.scrollEventQueue )
		{
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
		}// synchronized ( this.scrollEventQueue ) .

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

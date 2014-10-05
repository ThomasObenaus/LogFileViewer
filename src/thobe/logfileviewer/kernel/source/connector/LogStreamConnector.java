/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.connector;

import java.io.File;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.listeners.ILogStreamStateListener;
import thobe.logfileviewer.kernel.source.reader.IpLogStreamReader;

/**
 * @author Thomas Obenaus
 * @source LogStreamConnector.java
 * @date Aug 16, 2014
 */
public class LogStreamConnector extends Thread implements ILogStreamStateListener
{
	private static final long		RESEND_DELAY_IN_MS	= 500;

	private AtomicBoolean			quitRequested;
	private Logger					log;

	/**
	 * Semaphore for the internal event-main-loop
	 */
	private Semaphore				eventSemaphore;

	private Deque<LSConnectorEvent>	eventQueue;

	private LSConnectorEvent		lastConnectionEvent;
	private LogStream				logStream;

	public LogStreamConnector( LogStream logStream )
	{
		super( "LogStreamConnector" );
		this.logStream = logStream;
		this.logStream.addLogStreamStateListener( this );
		this.quitRequested = new AtomicBoolean( false );
		this.eventSemaphore = new Semaphore( 1, true );
		this.eventQueue = new ConcurrentLinkedDeque<>( );
		this.lastConnectionEvent = null;
		this.log = Logger.getLogger( "thobe.logfileviewer.source.LogStreamConnector" );
	}

	public void connectToIP( String host, int port )
	{
		LSCEvt_ConnectToIP event = new LSCEvt_ConnectToIP( 0, host, port );
		this.addConnectEvent( event );
	}

	public void connectToFile( File file )
	{
		LSCEvt_ConnectoToFile event = new LSCEvt_ConnectoToFile( 0, file );
		this.addConnectEvent( event );
	}

	protected void addConnectEvent( LSConnectorEvent evt )
	{
		this.eventQueue.add( evt );
		this.eventSemaphore.release( );
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Started " );

		while ( !this.quitRequested.get( ) )
		{
			// process pending events
			processEvents( );

			try
			{
				this.eventSemaphore.tryAcquire( 2, TimeUnit.SECONDS );
			}
			catch ( InterruptedException e )
			{
				LOG( ).fine( "tryAcquire was interrupted" );
				break;
			}
		}

		LOG( ).info( "Stopped." );
	}

	private void processEvents( )
	{
		LSConnectorEvent evt = null;
		synchronized ( this.eventQueue )
		{
			while ( ( evt = this.eventQueue.poll( ) ) != null )
			{
				switch ( evt.getType( ) )
				{
				case CONNECT:
					this.lastConnectionEvent = evt;
					this.connectImpl( this.lastConnectionEvent );
					break;
				default:
					LOG( ).warning( "Unknown event: " + evt );
					break;
				}// switch ( evt ).
			}// while ( ( evt = this.eventQueue.poll( ) ) != null ) .
		}// synchronized ( this.eventQueue ) .
	}

	private void connectImpl( LSConnectorEvent connectToEvt )
	{
		try
		{
			LOG( ).info( "trying to connect to " + connectToEvt );

			if ( connectToEvt.getDelay( ) > 0 )
			{
				Thread.sleep( connectToEvt.getDelay( ) );
			}

			if ( connectToEvt instanceof LSCEvt_ConnectToIP )
			{
				LSCEvt_ConnectToIP connectToIpEvt = ( LSCEvt_ConnectToIP ) connectToEvt;
				this.logStream.open( new IpLogStreamReader( connectToIpEvt.getHost( ), connectToIpEvt.getPort( ) ) );
			}// if ( connectToEvt instanceof LSCEvt_ConnectToIP )
			else if ( connectToEvt instanceof LSCEvt_ConnectoToFile )
			{
				// TODO: open file
			}// else if ( connectToEvt instanceof LSCEvt_ConnectoToFile )
			else
			{
				LOG( ).severe( "Unknown type of connetion-event...ignoring." );
			}
		}
		catch ( LogStreamException e )
		{
			connectToEvt.setDelay( RESEND_DELAY_IN_MS );
			this.addConnectEvent( connectToEvt );
			LOG( ).severe( "Error opening connection: " + e.getLocalizedMessage( ) + ". Retrying in " + RESEND_DELAY_IN_MS + " ms..." );
		}
		catch ( InterruptedException e )
		{}
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return "LogStreamConnector";
	}

	private void tryReconnect( )
	{
		synchronized ( this.eventQueue )
		{
			// resend the event with a certain delay
			if ( this.lastConnectionEvent != null )
			{
				this.lastConnectionEvent.setDelay( RESEND_DELAY_IN_MS );
				this.addConnectEvent( this.lastConnectionEvent );
			}
		}
	}

	@Override
	public void onEOFReached( )
	{
		this.tryReconnect( );
	}

	@Override
	public void onOpened( )
	{}

	@Override
	public void onClosed( )
	{
		this.tryReconnect( );
	}

	public Logger LOG( )
	{
		return this.log;
	}

	public void quit( )
	{
		this.quitRequested.set( true );
	}
}

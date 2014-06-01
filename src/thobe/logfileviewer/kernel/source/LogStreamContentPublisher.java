/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.source;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.listeners.LogStreamContentPublisherListener;

/**
 * Class that is responsible to publish the contents that where read from the log via {@link LogStreamReader}.
 * @author Thomas Obenaus
 * @source LogStreamContentPublisher.java
 * @date May 29, 2014
 */
public class LogStreamContentPublisher extends Thread
{
	private LogStreamReader							traceSource;
	private AtomicBoolean							quitRequested;
	private Logger									log;

	private List<LogStreamContentPublisherListener>	listeners;
	private LogStreamState							stateOfLogStream;

	/**
	 * The time to sleep between two lines that where read from the source (in ms).
	 */
	private AtomicInteger							sleepTime;

	public LogStreamContentPublisher( )
	{
		this.quitRequested = new AtomicBoolean( false );
		this.log = Logger.getLogger( "thobe.logfileviewer.source.LogStreamContentPublisher" );
		this.sleepTime = new AtomicInteger( 100 );
		this.listeners = new ArrayList<>( );
		this.stateOfLogStream = LogStreamState.CLOSED;
	}

	/**
	 * Adds a new listener (thread-safe)
	 * @param listener
	 */
	public void addListener( LogStreamContentPublisherListener listener )
	{
		synchronized ( listeners )
		{
			this.listeners.add( listener );
		}
	}

	/**
	 * Removes the given listener (thread-safe)
	 * @param listener
	 */
	public void removeListener( LogStreamContentPublisherListener listener )
	{
		synchronized ( listeners )
		{
			this.listeners.remove( listener );
		}
	}

	public void startPublishing( LogStreamReader traceSource )
	{
		synchronized ( this )
		{
			this.traceSource = traceSource;
		}
	}

	public void stopPublishing( )
	{
		synchronized ( this )
		{
			this.traceSource = null;
		}
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Started " + this.getClass( ).getSimpleName( ) );

		while ( !this.quitRequested.get( ) )
		{
			synchronized ( this )
			{
				// check state of stream
				LogStreamState state = LogStreamState.CLOSED;

				// #### OPENED #####
				if ( this.traceSource != null && this.traceSource.isOpen( ) )
				{
					state = LogStreamState.OPEN;
				}// if ( this.traceSource != null || this.traceSource.isOpen( ) ) .

				// #### EOF REACHED #####
				if ( this.traceSource != null && this.traceSource.isEOFReached( ) )
				{
					state = LogStreamState.EOF_REACHED;
				}// if ( this.traceSource != null || this.traceSource.isEOFReached( ) ) .

				// #### CLOSED #####
				if ( this.traceSource == null || !this.traceSource.isOpen( ) )
				{
					state = LogStreamState.CLOSED;
				}// if ( this.traceSource == null || !this.traceSource.isOpen( ) ).

				// publish event if necessary
				this.publishState( state );

				// process new line 
				if ( state == LogStreamState.OPEN && traceSource.hasNextLine( ) )
				{
					try
					{
						String nextLine = this.traceSource.nextLine( );
						this.fireNewLine( nextLine );
						System.out.println( nextLine );
					}
					catch ( LogStreamException e )
					{
						LOG( ).severe( "Unable to obtain next line from log: " + e.getLocalizedMessage( ) );
					}
				}// if ( state == LogStreamState.OPEN && traceSource.hasNextLine( ) ) .
			}// synchronized ( this ) .

			try
			{
				Thread.sleep( this.sleepTime.get( ) );
			}
			catch ( InterruptedException e )
			{
				LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " interrupted: " + e.getLocalizedMessage( ) );
				break;
			}
		}

		LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " stopped." );
	}

	private synchronized void publishState( LogStreamState state )
	{
		if ( state != this.stateOfLogStream )
		{
			this.stateOfLogStream = state;
			switch ( this.stateOfLogStream )
			{
			case CLOSED:
				this.fireClosed( );
				break;
			case OPEN:
				this.fireOpened( );
				break;
			default:
			case EOF_REACHED:
				this.fireEOFReached( );
				break;
			}// switch ( this.stateOfLogStream ).
		}// if ( state != this.stateOfLogStream ).
	}

	/**
	 * Sets the time to sleep between publishing new contents read from the log (in ms).
	 * @param sleepTime
	 */
	public void setSleepTime( int sleepTime )
	{
		this.sleepTime.set( sleepTime );
	}

	public void kill( ) throws LogStreamException
	{
		this.quitRequested.set( true );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	private void fireNewLine( String newLine )
	{
		synchronized ( listeners )
		{
			for ( LogStreamContentPublisherListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onNewLine( newLine );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the new-line event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

	private void fireEOFReached( )
	{
		LOG( ).info( "LogStream eof reached" );
		synchronized ( listeners )
		{
			for ( LogStreamContentPublisherListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onEOFReached( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the EOF-reached event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

	private void fireOpened( )
	{
		LOG( ).info( "LogStream opened" );
		synchronized ( listeners )
		{
			for ( LogStreamContentPublisherListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onOpened( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the opened event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

	private void fireClosed( )
	{
		LOG( ).info( "LogStream closed" );
		synchronized ( listeners )
		{
			for ( LogStreamContentPublisherListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onClosed( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the closed event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

}

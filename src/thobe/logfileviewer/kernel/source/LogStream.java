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

import thobe.tools.log.ILoggable;

/**
 * @author Thomas Obenaus
 * @source LogStream.java
 * @date May 29, 2014
 */
public class LogStream extends ILoggable implements LogStreamContentPublisherListener
{
	private LogStreamReader					logStreamReader;
	private LogStreamContentPublisher		publishThread;

	private List<LogStreamStateListener>	logStreamStateListeners;

	public LogStream( )
	{
		this.logStreamStateListeners = new ArrayList<>( );
		this.logStreamReader = null;
		this.publishThread = new LogStreamContentPublisher( );
		this.publishThread.start( );
		this.publishThread.addListener( this );
	}

	public void addLogStreamStateListener( LogStreamStateListener l )
	{
		synchronized ( this.logStreamStateListeners )
		{
			this.logStreamStateListeners.add( l );
		}
	}

	public void removeLogStreamStateListener( LogStreamStateListener l )
	{
		synchronized ( this.logStreamStateListeners )
		{
			this.logStreamStateListeners.remove( l );
		}
	}

	public void open( LogStreamReader source ) throws LogStreamException
	{
		if ( this.logStreamReader != null && this.logStreamReader.isOpen( ) )
		{
			this.close( );
		}

		this.logStreamReader = source;
		this.logStreamReader.open( );
		this.logStreamReader.start( );

		this.publishThread.startPublishing( logStreamReader );

		LOG( ).info( "LogStream opened [" + this.logStreamReader.getClass( ).getSimpleName( ) + "]" );
	}

	public boolean isOpen( )
	{
		if ( this.logStreamReader == null )
			return false;
		if ( !this.logStreamReader.isOpen( ) )
			return false;
		return true;
	}

	public void close( ) throws LogStreamException
	{
		if ( this.logStreamReader != null )
		{
			this.logStreamReader.close( );
			this.publishThread.stopPublishing( );
			LOG( ).info( "LogStream closed [" + this.logStreamReader.getClass( ).getSimpleName( ) + "]" );
			this.logStreamReader = null;
		}
	}

	@Override
	protected String getLogChannelName( )
	{
		return "thobe.logfileviewer.source.LogStream";
	}

	@Override
	public void onNewLine( String newLine )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onEOFReached( )
	{
		this.fireOnEOFReached( );
	}

	@Override
	public void onOpened( )
	{
		this.fireOnOpened( );
	}

	@Override
	public void onClosed( )
	{
		this.fireOnClosed( );
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return this.getLogChannelName( );
	}

	private void fireOnClosed( )
	{
		synchronized ( this.logStreamStateListeners )
		{
			for ( LogStreamStateListener l : this.logStreamStateListeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onClosed( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the closed event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamStateListener l : this.logStreamStateListeners ) .
		}// synchronized ( this.logStreamStateListeners ) .
	}

	private void fireOnOpened( )
	{
		synchronized ( this.logStreamStateListeners )
		{
			for ( LogStreamStateListener l : this.logStreamStateListeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onOpened( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the opened event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamStateListener l : this.logStreamStateListeners ) .
		}// synchronized ( this.logStreamStateListeners ) .
	}

	private void fireOnEOFReached( )
	{
		synchronized ( this.logStreamStateListeners )
		{
			for ( LogStreamStateListener l : this.logStreamStateListeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onEOFReached( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the eof event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamStateListener l : this.logStreamStateListeners ) .
		}// synchronized ( this.logStreamStateListeners ) .
	}
}

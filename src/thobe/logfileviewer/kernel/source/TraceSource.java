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

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import thobe.tools.log.ILoggable;

/**
 * @author Thomas Obenaus
 * @source TraceSource.java
 * @date May 15, 2014
 */
public abstract class TraceSource extends ILoggable implements Runnable
{
	private Deque<String>	lineBuffer;
	private AtomicBoolean	quitRequested;
	private AtomicBoolean	opened;

	public TraceSource( )
	{
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.quitRequested = new AtomicBoolean( false );
		this.opened = new AtomicBoolean( false );
	}

	public String nextLine( ) throws TraceSourceException
	{
		if ( !this.hasNextLine( ) )
			throw new TraceSourceException( "The queue is empty" );
		return this.lineBuffer.pop( );
	}

	public boolean hasNextLine( )
	{
		return !this.lineBuffer.isEmpty( );
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Started " + this.getClass( ).getSimpleName( ) );

		while ( !this.quitRequested.get( ) )
		{
			if ( !isOpen( ) )
			{
				LOG( ).info( this.getClass( ).getSimpleName( ) + " interrupted: Source is not open." );
				break;
			}

			try
			{
				String newLine = readLineImpl( );
				this.lineBuffer.add( newLine );
			}
			catch ( TraceSourceException e1 )
			{
				LOG( ).warning( this.getClass( ).getSimpleName( ) + " error reading next line: " + e1.getLocalizedMessage( ) );
			}

			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " interrupted: " + e.getLocalizedMessage( ) );
				break;
			}
		}

		LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " stopped." );
	}

	public void close( ) throws TraceSourceException
	{
		this.quitRequested.set( true );
		if ( this.opened.get( ) )
		{
			try
			{
				this.closeImpl( );
				this.opened.set( false );
			}
			catch ( TraceSourceException e )
			{
				LOG( ).severe( "Failed to close: " + e.getLocalizedMessage( ) );
				throw e;
			}
		}
	}

	protected abstract String readLineImpl( ) throws TraceSourceException;

	protected abstract void openImpl( ) throws TraceSourceException;

	protected abstract void closeImpl( ) throws TraceSourceException;

	public void open( ) throws TraceSourceException
	{
		if ( this.opened.get( ) )
		{
			throw new TraceSourceException( "Already open" );
		}

		try
		{
			this.openImpl( );
			this.opened.set( true );
		}
		catch ( TraceSourceException e )
		{
			LOG( ).severe( "Unable to open: " + e.getLocalizedMessage( ) );
			throw e;
		}
	}

	public boolean isOpen( )
	{
		return this.opened.get( );
	}

	@Override
	protected String getLogChannelName( )
	{
		return "thobe.ethsource.TraceSource";
	}

}

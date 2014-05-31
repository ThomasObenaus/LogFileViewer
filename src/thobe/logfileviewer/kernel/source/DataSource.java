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

import thobe.tools.log.ILoggable;

/**
 * @author Thomas Obenaus
 * @source DataSource.java
 * @date May 29, 2014
 */
public class DataSource extends ILoggable
{
	private TraceSource		traceSource;
	private ReaderThread	readerThread;
	private PublishThread	publishThread;

	public DataSource( )
	{
		this.readerThread = null;

		this.publishThread = new PublishThread( );
		this.publishThread.start( );
	}

	public void open( TraceSource source ) throws TraceSourceException
	{
		if ( this.traceSource != null && this.traceSource.isOpen( ) )
		{
			this.close( );
		}

		this.traceSource = source;
		this.traceSource.open( );
		this.readerThread = new ReaderThread( this.traceSource );
		this.readerThread.start( );

		this.publishThread.startPublishing( traceSource );

		LOG( ).info( "DataSource opened [" + this.traceSource.getClass( ).getSimpleName( ) + "]" );
	}

	public boolean isOpen( )
	{
		if ( this.traceSource == null )
			return false;
		if ( !this.traceSource.isOpen( ) )
			return false;
		return true;
	}

	public void close( ) throws TraceSourceException
	{
		if ( this.traceSource != null )
		{
			this.traceSource.close( );
			this.readerThread = null;
			this.publishThread.stopPublishing( );
			LOG( ).info( "DataSource closed [" + this.traceSource.getClass( ).getSimpleName( ) + "]" );
			this.traceSource = null;
		}
	}

	@Override
	protected String getLogChannelName( )
	{
		return "thobe.ethsource.source.DataSource";
	}
}

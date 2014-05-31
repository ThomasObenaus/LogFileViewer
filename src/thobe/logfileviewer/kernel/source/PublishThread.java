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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Thomas Obenaus
 * @source PublishThread.java
 * @date May 29, 2014
 */
public class PublishThread extends Thread
{
	private TraceSource		traceSource;
	private AtomicBoolean	quitRequested;
	private Logger			log;

	public PublishThread( )
	{
		this.quitRequested = new AtomicBoolean( false );
		this.log = Logger.getLogger( "thobe.logfileviewer.source.PublishThread" );
	}

	public void startPublishing( TraceSource traceSource )
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
				if ( this.traceSource != null && this.traceSource.isOpen( ) )
				{
					if ( traceSource.hasNextLine( ) )
					{
						try
						{
							System.out.println( this.traceSource.nextLine( ) );
						}
						catch ( TraceSourceException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace( );
						}
					}
				}
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

	public void kill( ) throws TraceSourceException
	{
		this.quitRequested.set( true );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

}

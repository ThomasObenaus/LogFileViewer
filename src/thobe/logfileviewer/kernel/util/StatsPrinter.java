/*
 *  Copyright (C) 2014, j.umbel. All rights reserved.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    j.umbel
 */

package thobe.logfileviewer.kernel.util;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.kernel.source.LogStream;

/**
 * Simple class that prints statistics to log.
 * @author Thomas Obenaus
 * @source StatsPrinter.java
 * @date 20.06.2014
 */
public class StatsPrinter extends Thread
{
	private static final String	NAME	= "thobe.logfileviewer.kernel.StatsPrinter";

	private PluginManager		mngr;
	private LogStream			logStream;
	private Logger				log;
	private AtomicBoolean		quitRequested;
	private AtomicInteger		intervalTime;
	private AtomicBoolean		enabled;

	public StatsPrinter( PluginManager mngr, LogStream logStream )
	{
		super( NAME );
		this.log = Logger.getLogger( NAME );
		this.mngr = mngr;
		this.logStream = logStream;
		this.quitRequested = new AtomicBoolean( false );
		this.intervalTime = new AtomicInteger( 10000 );
		this.enabled = new AtomicBoolean( true );
	}

	public void quit( )
	{
		this.quitRequested.set( true );
	}

	public void setEnabled( boolean enabled )
	{
		this.enabled.set( enabled );
	}

	public void setIntervalTime( AtomicInteger intervalTime )
	{
		this.intervalTime = intervalTime;
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Thread: " + NAME + " started" );
		while ( !this.quitRequested.get( ) )
		{

			if ( this.enabled.get( ) )
			{
				StringBuffer strBuffer = new StringBuffer( );
				strBuffer.append( "---------------------------------------------\n" );
				strBuffer.append( "Memory-Consumption: \n" );
				long completeMemory = 0;
				for ( Entry<String, Plugin> entry : this.mngr.getPlugins( ).entrySet( ) )
				{
					Plugin plugin = entry.getValue( );
					completeMemory += plugin.getCurrentMemory( );
					strBuffer.append( "--|" + plugin.getName( ) + ": " + ( plugin.getCurrentMemory( ) / 1024.0f / 1024.0f ) + " MB\n" );
				}// for ( Entry<String, Plugin> entry : this.mngr.getPlugins( ).entrySet( ) ) .

				strBuffer.append( "\n" );
				strBuffer.append( "-OverAll: " + ( completeMemory / 1024.0f / 1024.0f ) + " MB\n" );

				strBuffer.append( "\n" );
				strBuffer.append( "Reader " + this.logStream.getLogStreamReaderLPS( ) + " lps\n" );
				strBuffer.append( "---------------------------------------------\n" );

				LOG( ).info( strBuffer.toString( ) );
			}// if ( this.enabled.get( ) ).

			// wait
			try
			{
				Thread.sleep( this.intervalTime.get( ) );
			}
			catch ( InterruptedException e )
			{
				this.quitRequested.set( true );
			}
		}// while ( !this.quitRequested.get( ) ) .

		LOG( ).info( "Thread: " + NAME + " stopped" );
	}

	protected Logger LOG( )
	{
		return log;
	}
}

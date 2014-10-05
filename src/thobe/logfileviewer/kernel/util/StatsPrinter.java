/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.util;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.logline.LogLine;
import thobe.logfileviewer.kernel.source.logline.LogLineDat;

/**
 * Simple class that prints statistics to log.
 * @author Thomas Obenaus
 * @source StatsPrinter.java
 * @date 20.06.2014
 */
public class StatsPrinter extends Thread
{
	private static final String	NAME		= "thobe.logfileviewer.kernel.StatsPrinter";
	private static final double	MB_DIVIDER	= 1024.0 * 1024.0;

	/**
	 * Monitoring the {@link PluginManager}
	 */
	private PluginManager		mngr;
	/**
	 * Monitoring the {@link LogStream}.
	 */
	private LogStream			logStream;

	/**
	 * Internal logger
	 */
	private Logger				log;

	/**
	 * Quit requested
	 */
	private AtomicBoolean		quitRequested;

	/**
	 * The interval for the print-output of the statistics
	 */
	private AtomicInteger		intervalTime;

	/**
	 * Is printoutput of statistics enabled/disabled
	 */
	private AtomicBoolean		enabled;

	/**
	 * Ctor
	 * @param mngr - {@link PluginManager} to be monitored
	 * @param logStream - {@link LogStream} - to be monitored
	 */
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

	/**
	 * Quit this thread
	 */
	public void quit( )
	{
		this.quitRequested.set( true );
	}

	/**
	 * Enable or disable the print-output of the statistics
	 * @param enabled
	 */
	public void setEnabled( boolean enabled )
	{
		this.enabled.set( enabled );
	}

	/**
	 * Set the time-interval for the printoutput.
	 * @param intervalTime
	 */
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
					completeMemory += plugin.getMemory( );
					strBuffer.append( "--|" + plugin.getName( ) + ": " + ( plugin.getMemory( ) / MB_DIVIDER ) + " MB\n" );
				}// for ( Entry<String, Plugin> entry : this.mngr.getPlugins( ).entrySet( ) ) .

				// add memory of the LogLineFactory
				strBuffer.append( "--|LogLineFactory: " + ( this.logStream.getLogLineFactory( ).getCacheMemory( ) / MB_DIVIDER ) + " MB\n" );
				completeMemory += this.logStream.getLogLineFactory( ).getCacheMemory( );

				// complete memory
				strBuffer.append( "\n" );
				strBuffer.append( "-OverAll: " + ( completeMemory / MB_DIVIDER ) + " MB\n" );

				long freeMemory = Runtime.getRuntime( ).freeMemory( );
				long maxMemory = Runtime.getRuntime( ).maxMemory( );
				long totalMemory = Runtime.getRuntime( ).totalMemory( );
				long usedMemory = totalMemory - freeMemory;

				strBuffer.append( "-JVM: free=" + ( freeMemory / MB_DIVIDER ) + " MB, used=" + ( usedMemory / MB_DIVIDER ) );
				strBuffer.append( "MB, currAvailInJVM=" + ( totalMemory / MB_DIVIDER ) + "MB, maxAvail=" + ( maxMemory / MB_DIVIDER ) + "MB\n" );

				// lines per second
				strBuffer.append( "\n" );
				strBuffer.append( "Reader " + this.logStream.getLogStreamReaderLPS( ) + " lps\n" );

				// cache statistics
				strBuffer.append( "\n" );
				strBuffer.append( "LogLineFactory:\n" );
				strBuffer.append( "-Cache: hits=" + this.logStream.getLogLineFactory( ).getCacheHits( ) );
				strBuffer.append( ", misses=" + this.logStream.getLogLineFactory( ).getCacheMisses( ) );
				strBuffer.append( ", ratio=" + this.logStream.getLogLineFactory( ).getCacheRatio( ) );
				strBuffer.append( ", size=" + this.logStream.getLogLineFactory( ).getCacheSize( ) + "/" + this.logStream.getLogLineFactory( ).getMaxCacheSize( ) + "\n" );
				strBuffer.append( "-#Instances: LogLine=" + LogLine.getNumberOfInstances( ) + ", LogLineDat=" + LogLineDat.getNumberOfInstances( ) + "\n" );
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

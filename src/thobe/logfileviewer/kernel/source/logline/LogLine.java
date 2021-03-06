/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logline;

import thobe.logfileviewer.plugin.source.logline.ILogLine;
import thobe.logfileviewer.plugin.util.SizeOf;

/**
 * Class representing one log-line.
 * @author Thomas Obenaus
 * @source LogLine.java
 * @date Jun 1, 2014
 */
public class LogLine implements ILogLine
{
	private final long			id;
	private final long			timeStamp;
	private static long			instances	= 0;

	private final LogLineDat	data;
	private boolean				dataCached;

	public LogLine( long id, long timeStamp, LogLineDat data, boolean dataCached )
	{
		this.id = id;
		this.timeStamp = timeStamp;
		this.data = data;
		this.dataCached = dataCached;
		instances++;
	}

	@Override
	protected void finalize( ) throws Throwable
	{
		instances--;
		super.finalize( );
	}

	public static long getNumberOfInstances( )
	{
		return instances;
	}

	public long getTimeStamp( )
	{
		return timeStamp;
	}

	public long getId( )
	{
		return id;
	}

	public String getData( )
	{
		return data.getData( );
	}

	public String getTimeStampStr( )
	{
		return String.format( "%tH:%tM:%tS.%tL", this.timeStamp, this.timeStamp, this.timeStamp, this.timeStamp );
	}

	@Override
	public String toString( )
	{
		return String.format( "{%5d|%s} - %s", this.id, this.getTimeStampStr( ), this.data.getData( ) );
	}

	public long getMemory( )
	{
		return ( SizeOf.LONG * 2 ) + SizeOf.BOOLEAN + ( dataCached ? SizeOf.STRING( this.getData( ) ) : 0 );
	}
}

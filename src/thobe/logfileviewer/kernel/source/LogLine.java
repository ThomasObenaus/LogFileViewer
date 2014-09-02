/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source;

import thobe.logfileviewer.kernel.plugin.SizeOf;

/**
 * @author Thomas Obenaus
 * @source LogLine.java
 * @date Jun 1, 2014
 */
public class LogLine
{
	private String		data;
	private long		id;
	private long		timeStamp;

	public LogLine( long id, long timeStamp, String data )
	{
		this.id = id;
		this.timeStamp = timeStamp;
		this.data = data;
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
		return data;
	}

	public String getTimeStampStr( )
	{
		return String.format( "%tH:%tM:%tS.%tL", this.timeStamp, this.timeStamp, this.timeStamp, this.timeStamp );
	}

	@Override
	public String toString( )
	{
		return String.format( "{%5d|%s} - %s", this.id, this.getTimeStampStr( ), this.data );
	}

	public long getMem( )
	{
		return ( SizeOf.LONG * 2 ) + SizeOf.STRING( this.data );
	}
}

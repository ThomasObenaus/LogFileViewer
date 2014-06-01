/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.timestamp;

/**
 * @author Thomas Obenaus
 * @source LineAndTime.java
 * @date Jun 1, 2014
 */
public class LineAndTime
{
	private String	lineWithoutTimeStamp;
	private long	timeStamp;

	public LineAndTime( long timeStamp, String lineWithoutTimeStamp )
	{
		this.timeStamp = timeStamp;
		this.lineWithoutTimeStamp = lineWithoutTimeStamp;
	}

	public String getLineWithoutTimeStamp( )
	{
		return lineWithoutTimeStamp;
	}

	public long getTimeStamp( )
	{
		return timeStamp;
	}
}

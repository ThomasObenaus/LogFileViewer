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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import thobe.tools.log.ILoggable;

/**
 * @author Thomas Obenaus
 * @source TimeStampExtractor.java
 * @date Jun 1, 2014
 */
public class TimeStampExtractor extends ILoggable
{
	private String				timePattern;
	private SimpleDateFormat	formatter;
	private long				previousTimeStamp;

	public TimeStampExtractor( )
	{
		String pattern = "HH:mm:ss.SSS";
		this.formatter = new SimpleDateFormat( pattern );
		this.timePattern = dateFormatPatternToRegex( pattern );
		this.previousTimeStamp = System.currentTimeMillis( );
	}

	private String dateFormatPatternToRegex( String dateFormatPattern )
	{
		String regex = dateFormatPattern.replaceAll( "H|m|s|S", "[0-9]" );
		return regex;
	}

	public void setTimePattern( String pattern )
	{
		this.formatter = new SimpleDateFormat( pattern );
		this.timePattern = dateFormatPatternToRegex( pattern );
	}

	public LineAndTime splitLineAndTimeStamp( String line )
	{
		try
		{
			Date date = this.formatter.parse( line );
			this.previousTimeStamp = date.getTime( );
			return new LineAndTime( this.previousTimeStamp, line.replaceFirst( this.timePattern, "" ).trim( ) );
		}
		catch ( ParseException e )
		{
			//LOG( ).info( "Failed to find/parse timestamp in line '" + line + "'" );
		}

		return new LineAndTime( this.previousTimeStamp, line );
	}

	@Override
	protected String getLogChannelName( )
	{
		return "thobe.logfileviewer.source.TimeStampExtractor";
	}
}

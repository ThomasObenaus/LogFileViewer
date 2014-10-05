/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.listeners;

import java.util.List;
import java.util.regex.Pattern;

import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.logline.LogLine;

/**
 * Implement this interface and attach the instance to {@link LogStream} to get log-lines as they where read.
 * @author Thomas Obenaus
 * @source LogStreamDataListener.java
 * @date Jun 1, 2014
 */
public interface ILogStreamDataListener
{
	/**
	 * Called whenever a new log-line matching the {@link Pattern} returned by {@link ILogStreamDataListener#getLineFilter()} is available.
	 * @param blockOfLines
	 */
	public void onNewLine( LogLine line );

	/**
	 * Called whenever a new list/block of log-lines matching the {@link Pattern} returned by {@link ILogStreamDataListener#getLineFilter()}
	 * is available.
	 * @param blockOfLines
	 */
	public void onNewBlockOfLines( List<LogLine> blockOfLines );

	/**
	 * This method should return a {@link Pattern}. This {@link Pattern} (regular expression) is used to filter the lines of the
	 * {@link LogStream} /log-file. Only for lines matching this expression the method {@link ILogStreamDataListener#onNewLine(LogLine)} or
	 * {@link ILogStreamDataListener#onNewBlockOfLines(List)} is called providing
	 * the matching line of the log-file.
	 * @return
	 */
	public Pattern getLineFilter( );
}

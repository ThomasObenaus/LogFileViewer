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

import thobe.logfileviewer.kernel.source.LogLine;
import thobe.logfileviewer.kernel.source.LogStream;

/**
 * @author Thomas Obenaus
 * @source LogStreamDataListener.java
 * @date Jun 1, 2014
 */
public interface LogStreamDataListener
{
	public void onNewLine( LogLine line );

	public void onNewBlockOfLines( List<LogLine> blockOfLines );

	/**
	 * This method should return a regular expression. This regular expression is used to filter the lines of the {@link LogStream}
	 * /log-file. Only for lines matching this expression the method {@link LogStreamDataListener#onNewLine(LogLine)} is called providing
	 * the matching line of the log-file.
	 * @return
	 */
	public Pattern getLineFilter( );
}

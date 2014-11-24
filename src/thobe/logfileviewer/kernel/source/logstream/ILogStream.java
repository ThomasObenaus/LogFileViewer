/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logstream;

import java.util.regex.Pattern;

import thobe.logfileviewer.kernel.source.logline.LogLine;

/**
 * @author Thomas Obenaus
 * @source ILogStream.java
 * @date Nov 24, 2014
 */
public interface ILogStream
{
	/**
	 * Request a block of {@link LogLine}s from the current {@link LogStream}.
	 * @param start - Id of the first {@link LogLine} (inclusive). -1 means from the beginning.
	 * @param end - Id of the last {@link LogLine} (inclusive). -1 means till the end.
	 * @param requester - the instance requesting the {@link LogLine}s.
	 */
	public int requestLogLines( long start, long end, ILogStreamRequester requester );

	/**
	 * Request a block of {@link LogLine}s from the current {@link LogStream}.
	 * @param start - Id of the first {@link LogLine} (inclusive). -1 means from the beginning.
	 * @param end - Id of the last {@link LogLine} (inclusive). -1 means till the end.
	 * @param requester - the instance requesting the {@link LogLine}s.
	 * @param filter - Filter that should be used to filter and return only the lines that are interesting for the
	 *            {@link ILogStreamRequester}. *
	 */
	public int requestLogLines( long start, long end, ILogStreamRequester requester, Pattern filter );
}

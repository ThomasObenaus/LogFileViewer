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

import java.util.List;

import thobe.logfileviewer.kernel.source.logline.ILogLine;

/**
 * @author Thomas Obenaus
 * @source ILogStreamRequester.java
 * @date Nov 24, 2014
 */
public interface ILogStreamRequester
{
	/**
	 * Response to a previous request to the {@link LogStream} via {@link LogStream#requestLogLines(long, long, ILogStreamRequester)} or
	 * {@link LogStream#requestLogLines(long, long, ILogStreamRequester, java.util.regex.Pattern)}.
	 * @param requestId - the id of the request
	 * @param logLines - the lines matching the filter
	 * @param valid - if true, the response is valid
	 */
	public void response( int requestId, List<ILogLine> logLines, boolean valid );

	/**
	 * Returns the name of the requester.
	 * @return
	 */
	public String getLSRequesterName( );
}

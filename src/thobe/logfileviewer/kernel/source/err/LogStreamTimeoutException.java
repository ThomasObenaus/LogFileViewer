/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.err;

/**
 * @author Thomas Obenaus
 * @source LogStreamTimeoutException.java
 * @date Jun 1, 2014
 */
@SuppressWarnings ( "serial")
public class LogStreamTimeoutException extends LogStreamException
{
	public LogStreamTimeoutException( String cause )
	{
		super( cause );
	}
}



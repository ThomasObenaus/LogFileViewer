/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.source.err;

/**
 * @author Thomas Obenaus
 * @source TraceSourceException.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class LogStreamException extends Exception
{
	public LogStreamException( String cause )
	{
		super( cause );
	}
}

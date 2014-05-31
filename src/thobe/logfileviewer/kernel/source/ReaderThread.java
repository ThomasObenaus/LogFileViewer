/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.source;

/**
 * @author Thomas Obenaus
 * @source ReaderThread.java
 * @date May 15, 2014
 */
public class ReaderThread extends Thread
{
	public ReaderThread( TraceSource source )
	{
		super( source );
	}	
}

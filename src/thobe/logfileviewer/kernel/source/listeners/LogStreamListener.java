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

/**
 * @author Thomas Obenaus
 * @source LogStreamListener.java
 * @date Jun 1, 2014
 */
public interface LogStreamListener
{
	/**
	 * Implement this method to identify the name of the {@link LogStreamListener}.
	 * @return
	 */
	public String getLogStreamListenerName( );
}

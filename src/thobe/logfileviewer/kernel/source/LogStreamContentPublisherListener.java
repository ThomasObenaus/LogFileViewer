/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source;

/**
 * @author Thomas Obenaus
 * @source LogStreamContentPublisherListener.java
 * @date May 31, 2014
 */
public interface LogStreamContentPublisherListener extends LogStreamStateListener
{
	public void onNewLine( String newLine );
	
}
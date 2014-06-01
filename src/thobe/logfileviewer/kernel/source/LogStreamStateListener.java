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
 * @source LogStreamStateListener.java
 * @date Jun 1, 2014
 */
public interface LogStreamStateListener extends LogStreamListener
{
	public void onEOFReached( );

	public void onOpened( );

	public void onClosed( );
}

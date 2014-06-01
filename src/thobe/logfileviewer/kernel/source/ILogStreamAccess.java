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

import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;
import thobe.logfileviewer.kernel.source.listeners.LogStreamStateListener;

/**
 * @author Thomas Obenaus
 * @source ILogStreamAccess.java
 * @date Jun 1, 2014
 */
public interface ILogStreamAccess
{
	/**
	 * Add a new {@link LogStreamDataListener}.
	 * @param l
	 */
	public void addLogStreamDataListener( LogStreamDataListener l );

	/**
	 * Remove a {@link LogStreamDataListener}.
	 * @param l
	 */
	public void removeLogStreamDataListener( LogStreamDataListener l );

	/**
	 * Add a new {@link LogStreamStateListener}.
	 * @param l
	 */
	public void addLogStreamStateListener( LogStreamStateListener l );

	/**
	 * Remove a {@link LogStreamStateListener}.
	 * @param l
	 */
	public void removeLogStreamStateListener( LogStreamStateListener l );
}

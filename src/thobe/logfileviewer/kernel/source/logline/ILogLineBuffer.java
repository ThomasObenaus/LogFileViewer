/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logline;

import thobe.logfileviewer.kernel.memory.IMemoryWatchable;

/**
 * @author Thomas Obenaus
 * @source ILogLineBuffer.java
 * @date Oct 12, 2014
 */
public interface ILogLineBuffer extends IMemoryWatchable
{
	public float getLoadFactor( );

	public int getMaxCapacity( );

	public int getCurrentLoad( );
}

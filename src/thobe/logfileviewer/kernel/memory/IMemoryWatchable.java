/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.memory;

/**
 * @author Thomas Obenaus
 * @source IMemoryWatchable.java
 * @date 02.10.2014
 */
public interface IMemoryWatchable
{
	/**
	 * Should return the memory obtained/allocated by this {@link IMemoryWatchable} (in bytes).
	 * @return
	 */
	public long getMemory( );

	/**
	 * This {@link IMemoryWatchable} should free all its internal memory.
	 */
	public void freeMemory( );

	/**
	 * Should return the name of this {@link IMemoryWatchable}.
	 * @return
	 */
	public String getNameOfMemoryWatchable( );
}

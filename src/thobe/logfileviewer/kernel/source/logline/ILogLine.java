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

/**
 * Interface for {@link LogLine}s whose intrinsic state (the data itself/ a string) is managed in the according factory
 * to reduce the memory-consumption needed for duplicates.
 * This interface represents the flyweight part of the flyweight-pattern.
 * @author Thomas Obenaus
 * @source ILogLineData.java
 * @date 11.09.2014
 */
public interface ILogLine
{

	/**
	 * Returns the data.
	 * @return
	 */
	public String getData( );

	public long getTimeStamp( );

	public long getId( );

	public long getMemory( );
}

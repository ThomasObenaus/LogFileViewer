/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.console;

import java.util.List;

import thobe.logfileviewer.kernel.source.logline.LogLine;

/**
 * @author Thomas Obenaus
 * @source ConsoleDataListener.java
 * @date Jul 27, 2014
 */
public interface ConsoleDataListener
{
	public void onNewData( List<LogLine> blockOfLines );

	public void freeMemory( );

	public long getCurrentMemory( );
}

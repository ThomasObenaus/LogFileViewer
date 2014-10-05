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

import java.util.List;

import thobe.logfileviewer.kernel.source.LogStreamContentPublisher;

/**
 * Raw listener that is used to monitor the {@link LogStreamContentPublisher}.
 * @author Thomas Obenaus
 * @source LogStreamContentPublisherListener.java
 * @date May 31, 2014
 */
public interface ILogStreamContentPublisherListener extends ILogStreamStateListener
{
	public void onNewLine( String newLine );

	public void onNewBlock( List<String> newBlock );
}

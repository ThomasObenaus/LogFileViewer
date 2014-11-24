/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logstream;

import java.util.List;

/**
 * Raw listener that is used to monitor the {@link InternalLogStreamReader}.
 * @author Thomas Obenaus
 * @source LogStreamContentPublisherListener.java
 * @date May 31, 2014
 */
public interface IInternalLogStreamReaderListener extends ILogStreamStateListener
{
	/**
	 * Called whenever a new line is available (was read from the stream).
	 * @param newLine
	 */
	public void onNewLine( String newLine );

	/**
	 * Called whenever a new block of lines is available (was read from the stream).
	 * @param newLine
	 */
	public void onNewBlock( List<String> newBlock );
}

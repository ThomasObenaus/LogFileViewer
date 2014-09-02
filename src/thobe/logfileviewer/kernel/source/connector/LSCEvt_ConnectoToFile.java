/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.connector;

import java.io.File;

/**
 * @author Thomas Obenaus
 * @source LSCEvt_ConnectoToFile.java
 * @date Aug 16, 2014
 */
public class LSCEvt_ConnectoToFile extends LSConnectorEvent
{
	private File	file;

	public LSCEvt_ConnectoToFile( long delay, File file )
	{
		super( LSConnectorEventType.CONNECT, delay );
		this.file = file;
	}

	public File getFile( )
	{
		return file;
	}
	
	@Override
	public String toString( )
	{
		return "Connect to file: '" + this.file + "' in " + this.getDelay( ) + " ms";
	}
}

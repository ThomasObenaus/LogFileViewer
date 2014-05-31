/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel;

import thobe.logfileviewer.kernel.source.DataSource;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerApp.java
 * @date May 15, 2014
 */
public class LogFileViewerApp
{
	private DataSource	dataSource;

	public LogFileViewerApp( )
	{
		this.dataSource = new DataSource( );
	}
	
	public DataSource getDataSource( )
	{
		return dataSource;
	}
}

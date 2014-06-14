/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.server;

/**
 * Structure keeping the arguments for the {@link EthSource}.
 * @author Thomas Obenaus
 * @source Arguments.java
 * @date May 15, 2014
 */
public final class Arguments
{
	private Integer	port;
	private String	filename;

	public Arguments( String filename, Integer port )
	{
		this.filename = filename;
		this.port = port;
	}

	public String getFilename( )
	{
		return filename;
	}

	public Integer getPort( )
	{
		return port;
	}
}

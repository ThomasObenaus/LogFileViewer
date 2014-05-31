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
 * @author Thomas Obenaus
 * @source Arguments.java
 * @date May 15, 2014
 */
public final class Arguments
{
	private Integer	port;
	private String	host;

	public Arguments( String host, Integer port )
	{
		this.host = host;
		this.port = port;
	}

	public String getHost( )
	{
		return host;
	}

	public Integer getPort( )
	{
		return port;
	}
}

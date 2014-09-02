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

/**
 * @author Thomas Obenaus
 * @source LSCEvt_ConnectToIP.java
 * @date Aug 16, 2014
 */
public class LSCEvt_ConnectToIP extends LSConnectorEvent
{

	private String	host;
	private int		port;

	public LSCEvt_ConnectToIP( long delay, String host, int port )
	{
		super( LSConnectorEventType.CONNECT, delay );
		this.host = host;
		this.port = port;
	}

	public String getHost( )
	{
		return host;
	}

	public int getPort( )
	{
		return port;
	}

	@Override
	public String toString( )
	{
		return "Connect to " + this.host + ":" + this.port + " in " + this.getDelay( ) + " ms";
	}
}

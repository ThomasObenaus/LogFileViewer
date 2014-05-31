/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * {@link TraceSource} reading over ip (source is a socket).
 * @author Thomas Obenaus
 * @source IpSource.java
 * @date May 15, 2014
 */
public class IpSource extends TraceSource
{
	private int				port;
	private String			host;
	private BufferedReader	reader;
	private Socket			socket;

	public IpSource( String host, int port )
	{
		this.host = host;
		this.port = port;
	}

	@Override
	protected String readLineImpl( ) throws TraceSourceException
	{
		if ( this.reader == null )
		{
			throw new TraceSourceException( "Reader not open, resource is null" );
		}// if ( this.reader == null ) .

		try
		{
			return this.reader.readLine( );
		}
		catch ( IOException e )
		{
			throw new TraceSourceException( e.getLocalizedMessage( ) );
		}
	}

	@Override
	protected void openImpl( ) throws TraceSourceException
	{
		if ( this.host == null || this.host.trim( ).isEmpty( ) )
			throw new TraceSourceException( "Hostname is missing" );
		if ( this.port == 0 )
			throw new TraceSourceException( "Port '" + this.port + "' is invalid" );

		try
		{
			this.socket = new Socket( this.host, this.port );			
			this.reader = new BufferedReader( new InputStreamReader( this.socket.getInputStream( ) ) );
		}
		catch ( IOException e )
		{
			try
			{
				// close open resources
				this.reader.close( );
				this.socket.close( );
			}
			catch ( IOException e1 )
			{
				LOG( ).severe( "Error while closing the ressources: " + e1.getLocalizedMessage( ) );
			}

			throw new TraceSourceException( "Unable to open connection to " + this.host + ":" + this.port + ". " + e.getLocalizedMessage( ) );
		}
	}

	@Override
	protected void closeImpl( ) throws TraceSourceException
	{
		try
		{
			// close open resources
			this.reader.close( );
			this.socket.close( );
			this.socket = null;
			this.reader = null;
		}
		catch ( IOException e )
		{
			throw new TraceSourceException( "Failed to close connection to " + this.host + ":" + this.port + ". " + e.getLocalizedMessage( ) );
		}
	}

}

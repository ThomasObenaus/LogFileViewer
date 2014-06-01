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
import java.net.SocketTimeoutException;

/**
 * {@link LogStreamReader} reading over ip (source is a socket).
 * @author Thomas Obenaus
 * @source IpLogStreamReader.java
 * @date May 15, 2014
 */
public class IpLogStreamReader extends LogStreamReader
{
	private int				port;
	private String			host;
	private BufferedReader	reader;
	private Socket			socket;

	public IpLogStreamReader( String host, int port )
	{
		this.host = host;
		this.port = port;
	}

	@Override
	protected String readLineImpl( int maxBlockTime ) throws LogStreamException,LogStreamTimeoutException
	{
		if ( this.reader == null )
		{
			throw new LogStreamException( "Reader not open, resource is null" );
		}// if ( this.reader == null ) .

		try
		{
			this.socket.setSoTimeout( maxBlockTime );
			return this.reader.readLine( );
		}
		catch ( SocketTimeoutException e )
		{
			throw new LogStreamTimeoutException( e.getLocalizedMessage( ) );
		}
		catch ( IOException e )
		{
			throw new LogStreamException( e.getLocalizedMessage( ) );
		}
	}

	@Override
	protected void openImpl( ) throws LogStreamException
	{
		if ( this.host == null || this.host.trim( ).isEmpty( ) )
			throw new LogStreamException( "Hostname is missing" );
		if ( this.port == 0 )
			throw new LogStreamException( "Port '" + this.port + "' is invalid" );

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
				if ( this.reader != null )
					this.reader.close( );
				if ( this.socket != null )
					this.socket.close( );
			}
			catch ( IOException e1 )
			{
				LOG( ).severe( "Error while closing the ressources: " + e1.getLocalizedMessage( ) );
			}

			throw new LogStreamException( "Unable to open connection to " + this.host + ":" + this.port + ". " + e.getLocalizedMessage( ) );
		}
	}

	@Override
	protected void closeImpl( ) throws LogStreamException
	{
		try
		{
			// close open resources
			if ( this.reader != null )
				this.reader.close( );
			if ( this.socket != null )
				this.socket.close( );
			this.socket = null;
			this.reader = null;
		}
		catch ( IOException e )
		{
			throw new LogStreamException( "Failed to close connection to " + this.host + ":" + this.port + ". " + e.getLocalizedMessage( ) );
		}
	}

}

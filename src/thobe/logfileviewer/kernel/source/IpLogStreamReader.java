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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.err.LogStreamTimeoutException;

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
	protected synchronized String readLineImpl( int maxBlockTime ) throws LogStreamException, LogStreamTimeoutException
	{
		if ( this.reader == null )
		{
			throw new LogStreamException( "Reader not open, resource is null" );
		}// if ( this.reader == null ) .

		try
		{
			if ( this.socket.getSoTimeout( ) != maxBlockTime )
			{
				this.socket.setSoTimeout( maxBlockTime );
			}
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
	protected synchronized void openImpl( int timeout ) throws LogStreamException
	{
		if ( this.host == null || this.host.trim( ).isEmpty( ) )
			throw new LogStreamException( "Hostname is missing" );
		if ( this.port == 0 )
			throw new LogStreamException( "Port '" + this.port + "' is invalid" );

		try
		{
			this.socket = new Socket( );
			// connect to socket regarding timeout
			this.socket.connect( new InetSocketAddress( this.host, this.port ), timeout );
			this.reader = new BufferedReader( new InputStreamReader( this.socket.getInputStream( ) ) );
		}
		catch ( SocketTimeoutException e )
		{
			// could not open connection within 
			throw new LogStreamException( "Unable to open connection to " + this.host + ":" + this.port + ". " + e.getLocalizedMessage( ) );
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
	protected synchronized void closeImpl( ) throws LogStreamException
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

	@Override
	protected synchronized List<String> readBlockImpl( int minBlockTime, int maxBlockTime, int minBlockSize, int maxBlockSize ) throws LogStreamException, LogStreamTimeoutException
	{
		List<String> block = new ArrayList<>( );

		if ( this.reader == null )
		{
			throw new LogStreamException( "Reader not open, resource is null" );
		}// if ( this.reader == null ) .

		try
		{
			if ( this.socket.getSoTimeout( ) != maxBlockTime )
			{
				this.socket.setSoTimeout( maxBlockTime );
			}

			long startTime = System.currentTimeMillis( );
			long elapsedTime = 0;
			boolean minBlockTimeExceeded = false;
			boolean minBlockSizeExceeded = false;

			while ( ( elapsedTime < maxBlockTime ) && ( block.size( ) < maxBlockSize ) )
			{
				block.add( this.reader.readLine( ) );
				elapsedTime = System.currentTimeMillis( ) - startTime;

				minBlockTimeExceeded = elapsedTime >= minBlockTime;
				minBlockSizeExceeded = block.size( ) >= minBlockSize;

				// leave loop if min-blocktime and min-blocksize where exceeded 
				if ( minBlockSizeExceeded && minBlockTimeExceeded )
				{
					break;
				}
			}// while ( ( elapsedTime < maxBlockTime ) && ( block.size( ) < maxBlockSize ) ).
			return block;
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

}

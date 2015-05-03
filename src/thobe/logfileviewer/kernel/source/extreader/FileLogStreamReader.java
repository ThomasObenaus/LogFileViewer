/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.extreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.err.LogStreamTimeoutException;

/**
 * @author Thomas Obenaus
 * @source FileLogStreamReader.java
 * @date May 2, 2015
 */
public class FileLogStreamReader extends ExternalLogStreamReader
{
	private File			file;
	private BufferedReader	reader;

	public FileLogStreamReader( File file )
	{
		super( "FileLogStreamReader" );
		this.file = file;
	}

	@Override
	protected synchronized String readLineImpl( int maxBlockTime ) throws LogStreamException, LogStreamTimeoutException
	{
		if ( this.reader == null )
		{
			throw new LogStreamException( "Reader not open, resource is null" );
		}// if ( this.reader == null ) .

		String result = null;
		try
		{
			result = this.reader.readLine( );
		}
		catch ( IOException e )
		{
			throw new LogStreamException( "Error reading line from '" + this.file.getAbsolutePath( ) + "': " + e.getLocalizedMessage( ) );
		}
		return result;
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
			long startTime = System.currentTimeMillis( );
			long elapsedTime = 0;
			boolean minBlockTimeExceeded = false;
			boolean minBlockSizeExceeded = false;
			long halfBlockTime = maxBlockTime / 2;

			while ( ( elapsedTime < maxBlockTime ) && ( block.size( ) < maxBlockSize ) )
			{
				String line = this.reader.readLine( );
				if ( line == null )
				{
					// eof reached
					break;
				}

				block.add( line );
				elapsedTime = System.currentTimeMillis( ) - startTime;

				minBlockTimeExceeded = elapsedTime >= minBlockTime;
				minBlockSizeExceeded = block.size( ) >= minBlockSize;

				// leave loop if min-blocktime and min-blocksize where exceeded 
				if ( ( minBlockSizeExceeded && minBlockTimeExceeded ) || ( minBlockSizeExceeded && ( elapsedTime >= halfBlockTime ) ) )
				{
					break;
				}
			}// while ( ( elapsedTime < maxBlockTime ) && ( block.size( ) < maxBlockSize ) ).
			return block;
		}
		catch ( IOException e )
		{
			throw new LogStreamException( e.getLocalizedMessage( ) );
		}
	}

	@Override
	protected synchronized void openImpl( int timeout ) throws LogStreamException
	{
		if ( this.file == null )
			throw new LogStreamException( "File is missing" );

		if ( this.file.isDirectory( ) )
			throw new LogStreamException( "File is a directory" );

		if ( !this.file.canRead( ) )
			throw new LogStreamException( "File not readable" );

		try
		{
			this.reader = new BufferedReader( new FileReader( this.file ) );
		}
		catch ( IOException e )
		{
			try
			{
				// close open resources
				if ( this.reader != null )
					this.reader.close( );
			}
			catch ( IOException e1 )
			{
				LOG( ).severe( "Error while closing the ressources: " + e1.getLocalizedMessage( ) );
			}

			throw new LogStreamException( "(IOException) Unable to open file '" + this.file.getAbsolutePath( ) + "': " + e.getLocalizedMessage( ) );
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
			this.reader = null;
		}
		catch ( IOException e )
		{
			throw new LogStreamException( "Failed to close file '" + this.file.getAbsolutePath( ) + "': " + e.getLocalizedMessage( ) );
		}
	}

}

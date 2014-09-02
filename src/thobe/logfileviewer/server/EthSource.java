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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Ehternet based logger, needed for testing.
 * @author Thomas Obenaus
 * @source EthSource.java
 * @date May 15, 2014
 */
public class EthSource extends Thread
{
	private static final String			APP_NAME					= "ethsource";
	private static final int			CLIENT_CONNECTION_TIMEOUT	= 10000;
	private static final long			MAX_TIMEOUT					= 25000;
	private static final int			MAX_LINES_PER_BLOCK			= 50;

	private Map<Socket, PrintWriter>	clientWriterMap;
	private ServerSocket				serverSocket;
	private boolean						quitRequested;
	private ClientAccepter				clientAccepter;
	private ClientConnectionChecker		clientConnectionChecker;
	private File						file;
	private AtomicLong					linesSend;
	private AtomicLong					startTime;
	private AtomicInteger				sleepTime;

	public EthSource( int port, File file ) throws IOException
	{
		this.file = file;
		this.clientWriterMap = new HashMap<>( );
		this.serverSocket = new ServerSocket( port );
		this.quitRequested = false;
		this.linesSend = new AtomicLong( 0 );
		this.startTime = new AtomicLong( 0 );
		this.sleepTime = new AtomicInteger( 5 );
	}

	/**
	 * Set the lines the eth-source should pump lines per second over ethernet. (Max ~9500 lps)
	 * @param lps
	 */
	public void setLinesPerSecond( int lps )
	{
		if ( lps > 9500 )
			lps = 9500;

		int blocksPerSecond = lps / MAX_LINES_PER_BLOCK;
		if ( blocksPerSecond == 0 )
			blocksPerSecond++;

		int sleepFor = 1000 / blocksPerSecond;
		if ( sleepFor < 5 )
			sleepFor = 5;

		this.sleepTime.set( sleepFor );
	}

	void addClient( Socket client ) throws IOException
	{
		PrintWriter writer = new PrintWriter( client.getOutputStream( ) );
		synchronized ( this.clientWriterMap )
		{
			this.clientWriterMap.put( client, writer );
		}
	}

	void removeClient( Socket client )
	{
		synchronized ( this.clientWriterMap )
		{
			this.clientWriterMap.remove( client );
		}
	}

	Map<Socket, PrintWriter> getClientWriterMap( )
	{
		Map<Socket, PrintWriter> copyOfMap = new HashMap<>( );
		synchronized ( this.clientWriterMap )
		{
			copyOfMap.putAll( this.clientWriterMap );
		}
		return copyOfMap;
	}

	public double getLinesPerSecond( )
	{
		long elapsed = System.currentTimeMillis( ) - this.startTime.get( );
		return ( this.linesSend.get( ) / ( elapsed / 1000.0d ) );
	}

	@Override
	public void run( )
	{

		System.out.println( "Starting EthSource" );
		this.clientAccepter = new ClientAccepter( this, this.serverSocket );
		System.out.println( "ClientAccepter created, start it" );
		this.clientAccepter.start( );

		System.out.println( "ClientConnectionChecker created, start it" );
		this.clientConnectionChecker = new ClientConnectionChecker( this );
		this.clientConnectionChecker.start( );

		this.startTime.set( System.currentTimeMillis( ) );

		Timer timer = new Timer( "ClientConnectionChecker.LPSPrinter.Timer" );
		timer.schedule( new LPSPrinter( this ), 5000, 5000 );

		BufferedReader reader = null;
		StringBuffer strBuffer = new StringBuffer( );
		while ( !this.quitRequested )
		{
			// Open the file 
			if ( reader == null )
			{
				System.out.println( "Opening " + this.file.getAbsolutePath( ) );
				try
				{
					reader = new BufferedReader( new FileReader( this.file ) );
				}
				catch ( FileNotFoundException e )
				{
					System.err.println( "Could not find file " + this.file.getAbsolutePath( ) + ": " + e.getLocalizedMessage( ) );
					break;
				}
			}

			// clear the strBuffer
			strBuffer.setLength( 0 );
			String line = null;
			int linesCollected = 0;

			// collect N lines from the file
			try
			{
				while ( ( ( line = reader.readLine( ) ) != null ) && linesCollected < MAX_LINES_PER_BLOCK )
				{
					strBuffer.append( line + "\n" );
					linesCollected++;
					this.linesSend.incrementAndGet( );
				}

				if ( line == null )
				{
					System.out.println( "EOF reached, reopeneing file" );
					reader = null;
				}
			}
			catch ( IOException e1 )
			{
				System.err.println( "Error reading file: " + e1.getLocalizedMessage( ) );
				break;
			}

			// now put the lines to the clients
			synchronized ( this.clientWriterMap )
			{
				String block = strBuffer.toString( );
				for ( Entry<Socket, PrintWriter> entry : this.clientWriterMap.entrySet( ) )
				{
					PrintWriter out = entry.getValue( );
					out.write( block, 0, block.length( ) );
				}
			}

			try
			{
				Thread.sleep( this.sleepTime.get( ) );
			}
			catch ( InterruptedException e )
			{
				break;
			}
		}

		// close the reader 
		if ( reader != null )
		{
			try
			{
				reader.close( );
			}
			catch ( IOException e )
			{
				System.err.println( "Error closing reader: " + e.getLocalizedMessage( ) );
			}
		}

		try
		{
			System.out.println( "Quit the client accepter" );
			this.clientAccepter.quit( );
			this.clientAccepter.interrupt( );
			this.clientAccepter.join( );

			System.out.println( "Quit the client connection checker" );
			this.clientConnectionChecker.quit( );
			this.clientConnectionChecker.interrupt( );
			this.clientConnectionChecker.join( );
		}
		catch ( InterruptedException e )
		{}

		System.out.println( "Stopping EthSource" );
	}

	private class ClientConnectionChecker extends Thread
	{
		private boolean		quitRequested;
		private EthSource	ethSource;

		public ClientConnectionChecker( EthSource ethSource )
		{
			this.ethSource = ethSource;
			this.quitRequested = false;
		}

		public void quit( )
		{
			this.quitRequested = true;
		}

		@Override
		public void run( )
		{
			System.out.println( "ClientConnectionChecker started" );
			while ( !this.quitRequested )
			{
				Map<Socket, PrintWriter> clients = this.ethSource.getClientWriterMap( );

				for ( Entry<Socket, PrintWriter> entry : clients.entrySet( ) )
				{
					Socket client = entry.getKey( );
					long lastReadTime = System.currentTimeMillis( );
					boolean clientDisconnected = false;
					try
					{
						int result = client.getInputStream( ).read( );
						clientDisconnected = ( result == -1 );
					}
					catch ( SocketTimeoutException e )
					{
						clientDisconnected = !( System.currentTimeMillis( ) - lastReadTime < MAX_TIMEOUT );
					}
					catch ( IOException e )
					{
						clientDisconnected = true;
					}

					if ( clientDisconnected )
					{
						System.out.println( "Client " + client.getInetAddress( ) + " disconnected" );
						this.ethSource.removeClient( client );
						try
						{
							client.close( );
						}
						catch ( IOException e )
						{
							e.printStackTrace( );
						}
					}
				}

				try
				{
					Thread.sleep( 500 );
				}
				catch ( InterruptedException e )
				{
					break;
				}
			}

			System.out.println( "ClientConnectionChecker stopped" );
		}
	}

	private class ClientAccepter extends Thread
	{
		private ServerSocket	serverSocket;
		private EthSource		ethSource;
		private boolean			quitRequested;

		public ClientAccepter( EthSource ethSource, ServerSocket serverSocket )
		{
			this.ethSource = ethSource;
			this.serverSocket = serverSocket;
			this.quitRequested = false;
		}

		public void quit( )
		{
			this.quitRequested = true;
		}

		@Override
		public void run( )
		{
			System.out.println( "Starting ClientAccepter" );
			while ( !this.quitRequested )
			{
				try
				{
					Socket client = this.serverSocket.accept( );
					client.setSoTimeout( CLIENT_CONNECTION_TIMEOUT );
					if ( client != null )
					{
						System.out.println( "New client " + client.getInetAddress( ) + " found" );
						this.ethSource.addClient( client );
					}
				}
				catch ( IOException e )
				{
					System.err.println( "Error waiting for clients: " + e.getLocalizedMessage( ) );
				}

				try
				{
					Thread.sleep( 500 );
				}
				catch ( InterruptedException e )
				{
					break;
				}
			}

			System.out.println( "Stopping ClientAccepter" );
		}
	}

	public static void main( String[] args )
	{
		Arguments parsedArgs = parseCommandLine( args );
		System.out.println( "Connecting to localhost at port=" + parsedArgs.getPort( ) + ", reading file='" + parsedArgs.getFilename( ) + "'" );

		try
		{
			EthSource ethSource = new EthSource( parsedArgs.getPort( ), new File( parsedArgs.getFilename( ) ) );
			ethSource.setLinesPerSecond( parsedArgs.getLps( ) );
			ethSource.start( );

			try
			{
				ethSource.join( );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace( );
			}
		}
		catch ( IOException e )
		{
			System.err.println( "Error opening connection: " + e.getLocalizedMessage( ) );
			System.exit( 1 );
		}
	}

	public static void usage( Options options )
	{
		HelpFormatter formatter = new HelpFormatter( );
		formatter.printHelp( APP_NAME, options );
	}

	public static Arguments parseCommandLine( String args[] )
	{

		final String OPT_PORT = "p";
		final String OPT_FILE = "f";
		final String OPT_LPS = "l";

		// create Options object
		Options options = new Options( );

		@SuppressWarnings ( "static-access")
		Option optFilename = OptionBuilder.withArgName( "filename" ).hasArg( ).withLongOpt( "filename" ).withDescription( "Name of the file that should be read and send over socket." ).create( OPT_FILE );

		@SuppressWarnings ( "static-access")
		Option optPort = OptionBuilder.withArgName( "portnumber" ).hasArg( ).withLongOpt( "port" ).withDescription( "The port to listen/send to." ).create( OPT_PORT );

		@SuppressWarnings ( "static-access")
		Option optLPS = OptionBuilder.withArgName( "lines per second" ).hasArg( ).withLongOpt( "lps" ).withDescription( "The lines per second that should be published over eth by this source." ).create( OPT_LPS );

		options.addOption( optFilename );
		options.addOption( optPort );
		options.addOption( optLPS );

		String filename = null;
		Integer port = null;
		int lps = 9500;
		try
		{
			CommandLineParser parser = new GnuParser( );
			CommandLine cmd = parser.parse( options, args );

			filename = cmd.getOptionValue( OPT_FILE );
			if ( filename == null )
			{
				System.err.println( "Filename is missing" );
				usage( options );
				System.exit( 1 );
			}

			String portStr = cmd.getOptionValue( OPT_PORT );
			if ( portStr == null )
			{
				System.err.println( "Port is missing" );
				usage( options );
				System.exit( 1 );
			}

			try
			{
				port = Integer.parseInt( portStr );
			}
			catch ( NumberFormatException e )
			{
				System.err.println( "Port is not a number" );
				usage( options );
				System.exit( 1 );
			}

			String lpsStr = cmd.getOptionValue( OPT_LPS );
			if ( lpsStr != null )
			{
				try
				{
					lps = Integer.parseInt( lpsStr );
				}
				catch ( NumberFormatException e )
				{
					System.err.println( "Ignore parameter '" + OPT_LPS + "' (lines per second) since '" + lpsStr + "' is not a number" );
				}
			}

		}
		catch ( ParseException e )
		{
			System.err.println( e.getLocalizedMessage( ) );
			usage( options );
			System.exit( 2 );
		}

		return new Arguments( filename, port, lps );
	}

	private class LPSPrinter extends TimerTask
	{
		private EthSource	src;

		public LPSPrinter( EthSource src )
		{
			this.src = src;
		}

		@Override
		public void run( )
		{
			System.out.println( String.format( "%.3f", this.src.getLinesPerSecond( ) ) + " lps" );
		}
	}
}

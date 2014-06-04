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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
public class EthSource
{
	private static final String	APP_NAME	= "ethsource";

	public static void main( String[] args )
	{
		Arguments parsedArgs = parseCommandLine( args );
		System.out.println( "Connecting to " + parsedArgs.getHost( ) + ":" + parsedArgs.getPort( ) );

		try
		{
			ServerSocket serverSocket = new ServerSocket( parsedArgs.getPort( ) );
			System.out.println( "waiting..." );
			Socket clientSocket = serverSocket.accept( );
			System.out.println( "client connected" );
			PrintWriter out = new PrintWriter( clientSocket.getOutputStream( ), true );

			while ( true )
			{
				String fileName = "backupfile.txt";
				System.out.println( "Opening file " + fileName );

				BufferedReader reader = new BufferedReader( new FileReader( new File( fileName ) ) );
				String line = null;

				while ( ( line = reader.readLine( ) ) != null )
				{
					out.println( line );

					try
					{
						Thread.sleep( 0 );
					}
					catch ( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace( );
					}
				}
				reader.close( );
			}
		}
		catch ( IOException e )
		{
			System.out.println( "Exception caught when trying to listen on port " + parsedArgs.getPort( ) + " or listening for a connection" );
			System.out.println( e.getMessage( ) );
		}
	}

	public static void usage( Options options )
	{
		HelpFormatter formatter = new HelpFormatter( );
		formatter.printHelp( APP_NAME, options );
	}

	public static Arguments parseCommandLine( String args[] )
	{

		final String OPT_HOSTNAME = "h";
		final String OPT_PORT = "p";

		// create Options object
		Options options = new Options( );

		@SuppressWarnings ( "static-access")
		Option optHostname = OptionBuilder.withArgName( "hostname" ).hasArg( ).withLongOpt( "host" ).withDescription( "Name of the host (e.g. 192.168.1.4)" ).create( OPT_HOSTNAME );

		@SuppressWarnings ( "static-access")
		Option optPort = OptionBuilder.withArgName( "portnumber" ).hasArg( ).withLongOpt( "port" ).withDescription( "The port to listen/send to." ).create( OPT_PORT );

		options.addOption( optHostname );
		options.addOption( optPort );

		String hostname = null;
		Integer port = null;
		try
		{
			CommandLineParser parser = new GnuParser( );
			CommandLine cmd = parser.parse( options, args );

			hostname = cmd.getOptionValue( OPT_HOSTNAME );
			if ( hostname == null )
			{
				System.err.println( "Hostname is missing" );
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

		}
		catch ( ParseException e )
		{
			System.err.println( e.getLocalizedMessage( ) );
			usage( options );
			System.exit( 2 );
		}

		return new Arguments( hostname, port );
	}
}

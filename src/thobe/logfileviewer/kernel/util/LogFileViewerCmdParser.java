/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerCmdParser.java
 * @date Apr 4, 2015
 */
public class LogFileViewerCmdParser
{
	public static CmdLineArguments parseCommandLine( String args[], String appName )
	{
		final String OPT_CONF_FILE = "cf";
		final String OPT_LOGGING_CONF_FILE = "lf";

		// create Options object
		Options options = new Options( );

		@SuppressWarnings ( "static-access")
		Option optConfFilename = OptionBuilder.withArgName( "name of configuration file" ).hasArg( ).withLongOpt( "cfg-file" ).withDescription( "Name of the configuration file." ).create( OPT_CONF_FILE );
		
		@SuppressWarnings ( "static-access")
		Option optLoggingConfFilename = OptionBuilder.withArgName( "name of logging-conf file" ).hasArg( ).withLongOpt( "log-cfg-file" ).withDescription( "Name of the logging configuration file." ).create( OPT_LOGGING_CONF_FILE );

		options.addOption( optConfFilename );
		options.addOption( optLoggingConfFilename );

		String confFilename = null;
		String loggingConfFilename = null;
		try
		{
			CommandLineParser parser = new GnuParser( );
			CommandLine cmd = parser.parse( options, args );

			confFilename = cmd.getOptionValue( OPT_CONF_FILE );
			loggingConfFilename = cmd.getOptionValue( OPT_LOGGING_CONF_FILE );
		}
		catch ( ParseException e )
		{
			System.err.println( e.getLocalizedMessage( ) );
			usage( options, appName );
			System.exit( 2 );
		}

		CmdLineArguments parsedArgs = new CmdLineArguments( );
		parsedArgs.setConfigurationFilename( confFilename );
		parsedArgs.setLoggingConfigurationFilename( loggingConfFilename );
		return parsedArgs;
	}

	public static void usage( Options options, String appName )
	{
		HelpFormatter formatter = new HelpFormatter( );
		formatter.printHelp( appName, options );
	}
}

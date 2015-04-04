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

		// create Options object
		Options options = new Options( );

		@SuppressWarnings ( "static-access")
		Option optConfFilename = OptionBuilder.withArgName( "name of cofiguration file" ).hasArg( ).withLongOpt( "cfg-file" ).withDescription( "Name of the configuration file." ).create( OPT_CONF_FILE );

		options.addOption( optConfFilename );

		String confFilename = null;
		try
		{
			CommandLineParser parser = new GnuParser( );
			CommandLine cmd = parser.parse( options, args );

			confFilename = cmd.getOptionValue( OPT_CONF_FILE );
		}
		catch ( ParseException e )
		{
			System.err.println( e.getLocalizedMessage( ) );
			usage( options, appName );
			System.exit( 2 );
		}

		CmdLineArguments parsedArgs = new CmdLineArguments( );
		parsedArgs.setConfigurationFileName( confFilename );
		return parsedArgs;
	}

	public static void usage( Options options, String appName )
	{
		HelpFormatter formatter = new HelpFormatter( );
		formatter.printHelp( appName, options );
	}
}

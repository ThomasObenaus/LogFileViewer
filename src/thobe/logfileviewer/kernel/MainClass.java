/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import thobe.logfileviewer.LogFileViewerInfo;
import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.kernel.util.CmdLineArguments;
import thobe.logfileviewer.kernel.util.LogFileViewerCmdParser;
import thobe.tools.log.Logging;
import thobe.tools.log.LoggingException;

/**
 * The main-class with main-entry-point for the LogFileViewerApplication.
 * @author Thomas Obenaus
 * @source MainClass.java
 * @date May 15, 2014
 */
public class MainClass
{
	private static String	OS	= System.getProperty( "os.name" ).toLowerCase( );

	public static void main( String[] args )
	{
		// parse the command-line arguments
		CmdLineArguments parsedArgs = LogFileViewerCmdParser.parseCommandLine( args, LogFileViewerInfo.getAppName( ) );

		String loggingIni = "logging.ini";
		if ( parsedArgs.getLoggingConfigurationFilename( ) != null )
		{
			loggingIni = parsedArgs.getLoggingConfigurationFilename( );
		}

		try
		{
			Logging.init( loggingIni );
		}
		catch ( LoggingException e1 )
		{
			System.err.println( "Unable to initialize logging using file '" + loggingIni + "'" );
		}

		// set the look and feel
		setLookAndFeel( );

		// instantiate the app
		LogFileViewerApp app = new LogFileViewerApp( parsedArgs );

		// add the running application to its visual representation
		MainFrame mainFrame = new MainFrame( app );

		// start the application
		app.start( );
		mainFrame.setVisible( true );
	}

	public static void setLookAndFeel( )
	{
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName( );

		if ( isUnix( ) )
		{
			// try to set nimbus:
			for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels( ) )
			{
				if ( "Nimbus".equals( info.getName( ) ) )
				{
					// Nimbus found
					lookAndFeel = info.getClassName( );
					break;
				}// if ( "Nimbus".equals( info.getName( ) ) ).
			}// for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels( ) ).
		}// if ( isUnix( ) )

		try
		{
			UIManager.setLookAndFeel( lookAndFeel );
		}
		catch ( UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e )
		{
			System.err.println( "Unable to set look-and-feel [" + lookAndFeel + "] trying " + UIManager.getCrossPlatformLookAndFeelClassName( ) );
			lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName( );
			try
			{
				UIManager.setLookAndFeel( lookAndFeel );
			}
			catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1 )
			{
				System.err.println( "Unable to set look-and-feel [" + lookAndFeel + "] exitig ..." );
				System.exit( 1 );
			}// catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1 ).
		}// catch ( UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e ).
	}

	public static boolean isUnix( )
	{
		return ( OS.indexOf( "nix" ) >= 0 || OS.indexOf( "nux" ) >= 0 || OS.indexOf( "aix" ) > 0 );
	}
}

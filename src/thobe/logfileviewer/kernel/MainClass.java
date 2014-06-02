/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import thobe.logfileviewer.gui.MainFrame;
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
	public static void main( String[] args )
	{
		final String loggingIni = "logging.ini";
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

		
		LogFileViewerApp app = new LogFileViewerApp( );
		// add the running application to its visual representation
		MainFrame mainFrame = new MainFrame( app );

		// start the application
		app.start( );
		mainFrame.setVisible( true );
	}

	public static void setLookAndFeel( )
	{
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName( );

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
}

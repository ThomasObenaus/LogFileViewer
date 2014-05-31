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

import thobe.logfileviewer.gui.MainFrame;
import thobe.tools.log.Logging;
import thobe.tools.log.LoggingException;

/**
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

		LogFileViewerApp app = new LogFileViewerApp( );
		MainFrame mainFrame = new MainFrame( app );
		mainFrame.setVisible( true );

		/*try
		{
			IpSource source = new IpSource( "127.0.0.1", 15000 );
			source.open( );
			ReaderThread<IpSource> ipSourceReader = new ReaderThread<IpSource>( source );

			ipSourceReader.start( );

			while ( true )
			{

				try
				{
					if ( source.hasNextLine( ) )
						System.out.println( source.nextLine( ) );
				}
				catch ( TraceSourceException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace( );
				}
			}

		}
		catch ( TraceSourceException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace( );
		}
		*/
	}
}

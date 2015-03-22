/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer;

/**
 * General information about the application.
 * @author Thomas Obenaus
 * @source LogFileViewerInfo.java
 * @date May 15, 2014
 */
public class LogFileViewerInfo
{
	private static final String	APP_NAME		= "LogFileViewer";
	private static final int	MAJOR_VERSION	= 1;
	private static final int	MINOR_VERSION	= 3;
	private static final int	BUGFIX_VERSION	= 0;

	public static String getAppName( )
	{
		return APP_NAME;
	}

	public static String getVersion( )
	{
		return MAJOR_VERSION + "." + MINOR_VERSION + "." + BUGFIX_VERSION;
	}
}

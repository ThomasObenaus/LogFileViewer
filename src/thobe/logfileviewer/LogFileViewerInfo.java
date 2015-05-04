/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
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
	private static final int MAJOR_VERSION = 1;
	private static final int MINOR_VERSION = 6;
	private static final int BUGFIX_VERSION = 1;

	public static String getAppName( )
	{
		return APP_NAME;
	}

	public static String getVersion( )
	{
		return MAJOR_VERSION + "." + MINOR_VERSION + "." + BUGFIX_VERSION;
	}

	public static String getWebsite( )
	{
		return "https://github.com/ThomasObenaus/LogFileViewer";
	}

	public static String getAuthor( )
	{
		return "Thomas Obenaus";
	}

	public static String getLicense( )
	{
		return "Copyright (C) 2014, Thomas Obenaus. All rights reserved. Licensed under the New BSD License (3-clause lic)";
	}

	public static String getAuthorEMailAddress( )
	{
		return "obenaus.thomas@gmail.com";
	}

	public static String getDescription( )
	{
		return "The LogFileViewer is a small tool for reading and displaying the contents of logfiles in a more useful/readable way. " + "On writing plugins for a software that supports logging you are able to provide a more informative view of the log " + "contents than the plain textual one. The goal is to use the knowledge about the meaning of the loglines and to show " + "it to other users in a way they can understand.";
	}
}

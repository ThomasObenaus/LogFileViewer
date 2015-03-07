/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin;

/**
 * @author Thomas Obenaus
 * @source PluginApiVersion.java
 * @date Mar 7, 2015
 */
public class PluginApiVersion
{
	/**
	 * Increase for incompatible changes
	 */
	private static final int	MAJOR_VERSION	= 1;

	/**
	 * Increase for compatible changes
	 */
	private static final int	MINOR_VERSION	= 1;

	/**
	 * Increase for bugfixes
	 */
	private static final int	BUGFIX_VERSION	= 0;

	public int getMajorVersion( )
	{
		return MAJOR_VERSION;
	}

	public int getMinorVersion( )
	{
		return MINOR_VERSION;
	}

	public int getBugfixVersion( )
	{
		return BUGFIX_VERSION;
	}

	public boolean isCompatible( PluginApiVersion versionOfPlugin )
	{
		if ( versionOfPlugin.getMajorVersion( ) != getMajorVersion( ) )
		{
			return false;
		}

		return true;
	}
}

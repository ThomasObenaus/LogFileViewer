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
 * @source PluginManagerException.java
 * @date Apr 4, 2015
 */
@SuppressWarnings ( "serial")
public class PluginManagerException extends Exception
{
	public PluginManagerException( String cause )
	{
		super( cause );
	}
}

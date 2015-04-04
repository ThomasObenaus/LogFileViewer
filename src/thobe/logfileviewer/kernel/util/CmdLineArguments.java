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

/**
 * @author Thomas Obenaus
 * @source CmdLineArguments.java
 * @date Apr 4, 2015
 */
public class CmdLineArguments
{
	private String	configurationFileName;

	public CmdLineArguments( )
	{
		this.configurationFileName = null;
	}

	public void setConfigurationFileName( String configurationFileName )
	{
		this.configurationFileName = configurationFileName;
	}

	public String getConfigurationFileName( )
	{
		return configurationFileName;
	}
}

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
	private String	configurationFilename;
	private String	loggingConfifurationFilename;

	public CmdLineArguments( )
	{
		this.configurationFilename = null;
		this.loggingConfifurationFilename = null;
	}

	public String getLoggingConfigurationFilename( )
	{
		return loggingConfifurationFilename;
	}

	public void setLoggingConfigurationFilename( String loggingConfifurationFilename )
	{
		this.loggingConfifurationFilename = loggingConfifurationFilename;
	}

	public void setConfigurationFilename( String configurationFilename )
	{
		this.configurationFilename = configurationFilename;
	}

	public String getConfigurationFilename( )
	{
		return configurationFilename;
	}
}

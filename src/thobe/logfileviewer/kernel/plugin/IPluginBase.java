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
 * @source IPluginBase.java
 * @date Jul 7, 2014
 */
public interface IPluginBase
{
	/**
	 * This method should return the name of this {@link IPlugin}.
	 * @return
	 */
	public String getPluginName( );

	/**
	 * This method should return a short description of this {@link IPlugin}
	 * @return
	 */
	public String getPluginDescription( );
}

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

import thobe.logfileviewer.kernel.plugin.IPluginAccess;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerAppListener.java
 * @date Jun 1, 2014
 */
public interface ILogFileViewerAppListener
{
	public void newPluginsAvailable( IPluginAccess pluginAccess );
}

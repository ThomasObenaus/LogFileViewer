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
 * @source IPluginWindowManagerAccess.java
 * @date Jul 7, 2014
 */
public interface IPluginWindowManagerAccess
{
	public void registerVisualComponent( IPluginUI pluginUI, IPluginUIComponent pComponent );

	public void unRegisterVisualComponent( IPluginUI pluginUI, IPluginUIComponent pComponent );
}

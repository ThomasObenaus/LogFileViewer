/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui.plugin;

import javax.swing.JPanel;

import thobe.logfileviewer.kernel.plugin.IPluginWindowManagerAccess;

/**
 * @author Thomas Obenaus
 * @source IPluginWindowManager.java
 * @date Jul 7, 2014
 */
public interface IPluginWindowManager extends IPluginWindowManagerAccess
{
	public JPanel getMainPanel( );
}

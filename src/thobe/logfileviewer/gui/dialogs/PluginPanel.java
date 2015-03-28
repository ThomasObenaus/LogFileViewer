/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui.dialogs;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import thobe.logfileviewer.plugin.Plugin;

/**
 * @author Thomas Obenaus
 * @source PluginPanel.java
 * @date Mar 24, 2015
 */
public class PluginPanel extends JPanel
{
	private Plugin	plugin;

	public PluginPanel( Plugin plugin )
	{
		this.plugin = plugin;

		this.buildGUI( );
	}

	private void buildGUI( )
	{
		this.setLayout( new BorderLayout( ) );
		this.add( new JLabel( this.plugin.getPluginName( ) ) );
	}
}

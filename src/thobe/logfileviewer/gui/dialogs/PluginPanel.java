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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import thobe.logfileviewer.kernel.preferences.PluginManagerPrefs;
import thobe.logfileviewer.plugin.Plugin;

/**
 * @author Thomas Obenaus
 * @source PluginPanel.java
 * @date Mar 24, 2015
 */
@SuppressWarnings ( "serial")
public class PluginPanel extends JPanel
{
	private Plugin				plugin;
	private JToggleButton		tb_enablePlugin;
	private PluginManagerPrefs	pluginManagerPreferences;

	public PluginPanel( Plugin plugin, PluginManagerPrefs pluginManagerPreferences )
	{
		this.plugin = plugin;
		this.pluginManagerPreferences = pluginManagerPreferences;

		this.buildGUI( );
	}

	private void buildGUI( )
	{
		final String pluginName = this.plugin.getPluginName( );

		this.setLayout( new BorderLayout( 5, 5 ) );

		this.tb_enablePlugin = new JToggleButton( "Disable", true );
		this.add( this.tb_enablePlugin, BorderLayout.WEST );
		this.tb_enablePlugin.setToolTipText( "Disable plugin '" + pluginName + "'" );
		this.tb_enablePlugin.setSelected( this.plugin.isEnabled( ) );
		this.tb_enablePlugin.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final boolean bIsEnabled = tb_enablePlugin.isSelected( );
				setEnabled( bIsEnabled );
			}
		} );

		this.setEnabled( this.plugin.isEnabled( ) );

		this.add( new JLabel( this.plugin.getPluginName( ) ) );
	}

	@Override
	public void setEnabled( boolean enabled )
	{
		final String pluginName = this.plugin.getPluginName( );
		tb_enablePlugin.setText( ( enabled ? "Disable" : "Enable" ) );
		this.tb_enablePlugin.setToolTipText( ( enabled ? "Disable" : "Enable" ) + " plugin '" + pluginName + "'" );
		this.pluginManagerPreferences.setPluginEnabled( this.plugin.getPluginName( ), enabled );
	}

}

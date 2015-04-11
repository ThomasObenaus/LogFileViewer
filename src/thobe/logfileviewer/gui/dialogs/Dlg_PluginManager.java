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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.plugin.api.IPlugin;
import thobe.widgets.editor.Editor;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Thomas Obenaus
 * @source Dlg_PluginManager.java
 * @date Mar 24, 2015
 */
@SuppressWarnings ( "serial")
public class Dlg_PluginManager extends Editor
{
	private static Dimension	minSize	= new Dimension( 780, 500 );
	private PluginManager		manager;
	private List<PluginPanel>	pluginPanels;
	private Timer				memUpdateTimer;

	public Dlg_PluginManager( Window owner, PluginManager manager )
	{
		super( owner, "Pluginmanager", ModalityType.MODELESS );
		this.manager = manager;
		this.pluginPanels = new ArrayList<PluginPanel>( );

		this.buildGUI( );

		this.memUpdateTimer = new Timer( "Dlg_PluginManager.MemoryUpdateTimer" );
		this.memUpdateTimer.schedule( new MemoryUpdater( ), 500, 2000 );
	}

	private void buildGUI( )
	{
		String rowSpec = "3dlu,default,3dlu,default,7dlu";
		Map<String, IPlugin> plugins = this.manager.getPlugins( );
		Map<String, IPlugin> invalidPlugins = this.manager.getIncompatiblePlugins( );

		for ( int i = 0; i < plugins.size( ); ++i )
		{
			rowSpec += ",pref,3dlu";
		}

		for ( int i = 0; i < invalidPlugins.size( ); ++i )
		{
			rowSpec += ",pref,3dlu";
		}

		FormLayout fla_main = new FormLayout( "3dlu,fill:default:grow,3dlu", rowSpec );
		CellConstraints cc_main = new CellConstraints( );

		this.setLayout( fla_main );

		// plugin-folder
		JLabel l_pluginFolder = new JLabel( "Plugins where loaded from: " + this.manager.getPluginDirectory( ) );
		this.add( l_pluginFolder, cc_main.xy( 2, 2 ) );

		// api-version
		JLabel l_apiVersion = new JLabel( "The LogFileViewer uses the PluginApi version " + this.manager.getPluginApiVersion( ) );
		this.add( l_apiVersion, cc_main.xy( 2, 4 ) );

		// PluginPanels
		int row = 6;
		for ( Map.Entry<String, IPlugin> entry : plugins.entrySet( ) )
		{
			PluginPanel pluginPanel = new PluginPanel( entry.getValue( ), this.manager.getPrefs( ), true );
			this.pluginPanels.add( pluginPanel );
			this.add( pluginPanel, cc_main.xy( 2, row ) );
			row += 2;
		}

		// add invalid plugins (for information)
		for ( Map.Entry<String, IPlugin> entry : invalidPlugins.entrySet( ) )
		{
			PluginPanel pluginPanel = new PluginPanel( entry.getValue( ), this.manager.getPrefs( ), false );
			this.pluginPanels.add( pluginPanel );
			this.add( pluginPanel, cc_main.xy( 2, row ) );
			row += 2;
		}

	}

	@Override
	public Dimension getMinimumEditorSize( )
	{
		return minSize;
	}

	@Override
	protected JPanel createButtonPanel( )
	{
		JPanel pa_buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
		JButton bu_close = new JButton( "Close" );
		pa_buttons.add( bu_close );
		bu_close.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				close_dlg( );
			}
		} );

		return pa_buttons;
	}

	private class MemoryUpdater extends TimerTask
	{

		@Override
		public void run( )
		{
			long freeMemory = Runtime.getRuntime( ).freeMemory( );
			long totalMemory = Runtime.getRuntime( ).totalMemory( );
			long usedMemory = totalMemory - freeMemory;

			for ( PluginPanel p : pluginPanels )
			{
				p.updateMemoryConsumption( usedMemory );
			}
		}
	}

}

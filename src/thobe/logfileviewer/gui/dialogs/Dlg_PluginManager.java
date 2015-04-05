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
import javax.swing.JPanel;

import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.plugin.Plugin;
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
		Map<String, Plugin> plugins = this.manager.getPlugins( );
		Map<String, Plugin> invalidPlugins = this.manager.getIncompatiblePlugins( );
		String rowSpec = "3dlu";
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
		int row = 2;
		for ( Map.Entry<String, Plugin> entry : plugins.entrySet( ) )
		{
			PluginPanel pluginPanel = new PluginPanel( entry.getValue( ), this.manager.getPrefs( ), true );
			this.pluginPanels.add( pluginPanel );
			this.add( pluginPanel, cc_main.xy( 2, row ) );
			row += 2;
		}

		// add invalid plugins (for information)
		for ( Map.Entry<String, Plugin> entry : invalidPlugins.entrySet( ) )
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

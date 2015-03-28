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
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.plugin.Plugin;
import thobe.widgets.editor.Editor;

/**
 * @author Thomas Obenaus
 * @source Dlg_PluginManager.java
 * @date Mar 24, 2015
 */
@SuppressWarnings ( "serial")
public class Dlg_PluginManager extends Editor
{
	private static Dimension	minSize	= new Dimension( 400, 300 );
	private PluginManager		manager;

	public Dlg_PluginManager( Window owner, PluginManager manager )
	{
		super( owner, "Pluginmanager", ModalityType.APPLICATION_MODAL );
		this.manager = manager;
		this.buildGUI( );
	}

	private void buildGUI( )
	{
		Map<String, Plugin> plugins = this.manager.getPlugins( );
		String rowSpec = "3dlu";
		for ( int i = 0; i < plugins.size( ); ++i )
		{
			rowSpec += ",pref,3dlu";
		}

		FormLayout fla_main = new FormLayout( "3dlu,fill:default:grow,3dlu", rowSpec );
		CellConstraints cc_main = new CellConstraints( );

		this.setLayout( fla_main );
		int row = 2;
		for ( Map.Entry<String, Plugin> entry : plugins.entrySet( ) )
		{
			this.add( new PluginPanel( entry.getValue( ) ), cc_main.xy( 2, row ) );
			row+=2;
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

}

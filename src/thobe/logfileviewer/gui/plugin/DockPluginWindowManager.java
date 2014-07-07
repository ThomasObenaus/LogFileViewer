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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import thobe.logfileviewer.kernel.plugin.IPluginUI;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * Implementation of the {@link IPluginWindowManager} using the window library docking-frames.
 * @author Thomas Obenaus
 * @source DockPluginWindowManager.java
 * @date Jul 7, 2014
 */
public class DockPluginWindowManager implements IPluginWindowManager
{
	private CControl	dockableControl;

	public DockPluginWindowManager( JFrame parentFrame )
	{
		this.dockableControl = new CControl( parentFrame );
	}

	public JPanel getMainPanel( )
	{
		return this.dockableControl.getContentArea( );
	}

	public void addPlugin( IPluginUI plugin )
	{
		final DefaultSingleCDockable dockable = new DefaultSingleCDockable( plugin.getPluginName( ), plugin.getVisualComponent( ) );
		this.dockableControl.addDockable( dockable );
		plugin.setAttachedToGUI( );
		plugin.setVisible( true );

		// show the dockable
		SwingUtilities.invokeLater( new Runnable( )
		{
			@Override
			public void run( )
			{
				dockable.setVisible( true );
			}
		} );
	}

}

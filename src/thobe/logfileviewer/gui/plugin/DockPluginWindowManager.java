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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JComponent;
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
	private Logger							log;

	private CControl						dockableControl;

	/**
	 * {@link Map} of {@link JComponent}s that are registered for a given {@link IPluginUI}.
	 */
	private Map<IPluginUI, Set<JComponent>>	pluginComponentMap;

	public DockPluginWindowManager( JFrame parentFrame )
	{
		this.dockableControl = new CControl( parentFrame );
		this.pluginComponentMap = new HashMap<IPluginUI, Set<JComponent>>( );
		this.log = Logger.getLogger( "thobe.logfileviewer.gui.plugin.DockPluginWindowManager" );
	}

	public JPanel getMainPanel( )
	{
		return this.dockableControl.getContentArea( );
	}

	public void addPlugin( final IPluginUI plugin )
	{
		LOG( ).info( "Adding plugin '" + plugin + "' to docking-frame." );
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
				LOG( ).info( "Plugin '" + plugin + "' is now visible." );
			}
		} );
	}

	@Override
	public void registerComponent( IPluginUI pluginUI, JComponent component )
	{
		synchronized ( this.pluginComponentMap )
		{

			Set<JComponent> registeredComponents = this.pluginComponentMap.get( pluginUI );

			// create a new entry if no components are registered for the given plugin 
			if ( registeredComponents == null )
			{
				registeredComponents = new HashSet<>( );
				this.pluginComponentMap.put( pluginUI, registeredComponents );
			}

			// only if not already registered
			if ( registeredComponents.add( component ) )
			{
				final String frameId = pluginUI.getPluginName( ) + "." + registeredComponents.size( );

				LOG( ).info( "New component for plugin '" + pluginUI.getPluginName( ) + "' registered using (id=" + frameId + ")" );
				final DefaultSingleCDockable dockable = new DefaultSingleCDockable( frameId, component );
				this.dockableControl.addDockable( dockable );

				// show the dockable
				SwingUtilities.invokeLater( new Runnable( )
				{
					@Override
					public void run( )
					{
						dockable.setVisible( true );
						LOG( ).info( "New component '" + frameId + "' is now visible" );
					}
				} );

			}// if ( registeredComponents.add( component ) ).
		}// synchronized ( this.pluginComponentMap ).
	}

	@Override
	public void unRegisterComponent( IPluginUI pluginUI, JComponent component )
	{
		// TODO Auto-generated method stub

	}

	protected Logger LOG( )
	{
		return this.log;
	}

}

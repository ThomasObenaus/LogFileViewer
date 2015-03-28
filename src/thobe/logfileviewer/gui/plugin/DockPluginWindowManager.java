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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import thobe.logfileviewer.plugin.api.IPluginUI;
import thobe.logfileviewer.plugin.api.IPluginUIComponent;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;

/**
 * Implementation of the {@link IPluginWindowManager} using the window library docking-frames.
 * @author Thomas Obenaus
 * @source DockPluginWindowManager.java
 * @date Jul 7, 2014
 */
public class DockPluginWindowManager implements IPluginWindowManager, CVetoClosingListener
{
	private Logger															log;

	private CControl														dockableControl;

	/**
	 * {@link Map} used for keeping track which {@link CDockable}s are registered for a certain {@link IPluginUIComponent}.
	 */
	private Map<IPluginUI, Map<IPluginUIComponent, DefaultSingleCDockable>>	pluginComponentMap;

	public DockPluginWindowManager( JFrame parentFrame )
	{
		this.dockableControl = new CControl( parentFrame );
		this.pluginComponentMap = new HashMap<IPluginUI, Map<IPluginUIComponent, DefaultSingleCDockable>>( );
		this.log = Logger.getLogger( "thobe.logfileviewer.gui.plugin.DockPluginWindowManager" );
	}

	public JPanel getMainPanel( )
	{
		return this.dockableControl.getContentArea( );
	}

	@Override
	public void registerVisualComponent( IPluginUI pluginUI, IPluginUIComponent pComponent )
	{
		if ( pComponent == null )
		{
			LOG( ).severe( "Ignore plugins GUI '" + pluginUI.getPluginName( ) + "' since its IPluginUIComponent is NULL" );
		}// if ( pComponent == null )
		else
		{
			synchronized ( this.pluginComponentMap )
			{

				Map<IPluginUIComponent, DefaultSingleCDockable> registeredComponents = this.pluginComponentMap.get( pluginUI );

				// create a new entry if no components are registered for the given plugin 
				if ( registeredComponents == null )
				{
					registeredComponents = new HashMap<>( );
					this.pluginComponentMap.put( pluginUI, registeredComponents );
				}

				// only if not already registered
				if ( !registeredComponents.containsKey( pComponent ) )
				{
					final String frameId = pluginUI.getPluginName( ) + "." + registeredComponents.size( );

					LOG( ).info( "New component for plugin '" + pluginUI.getPluginName( ) + "' registered using (id=" + frameId + ")" );

					final DefaultSingleCDockable dockable = new DefaultSingleCDockable( frameId, pComponent.getVisualComponent( ) );

					// store the connection between the dockable and the plugin-component
					registeredComponents.put( pComponent, dockable );

					// register for visibility, change-events
					dockable.addVetoClosingListener( this );
					dockable.setCloseable( pComponent.isCloseable( ) );

					dockable.setTitleText( pComponent.getTitle( ) );
					dockable.setTitleToolTip( pComponent.getTooltip( ) );

					// now add the dockable to the main controller
					this.dockableControl.addDockable( dockable );

					if ( !pluginUI.isAttachedToGUI( ) )
						pluginUI.setAttachedToGUI( true );

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
		}// if ( pComponent == null ) ... else ...
	}

	@Override
	public void unRegisterVisualComponent( IPluginUI pluginUI, IPluginUIComponent pComponent )
	{
		int remainingVisualComponents = 0;
		synchronized ( this.pluginComponentMap )
		{
			// find the component to remove
			Map<IPluginUIComponent, DefaultSingleCDockable> component2DockableMap = this.pluginComponentMap.get( pluginUI );
			if ( component2DockableMap != null )
			{
				// find the dockable to remove
				DefaultSingleCDockable dockable = component2DockableMap.remove( pComponent );
				if ( dockable != null )
				{
					if ( this.dockableControl.getSingleDockable( dockable.getUniqueId( ) ) != null )
						this.dockableControl.removeDockable( dockable );
					remainingVisualComponents = component2DockableMap.size( );
					LOG( ).info( "Unregistered component '" + dockable.getUniqueId( ) + "' of plugin '" + pluginUI.getPluginName( ) + "'. " + remainingVisualComponents + " remaining." );
				}
				else LOG( ).warning( "No need to unregister component for Plugin '" + pluginUI.getPluginName( ) + "' since no such component is registered." );
			}// if ( component2DockableMap != null ).
			else LOG( ).warning( "No need to unregister component for Plugin '" + pluginUI.getPluginName( ) + "' since no such plugin is registered." );
		}// synchronized ( this.pluginComponentMap ).

		if ( remainingVisualComponents == 0 && pluginUI.isAttachedToGUI( ) )
		{
			pluginUI.setAttachedToGUI( false );
			LOG( ).info( "Plugin '" + pluginUI.getPluginName( ) + "' is now fully detached from gui." );
		}

	}

	protected Logger LOG( )
	{
		return this.log;
	}

	/**
	 * Returns the {@link IPluginUIComponent} that is visible through/ placed on the given {@link CDockable}.
	 * @param dockable
	 * @return - The {@link IPluginUIComponent} or null.
	 */
	private PluginUIComponentDockableEntry getPluginUIComponentPair( CDockable dockable )
	{
		PluginUIComponentDockableEntry result = null;
		synchronized ( this.pluginComponentMap )
		{
			for ( Entry<IPluginUI, Map<IPluginUIComponent, DefaultSingleCDockable>> entry : this.pluginComponentMap.entrySet( ) )
			{
				IPluginUI pluginUI = entry.getKey( );
				Map<IPluginUIComponent, DefaultSingleCDockable> component2DockableMap = entry.getValue( );
				for ( Entry<IPluginUIComponent, DefaultSingleCDockable> comp2DockableMapEntry : component2DockableMap.entrySet( ) )
				{
					if ( comp2DockableMapEntry.getValue( ).equals( dockable ) )
					{
						result = new PluginUIComponentDockableEntry( pluginUI, comp2DockableMapEntry.getKey( ), comp2DockableMapEntry.getValue( ) );
						break;
					}
				}// for ( Entry<IPluginUIComponent, DefaultSingleCDockable> comp2DockableMapEntry : component2DockableMap.entrySet( ) )
			}// for ( Entry<IPluginUI, Map<IPluginUIComponent, DefaultSingleCDockable>> entry : this.pluginComponentMap.entrySet( ) )
		}// synchronized ( this.pluginComponentMap ).
		return result;
	}

	@Override
	public void closing( CVetoClosingEvent event )
	{
		for ( int i = 0; i < event.getDockableCount( ); ++i )
		{
			PluginUIComponentDockableEntry pluginUIComp = getPluginUIComponentPair( event.getDockable( i ) );
			LOG( ).info( "Closing " + pluginUIComp );
			pluginUIComp.getComponent( ).onClosing( );

		}
	}

	@Override
	public void closed( CVetoClosingEvent event )
	{
		for ( int i = 0; i < event.getDockableCount( ); ++i )
		{
			PluginUIComponentDockableEntry pluginUIComp = getPluginUIComponentPair( event.getDockable( i ) );
			this.unRegisterVisualComponent( pluginUIComp.getPluginUI( ), pluginUIComp.getComponent( ) );
			LOG( ).info( "Closed " + pluginUIComp );
			pluginUIComp.getComponent( ).onClosed( );
		}
	}

	private final class PluginUIComponentDockableEntry
	{
		private IPluginUIComponent		component;
		private IPluginUI				pluginUI;
		private DefaultSingleCDockable	dockable;

		public PluginUIComponentDockableEntry( IPluginUI pluginUI, IPluginUIComponent component, DefaultSingleCDockable dockable )
		{
			this.pluginUI = pluginUI;
			this.component = component;
			this.dockable = dockable;
		}

		public IPluginUIComponent getComponent( )
		{
			return component;
		}

		public IPluginUI getPluginUI( )
		{
			return pluginUI;
		}

		@Override
		public String toString( )
		{
			return "plugin='" + this.pluginUI.getPluginName( ) + "', dockable='" + dockable.getUniqueId( ) + "', component='" + component.getTitle( ) + "'";
		}
	}

}

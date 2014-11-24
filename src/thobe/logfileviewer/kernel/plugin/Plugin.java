/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.plugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.swing.JComponent;

/**
 * An abstract implementation of a {@link IPlugin} with a UI {@link IPluginUI}
 * @author Thomas Obenaus
 * @source Plugin.java
 * @date May 29, 2014
 */
public abstract class Plugin extends Thread implements IPluginUI, IPlugin
{
	/**
	 * true if a stop was requested --> in this case the {@link Plugin} has to leave its run-method
	 */
	private AtomicBoolean				quitRequested;

	/**
	 * Internal instance of the logger
	 */
	private Logger						log;

	/**
	 * Name of the {@link Plugin}
	 */
	private String						pluginName;

	/**
	 * True if the {@link Plugin}s visual component is already attached to gui/mainframe
	 */
	private AtomicBoolean				attachedToGUI;

	/**
	 * Access to the window-management for {@link Plugin}s
	 */
	private IPluginWindowManagerAccess	pluginWindowMngAccess;

	public Plugin( String pluginName, String logChannelName )
	{
		super( logChannelName );
		this.pluginName = pluginName;
		this.log = Logger.getLogger( logChannelName );
		this.quitRequested = new AtomicBoolean( false );
		this.attachedToGUI = new AtomicBoolean( false );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	public boolean isQuitRequested( )
	{
		return quitRequested.get( );
	}

	@Override
	public boolean isVisible( )
	{
		JComponent vComp = this.getUIComponent( ).getVisualComponent( );
		if ( vComp == null )
		{
			LOG( ).severe( "Plugin '" + this.getPluginName( ) + "' is invalid since its visual-component is null" );
			return false;
		}
		return vComp.isVisible( );
	}

	@Override
	public void setVisible( boolean visible )
	{
		JComponent vComp = this.getUIComponent( ).getVisualComponent( );
		if ( vComp == null )
		{
			LOG( ).severe( "Plugin '" + this.getPluginName( ) + "' is invalid since its visual-component is null" );
		}
		vComp.setVisible( visible );
	}

	@Override
	public void setAttachedToGUI( boolean attached )
	{
		this.attachedToGUI.set( attached );
	}

	@Override
	public boolean isAttachedToGUI( )
	{
		return this.attachedToGUI.get( );
	}

	/**
	 * Quits the {@link Plugin} by setting the quitRequested variable. Each {@link Plugin} has to guarantee to leave its run-method as
	 * soon as possible. The state of this variable can be requested via {@link Plugin#isQuitRequested()}
	 */
	public void quit( )
	{
		this.quitRequested.set( true );
	}

	public String getPluginName( )
	{
		return this.pluginName;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null )
			return false;
		if ( !( obj instanceof Plugin ) )
			return false;

		Plugin other = ( Plugin ) obj;
		if ( ( this.getPluginName( ) == null && other.getPluginName( ) != null ) || ( this.getPluginName( ) != null && other.getPluginName( ) == null ) )
			return false;
		if ( !this.getPluginName( ).equals( other.getPluginName( ) ) )
			return false;

		if ( ( this.getPluginDescription( ) == null && other.getPluginDescription( ) != null ) || ( this.getPluginDescription( ) != null && other.getPluginDescription( ) == null ) )
			return false;
		if ( !this.getPluginDescription( ).equals( other.getPluginDescription( ) ) )
			return false;
		return true;
	}

	@Override
	public int hashCode( )
	{
		int hCPluginName = ( this.getPluginName( ) == null ) ? 0 : this.getPluginName( ).hashCode( );
		int hCPluginDesc = ( this.getPluginDescription( ) == null ) ? 0 : this.getPluginDescription( ).hashCode( );
		return hCPluginName * hCPluginDesc;
	}

	@Override
	public String toString( )
	{
		return this.getPluginName( ) + " {" + this.getPluginDescription( ) + "}";
	}

	@Override
	public void setPluginWindowManagerAccess( IPluginWindowManagerAccess pWMA )
	{
		this.pluginWindowMngAccess = pWMA;
	}

	@Override
	public IPluginWindowManagerAccess getPluginWindowManagerAccess( )
	{
		return this.pluginWindowMngAccess;
	}
}

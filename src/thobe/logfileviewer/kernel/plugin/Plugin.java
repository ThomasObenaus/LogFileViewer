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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.swing.JComponent;

import thobe.logfileviewer.kernel.source.logline.ILogLine;
import thobe.logfileviewer.kernel.source.logstream.ILogStream;
import thobe.logfileviewer.kernel.source.logstream.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.logstream.LogStream;

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

	/**
	 * Access to the {@link LogStream}.
	 */
	private ILogStreamAccess			logStreamAccess;

	/**
	 * The version of the plugin-api used for this plugin.
	 */
	private static PluginApiVersion		apiVersion	= new PluginApiVersion( );

	public Plugin( String pluginName, String logChannelName )
	{
		super( logChannelName );
		this.pluginName = pluginName;
		this.log = Logger.getLogger( logChannelName );
		this.quitRequested = new AtomicBoolean( false );
		this.attachedToGUI = new AtomicBoolean( false );
		this.logStreamAccess = null;
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
	public final void setPluginWindowManagerAccess( IPluginWindowManagerAccess pWMA )
	{
		this.pluginWindowMngAccess = pWMA;
	}

	@Override
	public IPluginWindowManagerAccess getPluginWindowManagerAccess( )
	{
		return this.pluginWindowMngAccess;
	}

	@Override
	public ILogStream getLogstream( )
	{
		ILogStream result = null;
		synchronized ( this )
		{
			result = this.logStreamAccess;
		}
		return result;
	}

	@Override
	public final void onLogStreamAvailable( ILogStreamAccess logStreamAccess )
	{
		synchronized ( this )
		{
			this.logStreamAccess = logStreamAccess;
			if ( this.logStreamAccess == null )
			{
				LOG( ).severe( "Nullpointer as LogStream obtained, plugins will gain no data!" );
			}
			else
			{
				this.logStreamAccess.addLogStreamDataListener( this );
			}
		}// synchronized ( logStreamAccess )
	}

	@Override
	public final void onLogStreamLeavingScope( )
	{
		synchronized ( this )
		{
			if ( this.logStreamAccess != null )
			{
				this.logStreamAccess.removeLogStreamDataListener( this );
			}
			this.logStreamAccess = null;
		}// synchronized ( logStreamAccess )
	}

	@Override
	public String getLSRequesterName( )
	{
		return this.getPluginName( );
	}

	@Override
	public void response( int requestId, List<ILogLine> logLines, boolean valid )
	{
		throw new IllegalAccessError( "IlogStreamRequester.response() is not implemented. If you want to send a request to the LogStream you have to implement this method in your class." );
	}

	public String getVersion( )
	{
		return this.getMajorVersion( ) + "." + this.getMinorVersion( ) + "." + this.getBugfixVersion( );
	}

	public PluginApiVersion getPluginApiVersion( )
	{
		return apiVersion;
	}
}

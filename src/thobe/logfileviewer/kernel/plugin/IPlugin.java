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

import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;

/**
 * @author Thomas Obenaus
 * @source IPlugin.java
 * @date May 29, 2014
 */
public abstract class IPlugin extends Thread
{
	/**
	 * true if a stop was requested --> in this case the {@link IPlugin} has to leave its run-method
	 */
	private AtomicBoolean	quitRequested;

	private Logger			log;

	public IPlugin( String logChannelName )
	{
		this.log = Logger.getLogger( logChannelName );
		this.quitRequested = new AtomicBoolean( false );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	public boolean isQuitRequested( )
	{
		return quitRequested.get( );
	}

	/**
	 * Quits the {@link IPlugin} by setting the quitRequested variable. Each {@link IPlugin} has to guarantee to leave its run-method as
	 * soon as possible. The state of this variable can be requested via {@link IPlugin#isQuitRequested()}
	 */
	public void quit( )
	{
		this.quitRequested.set( true );
	}

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 1 <u>START</u> #####</b><br>
	 * All {@link IPlugin}s are started (run-method entered for the first time) but not registered.
	 * @return
	 */
	public abstract boolean onStarted( );

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 2 <u>REGISTER</u> #####</b><br>
	 * All {@link IPlugin}s are registered and running.
	 * @param pluginAccess - object to access other {@link IPlugin}s that are registered/ available.
	 * @return
	 */
	public abstract boolean onRegistered( IPluginAccess pluginAccess );

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 2a <u>OPEN LS</u> #####</b><br>
	 * A new {@link LogStream} was opened
	 * @param logStreamAccess - the object that can be used to access the {@link LogStream} by registering a {@link LogStreamDataListener}
	 *            via {@link ILogStreamAccess#addLogStreamDataListener(LogStreamDataListener)}.
	 */
	public abstract void onLogStreamOpened( ILogStreamAccess logStreamAccess );

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 2b <u>PREPARE CLOSE LS</u> #####</b><br>
	 * The currently open {@link LogStream} will be closed --> eac {@link IPlugin} has to unregister from the {@link LogStream}
	 * @param logStreamAccess - the object that can be used unregister from the {@link LogStream} via
	 *            {@link ILogStreamAccess#removeLogStreamStateListener(LogStreamStateListener)}
	 */
	public abstract void onPrepareCloseLogStream( ILogStreamAccess logStreamAccess );

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 2c <u>CLOSE LS</u> #####</b><br>
	 * The {@link LogStream} was closed.
	 */
	public abstract void onLogStreamClosed( );

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 3 <u>UNREGISTER</u> #####</b><br>
	 * All {@link IPlugin}s are unregistered (not available any more) but still running.
	 */
	public abstract void onUnRegistered( );

	/**
	 * Life-cycle of {@link IPlugin}: <b>##### Step 4 <u>STOP</u> #####</b><br>
	 * The {@link IPlugin} is stopped --> it has to guarantee to leave its run-method as quick as possible:
	 * <ol>
	 * <li>Use {@link IPlugin#isQuitRequested()} to check if the run-method should be left. Or</li>
	 * <li>Set your own internal flag if {@link IPlugin#onStopped} is called and check this flag in the {@link IPlugin}s run-method.</li>
	 * </ol>
	 * @return
	 */
	public abstract boolean onStopped( );

	public abstract String getPluginName( );

	public abstract String getPluginDescription( );

	public abstract JComponent getVisualComponent( );

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null )
			return false;
		if ( !( obj instanceof IPlugin ) )
			return false;

		IPlugin other = ( IPlugin ) obj;
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

}

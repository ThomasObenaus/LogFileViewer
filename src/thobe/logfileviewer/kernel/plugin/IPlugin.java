/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin;

import thobe.logfileviewer.kernel.source.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;
import thobe.logfileviewer.kernel.source.listeners.LogStreamStateListener;

/**
 * @author Thomas Obenaus
 * @source IPlugin.java
 * @date Jun 2, 2014
 */
public interface IPlugin
{
	/**
	 * Returns true if previously the method {@link IPlugin#quit()} was called.
	 * @return
	 */
	public boolean isQuitRequested( );

	/**
	 * Quits the {@link Plugin} by setting the quitRequested variable. Each {@link Plugin} has to guarantee to leave its run-method as
	 * soon as possible. The state of this variable can be requested via {@link Plugin#isQuitRequested()}
	 */
	public void quit( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 1 <u>START</u> #####</b><br>
	 * All {@link Plugin}s are started (run-method entered for the first time) but not registered.
	 * @return
	 */
	public boolean onStarted( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 2 <u>REGISTER</u> #####</b><br>
	 * All {@link Plugin}s are registered and running.
	 * @param pluginAccess - object to access other {@link Plugin}s that are registered/ available.
	 * @return
	 */
	public boolean onRegistered( IPluginAccess pluginAccess );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 2a <u>OPEN LS</u> #####</b><br>
	 * A new {@link LogStream} was opened
	 * @param logStreamAccess - the object that can be used to access the {@link LogStream} by registering a {@link LogStreamDataListener}
	 *            via {@link ILogStreamAccess#addLogStreamDataListener(LogStreamDataListener)}.
	 */
	public void onLogStreamOpened( ILogStreamAccess logStreamAccess );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 2b <u>PREPARE CLOSE LS</u> #####</b><br>
	 * The currently open {@link LogStream} will be closed --> eac {@link Plugin} has to unregister from the {@link LogStream}
	 * @param logStreamAccess - the object that can be used unregister from the {@link LogStream} via
	 *            {@link ILogStreamAccess#removeLogStreamStateListener(LogStreamStateListener)}
	 */
	public void onPrepareCloseLogStream( ILogStreamAccess logStreamAccess );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 2c <u>CLOSE LS</u> #####</b><br>
	 * The {@link LogStream} was closed.
	 */
	public void onLogStreamClosed( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 3 <u>UNREGISTER</u> #####</b><br>
	 * All {@link Plugin}s are unregistered (not available any more) but still running.
	 */
	public void onUnRegistered( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 4 <u>STOP</u> #####</b><br>
	 * The {@link Plugin} is stopped --> it has to guarantee to leave its run-method as quick as possible:
	 * <ol>
	 * <li>Use {@link Plugin#isQuitRequested()} to check if the run-method should be left. Or</li>
	 * <li>Set your own internal flag if {@link Plugin#onStopped} is called and check this flag in the {@link Plugin}s run-method.</li>
	 * </ol>
	 * @return
	 */
	public boolean onStopped( );

	/**
	 * This method should return the name of this {@link IPlugin}.
	 * @return
	 */
	public String getPluginName( );

	/**
	 * This method should return a short description of this {@link IPlugin}
	 * @return
	 */
	public String getPluginDescription( );
}

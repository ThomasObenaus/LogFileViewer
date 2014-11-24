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

import thobe.logfileviewer.kernel.memory.IMemoryWatchable;
import thobe.logfileviewer.kernel.source.logstream.ILogStream;
import thobe.logfileviewer.kernel.source.logstream.ILogStreamAccess;
import thobe.logfileviewer.kernel.source.logstream.ILogStreamDataListener;
import thobe.logfileviewer.kernel.source.logstream.ILogStreamRequester;
import thobe.logfileviewer.kernel.source.logstream.LogStream;

/**
 * @author Thomas Obenaus
 * @source IPlugin.java
 * @date Jun 2, 2014
 */
public interface IPlugin extends IPluginBase, IMemoryWatchable, ILogStreamDataListener,ILogStreamRequester
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
	 * HIDDEN-Method: This method has not to be implemented by a specific {@link IPlugin}. The corresponding method for the {@link IPlugin}s
	 * to be implemented is {@link IPlugin#onLogStreamOpened()}.
	 * Life-cycle of {@link Plugin}: <b>##### Step 2a <u>LS AVAILABLE</u> #####</b><br>
	 * A new {@link LogStream} is available plugins will gain access to this stream over {@link ILogStreamAccess} and will be registered as
	 * {@link ILogStreamDataListener}.
	 * @param logStreamAccess - the object that can be used to access the {@link LogStream} by registering a {@link ILogStreamDataListener}
	 *            via {@link ILogStreamAccess#addLogStreamDataListener(ILogStreamDataListener)}.
	 */
	public void onLogStreamAvailable( ILogStreamAccess logStreamAccess );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 2b <u>OPEN LS</u> #####</b><br>
	 * A new {@link LogStream} was opened. Now access to the {@link LogStream} over {@link IPlugin#getLogstreamAccess()} is granted/save.
	 */
	public void onLogStreamOpened( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 3a <u>PREPARE CLOSE LS</u> #####</b><br>
	 * The currently open {@link LogStream} will be closed. The access to the {@link LogStream} over {@link IPlugin#getLogstreamAccess()} is
	 * not longer granted and might return a null-pointer.
	 */
	public void onPrepareCloseLogStream( );

	/**
	 * HIDDEN-Method: This method has not to be implemented by a specific {@link IPlugin}. The corresponding method for the {@link IPlugin}s
	 * to be implemented is {@link IPlugin#onPrepareCloseLogStream()}.
	 * Life-cycle of {@link Plugin}: <b>##### Step 3b <u>LS LEAVING SCOPE</u> #####</b><br>
	 * The currently open {@link LogStream} will be closed. The access to the {@link LogStream} over {@link IPlugin#getLogstreamAccess()} is
	 * not longer granted and might return a null-pointer.
	 */
	public void onLogStreamLeavingScope( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 3c <u>CLOSE LS</u> #####</b><br>
	 * The {@link LogStream} was closed.
	 */
	public void onLogStreamClosed( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 4 <u>UNREGISTER</u> #####</b><br>
	 * All {@link Plugin}s are unregistered (not available any more) but still running.
	 */
	public void onUnRegistered( );

	/**
	 * Life-cycle of {@link Plugin}: <b>##### Step 5 <u>STOP</u> #####</b><br>
	 * The {@link Plugin} is stopped --> it has to guarantee to leave its run-method as quick as possible:
	 * <ol>
	 * <li>Use {@link Plugin#isQuitRequested()} to check if the run-method should be left. Or</li>
	 * <li>Set your own internal flag if {@link Plugin#onStopped} is called and check this flag in the {@link Plugin}s run-method.</li>
	 * </ol>
	 * @return
	 */
	public boolean onStopped( );

	/**
	 * Returns a reference to the currently open {@link LogStream}. Might return a null-pointer.
	 * @return
	 */
	public ILogStream getLogstream( );
}

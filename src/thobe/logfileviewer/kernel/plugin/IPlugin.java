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

import java.util.logging.Logger;

/**
 * @author Thomas Obenaus
 * @source IPlugin.java
 * @date May 29, 2014
 */
public abstract class IPlugin extends Thread
{
	private Logger	log;

	public IPlugin( String logChannelName )
	{
		this.log = Logger.getLogger( logChannelName );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	/**
	 * Registration of the plugin.
	 * @return
	 */
	public abstract boolean onRegister( );

	/**
	 * Start of the plugin
	 * @return
	 */
	public abstract boolean onStart( );

	/**
	 * A new datasource was opened
	 */
	public abstract void onDataSourceOpened( );

	/**
	 * The currently open datasource will be closed
	 */
	public abstract void onPrepareCloseDataSource( );

	/**
	 * The datasource was closed.
	 */
	public abstract void onDataSourceClosed( );

	/**
	 * The plugin is stopped
	 * @return
	 */
	public abstract boolean onStopped( );

	/**
	 * The plugin is unregistered.
	 */
	public abstract void onUnRegistered( );

	public abstract String getPrepareFilter( );
}

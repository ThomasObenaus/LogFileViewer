/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.plugin.console;

import thobe.logfileviewer.kernel.plugin.IPlugin;

/**
 * @author Thomas Obenaus
 * @source Console.java
 * @date May 29, 2014
 */
public class Console extends IPlugin
{

	public Console( )
	{
		super( "thobe.ethsource.plugin.Console" );
	}

	@Override
	public boolean onRegister( )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onStart( )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDataSourceOpened( )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepareCloseDataSource( )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataSourceClosed( )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onStopped( )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onUnRegistered( )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPrepareFilter( )
	{
		return "iMX6.Nav";
	}


}

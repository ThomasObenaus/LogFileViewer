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

import javax.swing.JComponent;
import javax.swing.JLabel;

import thobe.logfileviewer.kernel.plugin.IPlugin;
import thobe.logfileviewer.kernel.plugin.IPluginAccess;

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
	public JComponent getVisualComponent( )
	{
		return new JLabel( "sldslksldk" );
	}

	@Override
	public boolean onRegistered( IPluginAccess pluginAccess )
	{
		System.out.println( "Console.onRegistered()" );
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onStarted( )
	{
		System.out.println( "Console.onStarted()" );
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDataSourceOpened( )
	{
		System.out.println( "Console.onDataSourceOpened()" );
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrepareCloseDataSource( )
	{
		System.out.println( "Console.onPrepareCloseDataSource()" );
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataSourceClosed( )
	{
		System.out.println( "Console.onDataSourceClosed()" );
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onStopped( )
	{
		System.out.println( "Console.onStopped()" );
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onUnRegistered( )
	{
		System.out.println( "Console.onUnRegistered()" );
		// TODO Auto-generated method stub

	}

	@Override
	public String getPrepareFilter( )
	{
		return "iMX6.Nav";
	}

	@Override
	public String getPluginName( )
	{
		return "Console";
	}

	@Override
	public String getPluginDescription( )
	{
		return "A simple console displaying the whole logfile";
	}

	@Override
	public void run( )
	{
		while ( !this.isQuitRequested( ) )
		{

			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}

		}
	}
}

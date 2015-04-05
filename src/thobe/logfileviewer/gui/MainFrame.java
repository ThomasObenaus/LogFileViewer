/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import thobe.logfileviewer.LogFileViewerInfo;
import thobe.logfileviewer.gui.actions.Act_About;
import thobe.logfileviewer.gui.actions.Act_Close;
import thobe.logfileviewer.gui.actions.Act_Exit;
import thobe.logfileviewer.gui.actions.Act_OpenConnection;
import thobe.logfileviewer.gui.actions.Act_OpenFile;
import thobe.logfileviewer.gui.actions.Act_PluginManager;
import thobe.logfileviewer.gui.plugin.DockPluginWindowManager;
import thobe.logfileviewer.gui.plugin.IPluginWindowManager;
import thobe.logfileviewer.kernel.ILogFileViewerAppListener;
import thobe.logfileviewer.kernel.LogFileViewerApp;
import thobe.logfileviewer.plugin.api.IPluginAccess;
import thobe.logfileviewer.plugin.api.IPluginUI;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamStateListener;
import thobe.widgets.action.ActionRegistry;
import thobe.widgets.statusbar.StatusBar;
import thobe.widgets.statusbar.StatusBarMessageType;

/**
 * @author Thomas Obenaus
 * @source MainFrame.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class MainFrame extends JFrame implements ILogFileViewerAppListener, ILogStreamStateListener
{
	private Logger					log;
	private LogFileViewerApp		app;

	private IPluginWindowManager	pluginWindowManager;

	public MainFrame( LogFileViewerApp app )
	{
		this.setTitle( LogFileViewerInfo.getAppName( ) + " [" + LogFileViewerInfo.getVersion( ) + "]" );
		this.log = Logger.getLogger( "thobe.logfileviewer.gui.MainFrame" );

		this.app = app;
		this.app.addListener( this );

		this.app.getLogStream( ).addLogStreamStateListener( this );

		this.registerActions( );

		this.buildMenu( );
		this.buildGUI( );
		this.setSize( 1200, 700 );
		this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		this.addWindowListener( new WindowAdapter( )
		{
			@Override
			public void windowClosing( WindowEvent e )
			{
				MainFrame.this.exit( );
			}
		} );
	}

	private void buildMenu( )
	{
		JMenuBar mbar = new JMenuBar( );
		this.setJMenuBar( mbar );

		JMenu mu_file = new JMenu( "File" );
		mbar.add( mu_file );

		JMenuItem mi_openFile = new JMenuItem( ActionRegistry.get( ).getAction( Act_OpenFile.KEY ) );
		mu_file.add( mi_openFile );
		JMenuItem mi_openConn = new JMenuItem( ActionRegistry.get( ).getAction( Act_OpenConnection.KEY ) );
		mu_file.add( mi_openConn );
		JMenuItem mi_close = new JMenuItem( ActionRegistry.get( ).getAction( Act_Close.KEY ) );
		mu_file.add( mi_close );

		mu_file.add( new JSeparator( JSeparator.HORIZONTAL ) );
		JMenuItem mi_exit = new JMenuItem( ActionRegistry.get( ).getAction( Act_Exit.KEY ) );
		mu_file.add( mi_exit );

		JMenu mu_extra = new JMenu( "Extra" );
		mbar.add( mu_extra );

		JMenuItem mi_pluginManager = new JMenuItem( ActionRegistry.get( ).getAction( Act_PluginManager.KEY ) );
		mu_extra.add( mi_pluginManager );

		JMenu mu_help = new JMenu( "Help" );
		mbar.add( mu_help );

		JMenuItem mi_about = new JMenuItem( ActionRegistry.get( ).getAction( Act_About.KEY ) );
		mu_help.add( mi_about );

	}

	private void registerActions( )
	{
		ActionRegistry.get( ).registerAction( new Act_Exit( this ) );
		ActionRegistry.get( ).registerAction( new Act_OpenFile( this ) );
		ActionRegistry.get( ).registerAction( new Act_OpenConnection( this ) );
		ActionRegistry.get( ).registerAction( new Act_Close( this ) );
		ActionRegistry.get( ).registerAction( new Act_PluginManager( this, app.getPluginManager( ) ) );
		ActionRegistry.get( ).registerAction( new Act_About( this ) );
		ActionRegistry.get( ).getAction( Act_Close.KEY ).setEnabled( false );
	}

	private void buildGUI( )
	{
		this.setLayout( new BorderLayout( ) );

		// currently we use the docking-frames library
		this.pluginWindowManager = new DockPluginWindowManager( this );

		// add the main-panel
		this.add( this.pluginWindowManager.getMainPanel( ), BorderLayout.CENTER );

		try
		{
			// add the statusbar
			StatusBar.createStatusBar( this );
			this.add( StatusBar.get( ), BorderLayout.SOUTH );
			StatusBar.get( ).setMessage( "Not connected", StatusBarMessageType.WARNING );
		}
		catch ( Exception e )
		{
			e.printStackTrace( );
		}
	}

	public void exit( )
	{
		this.app.quit( );
		try
		{
			this.app.join( );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace( );
		}

		this.log.info( "Exit the application" );
		System.exit( 0 );
	}

	public LogFileViewerApp getApp( )
	{
		return app;
	}

	@Override
	public void newPluginsAvailable( IPluginAccess pluginAccess )
	{
		LOG( ).info( "New plugins available" );

		// rebuilds the Gui (e.g. adds new plugins)
		this.rebuildGUI( pluginAccess );
	}

	private void rebuildGUI( IPluginAccess pluginAccess )
	{
		boolean repaintNeeded = false;

		for ( IPluginUI plugin : pluginAccess.getPluginsNotAttachedToGui( ) )
		{
			plugin.setPluginWindowManagerAccess( this.pluginWindowManager );
			this.pluginWindowManager.registerVisualComponent( plugin, plugin.getUIComponent( ) );

			LOG( ).info( "IPlugin '" + plugin + "' found and added." );
			repaintNeeded = true;
		}// for ( IPluginUI plugins : pluginAccess.getPluginsNotAttachedToGui( ) )

		if ( repaintNeeded )
		{
			this.revalidate( );
			this.repaint( );
		}// if ( repaintNeeded )
	}

	private Logger LOG( )
	{
		return this.log;
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return "MainFrame";
	}

	@Override
	public void onEOFReached( )
	{
		StatusBar.get( ).setMessage( "Not connected", StatusBarMessageType.WARNING );
	}

	@Override
	public void onOpened( )
	{
		StatusBar.get( ).setMessage( "Connected", StatusBarMessageType.INFO );
	}

	@Override
	public void onClosed( )
	{
		StatusBar.get( ).setMessage( "Not connected", StatusBarMessageType.WARNING );
	}
}

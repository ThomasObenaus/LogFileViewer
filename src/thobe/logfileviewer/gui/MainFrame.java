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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import thobe.logfileviewer.LogFileViewerInfo;
import thobe.logfileviewer.gui.actions.Act_Close;
import thobe.logfileviewer.gui.actions.Act_Exit;
import thobe.logfileviewer.gui.actions.Act_OpenConnection;
import thobe.logfileviewer.gui.actions.Act_OpenFile;
import thobe.logfileviewer.kernel.LogFileViewerApp;
import thobe.logfileviewer.kernel.LogFileViewerAppListener;
import thobe.logfileviewer.kernel.plugin.IPluginAccess;
import thobe.logfileviewer.kernel.plugin.console.Console;
import thobe.widgets.action.ActionRegistry;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * @author Thomas Obenaus
 * @source MainFrame.java
 * @date May 15, 2014
 */
@SuppressWarnings ( "serial")
public class MainFrame extends JFrame implements LogFileViewerAppListener
{
	private Logger				log;
	private LogFileViewerApp	app;

	private CControl			dockableControl;

	private Console				console;

	public MainFrame( LogFileViewerApp app )
	{
		this.setTitle( LogFileViewerInfo.getAppName( ) + " [" + LogFileViewerInfo.getVersion( ) + "]" );
		this.log = Logger.getLogger( "thobe.logfileviewer.gui.MainFrame" );

		this.app = app;
		this.app.addListener( this );

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
	}

	private void registerActions( )
	{
		ActionRegistry.get( ).registerAction( new Act_Exit( this ) );
		ActionRegistry.get( ).registerAction( new Act_OpenFile( this ) );
		ActionRegistry.get( ).registerAction( new Act_OpenConnection( this ) );
		ActionRegistry.get( ).registerAction( new Act_Close( this ) );
		ActionRegistry.get( ).getAction( Act_Close.KEY ).setEnabled( false );

	}

	private void buildGUI( )
	{
		this.setLayout( new BorderLayout( ) );

		dockableControl = new CControl( this );
		this.add( this.dockableControl.getContentArea( ),BorderLayout.CENTER );
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
		boolean rebuildNeeded = false;

		// we dont have the console --> look for it
		if ( this.console == null )
		{
			this.console = pluginAccess.getConsole( );
			if ( this.console != null )
			{
				LOG( ).info( "IPlugin '" + this.console + "' found and added." );
				rebuildNeeded = true;
			}// if ( this.console != null ) .
		}// if ( this.console == null ).

		// rebuild GUI if needed (if a new plugin is available)
		if ( rebuildNeeded )
			this.rebuildGUI( );
	}

	private void rebuildGUI( )
	{
		// add console in case we have one
		if ( this.console != null && !this.console.isAttachedToGUI( ) )
		{
			final DefaultSingleCDockable consoleDockable = new DefaultSingleCDockable( "Console",this.console.getVisualComponent( ));
			this.dockableControl.addDockable( consoleDockable );

			this.console.setAttachedToGUI( );
			this.console.setVisible( true );
			
			
			final DefaultSingleCDockable consoleDockable2 = new DefaultSingleCDockable( "b",new JButton("jdhfjdh"));
			this.dockableControl.addDockable( consoleDockable2 );
			SwingUtilities.invokeLater( new Runnable( )
			{
				@Override
				public void run( )
				{
					consoleDockable.setVisible( true );
					consoleDockable2.setVisible( true );					
				}
			} );
			
		}// if ( this.console != null && !this.console.isAttachedToGUI( ) ) .

		this.revalidate( );
		this.repaint( );
	}

	private Logger LOG( )
	{
		return this.log;
	}
}

/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui.actions;

import java.awt.event.ActionEvent;

import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.gui.dialogs.Dlg_PluginManager;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.widgets.action.AbstrAction;

/**
 * @author Thomas Obenaus
 * @source Act_PluginManager.java
 * @date Mar 24, 2015
 */
@SuppressWarnings ( "serial")
public class Act_PluginManager extends AbstrAction
{
	public static final String	KEY	= "PLUGIN_MANAGER";
	private MainFrame			mainFrame;
	private PluginManager		manager;

	public Act_PluginManager( MainFrame mainFrame, PluginManager manager )
	{
		super( "Pluginmanager" );
		this.manager = manager;
		this.mainFrame = mainFrame;
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		Dlg_PluginManager dlg = new Dlg_PluginManager( this.mainFrame, this.manager );
		dlg.setVisible( true );
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}

}

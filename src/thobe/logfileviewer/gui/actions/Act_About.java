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

import thobe.logfileviewer.LogFileViewerInfo;
import thobe.logfileviewer.gui.MainFrame;
import thobe.logfileviewer.gui.dialogs.Dlg_About;
import thobe.logfileviewer.plugin.api.PluginApiVersion;
import thobe.widgets.action.AbstrAction;

/**
 * @author Thomas Obenaus
 * @source Act_About.java
 * @date Apr 4, 2015
 */
@SuppressWarnings ( "serial")
public class Act_About extends AbstrAction
{
	public static final String	KEY	= "ABOUT";
	private MainFrame			mainframe;

	public Act_About( MainFrame mainframe )
	{
		super( "About " + LogFileViewerInfo.getAppName( ) );
		this.mainframe = mainframe;
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		Dlg_About dlg = new Dlg_About( this.mainframe, LogFileViewerInfo.getAppName( ) );

		dlg.setAuthor( LogFileViewerInfo.getAuthor( ) );
		dlg.setAuthorEmailAddress( LogFileViewerInfo.getAuthorEMailAddress( ) );
		dlg.setWebsite( LogFileViewerInfo.getWebsite( ) );
		dlg.setVersion( LogFileViewerInfo.getVersion( ) );
		dlg.setLicense( LogFileViewerInfo.getLicense( ) );
		dlg.setDescription( LogFileViewerInfo.getDescription( ) );
		dlg.setPluginApiVersion( ( new PluginApiVersion( ) ).toString( ) );
		dlg.buildBody( );
		dlg.setVisible( true );
	}

	@Override
	public String getActionKey( )
	{
		return KEY;
	}
}

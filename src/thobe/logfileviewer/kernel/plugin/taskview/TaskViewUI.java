/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.taskview;

import javax.swing.JComponent;
import javax.swing.JPanel;

import thobe.logfileviewer.kernel.plugin.IPluginUIComponent;

/**
 * @author Thomas Obenaus
 * @source TaskViewUI.java
 * @date 01.10.2014
 */
@SuppressWarnings ( "serial")
public class TaskViewUI extends JPanel implements IPluginUIComponent
{

	@Override
	public JComponent getVisualComponent( )
	{
		return this;
	}

	@Override
	public void onClosing( )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClosed( )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle( )
	{
		return "TaskView";
	}

	@Override
	public String getTooltip( )
	{
		return "Shows the lifecycle of tasks/threads";
	}

	@Override
	public boolean isCloseable( )
	{
		return false;
	}

}



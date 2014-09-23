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

import javax.swing.JComponent;

/**
 * @author Thomas Obenaus
 * @source IPluginComponent.java
 * @date 13.08.2014
 */
public interface IPluginComponent
{
	/**
	 * This method should return the {@link JComponent} that should be visible/ displayed in the gui.
	 * @return
	 */
	public JComponent getVisualComponent( );

	/**
	 * Called whenever this component was closed.
	 */
	public void onClosed( );
}

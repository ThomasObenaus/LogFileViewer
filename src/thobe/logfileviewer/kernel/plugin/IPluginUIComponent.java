/*
 *  Copyright (C) 2014, j.umbel. All rights reserved.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    j.umbel
 */

package thobe.logfileviewer.kernel.plugin;

import javax.swing.JComponent;

/**
 * @author Thomas Obenaus
 * @source IPluginComponent.java
 * @date 13.08.2014
 */
public interface IPluginUIComponent
{
	/**
	 * This method should return the {@link JComponent} that should be visible/ displayed in the gui.
	 * @return
	 */
	public JComponent getVisualComponent( );

	/**
	 * Called whenever this component is about to be closed.
	 */
	public void onClosing( );

	/**
	 * Called whenever this component was closed.
	 */
	public void onClosed( );

	public String getTitle( );

	/**
	 * Should return true if the visual representation of this {@link IPluginUIComponent} should get a close-button, false otherwise.
	 * @return
	 */
	public boolean isCloseable( );
}

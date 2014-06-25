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
 * @source IPluginUI.java
 * @date Jun 2, 2014
 */
public interface IPluginUI
{
	/**
	 * If the {@link IPluginUI} was attached to a gui-component/ container this method should be called to store the state of attachment.
	 */
	public void setAttachedToGUI( );

	/**
	 * Returns true if the {@link IPluginUI} is attached to a gui-component/ container, false otherwise.
	 * @return
	 */
	public boolean isAttachedToGUI( );

	/**
	 * Set the visibility of this {@link IPluginUI}.
	 * @param visible
	 */
	public void setVisible( boolean visible );

	/**
	 * Returns true if the visual-component of this {@link IPluginUI} is visible, false otherwise.
	 * @return
	 */
	public boolean isVisible( );

	/**
	 * Returns the visual-component of this {@link IPluginUI}, which can be attached to a gui-component/ container. On attaching it please
	 * call {@link IPluginUI#setAttachedToGUI()}.
	 * @return
	 */
	public JComponent getVisualComponent( );
}
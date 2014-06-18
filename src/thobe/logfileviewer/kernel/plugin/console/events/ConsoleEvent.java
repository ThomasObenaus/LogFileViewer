/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin.console.events;

/**
 * @author Thomas Obenaus
 * @source ConsoleEvent.java
 * @date Jun 18, 2014
 */
public abstract class ConsoleEvent
{
	private ConsoleEventType	type;

	public ConsoleEvent( ConsoleEventType type )
	{
		this.type = type;
	}

	public ConsoleEventType getType( )
	{
		return type;
	}

	@Override
	public String toString( )
	{
		return this.getType( ) + "";
	}
}

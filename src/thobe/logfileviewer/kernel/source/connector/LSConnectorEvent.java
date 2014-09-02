/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.connector;

/**
 * @author Thomas Obenaus
 * @source LSConnectorEvent.java
 * @date Aug 16, 2014
 */
public abstract class LSConnectorEvent
{
	private LSConnectorEventType	type;
	private long					delay;

	public LSConnectorEvent( LSConnectorEventType type, long delay )
	{
		this.type = type;
		this.delay = delay;
	}

	public LSConnectorEventType getType( )
	{
		return type;
	}

	public long getDelay( )
	{
		return delay;
	}

	public void setDelay( long delay )
	{
		this.delay = delay;
	}
}

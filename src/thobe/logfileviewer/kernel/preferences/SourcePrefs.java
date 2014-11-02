/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.preferences;

import java.io.File;
import java.util.prefs.Preferences;

import thobe.logfileviewer.kernel.source.SourceType;

/**
 * @author Thomas Obenaus
 * @source SourcePrefs.java
 * @date Oct 31, 2014
 */
public class SourcePrefs implements ISubPrefs
{
	private static final String	NODE		= "LSSource";
	private static final String	PRP_TYPE	= "type";
	private static final String	PRP_HOST	= "host";
	private static final String	PRP_FILE	= "file";
	private static final String	PRP_PORT	= "port";

	private int					port;
	private String				host;
	private File				file;
	private SourceType			type;

	public SourcePrefs( )
	{
		this.port = 0;
		this.type = SourceType.UNDEF;
	}

	public void setIPSource( String host, int port )
	{
		this.host = host;
		this.port = port;
		this.file = null;
		this.type = SourceType.IP;
	}

	public void setFileSource( File file )
	{
		this.host = null;
		this.port = -1;
		this.file = file;
		this.type = SourceType.FILE;
	}

	public void setFileSource( String filename )
	{
		this.setFileSource( new File( filename ) );
	}

	public File getFile( )
	{
		return file;
	}

	public String getHost( )
	{
		return host;
	}

	public int getPort( )
	{
		return port;
	}

	public SourceType getType( )
	{
		return type;
	}

	@Override
	public void load( Preferences applicationRoot )
	{
		Preferences root = applicationRoot.node( NODE );

		this.type = SourceType.valueOf( root.get( PRP_TYPE, "" + SourceType.UNDEF ) );

		if ( this.type == SourceType.IP )
		{
			this.host = root.get( PRP_HOST, "127.0.0.1" );
			this.port = root.getInt( PRP_PORT, 0 );
		}
		else if ( this.type == SourceType.FILE )
		{
			this.file = new File( root.get( PRP_FILE, "" ) );
		}
	}

	@Override
	public void save( Preferences applicationRoot )
	{
		Preferences root = applicationRoot.node( NODE );
		if ( this.type == SourceType.IP )
		{
			root.put( PRP_HOST, this.host );
			root.put( PRP_PORT, this.port + "" );
		}
		else if ( this.type == SourceType.FILE )
		{
			root.put( PRP_FILE, this.file.getAbsolutePath( ) );
		}

		root.put( PRP_TYPE, this.type + "" );
	}

}

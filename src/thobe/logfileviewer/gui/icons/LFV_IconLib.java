/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui.icons;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import thobe.widgets.icons.IIconType;
import thobe.widgets.icons.IconContainer;
import thobe.widgets.icons.IconLib;

/**
 * @author Thomas Obenaus
 * @source LFV_IconLib.java
 * @date Apr 6, 2015
 */
public class LFV_IconLib extends IconLib
{
	private final static String	L_NAME		= "thobe.logfileviewer.gui.icons";
	private static LFV_IconLib	instance	= null;

	@Override
	protected Map<IIconType, IconContainer> getIconContainers( )
	{
		Map<IIconType, IconContainer> icons = new HashMap<>( );

		icons.put( LFV_IconType.EXPAND, new IconContainer( LOG( ), LFV_IconLib.class.getResource( "/thobe/logfileviewer/gui/icons/"), "expand", "png" ) );
		icons.put( LFV_IconType.COLLAPSE, new IconContainer( LOG( ), LFV_IconLib.class.getResource("/thobe/logfileviewer/gui/icons/"), "collapse", "png" ) );

		return icons;
	}

	public static LFV_IconLib get( )
	{
		if ( instance == null )
		{
			instance = new LFV_IconLib( );
		}
		return instance;
	}

	private LFV_IconLib( )
	{
		super( Logger.getLogger( L_NAME ) );
	}

}

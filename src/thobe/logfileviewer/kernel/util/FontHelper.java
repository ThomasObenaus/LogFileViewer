/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import thobe.logfileviewer.kernel.plugin.console.Console;

/**
 * @author Thomas Obenaus
 * @source FontHelper.java
 * @date 31.07.2014
 */
public class FontHelper
{
	private static final Font	CONSOLE_FONT		= new Font( "DejaVu Sans", Font.PLAIN, 12 );
	private static final Font	CONSOLE_FONT_FB_1	= new Font( "Arial", Font.PLAIN, 12 );

	/**
	 * Returns the {@link Font} that should be used for the {@link Console}.
	 * @return
	 */
	public static Font getConsoleFont( )
	{
		if ( hasFont( CONSOLE_FONT.getFamily( ) ) )
			return CONSOLE_FONT;
		if ( hasFont( CONSOLE_FONT_FB_1.getFamily( ) ) )
			return CONSOLE_FONT_FB_1;
		return getDefaultFont( );
	}

	/**
	 * Returns true if the given font-family (described by a string) is available in the system.
	 * @param font
	 * @return
	 */
	public static boolean hasFont( String font )
	{
		if ( font == null )
			return false;
		if ( font.trim( ).isEmpty( ) )
			return false;

		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment( );
		for ( String availableFont : g.getAvailableFontFamilyNames( ) )
		{
			if ( availableFont.equals( font ) )
			{
				return true;
			}// if ( availableFont.equals( font ) ).
		}// for ( String availableFont : g.getAvailableFontFamilyNames( ) )
		return false;
	}

	/**
	 * Returns the systems default font
	 * @return
	 */
	public static Font getDefaultFont( )
	{
		return Font.decode( null );
	}
}

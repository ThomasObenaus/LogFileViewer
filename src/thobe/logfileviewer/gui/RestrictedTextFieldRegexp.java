/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import thobe.widgets.textfield.ConvertException;
import thobe.widgets.textfield.InvalidValueExeption;
import thobe.widgets.textfield.RestrictedTextFieldString;

/**
 * @author Thomas Obenaus
 * @source RestrictedTextFieldRegexp.java
 * @date Jun 28, 2014
 */
@SuppressWarnings ( "serial")
public class RestrictedTextFieldRegexp extends RestrictedTextFieldString
{
	private Pattern	pattern;

	public RestrictedTextFieldRegexp( int columns )
	{
		super( columns, false );
		this.pattern = null;
	}

	public Pattern getPattern( )
	{
		return pattern;
	}

	@Override
	protected String convertStringToType( String stringToConvert ) throws ConvertException, InvalidValueExeption
	{

		try
		{
			this.pattern = Pattern.compile( stringToConvert );
		}
		catch ( PatternSyntaxException e )
		{
			throw new InvalidValueExeption( stringToConvert, "This is not a valid regexp: " + e.getLocalizedMessage( ) );
		}
		return stringToConvert;
	}

	@Override
	protected String convertTypeToString( String typeToConvert )
	{
		return typeToConvert;
	}
}

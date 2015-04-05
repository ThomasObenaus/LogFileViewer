/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLEditorKit;

import thobe.widgets.editor.Editor;
import thobe.widgets.utils.Utilities;

/**
 * @author Thomas Obenaus
 * @source Dlg_About.java
 * @date Apr 4, 2015
 */
@SuppressWarnings ( "serial")
public class Dlg_About extends Editor
{
	private static final Dimension	minSize	= new Dimension( 650, 350 );

	private String					author;
	private String					website;
	private String					license;
	private String					authorEmailAddress;
	private String					version;
	private String					description;
	private String					pluginApiVersion;

	private JEditorPane				tpa_main;

	public Dlg_About( Window owner, String appName )
	{
		super( owner, "About " + appName, ModalityType.APPLICATION_MODAL );
		this.buildGUI( );
		this.setResizable( false );
	}

	public void setAuthor( String author )
	{
		this.author = author;
	}

	public void setPluginApiVersion( String pluginApiVersion )
	{
		this.pluginApiVersion = pluginApiVersion;
	}

	public void setAuthorEmailAddress( String authorEmailAddress )
	{
		this.authorEmailAddress = authorEmailAddress;
	}

	public void setWebsite( String website )
	{
		this.website = website;
	}

	public void setLicense( String license )
	{
		this.license = license;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public void setVersion( String version )
	{
		this.version = version;
	}

	private void buildGUI( )
	{
		this.setLayout( new BorderLayout( ) );
		this.tpa_main = new JEditorPane( );
		this.tpa_main.setEditable( false );

		HTMLEditorKit kit = new HTMLEditorKit( );
		this.tpa_main.setEditorKit( kit );
		this.add( this.tpa_main, BorderLayout.CENTER );

		this.buildBody( );
	}

	public void buildBody( )
	{
		final Font descrFont = new Font( this.getFont( ).getFontName( ), Font.PLAIN, 12 );
		final FontMetrics descrFontMetrics = this.getFontMetrics( descrFont );

		final FontMetrics fontMetrics = this.getFontMetrics( this.getFont( ) );

		final int maxWidth = 650;

		final String SPACE = "&nbsp ";
		final String colorDesc = "#222222";
		final int txtSizeDesc = 5;

		final String colorInfo = "#444444";
		final int txtSizeInfo = 4;

		final String pluginApiVersion = ( this.pluginApiVersion != null ) ? this.pluginApiVersion : "N/A";
		final String author = ( this.author != null ) ? this.author : "N/A";
		final String email = ( this.authorEmailAddress != null ) ? this.authorEmailAddress : "N/A";
		final String website = ( this.website != null ) ? this.website : "N/A";
		final String license = ( this.license != null ) ? this.license : "N/A";
		final String description = ( this.description != null ) ? this.description : "N/A";
		final String version = ( this.version != null ) ? this.version : "N/A";

		StringBuffer buf = new StringBuffer( );
		buf.append( "<html><body>" );

		buf.append( "<p>" );
		buf.append( "<i><font color=\"" + colorDesc + "\" size=\"" + txtSizeDesc + "\">" + Utilities.resizeStringToMaxWidthHTML( descrFontMetrics, description, maxWidth, true ) + "</font>" );
		buf.append( "</p></i><br>" );

		buf.append( "<p>" );
		buf.append( "<font color=\"" + colorInfo + "\" size=\"" + txtSizeInfo + "\">" );
		buf.append( "<b>Version:</b> " + version + SPACE + SPACE );
		buf.append( "<b>API Version:</b> " + pluginApiVersion + "<br>" );
		buf.append( "<b>Author:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, author, maxWidth, true ) + SPACE + SPACE );
		buf.append( "<b>E-Mail:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, email, maxWidth, true ) + "<br>" );
		buf.append( "<b>Website:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, website, maxWidth, true ) + "<br><br>" );
		buf.append( "<b>License:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, license, maxWidth, true ) + "<br>" );
		buf.append( "</font>" );
		buf.append( "</p>" );

		buf.append( "</body></html>" );

		this.tpa_main.setText( buf.toString( ) );
	}

	@Override
	public Dimension getMinimumEditorSize( )
	{
		return minSize;
	}

	@Override
	protected JPanel createButtonPanel( )
	{
		JPanel pa_buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

		JButton bu_close = new JButton( "Close" );
		pa_buttons.add( bu_close );
		bu_close.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				close_dlg( );
			}
		} );
		return pa_buttons;
	}

}

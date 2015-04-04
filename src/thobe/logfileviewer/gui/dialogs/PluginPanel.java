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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

import thobe.logfileviewer.kernel.preferences.PluginManagerPrefs;
import thobe.logfileviewer.plugin.Plugin;
import thobe.widgets.utils.Utilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Thomas Obenaus
 * @source PluginPanel.java
 * @date Mar 24, 2015
 */
@SuppressWarnings ( "serial")
public class PluginPanel extends JPanel
{
	private static final int	MB_DIVIDER	= 1024 * 1024;
	private static final int	KB_DIVIDER	= 1024;
	private static final float	UNIT_EPS	= 0.01f;

	private Plugin				plugin;

	private JToggleButton		tb_enablePlugin;
	private JToggleButton		tb_expand;
	private JButton				bu_settings;

	private JPanel				pa_body;
	private JLabel				l_body;

	private PluginManagerPrefs	pluginManagerPreferences;

	private JProgressBar		pb_memory;

	public PluginPanel( Plugin plugin, PluginManagerPrefs pluginManagerPreferences )
	{
		this.plugin = plugin;
		this.pluginManagerPreferences = pluginManagerPreferences;

		this.buildGUI( );
	}

	private void buildGUI( )
	{
		final String pluginName = this.plugin.getPluginName( );

		this.setLayout( new BorderLayout( 0, 0 ) );
		this.setBorder( BorderFactory.createLineBorder( Color.DARK_GRAY ) );

		// header
		FormLayout fla_header = new FormLayout( "1dlu,25dlu,3dlu,fill:default:grow,3dlu,right:100dlu,3dlu,45dlu,3dlu,20dlu,1dlu", "1dlu,default,1dlu" );
		CellConstraints cc_header = new CellConstraints( );
		JPanel pa_header = new JPanel( fla_header );
		this.add( pa_header, BorderLayout.NORTH );
		pa_header.setBackground( Color.LIGHT_GRAY );

		this.tb_expand = new JToggleButton( "+", false );
		pa_header.add( this.tb_expand, cc_header.xy( 2, 2 ) );
		this.tb_expand.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				expand( tb_expand.isSelected( ) );
			}
		} );

		JLabel l_name = new JLabel( this.plugin.getPluginName( ) );
		pa_header.add( l_name, cc_header.xy( 4, 2 ) );
		l_name.setOpaque( false );

		this.pb_memory = new JProgressBar( 0, 100 );
		pa_header.add( this.pb_memory, cc_header.xy( 6, 2 ) );
		this.pb_memory.setValue( 0 );
		this.pb_memory.setStringPainted( true );
		this.pb_memory.setToolTipText( "Memory-consumption of this plugin" );

		this.tb_enablePlugin = new JToggleButton( "Disable", true );
		pa_header.add( this.tb_enablePlugin, cc_header.xy( 8, 2 ) );
		this.tb_enablePlugin.setToolTipText( "Disable plugin '" + pluginName + "'" );
		this.tb_enablePlugin.setSelected( this.plugin.isEnabled( ) );
		this.tb_enablePlugin.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final boolean bIsEnabled = tb_enablePlugin.isSelected( );
				setEnabled( bIsEnabled );
			}
		} );

		this.bu_settings = new JButton( "." );
		pa_header.add( this.bu_settings, cc_header.xy( 10, 2 ) );

		// body		
		FormLayout fla_body = new FormLayout( "2dlu,fill:default:grow,2dlu", "2dlu,default,2dlu" );
		CellConstraints cc_body = new CellConstraints( );
		this.pa_body = new JPanel( fla_body );
		this.add( pa_body, BorderLayout.CENTER );
		this.l_body = new JLabel( );
		this.l_body.setHorizontalAlignment( JLabel.LEADING );
		this.pa_body.add( this.l_body, cc_body.xy( 2, 2 ) );

		buildBody( );
		this.setEnabled( this.plugin.isEnabled( ) );
		this.expand( false );
	}

	private void expand( boolean expand )
	{
		this.pa_body.setVisible( expand );
		this.tb_expand.setText( expand ? "-" : "+" );
		this.revalidate( );
		this.repaint( );
	}

	private void buildBody( )
	{
		final FontMetrics fontMetrics = this.getFontMetrics( this.getFont( ) );
		final int maxWidth = 700;

		final String SPACE = "&nbsp ";
		final String colorDesc = "#222222";
		final int txtSizeDesc = 4;

		final String colorInfo = "#444444";
		final int txtSizeInfo = 3;

		final String pluginApiVersion = ( this.plugin.getPluginApiVersion( ) != null ) ? this.plugin.getPluginApiVersion( ).toString( ) : "N/A";
		final String pluginVersion = ( this.plugin.getVersion( ) != null ) ? this.plugin.getVersion( ) : "N/A";
		final String author = "Thomas Obenaus";
		final String email = "Thomas Obenaus";
		final String website = "https://github.com/ThomasObenaus/LogFileViewer";
		final String license = "Copyright (C) 2014, Thomas Obenaus. All rights reserved. Licensed under the New BSD License (3-clause lic) See attached license-file.";
		final String pluginDescription = ( this.plugin.getPluginDescription( ) != null ) ? this.plugin.getPluginDescription( ) : "No description available";

		StringBuffer buf = new StringBuffer( );
		buf.append( "<html><body>" );

		buf.append( "<p>" );
		buf.append( "<i><font color=\"" + colorDesc + "\" size=\"" + txtSizeDesc + "\">" + Utilities.resizeStringToMaxWidthHTML( fontMetrics, pluginDescription, maxWidth, true ) + "</font>" );
		buf.append( "</p></i><br>" );

		buf.append( "<p>" );
		buf.append( "<font color=\"" + colorInfo + "\" size=\"" + txtSizeInfo + "\">" );
		buf.append( "<b>Version:</b> " + pluginVersion + SPACE + SPACE );
		buf.append( "<b>API Version:</b> " + pluginApiVersion + "<br>" );
		buf.append( "<b>Author:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, author, maxWidth, true ) + SPACE + SPACE );
		buf.append( "<b>E-Mail:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, email, maxWidth, true ) + "<br>" );
		buf.append( "<b>Website:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, website, maxWidth, true ) + "<br>" );
		buf.append( "<b>License:</b> " + Utilities.resizeStringToMaxWidthHTML( fontMetrics, license, maxWidth, true ) + "<br>" );
		buf.append( "</font>" );
		buf.append( "</p>" );

		buf.append( "</body></html>" );

		this.l_body.setText( buf.toString( ) );

	}

	@Override
	public void setEnabled( boolean enabled )
	{
		final String pluginName = this.plugin.getPluginName( );
		tb_enablePlugin.setText( ( enabled ? "Disable" : "Enable" ) );
		this.tb_enablePlugin.setToolTipText( ( enabled ? "Disable" : "Enable" ) + " plugin '" + pluginName + "'" );
		this.pluginManagerPreferences.setPluginEnabled( this.plugin.getPluginName( ), enabled );
	}

	public void updateMemoryConsumption( long completeMemory )
	{
		long pluginMemory = this.plugin.getMemory( );

		if ( pluginMemory > completeMemory )
		{
			pluginMemory = completeMemory;
		}

		int perc = Math.round( ( pluginMemory / ( float ) completeMemory ) * 100.0f );
		this.pb_memory.setValue( perc );

		String unitPlugin = "MB";
		float pMemInMB = pluginMemory / ( float ) MB_DIVIDER;
		if ( pMemInMB < UNIT_EPS )
		{
			unitPlugin = "KB";
			pMemInMB = pluginMemory / ( float ) KB_DIVIDER;
		}

		String unitComplete = "MB";
		float cMemInMB = completeMemory / ( float ) MB_DIVIDER;
		if ( cMemInMB < UNIT_EPS )
		{
			unitComplete = "KB";
			cMemInMB = completeMemory / ( float ) KB_DIVIDER;
		}

		String txt = String.format( "%.2f %s / %.2f %s", pMemInMB, unitPlugin, cMemInMB, unitComplete );
		this.pb_memory.setString( txt );
	}

}

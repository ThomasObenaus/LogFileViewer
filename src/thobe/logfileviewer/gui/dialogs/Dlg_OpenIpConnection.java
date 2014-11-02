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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import thobe.logfileviewer.kernel.preferences.SourcePrefs;
import thobe.logfileviewer.kernel.source.connector.LogStreamConnector;
import thobe.widgets.editor.Editor;
import thobe.widgets.messagePanel.Message;
import thobe.widgets.messagePanel.MessageCategory;
import thobe.widgets.textfield.RestrictedTextFieldAdapter;
import thobe.widgets.textfield.RestrictedTextFieldInteger;
import thobe.widgets.textfield.RestrictedTextFieldString;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Thomas Obenaus
 * @source Dlg_OpenIpConnection.java
 * @date Aug 16, 2014
 */
@SuppressWarnings ( "serial")
public class Dlg_OpenIpConnection extends Editor
{
	private static final Dimension		minSize	= new Dimension( 400, 350 );

	private JButton						bu_cancel;
	private JButton						bu_connect;

	private LogStreamConnector			connector;

	private RestrictedTextFieldString	tf_host;

	private RestrictedTextFieldInteger	tf_port;

	private SourcePrefs					sourcePrefs;

	public Dlg_OpenIpConnection( Window owner, SourcePrefs sourcePrefs, LogStreamConnector connector )
	{
		super( owner, "Open Connection", ModalityType.APPLICATION_MODAL );
		this.sourcePrefs = sourcePrefs;
		this.connector = connector;
		this.buildGUI( );
	}

	private void buildGUI( )
	{
		FormLayout fla_main = new FormLayout( "3dlu,50dlu,3dlu,fill:default:grow,3dlu", "3dlu,pref,3dlu,pref,3dlu" );
		CellConstraints cc_main = new CellConstraints( );
		this.setLayout( fla_main );

		String tt = "The hostname/ip-address to connect to";
		JLabel l_host = new JLabel( "host/ip-address" );
		this.add( l_host, cc_main.xy( 2, 2 ) );
		l_host.setToolTipText( tt );

		this.tf_host = new RestrictedTextFieldString( true );
		this.tf_host.setValue( this.sourcePrefs.getHost( ) );
		this.add( this.tf_host, cc_main.xy( 4, 2 ) );
		this.tf_host.setToolTipText( tt );
		this.tf_host.addListener( new RestrictedTextFieldAdapter( )
		{
			@Override
			public void valueChanged( )
			{
				checkValid( );
			}

			@Override
			public void valueChangeCommitted( )
			{
				checkValid( );
			}
		} );

		tt = "The port used to connect over";
		JLabel l_port = new JLabel( "port" );
		this.add( l_port, cc_main.xy( 2, 4 ) );
		l_port.setToolTipText( tt );

		this.tf_port = new RestrictedTextFieldInteger( true );
		this.tf_port.setValue( this.sourcePrefs.getPort( ) );
		this.tf_port.addListener( new RestrictedTextFieldAdapter( )
		{
			@Override
			public void valueChanged( )
			{
				checkValid( );
			}

			@Override
			public void valueChangeCommitted( )
			{
				checkValid( );
			}
		} );
		this.add( this.tf_port, cc_main.xy( 4, 4 ) );
		this.tf_port.setToolTipText( tt );

		this.checkValid( );
	}

	private void checkValid( )
	{
		this.getMessagePanel( ).clearMessages( this.getClass( ) );
		String hostname = this.tf_host.getValue( );

		// check host
		boolean dataIsValid = true;
		if ( hostname == null || hostname.isEmpty( ) )
		{
			dataIsValid = false;
			this.getMessagePanel( ).registerMessage( this.getClass( ), new Message( 0, "Hostname empty", MessageCategory.ERROR ) );
		}

		// check port
		int port = this.tf_port.getValue( );
		if ( port < 2 )
		{
			dataIsValid = false;
			this.getMessagePanel( ).registerMessage( this.getClass( ), new Message( 1, "Port invalid", MessageCategory.ERROR ) );
		}

		this.bu_connect.setEnabled( dataIsValid );
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

		this.bu_connect = new JButton( "Connect" );
		pa_buttons.add( this.bu_connect );
		this.bu_connect.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				connect( );
				close_dlg( );
			}
		} );

		this.bu_cancel = new JButton( "Cancel" );
		pa_buttons.add( this.bu_cancel );
		this.bu_cancel.addActionListener( new ActionListener( )
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				close_dlg( );
			}
		} );

		this.bu_connect.setEnabled( false );
		return pa_buttons;
	}

	private void connect( )
	{
		// store connection settings
		this.sourcePrefs.setIPSource( this.tf_host.getValue( ), this.tf_port.getValue( ) );

		// try to connect
		this.connector.connectToIP( this.tf_host.getValue( ), this.tf_port.getValue( ) );
	}

}

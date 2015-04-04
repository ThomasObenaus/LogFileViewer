/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.playground.dockingframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;

/**
 * @author Thomas Obenaus
 * @source Example.java
 * @date Jul 2, 2014
 */
public class Example
{
	public static void main( String[] args )
	{
		JFrame frame = new JFrame( );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		final CControl control = new CControl( frame );

		frame.setLayout( new BorderLayout(10,10 ) );
		frame.add( control.getContentArea( ),BorderLayout.CENTER );

		JButton bu  = new JButton("add" );
		frame.add( bu,BorderLayout.SOUTH );
		bu.addActionListener( new ActionListener( )
		{
			
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				SingleCDockable red = create( "red2",Color.red );
				control.addDockable( red );
				red.setVisible( true );
				
			}
		} );
		
		SingleCDockable red = create( "red",Color.red );
		SingleCDockable blue = create( "blue",Color.blue );
		SingleCDockable green = create( "green",Color.green );
		
		control.addDockable( red );
		control.addDockable( blue );
		control.addDockable( green );
		
		
		red.setVisible( true );
		blue.setLocation( CLocation.base( ).normalEast( .8 ) );
		blue.setVisible( true );
		green.setLocation( CLocation.base( ).normalEast( .8 ) );
		green.setVisible( true );
		
		frame.setSize( 400, 400 );
		frame.setVisible( true );
	}

	public static SingleCDockable create( String title, Color bgColor )
	{
		JPanel bg = new JPanel( );
		bg.setOpaque( true );
		bg.setBackground( bgColor );
		return new DefaultSingleCDockable( title, bg );
	}
}

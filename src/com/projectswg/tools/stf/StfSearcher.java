/*******************************************************************************
 * Star Wars Galaxies Stf Searcher by Waverunner
 * Copyright (c) 2013 <Project SWG>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ******************************************************************************/
package com.projectswg.tools.stf;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import engine.clientdata.StfTable;
import engine.clientdata.StfTable.Pair;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.prefs.Preferences;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

public class StfSearcher {

	private static StfSearcher searcher;
	
	private JFrame frmPswgTools;
	private String searchDirectory = "";
	private JTextField tfDirectory;
	private JTextField tfValue;
	private JTextPane console;
	
	private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	
	public StfSearcher() {
		searcher = this;
		initialize();
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					StfSearcher window = new StfSearcher();
					window.frmPswgTools.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPswgTools = new JFrame();
		frmPswgTools.setResizable(false);
		frmPswgTools.setTitle("PSWG Tools - Stf Searcher");
		frmPswgTools.setBounds(100, 100, 510, 442);
		frmPswgTools.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPswgTools.getContentPane().setLayout(null);
		
		frmPswgTools.addWindowListener(new WindowListener() {
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) { prefs.put("searchDirectory", searchDirectory); }
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowOpened(WindowEvent e) {}
		});
		
		JPanel panelSettings = new JPanel();
		panelSettings.setBounds(10, 11, 474, 101);
		frmPswgTools.getContentPane().add(panelSettings);
		panelSettings.setLayout(null);
		
		JLabel lblSearchDirectory = new JLabel("Search Directory");
		lblSearchDirectory.setBounds(10, 11, 89, 14);
		panelSettings.add(lblSearchDirectory);
		
		tfDirectory = new JTextField();
		tfDirectory.setEditable(false);
		tfDirectory.setBounds(98, 8, 267, 20);
		panelSettings.add(tfDirectory);
		tfDirectory.setColumns(10);
		
		String searchDir = prefs.get("searchDirectory", "");

		tfDirectory.setText(searchDir);
		searchDirectory = searchDir;
		
		JButton btnChangeDir = new JButton("Change");
		btnChangeDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser directoryChooser = new JFileChooser();
				directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				if (searchDirectory != null && !searchDirectory.isEmpty())
					directoryChooser.setCurrentDirectory(Paths.get(searchDirectory).toFile());
				int returnValue = directoryChooser.showDialog(frmPswgTools, "Select");
				
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File file = directoryChooser.getSelectedFile();
					
					setSearchDirectory(file.getAbsolutePath());
					tfDirectory.setText(getSearchDirectory());
					printConsole("Search directory changed to " + file.getAbsolutePath());
				}
			}
		});
		btnChangeDir.setBounds(375, 7, 89, 23);
		panelSettings.add(btnChangeDir);
		
		JLabel lblSearchTerm = new JLabel("Find Value");
		lblSearchTerm.setBounds(10, 39, 89, 14);
		panelSettings.add(lblSearchTerm);
		
		tfValue = new JTextField();
		tfValue.setColumns(10);
		tfValue.setBounds(66, 36, 398, 20);
		panelSettings.add(tfValue);
		
		JPanel panelButtons = new JPanel();
		panelButtons.setBounds(10, 64, 454, 33);
		panelSettings.add(panelButtons);
		
		JCheckBox chckbxRecursive = new JCheckBox("Recursive");
		chckbxRecursive.setEnabled(false);
		panelButtons.add(chckbxRecursive);
		
		JButton btnSearch = new JButton("Search");
		panelButtons.add(btnSearch);
		
		JButton btnClearConsole = new JButton("Clear Console");
		btnClearConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearConsole();
			}
		});
		panelButtons.add(btnClearConsole);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				search(tfValue.getText(), false);
			}
		});
		
		JPanel panelOutput = new JPanel();
		panelOutput.setBounds(10, 123, 474, 270);
		frmPswgTools.getContentPane().add(panelOutput);
		panelOutput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panelOutput.add(scrollPane, BorderLayout.CENTER);
		
		console = new JTextPane();
		console.setEditable(false);
		console.setText("=== Star Wars Galaxies STF Searcher ===");
		scrollPane.setViewportView(console);

	}

	public void search(String term, boolean recursive) {
		printConsole("Looking for value: " + term);
		File directory = Paths.get(searchDirectory).toFile();

		if (directory.isFile()) {
			printConsole("Tried to load a file!");
			return;
		}
		
		boolean found = false;
		for (File f : directory.listFiles()) {
			if (found)
				break;
			
			if (f.isFile())
				found = searchFile(f, term);
		}
		
		if (!found)
			printConsole("No results were found.");
		else
			printConsole("Found a file with the search term.");
	}
	
	public boolean searchFile(File file, String term) {
		try {
			StfTable stfTable = new StfTable();
			stfTable.readFile(file.getAbsolutePath());
			
			for (int r = 0; r < stfTable.getRowCount(); r++) {
				Pair<String, String> pair = stfTable.getStringById(r);
				String value = pair.getValue();

				if (value != null && value.contains(term)) {
					printConsole("Found Value in " + file.getName());
					return true;
				}
				//printConsole(pair.getKey() + " " + pair.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void printConsole(String text) {
		console.setText(console.getText() + "\n" + (new Date().toString()) + ": " + text);
	}
	
	public void clearConsole() {
		console.setText("=== Star Wars Galaxies STF Searcher ===");
	}
	
	public String getSearchDirectory() {
		return searchDirectory;
	}

	public void setSearchDirectory(String coreLocation) {
		this.searchDirectory = coreLocation;
	}

	public static StfSearcher getInstance() {
		return searcher;
	}
	
	public Preferences getPrefs() {
		return prefs;
	}
}

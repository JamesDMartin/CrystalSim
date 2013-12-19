/*******************************************************************************
 * Copyright (c) 2013 Eric Dill -- eddill@ncsu.edu. North Carolina State University. All rights reserved.
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Eric Dill -- eddill@ncsu.edu - initial API and implementation
 * 	James D. Martin -- jdmartin@ncsu.edu - Principal Investigator
 ******************************************************************************/
package ui;

import geometry.JVector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import shapes.ShapeTypes;
import simulation.NucleationOrientation;
import simulation.SimulSetupParams;
import simulation.SimulSetupParams.FittableParametersOptions;
import simulation.SimulSetupParams.DimensionalityOptions;
import simulation.Termination;

public class UI extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2276152899197003090L;
	private JPanel pnlMain;
	private JTabbedPane pnlTab;
	private RunningSimulationPanel simPanel;
	private SimulSetupParams currentParams;
	
	public UI() {
		currentParams = new SimulSetupParams();
		setup();
	}
	
	
	
	/**
	 * UI CONSTRUCTION
	 */
	
	private JPanel pnlSettings;
	private JPanel pnlParams;
	private JPanel pnlRates;
	private JPanel pnlSampleUnitsSpherical, pnlSampleUnitsCubic, pnlSampleUnitsCylindrical;
	
	private JTextField txtRate1, txtRate2, txtRate3;
	private JLabel lblRate1, lblRate2, lblRate3;
	private JTextField txtUnits1, txtUnits2, txtUnits3;
	private JLabel lblUnits1, lblUnits2, lblUnits3;
	private JLabel lblApproxVolume;
	private JTextArea messages;
	private JScrollPane scroll;
	
	private JTextField txtFileRoot = new JTextField(10);
	private JTextField txtNumThreads = new JTextField(5);
	private JTextField txtDeltaT = new JTextField(5);
	private JTextField txtFixedN = new JTextField(5);
	private JTextField txtFixedK = new JTextField(5);
	
	private JButton btnRunSimulation;
	
	private Color badEntryColor = Color.YELLOW;
	private Color okEntryColor = txtFileRoot.getBackground();
	private Color uneditable = Color.LIGHT_GRAY;
	private NucleationOrientationSettingsFrame nucOrientSet;
	private JButton btnOrientationSettings;
	private void setup() {
		pnlMain = new JPanel();
		pnlTab = new JTabbedPane();
		pnlTab.addTab("Simulation Settings", setupSettings());
		pnlMain.add(pnlTab, BorderLayout.CENTER);
		pnlMain.add(setupMessageWindow(), BorderLayout.SOUTH);
		
		JScrollPane scroll = new JScrollPane(pnlMain);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		add(scroll);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		updateParamValues();
		setSize(1500, 1000);
		pack();
		setVisible(true);
	}
	private JLabel getSpacer(Dimension d) {
		JLabel spacer = new JLabel();
		spacer.setMaximumSize(d);
		spacer.setSize(spacer.getMaximumSize());
		return spacer;
	}
	private JComponent setupMessageWindow() {
		Box box = Box.createHorizontalBox();
		messages = new JTextArea(15, 50);
		messages.setMargin(new Insets(0, 5, 0, 5));
		scroll = new JScrollPane(messages);
		scroll.setVisible(true);
		scroll.setSize(messages.getSize());
		box.add(scroll);
		box.add(Box.createHorizontalGlue());
		return box;
	}
	private JPanel setupSettings() {
		pnlSettings = new JPanel();
		pnlSettings.setLayout(new BorderLayout());
		pnlSettings.add(setupParameters(), BorderLayout.EAST);
		JPanel pnlInput = new JPanel();
		pnlInput.setLayout(new BoxLayout(pnlInput, BoxLayout.Y_AXIS));
		/*JLabel lbl = new JLabel();
		lbl.setText("Press Enter after modifying a value.");
		Font f = lbl.getFont();
		lbl.setFont(new Font(f.getName(), Font.BOLD, 16));
		lbl.setForeground(Color.blue);
		pnlInput.add(lbl);*/
		pnlInput.add(setupNucleation());
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		pnl.add(setupSample());
		pnl.add(setupSimulationEndConditions());
		pnl.add(setupDimensionalityOptions());
		pnlInput.add(pnl);
		Box boxH = Box.createHorizontalBox();
		boxH.add(setupMiscOptions());
		boxH.add(setupFittingParameters());
		pnlInput.add(boxH);
		pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		pnl.add(setupFileIO());
		pnl.add(setupSimulationExecutionPanel());
		pnlInput.add(pnl);
		pnlSettings.add(pnlInput, BorderLayout.CENTER);
		return pnlSettings;
	}
	private JComponent setupSimulationExecutionPanel() {
		Box box = Box.createVerticalBox();
		
		box.setBorder(BorderFactory.createTitledBorder("Simulation Execution"));
		
		btnRunSimulation = new JButton("Run Simulation");
		Font f = btnRunSimulation.getFont();
		btnRunSimulation.setFont(new Font(f.getName(), Font.BOLD, 24));
		btnRunSimulation.setMaximumSize(btnRunSimulation.getPreferredSize());
		btnRunSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					launch();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		Box box1 = Box.createHorizontalBox();
		box1.add(btnRunSimulation);
		box1.add(Box.createHorizontalGlue());
		box.add(box1);
		box.add(Box.createVerticalStrut(10));
		simPanel = new RunningSimulationPanel();
		simPanel.addObserver(this);
		box.add(simPanel.getBox());
		return box;
	}
	private JPanel setupSimulationEndConditions() {
		JPanel pnl = new JPanel();
		JPanel[] pnls = {new JPanel(), new JPanel(), new JPanel() };
		
		pnl.setBorder(BorderFactory.createTitledBorder("Simulation End Conditions"));
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnls[0].setLayout(new GridLayout(1, 2));
		pnls[1].setLayout(new GridLayout(1, 2));
		final String[] labels = {"Fraction Transformed", "Time Elapsed", "After"};
		final JTextField[] fields = {new JTextField(5), new JTextField(5), new JTextField(5) };
		fields[0].setMaximumSize(fields[0].getPreferredSize());
		fields[0].setText("1.0");
		fields[1].setMaximumSize(fields[1].getPreferredSize());
		fields[2].setMaximumSize(fields[2].getPreferredSize());
		
		fields[0].addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE, 0, 1));
		fields[1].addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 1, -1));
		fields[2].addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 1, -1));
		
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox chk = (JCheckBox) arg0.getSource();
				String s = chk.getText();
				int idx = 0;
				for(int i = 0; i < labels.length; i++) {
					fields[i].setEnabled(false);
					fields[i].setBackground(uneditable);
					if(labels[i].compareTo(s) == 0) {
						idx = i;
						fields[i].setEnabled(true);
						fields[i].setBackground(okEntryColor);
					}
				}
				currentParams.setTerm(Termination.values()[idx]);
				message("Termination type: " + currentParams.getTerm()+ " has been updated.");
			}
		};
		ButtonGroup group = new ButtonGroup();
		JCheckBox chk = null;
		for(int i = 0; i < labels.length; i++) {
			chk = new JCheckBox(labels[i]);
			chk.addActionListener(listener);
			if(i == 0) { chk.setSelected(true); }
			group.add(chk);
			pnls[i].add(chk);
			if(i == 0) {
				listener.actionPerformed(new ActionEvent(chk, 0, ""));
			}
			if(i == 2) {
				pnls[i].add(getSpacer(new Dimension(15, 5)));
			}
			pnls[i].add(fields[i]);
		}
		fields[0].addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE, 0, 1));
		fields[0].addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.setTermVal(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Termination value: " + currentParams.getTermVal() + " has been updated.");
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		FocusListener focus = new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.setTermVal(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Termination value: " + currentParams.getTermVal() + " has been updated.");
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		};
			
		fields[1].addFocusListener(focus);
		fields[1].addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 0, Double.MAX_VALUE));
		fields[2].addFocusListener(focus);
		fields[2].addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 0, Double.MAX_VALUE));
		JLabel lbl = new JLabel("crystals have nucleated.");
		pnls[2].add(getSpacer(new Dimension(15, 5)));
		pnls[2].add(lbl);
		pnl.add(pnls[0]);
		pnl.add(pnls[1]);
		pnl.add(pnls[2]);
		pnl.setMaximumSize(pnl.getPreferredSize());
		return pnl;
	}
	private JPanel setupFileIO() {
		JPanel pnlMain = new JPanel();
		pnlMain.setBorder(BorderFactory.createTitledBorder("File I/O"));
		
		Box boxMain = Box.createVerticalBox();
		final JLabel lblXYZ = new JLabel(currentParams.getXYZsFolder().toString()),
				lblMovie= new JLabel(currentParams.getMoviesFile().toString()),
				lblZip= new JLabel(currentParams.getZipFile().toString()),
				lblTrans= new JLabel(currentParams.getTransformationsFolder().toString()),
				lblObj= new JLabel(currentParams.getObjectsFile().toString());
		
		final String[] btnLabels = {"Y", "N"};
		int spacerSize = 15;
		// output folder
		{
			Box box = Box.createHorizontalBox();
			
			JButton btn = new JButton("Set output folder");

			final JLabel lbl = new JLabel(currentParams.getFolderOutput().getAbsolutePath());
			
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser(currentParams.getFolderOutput());
					fc.setMultiSelectionEnabled(false);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showSaveDialog(null);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						currentParams.setFolderOutput(fc.getSelectedFile());
						lbl.setText(currentParams.getFolderOutput().getAbsolutePath());
					}
				}
			});

			box.add(btn);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(lbl);
			box.add(Box.createHorizontalGlue());

			box.add(Box.createHorizontalGlue());
			
			boxMain.add(box);
		}
		{
			Box box = Box.createHorizontalBox();
			
			JLabel lbl = new JLabel("Project file root");
			txtFileRoot.setText(currentParams.getCurrentFileRoot());
			txtFileRoot.addFocusListener(new FocusListener() {

				String previousVal = "", newVal, defaultVal = "CrystallizationSimulation";
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = ((JTextField) e.getSource()).getText();
						if(previousVal.compareTo("") == 0) 
							previousVal = newVal;
						if(previousVal.compareTo(newVal) != 0) {
							if(newVal.compareTo("") == 0) {
								newVal = defaultVal;
								((JTextField) e.getSource()).setText(defaultVal);
							}
							currentParams.setCurrentFileRoot(newVal);
							lblXYZ.setText(currentParams.getXYZsFolder().getName());
							lblMovie.setText(currentParams.getMoviesFile().getName());
							lblZip.setText(currentParams.getZipFile().getName());
							lblTrans.setText(currentParams.getTransformationsFolder().getName());
							lblObj.setText(currentParams.getObjectsFile().getName());
							message("Project file root updated to: " + newVal);
							updateParamValues();
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				@Override
				public void focusGained(FocusEvent e) { 
					previousVal = ((JTextField) e.getSource()).getText();
					((JTextField) e.getSource()).selectAll(); 
				}
			});
			box.add(lbl);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(txtFileRoot);
			box.add(Box.createHorizontalGlue());

			boxMain.add(box);
		}
		{
			Box box = Box.createHorizontalBox();
			box.add(new JLabel("Output options: "));
			box.add(Box.createHorizontalGlue());
			boxMain.add(box);
		}
		
		// xyz folder
		{
			Box box = Box.createHorizontalBox();
			ButtonGroup grp1 = new ButtonGroup();
			JCheckBox chk1 = new JCheckBox(btnLabels[0]);
			JCheckBox chk2 = new JCheckBox(btnLabels[1]);

			ActionListener al1 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox chk = (JCheckBox) e.getSource();
					if(chk.getText().compareTo(btnLabels[0]) == 0 && !currentParams.isXyz()) {
						currentParams.setXyz(true);
						message("Structure output enabled.");
					} else if(chk.getText().compareTo(btnLabels[1]) == 0 && currentParams.isXyz()){
						currentParams.setXyz(false);
						message("Structure output disabled");
					}
				}
			};
			
			grp1.add(chk1);
			grp1.add(chk2);
			
			chk1.addActionListener(al1);
			chk2.addActionListener(al1);
			if(currentParams.isXyz()) {
				chk1.setSelected(true);
			} else {
				chk2.setSelected(true);
			}

			box.add(chk1);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(chk2);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(lblXYZ);

			box.add(Box.createHorizontalGlue());
			
			boxMain.add(box);
		}
			
		// movie folder
		{
			Box box = Box.createHorizontalBox();
			ButtonGroup grp1 = new ButtonGroup();
			JCheckBox chk1 = new JCheckBox(btnLabels[0]);
			JCheckBox chk2 = new JCheckBox(btnLabels[1]);

			ActionListener al1 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox chk = (JCheckBox) e.getSource();
					if(chk.getText().compareTo(btnLabels[0]) == 0 && !currentParams.isMovie()) {
						currentParams.setMovie(true);
						message("Movie output enabled.");
					} else if(chk.getText().compareTo(btnLabels[1]) == 0 && currentParams.isMovie()){
						currentParams.setMovie(false);
						message("Movie output disabled");
					}
				}
			};
			
			grp1.add(chk1);
			grp1.add(chk2);
			
			chk1.addActionListener(al1);
			chk2.addActionListener(al1);
			if(currentParams.isMovie()) {
				chk1.setSelected(true);
			} else {
				chk2.setSelected(true);
			}

			box.add(chk1);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(chk2);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(lblMovie);
			box.add(Box.createHorizontalGlue());

			boxMain.add(box);
		}
		
		// fitting folder
		{
			Box box = Box.createHorizontalBox();
			ButtonGroup grp1 = new ButtonGroup();
			JCheckBox chk1 = new JCheckBox(btnLabels[0]);
			JCheckBox chk2 = new JCheckBox(btnLabels[1]);

			ActionListener al1 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox chk = (JCheckBox) e.getSource();
					if(chk.getText().compareTo(btnLabels[0]) == 0 && !currentParams.isFit()) {
						currentParams.setFit(true);
						message("Fitting output enabled.");
					} else if(chk.getText().compareTo(btnLabels[1]) == 0 && currentParams.isFit()){
						currentParams.setFit(false);
						message("Fitting output disabled");
					}
				}
			};
			
			grp1.add(chk1);
			grp1.add(chk2);
			
			chk1.addActionListener(al1);
			chk2.addActionListener(al1);
			if(currentParams.isFit()) {
				chk1.setSelected(true);
			} else {
				chk2.setSelected(true);
			}

			box.add(chk1);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(chk2);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(lblZip);
			box.add(Box.createHorizontalGlue());

			boxMain.add(box);
		}
		
		// java objects folder
		{
			Box box = Box.createHorizontalBox();
			ButtonGroup grp1 = new ButtonGroup();
			JCheckBox chk1 = new JCheckBox(btnLabels[0]);
			JCheckBox chk2 = new JCheckBox(btnLabels[1]);

			ActionListener al1 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox chk = (JCheckBox) e.getSource();
					if(chk.getText().compareTo(btnLabels[0]) == 0 && !currentParams.isObj()) {
						currentParams.setObj(true);
						message("Java objects output enabled.");
					} else if(chk.getText().compareTo(btnLabels[1]) == 0 && currentParams.isObj()){
						currentParams.setObj(false);
						message("Java objects output disabled");
					}
				}
			};
			
			grp1.add(chk1);
			grp1.add(chk2);
			
			chk1.addActionListener(al1);
			chk2.addActionListener(al1);
			if(currentParams.isObj()) {
				chk1.setSelected(true);
			} else {
				chk2.setSelected(true);
			}

			box.add(chk1);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(chk2);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(lblObj);
			box.add(Box.createHorizontalGlue());

			boxMain.add(box);
		}
		
		// transformation output folder
		{
			Box box = Box.createHorizontalBox();
			ButtonGroup grp1 = new ButtonGroup();
			JCheckBox chk1 = new JCheckBox(btnLabels[0]);
			JCheckBox chk2 = new JCheckBox(btnLabels[1]);

			ActionListener al1 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox chk = (JCheckBox) e.getSource();
					if(chk.getText().compareTo(btnLabels[0]) == 0 && !currentParams.isTransformed()) {
						currentParams.setTransformed(true);
						message("Transformation output enabled.");
					} else if(chk.getText().compareTo(btnLabels[1]) == 0 && currentParams.isTransformed()){
						currentParams.setTransformed(false);
						message("Transformation output disabled");
					}
				}
			};
			
			grp1.add(chk1);
			grp1.add(chk2);
			
			chk1.addActionListener(al1);
			chk2.addActionListener(al1);
			if(currentParams.isTransformed()) {
				chk1.setSelected(true);
			} else {
				chk2.setSelected(true);
			}

			box.add(chk1);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(chk2);
			box.add(Box.createHorizontalStrut(spacerSize));
			box.add(lblTrans);
			box.add(Box.createHorizontalGlue());

			boxMain.add(box);
		}
		lblXYZ.setText(currentParams.getXYZsFolder().getName());
		lblMovie.setText(currentParams.getMoviesFile().getName());
		lblZip.setText(currentParams.getZipFile().getName());
		lblTrans.setText(currentParams.getTransformationsFolder().getName());
		lblObj.setText(currentParams.getObjectsFile().getName());
		pnlMain.add(boxMain);
		return pnlMain;
	}
	private Component setupSample() {
		Box boxMain = Box.createVerticalBox();
		boxMain.setBorder(BorderFactory.createTitledBorder("Simulation volume parameters"));

		Box box2 = Box.createHorizontalBox();
		box2.add(setupSampleShape());
		box2.add(Box.createHorizontalStrut(10));
		box2.add(setupSampleUnits());
		boxMain.add(box2);
		
		return boxMain;
	}
	private void setUnitsVisibility() {
		switch(currentParams.getSampleShape()) {
		case Cubic:
			lblUnits1.setText("a");
			lblUnits2.setText("");
			lblUnits3.setText("");
			txtUnits1.setEnabled(true);
			txtUnits2.setEnabled(false);
			txtUnits3.setEnabled(false);
			break;
		case Tetragonal:
			lblUnits1.setText("a");
			lblUnits2.setText("");
			lblUnits3.setText("c");
			txtUnits1.setEnabled(true);
			txtUnits2.setEnabled(false);
			txtUnits3.setEnabled(true);
			break;
		case Orthorhombic:
			lblUnits1.setText("a");
			lblUnits2.setText("b");
			lblUnits3.setText("c");
			txtUnits1.setEnabled(true);
			txtUnits2.setEnabled(true);
			txtUnits3.setEnabled(true);
			break;
		case Cylindrical:
			lblUnits1.setText("r");
			lblUnits2.setText("");
			lblUnits3.setText("h");
			txtUnits1.setEnabled(true);
			txtUnits2.setEnabled(false);
			txtUnits3.setEnabled(true);
			break;
		case Spherical:
			lblUnits1.setText("r");
			lblUnits2.setText("");
			lblUnits3.setText("");
			txtUnits1.setEnabled(true);
			txtUnits2.setEnabled(false);
			txtUnits3.setEnabled(false);
			break;
		}
	}
	private JComponent setupFittingParameters() {
		JPanel pnlMain = new JPanel();
		pnlMain.setBorder(BorderFactory.createTitledBorder("Fitting Parameters"));
		pnlMain.setLayout(new GridLayout(0, 3));
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(arg0.getSource() instanceof JToggleButton) {
					JToggleButton tog = (JToggleButton) arg0.getSource();
					String togName = tog.getName();
					String togText = tog.getText();
					FittableParametersOptions option = FittableParametersOptions.valueOf(togName);

					currentParams.setFittableParameterSelection(option);
					message("Fitting type set to: " + togText);
					updateParamValues();
				}
			}
		};
		ButtonGroup bg = new ButtonGroup();
		for(FittableParametersOptions options : FittableParametersOptions.values()) {
			JToggleButton tog = new JToggleButton(options.toString());
			tog.setName(options.name());
			tog.addActionListener(al);
			bg.add(tog);
			pnlMain.add(tog);
		}
		
		for(Component comp : pnlMain.getComponents()) {
			String compName = comp.getName();
			if(FittableParametersOptions.valueOf(compName) == currentParams.getFittableParameterSelection())
				((JToggleButton) comp).doClick();
		}
		
		return pnlMain;
	}
	private JComponent setupDimensionalityOptions() {
		JPanel pnlMain = new JPanel();
		pnlMain.setBorder(BorderFactory.createTitledBorder("Dimensionality"));
		
		Box box = Box.createVerticalBox();
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(arg0.getSource() instanceof JToggleButton) {
					JToggleButton tog = (JToggleButton) arg0.getSource();
					String togName = tog.getName();
					String togText = tog.getText();
					DimensionalityOptions option = DimensionalityOptions.valueOf(togName);

					currentParams.setDimensionalitySelection(option);
					message("Fitting type set to: " + togText);
					updateParamValues();
				}
			}
		};
		ButtonGroup bg = new ButtonGroup();
		for(DimensionalityOptions options : DimensionalityOptions.values()) {
			JToggleButton tog = new JToggleButton(options.toString());
			tog.setName(options.name());
			tog.addActionListener(al);
			bg.add(tog);
			box.add(tog);
		}
		for(Component comp : box.getComponents()) {
			String compName = comp.getName();
			if(DimensionalityOptions.valueOf(compName) == currentParams.getDimensionalitySelection())
				((JToggleButton) comp).doClick();
		}
		pnlMain.add(box);
		return pnlMain;
	}
	private JComponent setupMiscOptions() {
		Box boxMain = Box.createVerticalBox();
		boxMain.setBorder(BorderFactory.createTitledBorder("Misc Options"));
		
		Box box2 = Box.createHorizontalBox();
		Box box3 = Box.createHorizontalBox();
		Box box4 = Box.createHorizontalBox();
		
		txtNumThreads = new JTextField(5);
		txtNumThreads.setText(currentParams.getNumberOfSimulationsToRun() + "");
		txtNumThreads.setMaximumSize(txtNumThreads.getPreferredSize());
		txtNumThreads.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 1, Integer.MAX_VALUE));
		txtNumThreads.addFocusListener(new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.setNumberOfSimulationsToRun(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Number of threads set to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		JLabel lblNumThreads = new JLabel("Number of simulations to compute with these selected parameters.");
		
		box2.add(txtNumThreads);
		box2.add(Box.createHorizontalStrut(10));
		box2.add(lblNumThreads);
		box2.add(Box.createHorizontalGlue());

		txtDeltaT = new JTextField(5);
		txtDeltaT.setText(currentParams.getTimeStep() + "");
		txtDeltaT.setMaximumSize(txtDeltaT.getPreferredSize());
		txtDeltaT.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE, 0, Double.MAX_VALUE));
		txtDeltaT.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.setTimeStep(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Delta t set to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}

		});
		box3.add(txtDeltaT);
		box3.add(Box.createHorizontalStrut(10));
		box3.add(new JLabel("Delta t"));
		box3.add(Box.createHorizontalGlue());
		

		txtFixedN = new JTextField(5);
		txtFixedN.setText(currentParams.getInit_n() + "");
		txtFixedN.setMaximumSize(txtFixedN.getPreferredSize());
		txtFixedN.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE, 0, Double.MAX_VALUE));
		txtFixedN.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.setInit_n(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Initial n set to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}

		});
		box4.add(new JLabel("Fixed n = "));
		box4.add(Box.createHorizontalStrut(10));
		box4.add(txtFixedN);
		box4.add(Box.createHorizontalStrut(10));
		
		txtFixedK = new JTextField(5);
		txtFixedK.setText(currentParams.getInit_k_factor() + "");
		txtFixedK.setMaximumSize(txtFixedK.getPreferredSize());
		txtFixedK.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE, 0, Double.MAX_VALUE));
		txtFixedK.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.setInit_k_factor(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Initial k factor set to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}

		});
		box4.add(new JLabel("initial k = v_pb / V^0.333 * "));
		box4.add(Box.createHorizontalStrut(10));
		box4.add(txtFixedK);
		box4.add(Box.createHorizontalGlue());
		
		boxMain.add(Box.createVerticalStrut(10));
		boxMain.add(box2);
		boxMain.add(Box.createVerticalStrut(10));
		boxMain.add(box3);
		boxMain.add(Box.createVerticalStrut(10));
		boxMain.add(box4);
		return boxMain;
	}
	private JComponent setupSampleUnits() {
		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createTitledBorder("Units per axis"));
		Box box1 = Box.createHorizontalBox();
		box1.add(setupUnits());
		setUnitsVisibility();
		box.add(box1);
		box.add(Box.createVerticalStrut(5));
		box.add(setupSampleVolume());
		return box;
	}
	private Component setupSampleVolume() {
		Box box = Box.createHorizontalBox();
		int approxVolume = currentParams.getApproxVolume();
		box.add(new JLabel("Vol: "));
		lblApproxVolume = new JLabel(approxVolume + "");
		box.add(Box.createHorizontalStrut(5));
		box.add(lblApproxVolume);
		box.add(Box.createHorizontalGlue());
		return box;
	}
	private JComponent setupUnits() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new GridLayout(2, 3));
		
		lblUnits1 = new JLabel("1");
		lblUnits2 = new JLabel("2");
		lblUnits3 = new JLabel("3");
		
		txtUnits1 = new JTextField(5);
		txtUnits2 = new JTextField(5);
		txtUnits3 = new JTextField(5);
		
		txtUnits1.setMaximumSize(txtUnits1.getPreferredSize());
		txtUnits2.setMaximumSize(txtUnits2.getPreferredSize());
		txtUnits3.setMaximumSize(txtUnits3.getPreferredSize());

		txtUnits1.setText("" + currentParams.getSampleUnitsPerAxis()[0]);
		txtUnits2.setText("" + currentParams.getSampleUnitsPerAxis()[1]);
		txtUnits3.setText("" + currentParams.getSampleUnitsPerAxis()[2]);

		Box box1 = Box.createHorizontalBox();
		box1.add(txtUnits1);
		box1.add(Box.createHorizontalStrut(5));
		box1.add(lblUnits1);
		box1.add(Box.createHorizontalGlue());
		
		Box box2 = Box.createHorizontalBox();
		box2.add(txtUnits2);
		box2.add(Box.createHorizontalStrut(5));
		box2.add(lblUnits2);
		box2.add(Box.createHorizontalGlue());

		Box box3 = Box.createHorizontalBox();
		box3.add(txtUnits3);
		box3.add(Box.createHorizontalStrut(5));
		box3.add(lblUnits3);
		box3.add(Box.createHorizontalGlue());

		Box box = Box.createVerticalBox();
		box.add(box1);
		box.add(Box.createVerticalStrut(5));
		box.add(box2);
		box.add(Box.createVerticalStrut(5));
		box.add(box3);
		box.add(Box.createVerticalStrut(5));
		
		txtUnits1.addFocusListener(new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.getSampleUnitsPerAxis()[0] = newVal;
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Sample size dimension a updated to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		
		txtUnits2.addFocusListener(new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.getSampleUnitsPerAxis()[1] = newVal;
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Sample size dimension a updated to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		txtUnits3.addFocusListener(new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.getSampleUnitsPerAxis()[2] = newVal;
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Sample size dimension a updated to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		return box;
	}
	private JPanel setupSampleShape() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBorder(BorderFactory.createTitledBorder("Sample Shape"));
		pnl.setSize(pnl.getPreferredSize());
		ShapeTypes[] types = ShapeTypes.values();
		ButtonGroup btnCrystalShapes = new ButtonGroup();
		JRadioButton btn;
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton btn = (JRadioButton) e.getSource();
				currentParams.setSampleShape(ShapeTypes.valueOf(btn.getText()));
				message("Sample volume shape selection: " + currentParams.getSampleShape() + " has been updated.");
				
				//setRateVisibility();
				setUnitsVisibility();
			}	
		};
		for(int i = 0; i < types.length; i++) {
			btn = new JRadioButton(types[i].toString());
			btn.addActionListener(listener);
			btnCrystalShapes.add(btn);
			pnl.add(btn);
			if(types[i].equals(currentParams.getSampleShape())) {
				btn.setSelected(true);	
			}
		}
		return pnl;
	}
	private JPanel setupParameters() {
		pnlParams = new JPanel();
		pnlParams.setBorder(BorderFactory.createTitledBorder("Current Parameters"));
		Box box = Box.createHorizontalBox();
		box.add(paramLabels());
		box.add(Box.createHorizontalStrut(10));
		box.add(paramValues());
		return pnlParams;
	}
	private void updateParamValues() {
		pnlParams.removeAll();		
		Box box = Box.createHorizontalBox();
		box.add(paramLabels());
		box.add(Box.createHorizontalStrut(10));
		box.add(paramValues());
		pnlParams.add(box);
		repaint();
	}
	private Box paramLabels() {
		String s = "";
		Box box = Box.createVerticalBox();
		int height = 2;
		
		// labels
		box.add(Box.createVerticalStrut(height));
		Box boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		boxH.add(new JLabel("Number of seed crystals"));
		box.add(boxH);

		box.add(Box.createVerticalStrut(height));
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		boxH.add(new JLabel("Maximum number of crystallites in simulation"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Probability of continuous nucleation"));
		box.add(boxH);

		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Delta t"));
		box.add(boxH);

		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Fixed n"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Orientation strategy of nucleation events"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Location strategy of nucleation events"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Geometric shape of nucleation events"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		switch(currentParams.getNucShape()) {
		case Cubic:
			s = "(a, a, a)";
			break;
		case Tetragonal:
			s = "(a, a, c)";
			break;
		case Orthorhombic:
			s = "(a, b, c)";
			break;
		case Cylindrical:
			s = "(r, r, h)";
			break;
		case Spherical:
			s = "(r, r, r)";
			break;
		}
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Axial growth rates " + s));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Simulation termination conditions"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Sample bounding shape"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		switch(currentParams.getSampleShape()) {
		case Cubic:
		case Tetragonal:
		case Orthorhombic:
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Alignment axis 1"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Alignment axis 2"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Alignment axis 3"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
			break;
		case Cylindrical:
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Cylindrical axis"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
			break;
		case Spherical:
			break;
		}

		switch(currentParams.getSampleShape()) {
		case Cubic:
			s = "(a)";
		case Tetragonal:
			s = "(a, c)";
		case Orthorhombic:
			s = "(a, b, c)";
			break;
		case Cylindrical:
			s = "(r, r, h)";
			break;
		case Spherical:
			s = "(r, r, r)";
			break;
		}
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Units per axis " + s));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());

		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Output enabled"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		if(currentParams.isOutput()) {
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Output folder location"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
		}

		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Structure output enabled"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		if(currentParams.isXyz()) {
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Structure output folder"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
			
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Current structure output location"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
		}
		
		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Fitting output enabled"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		if(currentParams.isOutput()) {
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Fitting output folder"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
		}

		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Movie output enabled"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		if(currentParams.isMovie()) {
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Movie output folder"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
		}

		box.add(Box.createVerticalStrut(height));
		boxH.add(new JLabel("Java object output enabled"));
		box.add(boxH);
		
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		if(currentParams.isObj()) {
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Java object output folder"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
		}

		if(currentParams.isTransformed()) {
			box.add(Box.createVerticalStrut(height));
			boxH.add(new JLabel("Current transformation data file name"));
			box.add(boxH);
			
			boxH = Box.createHorizontalBox();
			boxH.add(Box.createHorizontalGlue());
		}
		box.add(Box.createVerticalStrut(height));
		boxH = Box.createHorizontalBox();
		boxH.add(Box.createHorizontalGlue());
		boxH.add(new JLabel("Fittable Parameters"));
		box.add(boxH);
		return box;
	}
	private Box paramValues() {
		String s = "";
		Box box = Box.createVerticalBox();
		// seed crystals
		int height = 2;
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getNucInitVal()));
		// max crystallites
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getMaxNumCrystals()));
		// subsequent nucleation
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getNucContVal()));
		// delta t
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getTimeStep()));
		// initial n
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getInit_n()));
		// nucleation orientation
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getNucOrient()));

		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getNucLoc()));
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getNucShape()));
		double ra = currentParams.getAxialGrowthRates()[0];
		double rb = currentParams.getAxialGrowthRates()[1];
		double rc = currentParams.getAxialGrowthRates()[2];
		double[] newAxialGrowthRates = new double[] {ra, rb, rc};
		switch(currentParams.getNucShape()) {
		case Cubic:
			newAxialGrowthRates = new double[] {ra, ra, ra};
			s = "(" + ra + ", " + ra + ", " + ra + ")";
			break;
		case Tetragonal:
			newAxialGrowthRates = new double[] {ra, ra, rc};
			s = "(" + ra + ", " + ra + ", " + rc + ")";
			break;
		case Orthorhombic:
			newAxialGrowthRates = new double[] {ra, rb, rc};
			s = "(" + ra + ", " + rb + ", " + rc + ")";
			break;
		case Cylindrical:
			newAxialGrowthRates = new double[] {ra, ra, rc};
			s = "(" + ra + ", " + ra + ", " + rc + ")";
			break;
		case Spherical:
			newAxialGrowthRates = new double[] {ra, ra, ra};
			s = "(" + ra + ", " + ra + ", " + ra + ")";
			break;
		}
		if(!(txtRate1 == null || txtRate2 == null || txtRate3 == null)) {
			txtRate1.setText(newAxialGrowthRates[0] + "");
			txtRate2.setText(newAxialGrowthRates[1] + "");
			txtRate3.setText(newAxialGrowthRates[2] + "");
		}
		currentParams.setAxialGrowthRates(newAxialGrowthRates);
		if(lblApproxVolume != null) {
			lblApproxVolume.setText(currentParams.getApproxVolume() + "");
		}
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + s));

		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getTerm() + "(" + currentParams.getTermVal().toString() + ")"));

		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getSampleShape()));

		switch(currentParams.getSampleShape()) {
		case Cubic:
		case Tetragonal:
		case Orthorhombic:
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("" + currentParams.getSampleUnitAxes()[0].toString()));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("" + currentParams.getSampleUnitAxes()[1].toString()));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("" + currentParams.getSampleUnitAxes()[2].toString()));
			break;
		case Cylindrical:
			currentParams.getSampleUnitAxes()[2] = JVector.z;
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("" + currentParams.getSampleUnitAxes()[0].toString()));
			break;
		case Spherical:
			break;
		}

		int a = currentParams.getSampleUnitsPerAxis()[0];
		int b = currentParams.getSampleUnitsPerAxis()[1];
		int c = currentParams.getSampleUnitsPerAxis()[2];
		int[] newSampleUnitsPerAxis = new int[] {a, b, c};
		switch(currentParams.getSampleShape()) {
		case Cubic:
			newSampleUnitsPerAxis = new int[] {a, a, a};
			s = "(" + a + ", " + a + ", " + a + ")";
			break;
		case Tetragonal:
			newSampleUnitsPerAxis = new int[] {a, a, c};
			s = "(" + a + ", " + a + ", " + c + ")";
			break;
		case Orthorhombic:
			newSampleUnitsPerAxis = new int[] {a, b, c};
			s = "(" + a + ", " + b + ", " + c + ")";
			break;
		case Cylindrical:
			newSampleUnitsPerAxis = new int[] {a, a, c};
			s = "(" + a + ", " + a + ", " + c + ")";
			break;
		case Spherical:
			newSampleUnitsPerAxis = new int[] {a, a, a};
			s = "(" + a + ", " + a + ", " + a + ")";
			break;
		}
		if(!(txtUnits1 == null || txtUnits2 == null || txtUnits3 == null)) {
			txtUnits1.setText(newSampleUnitsPerAxis[0] + "");
			txtUnits2.setText(newSampleUnitsPerAxis[1] + "");
			txtUnits3.setText(newSampleUnitsPerAxis[2] + "");
		}
		currentParams.setSampleUnitsPerAxis(newSampleUnitsPerAxis);
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel(s));


		if(currentParams.isOutput()) {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("Yes"));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getFolderOutput() + ""));
		} else {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("No"));
		}
		
		if(currentParams.isXyz()) {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("Yes"));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getXYZsFolder() + ""));

			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getXYZsFolder() + ""));
		} else {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("No"));
		}
		
		if(currentParams.isFit()) {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("Yes"));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getZipFile() + ""));
		} else {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("No"));
		}
		
		if(currentParams.isMovie()) {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("Yes"));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getMoviesFile() + ""));
		} else {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("No"));
		}
		
		if(currentParams.isObj()) {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("Yes"));
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getObjectsFile() + ""));
		} else {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel("No"));
		}
		
		if(currentParams.isTransformed()) {
			box.add(Box.createVerticalStrut(height));
			box.add(new JLabel(currentParams.getTransformationsFolder() + ""));
		}
		box.add(Box.createVerticalStrut(height));
		box.add(new JLabel("" + currentParams.getFittableParameterSelection().toString()));
		return box;
	}
	private JPanel setupNucleation() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		pnl.setBorder(BorderFactory.createTitledBorder("Nucleation Parameters"));
		pnl.add(setupNucleationShape());
		pnl.add(setupGrowthRates());
		pnl.add(setupNucleationType());
		pnl.add(setupNucleationOrientation());
		return pnl;
	}
	private JPanel setupGrowthRates() {
		pnlRates = new JPanel();
		pnlRates.setLayout(new BoxLayout(pnlRates, BoxLayout.X_AXIS));
		TitledBorder b = BorderFactory.createTitledBorder("Axial Growth Velocity");
		Dimension d = b.getMinimumSize(pnlRates);
		pnlRates.setBorder(b);
		pnlRates.setMinimumSize(d);
		pnlRates.add(setupRates());
		setRateVisibility();
		return pnlRates;
	}
	private void setRateVisibility() {
		switch(currentParams.getNucShape()) {
		case Cubic:
			lblRate1.setText("a");
			lblRate2.setText("");
			lblRate3.setText("");
			txtRate1.setEnabled(true);
			txtRate2.setEnabled(false);
			txtRate3.setEnabled(false);
			break;
		case Tetragonal:
			lblRate1.setText("a");
			lblRate2.setText("");
			lblRate3.setText("c");
			txtRate1.setEnabled(true);
			txtRate2.setEnabled(false);
			txtRate3.setEnabled(true);
			break;
		case Orthorhombic:
			lblRate1.setText("a");
			lblRate2.setText("b");
			lblRate3.setText("c");
			txtRate1.setEnabled(true);
			txtRate2.setEnabled(true);
			txtRate3.setEnabled(true);
			break;
		case Cylindrical:
			lblRate1.setText("r");
			lblRate2.setText("");
			lblRate3.setText("h");
			txtRate1.setEnabled(true);
			txtRate2.setEnabled(false);
			txtRate3.setEnabled(true);
			break;
		case Spherical:
			lblRate1.setText("r");
			lblRate2.setText("");
			lblRate3.setText("");
			txtRate1.setEnabled(true);
			txtRate2.setEnabled(false);
			txtRate3.setEnabled(false);
			break;
		}
	}
	
	private JPanel setupRates() {
		JPanel pnlMain = new JPanel();
		
		pnlMain.setLayout(new GridLayout(3, 2));
		
		lblRate1 = new JLabel("r1");
		lblRate2 = new JLabel("r2");
		lblRate3 = new JLabel("r3");
		
		txtRate1 = new JTextField(5);
		txtRate2 = new JTextField(5);
		txtRate3 = new JTextField(5);
		
		txtRate1.setMaximumSize(txtRate1.getPreferredSize());
		txtRate2.setMaximumSize(txtRate2.getPreferredSize());
		txtRate3.setMaximumSize(txtRate3.getPreferredSize());
		
		txtRate1.setText("" + currentParams.getAxialGrowthRates()[0]);
		txtRate2.setText("" + currentParams.getAxialGrowthRates()[1]);
		txtRate3.setText("" + currentParams.getAxialGrowthRates()[2]);
		
		pnlMain.add(lblRate1);
		pnlMain.add(txtRate1);
		
		pnlMain.add(lblRate2);
		pnlMain.add(txtRate2);
		
		pnlMain.add(lblRate3);
		pnlMain.add(txtRate3);
		
		txtRate1.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.getAxialGrowthRates()[0] = newVal;
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Axial growth rate in the a direction updated to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		txtRate2.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.getAxialGrowthRates()[1] = newVal;
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Axial growth rate in the b direction updated to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		txtRate3.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.getAxialGrowthRates()[2] = newVal;
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Axial growth rate in the c direction updated to: " + newVal);
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		
		pnlMain.setVisible(true);
		pnlMain.setMaximumSize(pnlMain.getPreferredSize());
		return pnlMain;
	}

	private JPanel setupNucleationOrientation() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBorder(BorderFactory.createTitledBorder("Crystal Orientation"));
		pnl.setSize(pnl.getPreferredSize());
		NucleationOrientation[] nucOrient = NucleationOrientation.values();
		ButtonGroup group = new ButtonGroup();
		JRadioButton btn;
		nucOrientSet = new NucleationOrientationSettingsFrame();
		for(int i = 0; i < nucOrient.length; i++) {
			btn = new JRadioButton(nucOrient[i].toString());
			group.add(btn);
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JRadioButton btn = (JRadioButton) e.getSource();
					currentParams.setNucOrient(NucleationOrientation.valueOf(btn.getText()));
					message("Nucleation orientation setting: " + currentParams.getNucOrient() + " has been updated.");
					switch(currentParams.getNucOrient()){
					case SetOfOrientations:
						btnOrientationSettings.setEnabled(true);
						break;
					case Random:
						btnOrientationSettings.setEnabled(false);
						break;
					}
				}
			});
			
			if(nucOrient[i] == NucleationOrientation.SetOfOrientations) {
				JPanel pnl1 = new JPanel();
				pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.Y_AXIS));
				pnl1.add(btn);
				btnOrientationSettings = new JButton("Orientation Settings");
				btnOrientationSettings.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						nucOrientSet.setVisible(true);
					}
					
				});
				pnl.add(pnl1);
				pnl1.add(btnOrientationSettings);
			} else {
				pnl.add(btn);
			}
			if(nucOrient[i].equals(currentParams.getNucOrient())) {
				btn.setSelected(true);	
			}
			
		}
		switch(currentParams.getNucOrient()){
		case SetOfOrientations:
			btnOrientationSettings.setEnabled(true);
			break;
		case Random:
			btnOrientationSettings.setEnabled(false);
			break;
		}
		return pnl;
	}
	private JPanel setupNucleationShape() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBorder(BorderFactory.createTitledBorder("Crystal Shape"));
		pnl.setSize(pnl.getPreferredSize());
		ShapeTypes[] types = ShapeTypes.values();
		ButtonGroup btnCrystalShapes = new ButtonGroup();
		JRadioButton btn;
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton btn = (JRadioButton) e.getSource();
				currentParams.setNucShape(ShapeTypes.valueOf(btn.getText()));
				message("Nucleation selection: " + currentParams.getNucShape() + " has been updated.");
				setRateVisibility();
			}	
		};
		for(int i = 0; i < types.length; i++) {
			btn = new JRadioButton(types[i].toString());
			btn.addActionListener(listener);
			btnCrystalShapes.add(btn);
			pnl.add(btn);
			if(types[i].equals(currentParams.getNucShape())) {
				btn.setSelected(true);	
			}
		}
		return pnl;
	}
	private JComponent setupNucleationType() {
		Box boxMain = Box.createVerticalBox();
		Box box1 = Box.createHorizontalBox();
		Box box2 = Box.createHorizontalBox();
		Box box3 = Box.createHorizontalBox();
		boxMain.setBorder(BorderFactory.createTitledBorder("Types of Nucleation"));
		
		JLabel lbl1 = new JLabel("Initial number of seed crystals");
		lbl1.setSize(lbl1.getPreferredSize());
		final JTextField txtInitXtals = new JTextField(5);
		txtInitXtals.setText("" + currentParams.getNucInitVal());
		txtInitXtals.setMaximumSize(txtInitXtals.getPreferredSize());
		txtInitXtals.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 0, Double.MAX_VALUE));
		txtInitXtals.addFocusListener(new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.setNucInitVal(newVal); 
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Inital Number of crystallites: " + currentParams.getNucInitVal() + " has been updated.");
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		box1.add(txtInitXtals);
		box1.add(lbl1);
		box1.add(Box.createHorizontalGlue());

		JLabel lbl2 = new JLabel("Probability of subsequent nucleation (0 <= P <= 1)");
		lbl2.setSize(lbl2.getPreferredSize());
		final JTextField txtNucProb = new JTextField(5);
		txtNucProb.setText("" + currentParams.getNucContVal());
		txtNucProb.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE, 0, 1));
		txtNucProb.setMaximumSize(txtNucProb.getPreferredSize());
		txtNucProb.addFocusListener(new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					currentParams.setNucContVal(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Probability of continuous nucleation: " + currentParams.getNucContVal() + " has been updated.");
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		box2.add(txtNucProb);
		box2.add(lbl2);
		box2.add(Box.createHorizontalGlue());
		
		JLabel lbl3 = new JLabel("Maximum Number of Crystallites in Simulation");
		lbl3.setSize(lbl3.getPreferredSize());
		final JTextField maxNumXtals = new JTextField(5);
		maxNumXtals.setText("" + currentParams.getMaxNumCrystals());
		maxNumXtals.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 0, -1));
		maxNumXtals.setMaximumSize(maxNumXtals.getPreferredSize());
		maxNumXtals.addFocusListener(new FocusListener() {

			int previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Integer.parseInt(((JTextField) e.getSource()).getText());
					currentParams.setMaxNumCrystals(newVal);
					((JTextField) e.getSource()).setBackground(okEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						message("Maximum number of crystallites in simulation has been updated to: " + currentParams.getMaxNumCrystals());
						updateParamValues();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Integer.parseInt(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
		});
		box3.add(maxNumXtals);
		box3.add(lbl3);
		box3.add(Box.createHorizontalGlue());		
		
		boxMain.add(box1);
		boxMain.add(box3);
		boxMain.add(box2);
		return boxMain;
	}

	/**
	 * RUNTIME VARIABLES
	 */
	
	/**
	 * 
	 */
	private int numProcessorsToUse = 1;
	
	public void launch() throws IOException {
		simPanel.initialize(new SimulationController_UI(currentParams));
		repaint();
		btnRunSimulation.setEnabled(false);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				simPanel.execute();
			}
        });
	}
	
	/**
	 * 
	 */
	public void newSimulSetupParams() {
		SimulSetupParams ssp = new SimulSetupParams();
		ssp.copy(currentParams);
	}
	public void message(String message) {
		if(messages != null) {
			messages.setText(message + "\n" + messages.getText());
			/*Dimension newDim = messages.getPreferredSize();
			newDim.height = scroll.getHeight();
			newDim.width = getWidth() - 50;
			Dimension windowDim = getSize();
			if(newDim.width > windowDim.width) {
				newDim.width = windowDim.width - 25;
			}
			scroll.setSize(newDim);*/
			messages.setCaretPosition(0);
		}
		//updateParamValues();
	}
	class NucleationOrientationSettingsFrame extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6440047359787744736L;
		public NucleationOrientationSettingsFrame() {
			super();
			setup();
			setPreferredSize(new Dimension(250, 250));
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			pack();
		}
		private void setup() {
			JPanel pnl = new JPanel();
			JTextField txt = new JTextField(10);
			txt.setText("placeholder");
			txt.setEditable(false);
			add(txt);
		}
	}
	class DataTypeChecker implements FocusListener, ActionListener {

		public final static int INTEGER = 0;
		public final static int DOUBLE = 1;
		public final static int STRING = 2;
		private int dataType;
		private double min, max;
		private boolean enforceBounds = false;
		
		public DataTypeChecker(int dataType) {
			this.dataType = dataType;
		}
		
		public DataTypeChecker(int dataType, double min, double max) {
			this.dataType = dataType;
			this.min = min;
			this.max = max;
			enforceBounds = true;
		}
		@Override
		public void focusGained(java.awt.event.FocusEvent e) {
			check(e.getSource());
		}

		@Override
		public void focusLost(java.awt.event.FocusEvent e) {
			check(e.getSource());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			check(e.getSource());
		}
		
		public void check(Object o) {
			JTextField txt;
			if(o instanceof JTextField) {
				txt = (JTextField) o;
				switch(dataType) {
				case INTEGER:
					try {
						int val = Integer.parseInt(txt.getText());
						if(enforceBounds) {
							if(val <= min ) {
								txt.setText("" + (int) min);
							} else if (val >= max && max >= min) {
								txt.setText("" + (int) max);
							}
						}
					}
					catch (NumberFormatException nfe) {
						txt.setText("0");
					}
					break;
				case DOUBLE:
					try {
						double val = Double.parseDouble(txt.getText());
						if(enforceBounds) {
							if(val <= min ) {
								txt.setText(min + "");
							} else if (val >= max) {
								txt.setText(max + "");
							}
						}
					} 
					catch (NumberFormatException nfe) {
						txt.setText("0");
					}
					break;
				case STRING:
					try {
						txt.getText();
					}
					catch (NullPointerException npe) {
						txt.setText("");
					}
				}
			}
		}
	}
	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg1 instanceof String[] ) {
			String[] complexMessage = (String[]) arg1;
			if(complexMessage.length == 1)
				message(complexMessage[0]);
			else
				message(complexMessage[1]);
			if(arg0 instanceof RunningSimulationPanel) {
				if(complexMessage[0].compareTo(RunningSimulationPanel.SIMULATION_COMPLETE) == 0) {
					message("Simulation Complete.");
					btnRunSimulation.setEnabled(true);
//					SendMailTLS.send("fhou@ncsu.edu", currentParams.toString());
				}
				if(complexMessage[0].compareTo(RunningSimulationPanel.SIMULATION_TERMINATED) == 0) {
					message("Simulation was terminated.");
					btnRunSimulation.setEnabled(true);
	//					repaint();
	//					pack();
				} 
			}
		} else if(arg1 instanceof String) {
			message((String) arg1);
		}
	}
}

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

import io.StringConverter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import shapes.ShapeTypes;
import simulation.Simulation;

public class RunningSimulationPanel extends Observable implements Observer {

	private final Box boxMain;
	private SimulationController_UI simController;
	private JProgressBar bar1;
	private JProgressBar bar2;	
	private JLabel lblVolume;
	private JLabel lblSampleShape, lblSampleAxes;
	private JButton btnEnd;
	private int numSimulations = 0;
	public final static String SIMULATION_TERMINATED = "simulation terminated";
	public final static String SIMULATION_COMPLETE = "simulation complete";
	public final static String THREAD_COMPLETE = "thread complete";
	private ExecutorService es;
	public RunningSimulationPanel() {
		boxMain = Box.createVerticalBox();
		setupPanel();
	}
	public Box getBox() { 
		boxMain.setSize(boxMain.getPreferredSize());
		return boxMain; 
	}
	
	public void initialize(SimulationController_UI sc) {
		simController = sc;
		simController.addObserver(this);
		simController.initialize();
		addSimulationController();
	}
	public void execute() {
		es = Executors.newFixedThreadPool(1);
		Simulation simul = simController.getNextSimulation();
		while(simul != null) {
			es.execute(simul);
			simul = simController.getNextSimulation();
		}
		
	}
	public void terminate() {
		if(simController.isRunning()) {
			simController.kill();
			es.shutdown();
		} else {
			resetLabels();
			
			setChanged();
			notifyObservers(SIMULATION_TERMINATED);
		}
	}
	private void resetLabels() {
		btnEnd.setText("Terminate <<Experiment Name>>");
		bar1.setValue(0);
		bar2.setValue(0);
		bar1.setString("0 / 0");
		bar2.setString("0%");
		lblSampleShape.setText("Sample shape");
		lblSampleAxes.setText("Sample axes");
		lblVolume.setText("Approximate Sample Volume = ??");
		boxMain.repaint();
	}
	public void setupPanel() {
		//box.remove(box.getComponentCount()-1);
		
		Box box0 = Box.createHorizontalBox();
		btnEnd = new JButton("Terminate ");
		btnEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				terminate();
			}
		});
		
		
		box0.add(btnEnd);
		bar1 = new JProgressBar(0, 100);
		bar1.setStringPainted(true);
		bar1.setString("Simulation " + numSimulations);
		Box box1 = Box.createHorizontalBox();
		box1.add(bar1);
		box1.add(Box.createHorizontalStrut(10));
		
		Box box2 = Box.createHorizontalBox();
		bar2 = new JProgressBar(0, 100);
		bar2.setStringPainted(true);
		box2.add(bar2);
		box2.add(Box.createHorizontalStrut(10));
		
		
		Box box3 = Box.createHorizontalBox();
		lblSampleShape = new JLabel(ShapeTypes.Cubic.name() + "");
		box3.add(lblSampleShape);
		box3.add(Box.createHorizontalStrut(10));
		lblSampleAxes = new JLabel("(a) = (50)");
		box3.add(lblSampleAxes);
		
		Box box4 = Box.createHorizontalBox();
		lblVolume = new JLabel("Approximate simulation volume = 0");
		box4.add(lblVolume);

		boxMain.add(box0);
		boxMain.add(Box.createVerticalStrut(5));
		boxMain.add(box1);
		boxMain.add(Box.createVerticalStrut(5));
		boxMain.add(box2);
		boxMain.add(Box.createVerticalStrut(5));
		boxMain.add(box3);
		boxMain.add(Box.createVerticalStrut(5));
		boxMain.add(box4);
		boxMain.add(Box.createVerticalStrut(5));
		boxMain.setMaximumSize(boxMain.getPreferredSize());
		
		boxMain.add(Box.createVerticalStrut(10));
		boxMain.add(Box.createVerticalGlue());
		resetLabels();
		boxMain.repaint();
	}
	private void addSimulationController() {
		lblVolume.setText("Approximate simulation volume = " + simController.getTotalVolume() + "");
		lblSampleShape.setText(simController.sp.getSampleShape().name() + "");
		lblSampleAxes.setText(simController.sp.getSampleAxes());
		bar1.setString("0 / " + simController.sp.getNumberOfSimulationsToRun());
		btnEnd.setText("Terminate " + simController.sp.getCurrentFileRoot());
		boxMain.repaint();
	}
	public void removeSimulation(int idx) {
		boxMain.remove(idx);
		bar1.setValue(0);
		bar2.setValue(0);
		bar1.setString("0 / 0");
		bar2.setString("0%");
		lblVolume.setText("0");
		boxMain.repaint();
	}
	/**
	 * 
	 * @param idx number of completed simulations
	 */
	private void updateSimulationThreadComplete(int threadsComplete) {
		double val = 100*((double) threadsComplete) / ((double) simController.sp.getNumberOfSimulationsToRun());
		bar1.setValue((int) Math.rint(val));
		bar1.setString(threadsComplete + " / " + simController.sp.getNumberOfSimulationsToRun());
		boxMain.repaint();
	}
	private void updateSimulationPercent(double percentFinished) {
		bar2.setValue((int) Math.rint(percentFinished*100));
		bar2.repaint();
		bar2.setString(Math.rint(percentFinished * 1000)/10. + "%");
		boxMain.repaint();
	}
	

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof SimulationController_UI) {
			if(arg1 instanceof String[]) {
				String[] complexMessage = (String[]) arg1;
				if(complexMessage[0].compareTo(SimulationController_UI.THREAD_COMPLETE) == 0) {
					int threadsComplete = Integer.valueOf(complexMessage[1]);
					updateSimulationThreadComplete(threadsComplete);
					notifyObservers(new String[] {THREAD_COMPLETE, threadsComplete + ""});
				} else if(complexMessage[0].compareTo(SimulationController_UI.UPDATE_PERCENT) == 0) {
					double percentComplete = Double.valueOf(complexMessage[1]);
					updateSimulationPercent(percentComplete);
				} else if(complexMessage[0].compareTo(SimulationController_UI.MESSAGE) == 0){
					setChanged();
					notifyObservers(complexMessage);
				}
			} else if(arg1 instanceof String) {
				String message = (String) arg1;
				if(message.compareTo(SimulationController_UI.ALL_THREADS_KILLED) == 0) {
					terminate();
					setChanged();
					notifyObservers(new String[] {SIMULATION_TERMINATED});
				} else if(message.compareTo(SimulationController_UI.ALL_THREADS_COMPLETE) == 0) {
					setChanged();
					notifyObservers(new String[] {SIMULATION_COMPLETE});
				}
			}
		}
	}
}

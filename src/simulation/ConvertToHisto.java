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
package simulation;

import io.MyFileInputStream;
import io.MyPrintStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

public class ConvertToHisto {

	private File aFile; 
	private double bin_min_V = 10;
	private double bin_max_V = 0;
	private double bin_min_kA = 0;
	private double bin_max_kA = -10;
	private int bin_num_V = 30;
	private int bin_num_kA = 30;
	private double bin_step_V = 0.01;
	private double bin_step_kA = 0.01;
	
	public double[][] readFile() throws FileNotFoundException {
		MyFileInputStream mfis = new MyFileInputStream(aFile);
		Scanner s = mfis.getScanner();
		
		Vector<double[]> data = new Vector<double[]>();
		String[] line;
		double ln_V, ln_kA;
		while(s.hasNext()) {
			line = s.nextLine().split("\t");
			ln_V = Double.valueOf(line[0]);
			ln_kA = Double.valueOf(line[1]);
			data.add(new double[] {ln_V, ln_kA});
		}
		double[][] theData = new double[data.size()][2];
		for(int i = 0; i < theData.length; i++) {
			theData[i] = data.remove(0);
			if(bin_min_V > theData[i][0]) { bin_min_V = theData[i][0]; }
			if(bin_max_V < theData[i][0]) { bin_max_V = theData[i][0]; }
			if(bin_min_kA > theData[i][1]) { bin_min_kA = theData[i][1]; }
			if(bin_max_kA < theData[i][1]) { bin_max_kA = theData[i][1]; }
		}
		// set up the histogram
		bin_step_V = (bin_max_V - bin_min_V) / (double) bin_num_V;
		bin_step_kA = (bin_max_kA - bin_min_kA) / (double) bin_num_kA;
		double[][] histogram = new double[bin_num_V+1][bin_num_kA+1];
		// set the histogram values to 0
		for(int i = 0; i < histogram.length; i++) {
			for(int j = 0; j < histogram[0].length; j++) {
				histogram[i][j] = 0;
			}
		}
		// fill the histogram
		int v, kA;
		for(int i = 0; i < theData.length; i++) {
			v = (int) Math.round((theData[i][0]-bin_min_V) / bin_step_V);
			kA = (int) Math.round((theData[i][1]-bin_min_kA) / bin_step_kA);
			histogram[v][kA] += 1;
		}
		// convert the 2d histogram to 1d
		int idx = 0;
		double[][] histogram2 = new double[(bin_num_V+1) * (bin_num_kA+1)][3];
		for(int i = 0; i < histogram.length; i++) {
			for(int j = 0; j < histogram[0].length; j++) {
				histogram2[idx][0] = bin_min_V + i * bin_step_V;
				histogram2[idx][1] = bin_min_kA + j * bin_step_kA;
				histogram2[idx][2] = histogram[i][j];
				idx++;
			}
		}
		mfis.close();
		return histogram2;
	}
	public void printFile(double[][] histogram) throws FileNotFoundException {
		File outFile = new File(aFile.toString() + ".histo");
		MyPrintStream mps = new MyPrintStream(outFile);
		
		for(int i = 0; i < histogram.length; i++) {
			for(int j = 0; j < histogram[i].length; j++) {
				mps.print(histogram[i][j] + "\t");
			}
			mps.println();
		}
		mps.close();
	}
	
	public static void main(String[] args) {
		ConvertToHisto histo = new ConvertToHisto();
		histo.aFile = new File("all histogram data.txt");
		double[][] histogram = null;
		try {
			histogram = histo.readFile();
			histo.printFile(histogram);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

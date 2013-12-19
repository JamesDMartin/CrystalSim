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
package analysis;
import io.StringConverter;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;

public class ThinFilmAnalysis {

	private double[][] bulk, individual;
	public ThinFilmAnalysis(File bulk, File individual) throws FileNotFoundException {
		this.bulk = readFile(bulk);
		this.individual = readFile(individual);
	}
	public ThinFilmAnalysis() {
		
	}
	private double[][] readFile(File toRead) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(toRead);
		Scanner s = new Scanner(fis);

		// skip header line
		s.nextLine();
		
		Vector<double[]> lines = new Vector<double[]>();
		String[] line;
		double n, sumSq, vol;
		while(s.hasNextLine()) {
			line = s.nextLine().split("\t");
			n = Double.valueOf(line[0]);
			sumSq = Double.valueOf(line[1]);
			vol = Double.valueOf(line[2]);
			lines.add(new double[] {n, sumSq, vol});
		}
		
		double[][] arr = new double[lines.size()][3];
		arr = lines.toArray(arr);
		return arr;
	}
	
	private Integer[] bulkVolumes() {
		double[][] arr = bulk;
		Vector<Integer> volumes = new Vector<Integer>();
		Integer V;
		for(int i = 0; i < arr.length; i++) {
			V = (int) (arr[i][2]);
			if(!volumes.contains(V)) {
				volumes.add(V);
			}
		}
		Integer[] v = new Integer[volumes.size()];
		v = volumes.toArray(v);
		return v;
	}
	
	private Double[] bulkNs() {
		double[][] arr = bulk;
		Vector<Double> volumes = new Vector<Double>();
		Double V;
		for(int i = 0; i < arr.length; i++) {
			V = arr[i][0];
			if(!volumes.contains(V)) {
				volumes.add(V);
			}
		}
		Double[] v = new Double[volumes.size()];
		v = volumes.toArray(v);
		return v;
	}
	
	private double[][][] sortBulk() {
		Integer[] bulkVolumes = bulkVolumes();
		double[][][] sortedBulk = new double[bulkVolumes.length][][];
		int volIdx;
		Vector<double[]> volumes = new Vector<double[]>();
		for(int i = 0; i < bulkVolumes.length; i++) {
			volIdx = 0;
			for(int j = 0; j < bulk.length; j++) {
				if(bulk[j][2] == bulkVolumes[i]) {
					volumes.add(bulk[j]);
				}
			}
			sortedBulk[i] = new double[volumes.size()][3];
			
			sortedBulk[i] = volumes.toArray(sortedBulk[i]);
			volumes.clear();
		}
		return sortedBulk;
	}
	private double[][] averages() {
		double[][][] sortedBulk = sortBulk();
		double n, totalSumSq;
		int numSummed, volume = 0;
		Double[] allN = bulkNs();
		Integer[] allVolumes = bulkVolumes();
		double[][] averages = new double[allN.length * allVolumes.length][5];
		int avgIdx = 0;
		for(int i = 0; i < sortedBulk.length; i++) {
			for(int a = 0; a < allN.length; a++) {
				numSummed = 0;
				totalSumSq = 0;
				for(int j = 0; j < sortedBulk[i].length; j++) {
					if(sortedBulk[i][j][0] == allN[a]) {
						numSummed++;
						totalSumSq += sortedBulk[i][j][1];
						volume = (int) sortedBulk[i][j][2];
					}
				}
				averages[avgIdx][0] = allN[a];
				averages[avgIdx][1] = numSummed;
				averages[avgIdx][2] = totalSumSq;
				averages[avgIdx][3] = totalSumSq / ((double) numSummed);
				averages[avgIdx][4] = volume;
				avgIdx++;
			}
		}
		
		return averages;
	}
	private void run(String[] roots) throws FileNotFoundException {
		FileOutputStream fos;
		PrintStream ps;
		for(int i = 0; i < roots.length; i++) {
			bulk = readFile(new File(roots[i] + "_bulk.analyzed"));
			individual = readFile(new File(roots[i] + "_individual.analyzed"));
			fos = new FileOutputStream(roots[i] + "_sorted.analyzed");
			ps = new PrintStream(fos);
			double[][] averages = averages();
			for(int j = 0; j < averages.length; j++) {
				ps.println(StringConverter.arrayToTabString(averages[j]));
			}
		}
	}
	public static void main(String[] args) throws FileNotFoundException {
		File bulk = new File("all_bulk.analyzed");
		File individual = new File("all_individual.analyzed");
		ThinFilmAnalysis tfa = new ThinFilmAnalysis();
		String[] roots = new String[] {"all"};
		tfa.run(roots);
		
	}
}

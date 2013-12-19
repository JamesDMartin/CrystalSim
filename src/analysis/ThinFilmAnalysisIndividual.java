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

public class ThinFilmAnalysisIndividual {

	private double[][] bulk, individual;
	private String[] read_keys;
	private String[] expected_keys = new String[]
			{"kA", "t0", "n", "kA_err", "t0_err", "sumSq", "xtal_size", "total_sample_volume", "nuc_time"};
	private int kA_idx = 0, 
			t0_idx = 1, 
			n_idx = 2, 
			kA_err_idx = 3, 
			t0_err_idx = 4,
			sumSq_idx = 5, 
			xtal_vol_idx = 6, 
			vol_idx = 7, 
			nuc_time_idx = 8;
	
	private int sortByIdx1 = 1, sortByIdx2 = 2;
	public ThinFilmAnalysisIndividual(File bulk, File individual) throws FileNotFoundException {
		this.individual = readFile(individual);
	}
	public ThinFilmAnalysisIndividual() {
		
	}
	private boolean checkKeys() {
		if(read_keys.length != expected_keys.length) {
			System.out.println("Key sets are different lengths: ");
			printKeys();
			System.out.println("Exiting...");
			return false;
		}
		for(int i = 0; i < read_keys.length; i++) {
			if(read_keys[i].compareTo(expected_keys[i]) != 0) {
				System.out.println("read_key: " + read_keys[i] + " is different from the "
						+ "expected key: " + expected_keys[i]);
				System.out.println("Exiting...");
				return false;
			}
		}
		return true;
	}
	private void printKeys() {
		System.out.println("expected_keys: " + StringConverter.arrayToTabString(expected_keys));
		System.out.println("read_keys: " + StringConverter.arrayToTabString(read_keys));
	}
	private double[][] readFile(File toRead) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(toRead);
		Scanner s = new Scanner(fis);

		// get keys line
		read_keys = s.nextLine().split("\t");
		if(!checkKeys()) {
			System.exit(1);
		}
		Vector<double[]> lines = new Vector<double[]>();
		String[] line;
		double[] line_vals = new double[read_keys.length];
		while(s.hasNextLine()) {
			line = s.nextLine().split("\t");
			for(int i = 0; i < line.length; i++) {
				line_vals[i] = Double.valueOf(line[i]);
			}
			lines.add(Arrays.copyOf(line_vals, line_vals.length));
		}
		
		double[][] arr = new double[lines.size()][read_keys.length];
		arr = lines.toArray(arr);
		return arr;
	}
	
	private Integer[] uniqueInts(int key) {
		double[][] arr = individual;
		Vector<Integer> vals = new Vector<Integer>();
		Integer val;
		for(int i = 0; i < arr.length; i++) {
			val = (int) (arr[i][key]);
			if(!vals.contains(val)) {
				vals.add(val);
			}
		}
		Integer[] v = new Integer[vals.size()];
		v = vals.toArray(v);
		return v;
	}
	
	private Double[] uniqueDoubles(int key) {
		double[][] arr = individual;
		Vector<Double> vals = new Vector<Double>();
		Double val;
		for(int i = 0; i < arr.length; i++) {
			val = arr[i][key];
			if(!vals.contains(val)) {
				vals.add(val);
			}
		}
		Double[] v = new Double[vals.size()];
		v = vals.toArray(v);
		return v;
	}
	
	private double[][][] sort() {
		double[][] arr = individual;
		Integer[] bulkVolumes = uniqueInts(vol_idx);
		double[][][] sortedBulk = new double[bulkVolumes.length][][];
		int volIdx;
		Vector<double[]> volumes = new Vector<double[]>();
		for(int i = 0; i < bulkVolumes.length; i++) {
			volIdx = 0;
			for(int j = 0; j < arr.length; j++) {
				if(arr[j][vol_idx] == bulkVolumes[i]) {
					volumes.add(arr[j]);
				}
			}
			sortedBulk[i] = new double[volumes.size()][3];
			
			sortedBulk[i] = volumes.toArray(sortedBulk[i]);
			volumes.clear();
		}
		return sortedBulk;
	}
	private double[][] averages() {
		double[][][] sortedBulk = sort();
		double totalSumSq, vol_key, vol_check, sumSqInv = 0, totalSumSqInv = 0, avg_n=0;
		int numSummed, volume = 0;
		Double[] allN = uniqueDoubles(n_idx);
		Integer[] allVolumes = uniqueInts(vol_idx);
		double[][] averages = new double[allN.length * allVolumes.length][5];
		int avgIdx = 0;
		for(int i = 0; i < sortedBulk.length; i++) {
			for(int a = 0; a < allVolumes.length; a++) {
				numSummed = 0;
				totalSumSq = 0;
				for(int j = 0; j < sortedBulk[i].length; j++) {
					vol_check = sortedBulk[i][j][vol_idx];
					vol_key = allVolumes[a];
					if(vol_check == vol_key) {
						numSummed++;
						totalSumSqInv += 1./sortedBulk[i][j][sumSq_idx];
					}
				}
				if(totalSumSqInv != 0) {
					// inverse of the total sum of squares
					for(int j = 0; j < sortedBulk[i].length; j++) {
						vol_check = sortedBulk[i][j][vol_idx];
						vol_key = allVolumes[a];
						if(vol_check == vol_key) {
							numSummed++;
							sumSqInv = 1./sortedBulk[i][j][sumSq_idx];
							sumSqInv /= totalSumSqInv;
							avg_n += sumSqInv * sortedBulk[i][j][n_idx];
							volume = (int) sortedBulk[i][j][vol_idx];
						}
					}
					averages[avgIdx][0] = allVolumes[a];
					averages[avgIdx][1] = numSummed;
					averages[avgIdx][2] = totalSumSq;
					averages[avgIdx][3] = totalSumSq / ((double) numSummed);
					averages[avgIdx][4] = volume;
					avgIdx++;
				}
			}
		}
		
		return averages;
	}
	private void run(String[] roots) throws FileNotFoundException {
		FileOutputStream fos;
		PrintStream ps;
		for(int i = 0; i < roots.length; i++) {
			individual = readFile(new File(roots[i] + "_individual.analyzed"));
			fos = new FileOutputStream(roots[i] + "_sorted.analyzed");
			ps = new PrintStream(fos);
			double[][] averages = averages();
			ps.println("n\tnumSummed\ttotalSumSq\ttotalSumSq/numSummed\tvolume");
			for(int j = 0; j < averages.length; j++) {
				ps.println(StringConverter.arrayToTabString(averages[j]));
			}
		}
	}
	public static void main(String[] args) throws FileNotFoundException {
		ThinFilmAnalysisIndividual tfa = new ThinFilmAnalysisIndividual();
		String[] roots = new String[] {"1 layer all"};
		tfa.run(roots);
		
	}
}

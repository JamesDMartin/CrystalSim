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
package tests;

import io.MyFileInputStream;

import java.io.File;
import java.util.Scanner;
import java.util.Vector;

public class LinearInterpolateTest {

	private double[][] nAsFuncOfAspectRatio;
	
	private void readNVals() {
		File f = new File("D:\\Documents referenced in lab notebooks\\Dill-10\\178\\EDD_10-178i.txt");
		MyFileInputStream mfis = new MyFileInputStream(f);
		Scanner s = mfis.getScanner();
		Vector<double[]> vals = new Vector<double[]>();
		
		String[] line;
		double val1, val2;
		while(s.hasNextLine()) {
			line = s.nextLine().split("\t");
			val1 = Double.valueOf(line[0]);
			val2 = Double.valueOf(line[1]);
			vals.add(new double[] {val1, val2});
		}
		
		nAsFuncOfAspectRatio = new double[vals.size()][2];
		nAsFuncOfAspectRatio = vals.toArray(nAsFuncOfAspectRatio);
	}
	private double linearInterpolate(double aspectRatio) {
		double n1, n2, navg, asp1, asp2;

		
		if(nAsFuncOfAspectRatio == null) {
			readNVals();
		}
		
		double[][] nfunc = nAsFuncOfAspectRatio;
		
		int i = 0;
		while(aspectRatio > nfunc[i][0]) {
			i++;
		}
		
		n1 = nfunc[i-1][1];
		n2 = nfunc[i][1];
		asp1 = nfunc[i-1][0];
		asp2 = nfunc[i][0];
		
		double x = (aspectRatio - asp1) / (asp2 - asp1);
		double n = n1 * (1-x) + n2 * x;
		
		return n;
		
	}
	
	public static void main(String[] args) {
		LinearInterpolateTest lit = new LinearInterpolateTest();
		
		double[] aspectRatio = new double[] {19.93028311, 12.03710168, 9.175451091};
		
		for(int i = 0; i < aspectRatio.length; i++) {
			System.out.println(aspectRatio[i] + "\t" + lit.linearInterpolate(aspectRatio[i]));
		}

	}
}

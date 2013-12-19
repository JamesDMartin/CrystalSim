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
package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

public class ReadAlphaRangeTest {

	private File in, out;
	public ReadAlphaRangeTest(File inFile, File outFile) {
		in = inFile;
		out = outFile;
	}
	public void run() {
		double[][] vals = readFile();
	}
	private double[][] readFile() {
		double[][] vals;
		Vector<double[]> vecVals = new Vector<double[]>();
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner s = new Scanner(fis);
		
		return null;
	}
	public static void main(String[] args) {
		File in = new File("C:\\Users\\JDM_5\\Desktop\\$research\\papers\\size correction\\II. Theoretical\\kinetic size correction\\alpha range test\\alpha range test.txt");
		File out = new File("C:\\Users\\JDM_5\\Desktop\\$research\\papers\\size correction\\II. Theoretical\\kinetic size correction\\alpha range test\\alpha range test analyzed.txt");
		ReadAlphaRangeTest r = new ReadAlphaRangeTest(in, out);
		r.run();
	}
}

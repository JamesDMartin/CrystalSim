package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MergeObjectFiles {

	private File out;
	private File[] in;
	public MergeObjectFiles(File[] in, File out) {
		this.in = in;
		this.out = out;
		merge();
	}
	
	private void merge() {
		FileInputStream fis;
		ObjectInputStream ois = null;
		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(out);
			oos = new ObjectOutputStream(fos); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < in.length; i++) {
			try {
				fis = new FileInputStream(in[i]);
				ois = new ObjectInputStream(fis);
			} catch (IOException e) {
				e.printStackTrace();
			}
			boolean keepReading = true;
			while(keepReading) {
				try {
					oos.writeObject(ois.readObject());
				} catch (ClassNotFoundException e) {
					keepReading = false;
					e.printStackTrace();
				} catch (IOException e) {
					keepReading = false;
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args) {
		File in1, in2, out;
		in1 = new File("C:\\Users\\JDM_5\\Desktop\\$research\\programming\\workspace\\Kinetics3\\obj files\\125\\DSC26.5.obj");
		in2 = new File("C:\\Users\\JDM_5\\Desktop\\$research\\programming\\workspace\\Kinetics3\\obj files\\500\\DSC26.5.obj");
		out = new File("C:\\Users\\JDM_5\\Desktop\\$research\\programming\\workspace\\Kinetics3\\obj files\\625_DSC26.5.obj");
		File[] in = new File[] {in1, in2};
		new MergeObjectFiles(in, out);
	}
}

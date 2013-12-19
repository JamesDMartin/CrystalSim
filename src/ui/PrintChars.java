package ui;

public class PrintChars {

	public static void main(String[] args) {
		char a = 0;
		for(int i = 0; i < 255; i++) {
			System.out.println(i + ": " + a++);
		}
	}
}

package ui;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class UIViewer {

	public static void main(String[] args) {

		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }catch(Exception ex) {
	        ex.printStackTrace();
	    }		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UI();
            }
        });
	}
}

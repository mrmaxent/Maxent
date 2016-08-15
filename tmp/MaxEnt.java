package density;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class MaxEnt {

    static void checkVersion() {
	String version = System.getProperties().getProperty("java.version");
	double v = Double.parseDouble(version.substring(0,3));
	// can't call Utils, as Utils needs 1.4 to load (prefs.Preferences)
	//	if (v < 1.4) Utils.fatalException("Java version is " + version + ", need 1.4 or later", null);
	if (v < 1.5) {
	    JOptionPane.showMessageDialog(null, "Java version is " + version + ", but Maxent needs 1.5 or later.", "Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(1);
	}
    }

    public static void main(String args[]) {
	final Params params = new Params();
	params.readFromArgs(args);
	Utils.applyStaticParams(params);  // also in runner
	if (params.getboolean("visible")) {
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			checkVersion();
			createAndShowGUI(params);
		    }
		});
	}
	else {
	    checkVersion();
	    params.setSelections();
	    Runner runner = new Runner(params);
	    runner.start();
	    runner.end();
	}
    }
    
    private static void createAndShowGUI(Params params) {
        JFrame f = Utils.topLevelFrame = new JFrame("Maximum Entropy Species Distribution Modeling, Version " + Utils.version);
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	GUI gui = new GUI(params);
	//	f.setJMenuBar(gui.makeMenuBar());
	f.setContentPane(gui.createContentPane());
	gui.topLevelFrame = f;
	//	params.applyToInterface(gui);
	gui.applyParams();
	f.pack();
	//	f.setResizable(false);
	f.setVisible(true);
	if (params.getboolean("autoRun"))
	    gui.doRun(true);
    }
}



/*
Copyright (c) 2016 Steven Phillips, Miro Dudik and Rob Schapire

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions: 

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
*/

package density;

import gnu.getopt.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class View extends JFrame implements ActionListener {
    GridZoom zoom;
    Grid[] predictors = new Grid[0];
    Grid pred;

    static final private String LEFT = "left";
    static final private String RIGHT = "right";
    static final private String UP = "up";
    static final private String DOWN = "down";
    static final private String IN = "in";
    static final private String OUT = "out";
    static final private String CENTER = "center";
    static final private String MODEL = "model";

    public View(String[] args) {
	//	Zoom zoom = new Zoom(new Dimension(600,600));
	try {
	    pred = GridIO.readGrid(args[0]);
	    if (args.length>0) {
		String[] files = Utils.gridFileNames(args[1]);
		predictors = new Grid[files.length];
		for (int i=0; i<files.length; i++) {
		    System.out.println("Loading " + files[i]);
		    predictors[i] = GridIO.readGrid(files[i]);
		}
	    }
	} catch(IOException e) { System.exit(0); }
	zoom = new GridZoom(pred) {
		public void mouseClicked(int r, int c) {
		    for (int i=0; i<predictors.length; i++)
			System.out.print(predictors[i].getName() + ": " + (predictors[i].hasData(r,c) ? predictors[i].eval(r,c) : "NA") + "  ");
		    System.out.println();
		}};
	JToolBar toolBar = makeToolBar();
		
	JPanel displayPane = new JPanel();
	displayPane.setPreferredSize(new Dimension(800, 600));
	displayPane.setLayout(new BorderLayout());
	displayPane.add(toolBar, BorderLayout.PAGE_START);
	displayPane.add(zoom, BorderLayout.CENTER);
	final Container contentPane = getContentPane();
	contentPane.add(displayPane);
	pack();
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) { System.exit(0); }
	    });
	setVisible(true);
    }
    
    JToolBar makeToolBar() {
	JToolBar toolBar = new JToolBar("Navigation buttons");
	addButton(toolBar, "Back24", LEFT, "Shift left", "<");
	addButton(toolBar, "Up24", UP, "Shift up", "^");
	addButton(toolBar, "Down24", DOWN, "Shift down", "v");
	addButton(toolBar, "Forward24", RIGHT, "Shift right", ">");
	addButton(toolBar, "Plus", IN, "Zoom in", "+");
	addButton(toolBar, "Minus", OUT, "Zoom out", "-");
	addButton(toolBar, "Dot", CENTER, "Recenter", "@");
	if (predictors.length>0)
	    addButton(toolBar, "", MODEL, "Show model", "Model");
	for (int i=0; i<predictors.length; i++)
	    addButton(toolBar, "", "var_"+i, "Show predictor " + predictors[i].getName(), predictors[i].getName());
	return toolBar;
    }

    protected void addButton(JToolBar toolBar, String imageName, String actionCommand, String toolTipText, String altText) {
	//	String imgLocation = "images/" + imageName + ".gif";
	//	URL imageURL = ToolBarDemo.class.getResource(imgLocation);

	JButton button = new JButton();
	button.setActionCommand(actionCommand);
	button.setToolTipText(toolTipText);
	button.addActionListener(this);
	//	if (imageURL != null) {                      //image found
	//	    button.setIcon(new ImageIcon(imageURL, altText));
	//	} else {                                     //no image found
	    button.setText(altText);
	    //	    System.err.println("Resource not found: " + imgLocation);
	    //	}
	    toolBar.add(button);
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (LEFT.equals(cmd)) zoom.right(0.5);
	else if (RIGHT.equals(cmd)) zoom.right(-0.5);
	else if (UP.equals(cmd)) zoom.down(0.5);
	else if (DOWN.equals(cmd)) zoom.down(-0.5);
	else if (IN.equals(cmd)) zoom.adjustZoom(2.0);
	else if (OUT.equals(cmd)) zoom.adjustZoom(0.5);
	else if (CENTER.equals(cmd)) zoom.recenter(true);
	else if (MODEL.equals(cmd)) { zoom.setGrid(pred); zoom.makeImage(); }
	else if (cmd!=null && cmd.startsWith("var")) { 
	    int i = Integer.parseInt(cmd.replaceAll("var_", ""));
	    zoom.setGrid(predictors[i]); 
	    zoom.makeImage(); 
	}
    }

    public static void main(String args[]) {
	View view = new View(args);
    }
}

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

import javax.swing.*;
import java.beans.*; //Property change stuff
import java.awt.*;
import java.awt.event.*;

class MyProgressMonitor extends JDialog {
    private JOptionPane optionPane;
    private int max;
    private String prefix = "";
    private JLabel statusString = new JLabel();
    private JProgressBar progressBar;
    void setNote(String note) { statusString.setText(prefix + note); }
    void setPrefix(String s) { prefix = s; }
    void setProgress(int current) { 
	//	setNote(current + " out of " + max);
	progressBar.setValue(current);
    }
    private ActionListener listener = null;

    public void addActionListener(ActionListener l) { listener = l; }
    public void setMaximum (int m) { max = m; progressBar.setMaximum(m); }

    public MyProgressMonitor(Frame aFrame, String title, String initial, int max) {
        super(aFrame, false);  // doesn't freeze parent frame
	
	this.max = max;
        setTitle(title);
	setFocusableWindowState(false);
        progressBar = new JProgressBar(0, max);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
	statusString.setText(initial);

        Object[] array = {statusString, progressBar};
        Object[] options = {"Cancel"};

        optionPane = new JOptionPane(array, 
                                    JOptionPane.INFORMATION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
        setContentPane(optionPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (isVisible() 
                 && (e.getSource() == optionPane)
                 && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
                     prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
                    Object value = optionPane.getValue();

                    if (value == JOptionPane.UNINITIALIZED_VALUE) {
                        //ignore reset
                        return;
                    }

                    // Reset the JOptionPane's value.
                    // If you don't do this, then if the user
                    // presses the same button next time, no
                    // property change event will be fired.
                    optionPane.setValue(
                            JOptionPane.UNINITIALIZED_VALUE);

		    if (listener != null)
			listener.actionPerformed(new java.awt.event.ActionEvent(this, 0, "Cancel"));
		    setVisible(false);
		}
            }
        });
    }
}

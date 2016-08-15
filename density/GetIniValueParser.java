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

import java.io.*;
import java.util.*;
import java.util.*;

/**
* A class for handling Windows-style INI files. The file format is as 
* follows:  <dl>
*<dd>   [subject]       - anything beginning with [ and ending with ] is a subject 
*<dd>   ;comment        - anything beginning with a ; is a comment 
*<dd>   variable=value  - anything of the format string=string is an assignment 
*<dd>   comment         - anything that doesn't match any of the above is a comment 
*
* @author Steve DeGroof 
* @author <A HREF="http://www.mindspring.com/~degroof"><I>http://www.mindspring.com/~degroof</A></I>
* 
* @author ISMPified by Jeff Dillon
* @version 2.0
* 
* Original non-ismpified code at
* http://degroof.home.mindspring.com/java/
* 
* ISMPifying involved simply making all the methods throw 
* IOExceptions instead of doing System.err's for errors.
*
* @author Steven Phillips
* Made subjects and fields case insensitive, and getvalue(..) call trim()
* 
*/

public class GetIniValueParser extends Object {
    /**Actual text lines of the file stored in a vector.*/
    protected Vector lines;
    /**A vector of all subjects*/
    protected Vector subjects;
    /**A vector of variable name vectors grouped by subject*/
    protected Vector variables;
    /**A vector of variable value vectors grouped by subject*/
    protected Vector values;
    /**Name of the file*/
    protected String fileName;
    /**If true, INI file will be saved every time a value is changed. Defaults to false*/
    protected boolean saveOnChange = false;

    /**
    * Creates an INI file object using the specified name
    * If the named file doesn't exist, create one
    * @param name the name of the file
    */
    public GetIniValueParser(String name) throws IOException {
        this(name, false);
    }

    /**
    * Creates an INI file object using the specified name
    * If the named file doesn't exist, create one
    * @param name the name of the file
    * @param saveOnSet save file whenever a value is set
    */
    public GetIniValueParser(String name, boolean save) throws IOException {
        saveOnChange = save;
        fileName = name;
        if (!((new File(name)).exists())) {
            if (!createFile())
                return;
        }
        loadFile();
        parseLines();
    }

    /**
    * Loads and parses the INI file. Can be used to reload from file.
    */
    public void loadFile() throws IOException {
        //reset all vectors
        lines = new Vector();
        subjects = new Vector();
        variables = new Vector();
        values = new Vector();
        //open the file
        try {
            DataInputStream ini =
                new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            String line = "";
            //read all the lines in
            while (true) {
                line = ini.readLine();
                if (line == null)
                    break;
                else
                    lines.addElement(line.trim());
            }
            ini.close();
        } catch (IOException e) {
            throw new IOException("IniFile load failed: " + e.getMessage());
        }
    }

    /**
    * Create a new INI file.
    */
    protected boolean createFile() throws IOException {
        try {
            DataOutputStream newFile = new DataOutputStream(new FileOutputStream(fileName));
            newFile.writeBytes(";INI File: " + fileName + System.getProperty("line.separator"));
            newFile.close();
            return true;
        } catch (IOException e) {
            throw new IOException("IniFile create failed: " + e.getMessage());
        }
    }

    /**
    * Reads lines, filling in subjects, variables and values.
    */
    protected void parseLines() throws IOException {
        String currentLine = null; //current line being parsed
        String currentSubject = null; //the last subject found
        for (int i = 0; i < lines.size(); i++) //parse all lines
            {
            currentLine = (String) lines.elementAt(i);
            if (isaSubject(currentLine)) //if line is a subject, set currentSubject
                {
		    currentSubject = currentLine.substring(1, currentLine.length() - 1).toLowerCase();
		} else if (isanAssignment(currentLine)) //if line is an assignment, add it
                {
                String assignment = currentLine;
                addAssignment(currentSubject, assignment);
            }
        }
    }

    /**
    * Adds and assignment (i.e. "variable=value") to a subject.
    */
    protected boolean addAssignment(String subject, String assignment) throws IOException {
        String value;
        String variable;
        int index = assignment.indexOf("=");
        variable = assignment.substring(0, index).toLowerCase();
        value = assignment.substring(index + 1, assignment.length());
        if ((value.length() == 0) || (variable.length() == 0))
            return false;
        else
            return addValue(subject, variable, value, false);
    }

    /**
    * Sets a specific subject/variable combination the given value. If the subject
    * doesn't exist, create it. If the variable doesn't exist, create it. If 
    * saveOnChange is true, save the file;
    * @param subject the subject heading (e.g. "Widget Settings")
    * @param variable the variable name (e.g. "Color")
    * @param value the value of the variable (e.g. "green")
    * @return true if successful
    */
    public boolean setValue(String subject, String variable, String value) throws IOException {
        boolean result = addValue(subject, variable, value, true);
        if (saveOnChange)
            saveFile();
        return result;
    }

    /**
    * Sets a specific subject/variable combination the given value. If the subject
    * doesn't exist, create it. If the variable doesn't exist, create it.
    * @param subject the subject heading (e.g. "Widget Settings")
    * @param variable the variable name (e.g. "Color")
    * @param value the value of the variable (e.g. "green")
    * @param addToLines add the information to the lines vector
    * @return true if successful
    */
    protected boolean addValue(String subject, String variable, String value, boolean addToLines)
        throws IOException {
        //if no subject, quit
        if ((subject == null) || (subject.length() == 0))
            return false;

        //if no variable, quit
        if ((variable == null) || (variable.length() == 0))
            return false;

        //if the subject doesn't exist, add it to the end
        if (!subjects.contains(subject)) {
            subjects.addElement(subject);
            variables.addElement(new Vector());
            values.addElement(new Vector());
        }

        //set the value, if the variable doesn't exist, add it to the end of the subject
        int subjectIndex = subjects.indexOf(subject);
        Vector subjectVariables = (Vector) (variables.elementAt(subjectIndex));
        Vector subjectValues = (Vector) (values.elementAt(subjectIndex));
        if (!subjectVariables.contains(variable)) {
            subjectVariables.addElement(variable);
            subjectValues.addElement(value);
        }
        int variableIndex = subjectVariables.indexOf(variable);
        subjectValues.setElementAt(value, variableIndex);

        //add it to the lines vector?
        if (addToLines)
            setLine(subject, variable, value);

        return true;
    }

    /**
    * does the line represent a subject?
    * @param line a string representing a line from an INI file
    * @return true if line is a subject
    */
    protected boolean isaSubject(String line) {
        return (line.startsWith("[") && line.endsWith("]"));
    }

    /**
    * set a line in the lines vector 
    * @param subject the subject heading (e.g. "Widget Settings")
    * @param variable the variable name (e.g. "Color")
    * @param value the value of the variable (e.g. "green")
    */
    protected void setLine(String subject, String variable, String value) {
        //find the line containing the subject
        int subjectLine = findSubjectLine(subject);
        if (subjectLine == -1) {
            addSubjectLine(subject);
            subjectLine = lines.size() - 1;
        }
        //find the last line of the subject
        int endOfSubject = endOfSubject(subjectLine);
        //find the assignment within the subject
        int lineNumber = findAssignmentBetween(variable, subjectLine, endOfSubject);

        //if an assignment line doesn't exist, insert one, else change the existing one
        if (lineNumber == -1)
            lines.insertElementAt(variable + "=" + value, endOfSubject);
        else
            lines.setElementAt(variable + "=" + value, lineNumber);
    }

    /**
    * find the line containing a variable within a subject
    * @param subject the subject heading (e.g. "Widget Settings")
    * @param variable the variable name (e.g. "Color")
    * @return the line number of the assignment, -1 if not found
    */
    protected int findAssignmentLine(String subject, String variable) {
        int start = findSubjectLine(subject);
        int end = endOfSubject(start);
        return findAssignmentBetween(variable, start, end);
    }

    /**
    * find the line containing a variable within a range of lines
    * @param variable the variable name (e.g. "Color")
    * @param start the start of the range (inclusive)
    * @param end the end of the range (exclusive)
    * @return the line number of the assignment, -1 if not found
    */
    protected int findAssignmentBetween(String variable, int start, int end) {
        for (int i = start; i < end; i++) {
            if (((String) lines.elementAt(i)).startsWith(variable + "="))
                return i;
        }
        return -1;
    }

    /**
    * add a subject line to the end of the lines vector
    * @param subject the subject heading (e.g. "Widget Settings")
    */
    protected void addSubjectLine(String subject) {
        lines.addElement("[" + subject + "]");
    }

    /**
    * find a subject line within the lines vector
    * @param subject the subject heading (e.g. "Widget Settings")
    * @return the line number of the subject, -1 if not found
    */
    protected int findSubjectLine(String subject) {
        String line;
        String formattedSubject = "[" + subject + "]";
        for (int i = 0; i < lines.size(); i++) {
            line = (String) lines.elementAt(i);
            if (formattedSubject.equals(line))
                return i;
        }
        return -1;
    }

    /**
    * find the line number which is 1 past the last assignment in a subject
    * starting at a given line
    * @param start the line number at which to start looking
    * @return the line number of the last assignment + 1
    */
    protected int endOfSubject(int start) {
        int endIndex = start + 1;
        if (start >= lines.size())
            return lines.size();
        for (int i = start + 1; i < lines.size(); i++) {
            if (isanAssignment((String) lines.elementAt(i)))
                endIndex = i + 1;
            if (isaSubject((String) lines.elementAt(i)))
                return endIndex;
        }
        return endIndex;
    }

    /**
    * does the line represent an assignment?
    * @param line a string representing a line from an INI file
    * @return true if line is an assignment
    */
    protected boolean isanAssignment(String line) {
        if ((line.indexOf("=") != -1) && (!line.startsWith(";")))
            return true;
        else
            return false;
    }

    /**
    * get a copy of the lines vector
    */
    public Vector getLines() {
        return (Vector) lines.clone();
    }

    /**
    * get a vector containing all variables in a subject
    * @param subject the subject heading (e.g. "Widget Settings")
    * @return a list of variables, empty vector if subject not found
    */
    public String[] getVariables(String subject) {
        String[] v;
        int index = subjects.indexOf(subject);
        if (index != -1) {
            Vector vars = (Vector) (variables.elementAt(index));
            v = new String[vars.size()];
            vars.copyInto(v);
            return v;
        } else {
            v = new String[0];
            return v;
        }
    }

    /**
    * get an array containing all subjects
    * @return a list of subjects
    */
    public String[] getSubjects() {
        String[] s = new String[subjects.size()];
        subjects.copyInto(s);
        return s;
    }

    /**
    * get the value of a variable within a subject
    * @param subject the subject heading (e.g. "Widget Settings")
    * @param variable the variable name (e.g. "Color")
    * @return the value of the variable (e.g. "green"), empty string if not found
    */
    public String getValue(String subject, String variable) {
	subject = subject.toLowerCase();
	variable = variable.toLowerCase();
        int subjectIndex = subjects.indexOf(subject);
        if (subjectIndex == -1)
            return "";
        Vector valVector = (Vector) (values.elementAt(subjectIndex));
        Vector varVector = (Vector) (variables.elementAt(subjectIndex));
        int valueIndex = varVector.indexOf(variable);
        if (valueIndex != -1) {
            return ((String) (valVector.elementAt(valueIndex))).trim();
        }
        return "";
    }

    /**
    * delete variable within a subject
    * @param subject the subject heading (e.g. "Widget Settings")
    * @param variable the variable name (e.g. "Color")
    */
    public void deleteValue(String subject, String variable) throws IOException {
        int subjectIndex = subjects.indexOf(subject);
        if (subjectIndex == -1)
            return;

        Vector valVector = (Vector) (values.elementAt(subjectIndex));
        Vector varVector = (Vector) (variables.elementAt(subjectIndex));

        int valueIndex = varVector.indexOf(variable);
        if (valueIndex != -1) {
            //delete from variables and values vectors
            valVector.removeElementAt(valueIndex);
            varVector.removeElementAt(valueIndex);
            //delete from lines vector
            int assignmentLine = findAssignmentLine(subject, variable);
            if (assignmentLine != -1) {
                lines.removeElementAt(assignmentLine);
            }
            //if the subject is empty, delete it
            if (varVector.size() == 0) {
                deleteSubject(subject);
            }
            if (saveOnChange)
                saveFile();
        }
    }

    /**
    * delete a subject and all its variables
    * @param subject the subject heading (e.g. "Widget Settings")
    */
    public void deleteSubject(String subject) throws IOException {
        int subjectIndex = subjects.indexOf(subject);
        if (subjectIndex == -1)
            return;
        //delete from subjects, variables and values vectors
        values.removeElementAt(subjectIndex);
        variables.removeElementAt(subjectIndex);
        subjects.removeElementAt(subjectIndex);
        //delete from lines vector
        int start = findSubjectLine(subject);
        int end = endOfSubject(start);
        for (int i = start; i < end; i++) {
            lines.removeElementAt(start);
        }
        if (saveOnChange)
            saveFile();
    }

    /**
    * save the lines vector back to the INI file
    */
    public void saveFile() throws IOException {
        try {
            DataOutputStream outFile =
                new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            for (int i = 0; i < lines.size(); i++) {
                outFile.writeBytes(
                    (String) (lines.elementAt(i)) + System.getProperty("line.separator"));
            }
            outFile.close();
        } catch (IOException e) {
            throw new IOException("IniFile save failed: " + e.getMessage());
        }
    }

    /**
    * clean up
    */
    protected void finalize() throws IOException {
        saveFile();
    }

}

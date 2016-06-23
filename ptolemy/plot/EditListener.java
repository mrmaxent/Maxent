/* Interface for listeners that are informed of plot edit events.

Copyright (c) 1998-2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/
package ptolemy.plot;


//////////////////////////////////////////////////////////////////////////
//// EditListener

/**
   Interface for listeners that are informed of plot edit events.
   These events are generated when a user modifies the plot data using
   interactive facilities of an editable subclass of Plot.

   @author  Edward A. Lee
   @version $Id: EditListener.java,v 1.12 2005/03/01 01:00:40 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
   @see EditablePlot

*/
public interface EditListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify that data in the specified plot has been modified
     *  by a user edit action.
     *  @param source The plot containing the modified data.
     *  @param dataset The data set that has been modified.
     */
    public void editDataModified(EditablePlot source, int dataset);
}

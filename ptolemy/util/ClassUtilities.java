/* Utilities used to manipulate classes

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ClassUtilities

/**
 A collection of utilities for manipulating classes.
 These utilities do not depend on any other ptolemy.* packages.


 @author Christopher Hylands
 @version $Id: ClassUtilities.java,v 1.18.2.1 2005/07/14 20:44:06 cxh Exp $
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class ClassUtilities {
    /** Instances of this class cannot be created.
     */
    private ClassUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Lookup a jar URL and return the resource.

     *  A resource is a file such as a class file or image file that
     *  is found in the classpath.  A jar URL is a URL that refers to
     *  a resource in a jar file.  For example,
     *  <code>file://./foo.jar!/a/b/c.class</code> is a jar URL that
     *  refers to the <code>a/b/c.class</code> resource in
     *  <code>foo.jar</code>.  If this method is called with
     *  <code>file://./foo.jar!/a/b/c.class</code> then it will return
     *  <code>a/b/c.class</code> if <code>a/b/c.class</code> can be
     *  found as a resource in the class loader that loaded this class
     *  (ptolemy.util.ClassUtilities).  If the resource cannot be found,
     *  then an IOException is thrown. If the jarURLString parameter
     *  does not contain <code>!/</code>, then return null.
     *  Note that everything before the <code>!/</code> is removed before
     *  searching the classpath.
     *
     *  <p>This method is necessary because Web Start uses jar URL, and
     *  there are some cases where if we have a jar URL, then we may
     *  need to strip off the jar:<i>url</i>!/ part so that we can
     *  search for the {entry} as a resource.
     *
     *  @param jarURLString The string containing the jar URL.
     *  @return The resource, if any.If the spec string does not
     *  contain <code>!/</code>, then return null.
     *  @exception IOException If this method cannot convert the specification
     *  to a URL.
     *  @see java.net.JarURLConnection
     */
    public static URL jarURLEntryResource(String jarURLString)
            throws IOException {
        // At first glance, it would appear that this method could appear
        // in specToURL(), but the problem is that specToURL() creates
        // a new URL with the spec, so it only does further checks if
        // the URL is malformed.  Unfortunately, in Web Start applications
        // the URL will often refer to a resource in another jar file,
        // which means that the jar url is not malformed, but there is
        // no resource by that name.  Probably specToURL() should return
        // the resource after calling new URL().
        int jarEntry = jarURLString.indexOf("!/");

        if (jarEntry == -1) {
            jarEntry = jarURLString.indexOf("!\\");
            if (jarEntry == -1) {
                return null;
            }
        }
        
        try {
            // !/ means that this could be in a jar file.
            String entry = jarURLString.substring(jarEntry + 2);

            // We might be in the Swing Event thread, so
            // Thread.currentThread().getContextClassLoader()
            // .getResource(entry) probably will not work.
            Class refClass = Class.forName("ptolemy.util.ClassUtilities");
            URL entryURL = refClass.getClassLoader().getResource(entry);
            return entryURL;
        } catch (Exception ex) {
            // IOException constructor does not take a cause, so we add it.
            IOException ioException = new IOException("Cannot find \""
                    + jarURLString + "\".");
            ioException.initCause(ex);
            throw ioException;
        }
    }

    /** Given a dot separated classname, return the jar file or directory
     *  where the class can be found.
     *  @param necessaryClass  The dot separated class name, for example
     *  "ptolemy.util.ClassUtilities"
     *  @return If the class can be found as a resource, return the
     *  directory or jar file where the necessary class can be found.
     *  otherwise, return null.  If the resource is found in a directory,
     *  then the return value will always have forward slashes, it will
     *  never use backslashes.
     */
    public static String lookupClassAsResource(String necessaryClass) {
        // This method is called from copernicus.kernel.GeneratorAttribute
        // and actor.lib.python.PythonScript.  We moved it here
        // to avoid dependencies.
        String necessaryResource = StringUtilities.substitute(necessaryClass,
                ".", "/")
                + ".class";

        URL necessaryURL = Thread.currentThread().getContextClassLoader()
                .getResource(necessaryResource);

        if (necessaryURL != null) {
            String resourceResults = necessaryURL.getFile();

            // Strip off the file:/ and the necessaryResource.
            if (resourceResults.startsWith("file:/")) {
                resourceResults = resourceResults.substring(6);
            }

            // Strip off the name of the resource we were looking for
            // so that we are left with the directory or jar file
            // it is in
            resourceResults = resourceResults.substring(0, resourceResults
                    .length()
                    - necessaryResource.length());

            // Strip off the trailing !/
            if (resourceResults.endsWith("!/")) {
                resourceResults = resourceResults.substring(0, resourceResults
                        .length() - 2);
            }

            // Unfortunately, under Windows, URL.getFile() may
            // return things like /c:/ptII, so we create a new
            // File and get its path, which will return c:\ptII
            File resourceFile = new File(resourceResults);

            // Convert backslashes
            String sanitizedResourceName = StringUtilities.substitute(
                    resourceFile.getPath(), "\\", "/");
            return sanitizedResourceName;
        }

        return null;
    }
}

/**
  * LETest.java
  *
  * copyright (c) 1998-2005 Roedy Green, Canadian Mind Products
  * #327 - 964 Heywood Avenue
  * Victoria, BC Canada V8V 2Y5
  * tel: (250) 361-9093
  * mailto:roedyg@mindprod.com
  * http://mindprod.com
  *
  * Version 1.0 1998 January 6
  *         1.1 1998 January 7 - officially implements DataInput
  *         1.2 1998 January 9 - add LERandomAccessFile
  *         1.3 1998 August 27
  *         1.4 1998 November 10 - add new address and phone.
  *         1.5 1999 October 8 - use com.mindprod.ledatastream package name.
  *
  * Tests Little Endian LEDataInputStream and LEDataOutputStream
  * and demonstrates the use of its methods.
  *
  * Output should look like this:
  *
  * 44
  * -1
  * a
  * -249
  * -123456789012
  * -649
  * -749
  * true
  * 3.14
  * 4.14
  * echidna
  * kangaroo
  * dingo
  *
  * Then repeated.
  *
  */
package com.mindprod.ledatastream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LETest
   {

   public static void main (String[] args)
      {

      // Write little-endian binary data into a sequential file

      // O P E N
      FileOutputStream fos;
      try
         {
         fos = new FileOutputStream("C:/temp/temp.dat", false /* append */);
         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException opening LEDataOutputStream");
         return;
         }

      LEDataOutputStream ledos = new LEDataOutputStream(fos);
      
      // W R I T E
      try
         {
         ledos.writeByte((byte)44);
         ledos.writeByte((byte)0xff);
         ledos.writeChar('a');
         ledos.writeInt(-249);
         ledos.writeLong(-123456789012L);
         ledos.writeShort((short)-649);
         ledos.writeShort((short)-749);
         ledos.writeBoolean(true);
         ledos.writeDouble(3.14D);
         ledos.writeFloat(4.14F);
         ledos.writeUTF("echidna");
         ledos.writeBytes("kangaroo" /* string -> LSB 8-bit */);
         ledos.writeChars("dingo" /* string 16-bit Unicode */);
         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException writing LEDataOutputStream");
         return;
         }

      // C L O S E
      try
         {
         ledos.close();
         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException closing LEDataOutputStream");
         return;
         }

      // Read little-endian binary data from a sequential file
      // import java.io.*;

      // O P E N
      FileInputStream fis;
      try
         {
         fis = new FileInputStream("C:/temp/temp.dat");
         }
      catch ( FileNotFoundException e )
         {
         System.out.println("Unexpected IOException opening LEDataInputStream");
         return;
         }
      LEDataInputStream ledis = new LEDataInputStream(fis);

      // R E A D
      try
         {
         byte b = ledis.readByte();
         System.out.println(b);
         byte ub = (byte) ledis.readUnsignedByte();
         System.out.println(ub);
         char c = ledis.readChar();
         System.out.println(c);
         int j = ledis.readInt();
         System.out.println(j);
         long l = ledis.readLong();
         System.out.println(l);
         short s = ledis.readShort();
         System.out.println(s);
         short us = (short) ledis.readUnsignedShort();
         System.out.println(us);
         boolean q = ledis.readBoolean();
         System.out.println(q);
         double d = ledis.readDouble();
         System.out.println(d);
         float f = ledis.readFloat();
         System.out.println(f);
         String u = ledis.readUTF();
         System.out.println(u);
         byte[] ba = new byte[8];
         ledis.readFully(ba, 0 /* offset in ba */, ba.length /* bytes to read */);
         System.out.println(new String(ba));
         /* there is no readChars method */
         c = ledis.readChar();
         System.out.print(c);
         c = ledis.readChar();
         System.out.print(c);
         c = ledis.readChar();
         System.out.print(c);
         c = ledis.readChar();
         System.out.print(c);
         c = ledis.readChar();
         System.out.println(c);

         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException reading LEDataInputStream");
         return;
         }

      // C L O S E
      try
         {
         ledis.close();
         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException closing LEDataInputStream");
         return;
         }
      // Write little endian data to a random access files

      // O P E N
      LERandomAccessFile leraf;
      try
         {
         leraf = new LERandomAccessFile("C:/temp/rand.dat", "rw" /* read/write */);
         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException creating LERandomAccessFile");

         return;
         }

      try
         {
         // W R I T E
         leraf.seek(0 /* byte offset in file*/);
         leraf.writeByte((byte)44);
         leraf.writeByte((byte)0xff);
         leraf.writeChar('a');
         leraf.writeInt(-249);
         leraf.writeLong(-123456789012L);
         leraf.writeShort((short)-649);
         leraf.writeShort((short)-749);
         leraf.writeBoolean(true);
         leraf.writeDouble(3.14D);
         leraf.writeFloat(4.14F);
         leraf.writeUTF("echidna");
         leraf.writeBytes("kangaroo" /* string -> LSB 8-bit */);
         leraf.writeChars("dingo" /* string 16-bit Unicode */);
         leraf.seek(0 /* byte offset in file*/);

         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException writing LERandomAccessFile");
         return;
         }

      try
         {
         // R E A D
         byte b = leraf.readByte();
         System.out.println(b);
         byte ub = (byte) leraf.readUnsignedByte();
         System.out.println(ub);
         char c = leraf.readChar();
         System.out.println(c);
         int j = leraf.readInt();
         System.out.println(j);
         long l = leraf.readLong();
         System.out.println(l);
         short s = leraf.readShort();
         System.out.println(s);
         short us = (short) leraf.readUnsignedShort();
         System.out.println(us);
         boolean q = leraf.readBoolean();
         System.out.println(q);
         double d = leraf.readDouble();
         System.out.println(d);
         float f = leraf.readFloat();
         System.out.println(f);
         String u = leraf.readUTF();
         System.out.println(u);
         byte[] ba = new byte[8];
         leraf.readFully(ba, 0 /* offset in ba */, ba.length /* bytes to read */);
         System.out.println(new String(ba));
         /* there is no readChars method */
         c = leraf.readChar();
         System.out.print(c);
         c = leraf.readChar();
         System.out.print(c);
         c = leraf.readChar();
         System.out.print(c);
         c = leraf.readChar();
         System.out.print(c);
         c = leraf.readChar();
         System.out.println(c);

         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException reading LERandomAccessFile");
         return;
         }

      // C L O S E
      try
         {
         leraf.close();
         }
      catch ( IOException e )
         {
         System.out.println("Unexpected IOException closing LERandomAccessFile");
         return;
         }

      } // end main
   } // end class LETest

// *****************************************************************************
//
// Copyright (c) 2011 Christian Meilicke (University of Mannheim)
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
// IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************************************

package de.unima.alcomox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public class Tools {
	
	public static String toDecimalFormat(double value) {
		DecimalFormat df = new DecimalFormat("0.000");
		return df.format(value).replace(',', '.');
	}
	
	public static String toDecimalFormat(double value, String pattern) {
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(value).replace(',', '.');
	}
	
	public static void deactivateLogger() {
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
		logger.setLevel(java.util.logging.Level.SEVERE);
	}

	public static void copyFile(String in, String out) throws IOException  {
		copyFile(new File(in), new File(out));
	}
	
	public static void writeToFile(String filepath, String output) {
		File outFile = new File(filepath);
        FileWriter out;
		try {
			out = new FileWriter(outFile);
	        out.write(output);
	        out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}


	}
	
	public static void copyFile(File in, File out) throws IOException  {
		FileChannel inChannel = new
		FileInputStream(in).getChannel();
		FileChannel outChannel = new
		FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} 
		catch (IOException e) { throw e; }
		finally {
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
		}
	}

}

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

package de.unima.alcomox.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import de.unima.alcomox.exceptions.CorrespondenceException;
import de.unima.alcomox.exceptions.MappingException;


/**
* A mapping reader for txt format reads mappings from txt-files. 
*
*/
public class MappingReaderTxt implements MappingReader {
	
	public MappingReaderTxt() {	}
	
	public Mapping getMapping(String filepath) throws MappingException {
		File file = new File(filepath);
		Mapping mapping = new Mapping();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			ArrayList<Correspondence> correspondences = new ArrayList<Correspondence>();
			String[] parts;
			Correspondence correspondence;
			String line = "";
			double sim;
			try {
				
				while ((line = reader.readLine())!= null) {
					// System.out.println("---> " + line);
					if (line.length() == 0 || line.startsWith("#")) {
						// do nothing if line is empty or comment	
					}
					
					else if (line.contains("|") && (!line.startsWith("#"))) {
						// System.out.println("yyy" + line);
						parts = line.split(" .{1,2} ");
						sim = Double.parseDouble(parts[2]);
						SemanticRelation sr = null;
						if (line.contains(" = ")) { sr = new SemanticRelation(SemanticRelation.EQUIV); }
						else if (line.contains(" < ")) { sr = new SemanticRelation(SemanticRelation.SUB); }
						else if (line.contains(" > ")) { sr = new SemanticRelation(SemanticRelation.SUPER); }
						else if (line.contains(" != ")) { sr = new SemanticRelation(SemanticRelation.DIS); }
						else { sr = new SemanticRelation(SemanticRelation.NA); }
						correspondence = new Correspondence(parts[0].trim(), parts[1].trim(), sr, sim);
						correspondences.add(correspondence);
					}
					else {
						throw new MappingException(
								MappingException.INVALID_FORMAT,
								"format of " + file.toString() + " is invalid (see line '" + line + "')"
						);						
						
					}
				}
			}
			catch (CorrespondenceException ce) {
				throw new MappingException(
						MappingException.CORRESPONDENCE_PROBLEM,
						"correspondences in " + file.toString() + " is invalid",
						ce
				);
			}
			catch (NumberFormatException nfe) {
				throw new MappingException(
						MappingException.IO_ERROR,
						"caused by " + file.toString() + " where a number has been expected",
						nfe
				);
			}
			catch (IOException ioe) {
				throw new MappingException(
						MappingException.IO_ERROR,
						"file at " + file.toString() + " cannot be accessed",
						ioe
				);
			}
			catch (ArrayIndexOutOfBoundsException ae) {
				throw new MappingException(
						MappingException.IO_ERROR,
						"line " + line + " in file " + file.toString() + " has bad format (check missing # | or whitespace)",
						ae
				);
			}
			mapping.setCorrespondences(correspondences);	
		}
		catch (FileNotFoundException e) {
			throw new MappingException(
					MappingException.IO_ERROR,
					"file at " + file.toString() + " not found",
					e
			);
		}
		return mapping;
	}
	
	
}

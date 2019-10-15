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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;


import de.unima.alcomox.exceptions.MappingException;
import de.unima.alcomox.ontology.ConflictPair;

/**
* A MappingWriterXml writes Mappings to xml-files. 
*
*/
public class MappingWriterXml implements MappingWriter {
	
	private boolean extended = false;
	
	public MappingWriterXml() { }
	
	protected void disableExtendedStyle() {
		this.extended = false;
	}
	
	protected void enableExtendedStyle() {
		this.extended = true;
	}
	
	public void writeMapping(String filepath, Mapping mapping) throws MappingException {
		File file = new File(filepath);
		try {
			file.createNewFile();
			BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));
			fw.write(getXMLString(mapping));
			fw.flush();
			fw.close();
		}
		catch (IOException e) {
			throw new MappingException(MappingException.IO_ERROR, "could not create/access file " + file);
		}
		
	}
	
	private String getXMLString(Mapping mapping) {
		StringBuffer mappingXML = new StringBuffer();
		ArrayList<Correspondence> correspondences = mapping.getCorrespondences();
		mappingXML.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		// mappingXML.append("<!DOCTYPE rdf:RDF SYSTEM \"align.dtd\">\n");
		mappingXML.append("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment\" ");
		mappingXML.append("\n\t xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ");
		mappingXML.append("\n\t xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n\n");		
		mappingXML.append("<Alignment>\n");
		mappingXML.append("<xml>yes</xml>\n");
		mappingXML.append("<level>0</level>\n");
		mappingXML.append("<type>??</type>\n\n");
		for (Correspondence c : correspondences) { mappingXML.append(getMappedCell(c)); }
		if (extended) {
			Set<ConflictPair> cps = mapping.getConflictPairs();
			int i = -1, j = -1, previous_i = -1;
			mappingXML.append("\t<conflicts>\n");
			for (ConflictPair cp : cps) {
				i = cp.getCorrespondence1().getId();
				j = cp.getCorrespondence2().getId();
				if (previous_i != i) {
					if (previous_i >= 0) {
						mappingXML.append("\t\t</correspondence>\n");
					}
					mappingXML.append("\t\t<correspondence cid=\"" + i + "\">\n");
				}
				mappingXML.append("\t\t\t<conflictswith cid=\"" + j + "\"/>\n"); 
				previous_i = i;
			}
			if (cps.size() > 0) {
				mappingXML.append("\t\t</correspondence>\n");
			}
			mappingXML.append("\t</conflicts>\n");
		}
		mappingXML.append("\n</Alignment>\n");
		mappingXML.append("</rdf:RDF>\n");
		return mappingXML.toString();
	}
	
	private StringBuffer getMappedCell(Correspondence correspondence) {
		StringBuffer mappedCell = new StringBuffer();
		mappedCell.append("<map>\n");
		if (!(this.extended)) {
			mappedCell.append("\t<Cell>\n");	
		}
		else {
			mappedCell.append("\t<Cell cid=\"" + correspondence.getId() + "\">\n");	
		}
		mappedCell.append("\t\t<entity1 rdf:resource=\"" + correspondence.getSourceEntityUri() + "\"/>\n");
		mappedCell.append("\t\t<entity2 rdf:resource=\"" + correspondence.getTargetEntityUri() + "\"/>\n");
		mappedCell.append("\t\t<measure rdf:datatype=\"xsd:float\">" + correspondence.getConfidence() + "</measure>\n");
        mappedCell.append("\t\t<relation>" + mask(correspondence.getRelation().toString()) + "</relation>\n");
		mappedCell.append("\t</Cell>\n");
		mappedCell.append("</map>\n");
		return mappedCell;
	}
	
	
	private String mask(String r) {
		if (r.equals("<")) { return "&lt;"; }
		else if (r.equals(">")) { return "&gt;"; }
		else { return r; }
	}

}


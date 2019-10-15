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


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import de.unima.alcomox.exceptions.CorrespondenceException;
import de.unima.alcomox.exceptions.MappingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
* A mapping reader for rdf/xml format reads mappings from rdf-files. The format supported is the
* one described in the Alignment API. But notice that not all kinds of specifications are supported.
* More precise it should work with any level 0 mapping, where the relations =, < and > are used
* to state correspodences between concepts or properties.
*/
public class MappingReaderXml extends DefaultHandler implements MappingReader {
    
	
	private String entity1;
	private String entity2;
	
	private String relation;
	private boolean readRelation;
	private String confidence;
	private boolean readConfidence;
	private ArrayList<Correspondence> correspondences;
	private MappingException internalException = null;
	private String filepathOrUri;
	
	
	public MappingReaderXml() {
		this.confidence = "";
	}

	public Mapping getMapping(String filepathOrUri) throws MappingException {
		this.readRelation = false;
		this.readConfidence = false;
		this.correspondences = new ArrayList<Correspondence>();   
		
		Mapping mapping = new Mapping();
		SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        
        URI uri =  null;
        File file = null;
        this.filepathOrUri = filepathOrUri;
       	try { uri = new URI(this.filepathOrUri); }
       	catch (URISyntaxException e1) { file = new File(this.filepathOrUri); }
     
       	
		try {
			saxParser = factory.newSAXParser();
			if (uri != null) {
				saxParser.parse(uri.toString(), this);
			}
			else {
				saxParser.parse(file, this);
			}
		}
		catch (ParserConfigurationException e) {
			throw new MappingException(
				MappingException.IO_ERROR,
				"caused by parsing " + filepathOrUri.toString() + " (ParserConfigurationException)",
				e
			);
		}
		catch (SAXException e) {
			// System.out.println(e.getMessage());
			throw new MappingException(
				MappingException.IO_ERROR,
				"caused by parsing " + filepathOrUri.toString() + " (SAXException)",
				e
			);
		}
		catch (IOException e) {
			throw new MappingException(
				MappingException.IO_ERROR,
				"caused by parsing " + filepathOrUri.toString(),
				e
			);
		}
		if (this.internalException != null) { throw internalException; }
		mapping.setCorrespondences(correspondences);
		return mapping;
	}

    public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
    	if (qName.toLowerCase().equals("entity1")) {
    		this.entity1 = attrs.getValue("rdf:resource");
    	}
    	if (qName.toLowerCase().equals("entity2")) {
    		this.entity2 = attrs.getValue("rdf:resource");
    	}
    	if (qName.toLowerCase().equals("relation")) {
    		this.readRelation = true;
    	}
    	if (qName.toLowerCase().equals("measure")) {
    		this.readConfidence = true;
    	}
    }
    
    public void characters(char buf[], int offset, int len) throws SAXException {
        if (this.readRelation) {
        	this.relation = new String(buf, offset, len);	
        }
        if (this.readConfidence) {
        	this.confidence += new String(buf, offset, len);
        }        
        
    }    

    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
    	if (qName.toLowerCase().equals("cell")) {
    		float sim = (float)Double.parseDouble(this.confidence);
    		if (sim < 0.0 || sim > 1.0) {
    			BigDecimal d = new BigDecimal(this.confidence);
    			sim = d.floatValue();
    		}
    		this.confidence = "";
    		Correspondence correspondence;
    		
    		int relationType = SemanticRelation.NA;
    		if (this.relation == null) { relationType = SemanticRelation.EQUIV; }
    		else {
	    		if (this.relation.equals("=")) { relationType = SemanticRelation.EQUIV; }
	    		if (this.relation.equals("\"=\"")) { relationType = SemanticRelation.EQUIV; }
	    		if (this.relation.startsWith("fr.inrialpes.exmo.align.impl.rel.EquivRelation")) { relationType = SemanticRelation.EQUIV; }
	    		if (this.relation.equals("<")) { relationType = SemanticRelation.SUB; }
	    		if (this.relation.equals(">")) { relationType = SemanticRelation.SUPER; }
    		}
    		SemanticRelation semanticRelation;
			try {
				semanticRelation = new SemanticRelation(relationType);
				// sim = (float) ((double)sim / 100.0d);
				correspondence = new Correspondence(this.entity1 , this.entity2, semanticRelation, (double)sim);
				correspondences.add(correspondence);
			}
			catch (CorrespondenceException e) {
				// System.out.println(this.relation);
				this.internalException = new MappingException(
					MappingException.IO_ERROR,
					"caused by parsing " + this.filepathOrUri + " (SAXException)",
					e
				);
			}	
    	}
    	this.readConfidence = false;
    	this.readRelation = false;
    }
    
    


}

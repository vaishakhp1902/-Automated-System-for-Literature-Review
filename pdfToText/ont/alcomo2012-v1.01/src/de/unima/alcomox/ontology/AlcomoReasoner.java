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

package de.unima.alcomox.ontology;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.util.AlcomoLogger;

/**
* This abstract class represents a reasoner used in the alcomo system.
*
*/
public abstract class AlcomoReasoner {
	
	protected AlcomoLogger log;

	protected LocalOntology sourceOntology;
	protected LocalOntology targetOntology;
	protected ExtractionProblem extractionProblem;
	
	public AlcomoReasoner(LocalOntology sourceOntology, LocalOntology targetOntology, ExtractionProblem extractionProblem2) {
		this.sourceOntology = sourceOntology;
		this.targetOntology = targetOntology;
		this.extractionProblem = extractionProblem2;
		this.log = new AlcomoLogger(this.getClass());
		
	}
	
	
}

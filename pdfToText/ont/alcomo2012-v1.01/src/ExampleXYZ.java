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

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Characteristic;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.IOntology;



/**
* This example illustrates the usage of ALCOMO to repair an
* incoherent alignment. It is shows a standard usage of the system.
* 
*/
public class ExampleXYZ {
	


	public static void main(String[] args) throws AlcomoException {
		
		// relative or absolute path to the ontologies and alignment 
		String ont1Path  = "/home/godass69/Desktop/t/Phish.owl";
		String ont2Path  = "/home/godass69/Desktop/t/both.owl";
		String alignPath = "testdata/conference/alignments/CSA-cmt-ekaw.rdf";
		// if available, you can compare against the reference alignment
		String refPath   = "testdata/conference/references/cmt-ekaw.rdf";
		
		// we ant to use Pellet as reasoner (alternatively use HERMIT)
		//Settings.BLACKBOX_REASONER = Settings.BlackBoxReasoner.PELLET;
		
		// if you want to force to generate a one-to-one alignment add this line
		// by default its set to false
		Settings.ONE_TO_ONE = true;
		
		// load ontologies as IOntology (uses fast indexing for efficient reasoning)
		// formerly LocalOntology now IOntology is recommended
		IOntology sourceOnt = new IOntology(ont1Path);
		IOntology targetOnt = new IOntology(ont2Path);

		// load the mapping
		Mapping mapping = new Mapping(alignPath);
		mapping.applyThreshhold(0.3);
		System.out.println("thresholded input mapping has " + mapping.size() + " correspondences");
		
		// define diagnostic problem
		ExtractionProblem ep = new ExtractionProblem(
				ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
				ExtractionProblem.METHOD_OPTIMAL,
				ExtractionProblem.REASONING_EFFICIENT
		);
		
		// attach ontologies and mapping to the problem
		ep.bindSourceOntology(sourceOnt);
		ep.bindTargetOntology(targetOnt);
		ep.bindMapping(mapping);
		
		// solve the problem
		ep.solve();
	
		Mapping extracted = ep.getExtractedMapping();
		System.out.println("mapping reduced from " + mapping.size() + " to " + extracted.size() + " correspondences");
		System.out.println("removed the following correspondences:\n" + ep.getDiscardedMapping());
		
		// compare against reference alignment
		Mapping ref = new Mapping(refPath);
		Characteristic cBefore = new Characteristic(mapping, ref);
		Characteristic cAfter = new Characteristic(extracted, ref);
		
		
		System.out.println("before debugging (pre, rec, f): " + cBefore.toShortDesc());
		System.out.println("after debugging (pre, rec, f):  " + cAfter.toShortDesc());
		

	
	}

}

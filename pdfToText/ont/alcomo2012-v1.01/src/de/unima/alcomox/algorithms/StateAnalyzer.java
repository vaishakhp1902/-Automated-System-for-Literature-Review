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


package de.unima.alcomox.algorithms;

import java.util.HashMap;
import java.util.Set;

import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;

public class StateAnalyzer {

	private HashMap<String, Correspondence> hashedCorrespondences;
	
	private Mapping mapping;
	
	
	public StateAnalyzer(Mapping mapping) {
		this.mapping = mapping.getCopy();
		this.hashedCorrespondences = new HashMap<String, Correspondence>();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Set<String> uris = hashedCorrespondences.keySet();
		for (String sourceUri : uris) {
			int[] values = neverSeenFraction(sourceUri, hashedCorrespondences.get(sourceUri).getConfidence());
			sb.append("[" + values[0] + "][" + values[1] + "][" + values[2] + "]");
			sb.append(hashedCorrespondences.get(sourceUri).toShortString() + "\n") ;
		}
		
		return sb.toString();
	}
	
	private int[] neverSeenFraction(String sourceUri, double border) {
		int counterAbove = 0;
		int counterEqual = 0;
		int counterBelow = 0;
		for (Correspondence c : this.mapping) {

			
			if (c.getSourceEntityUri().equals(sourceUri)) {
				if (c.getConfidence() < border) {
					counterBelow++;
				
				}
				else if (c.getConfidence() > border) {
					counterAbove++;
				}
				else {
					counterEqual++;
				}
			}
		}
		return new int[]{counterAbove, counterEqual, counterBelow};
	}

	public void store(Correspondence[] chosenCorrespondences) {
		String sourceUri;
		Correspondence pc;
		for (Correspondence c : chosenCorrespondences) {
			if (c == null) { continue; }
			sourceUri = c.getSourceEntityUri();
			if (this.hashedCorrespondences.containsKey(sourceUri)) {
				 pc = hashedCorrespondences.get(sourceUri);
				 if (c.getConfidence() < pc.getConfidence()) {
					 this.hashedCorrespondences.put(sourceUri, c);
				 }
			} 
			else {
				this.hashedCorrespondences.put(sourceUri, c);
			}
		}
		// if (this.counter > 3) System.exit(1);
	}
	
	
	
	

}

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

import java.util.HashMap;

import de.unima.alcomox.algorithms.Coord;
import de.unima.alcomox.exceptions.PCFException;

public class MappingMatrix {
	
	
	
	
	private Correspondence[][] matrix;
	private int[][] indices;
	private Coord[] coords;
	private double[][] distances;
	private int numOfSourceEntities;
	private int numOfTargetEntities;
	private int numOfEntities;
	private int numOfCorrespondences;
	private HashMap<String, Integer> sourceEntities;
	private HashMap<String, Integer> targetEntities;
	
	
	
	public MappingMatrix(Mapping mapping) throws PCFException {
		this.sourceEntities = new HashMap<String, Integer>();
		this.targetEntities = new HashMap<String, Integer>();
		int counterSource= 0;
		int counterTarget = 0;
		String sourceUri, targetUri;
		for (Correspondence c : mapping) {
			// check type of correspondence
			if (c.getRelation().getType() != SemanticRelation.EQUIV) {
				throw new PCFException(
					PCFException.INVALID_OPERATION,
					"hungarian methods works only on equivalence correspondences"
				);
			}
			sourceUri = c.getSourceEntityUri();
			targetUri = c.getTargetEntityUri();

			if (!(sourceEntities.containsKey(sourceUri))) {
				sourceEntities.put(sourceUri, counterSource);
				counterSource++;
			}
			if (!(targetEntities.containsKey(targetUri))) {
				targetEntities.put(targetUri, counterTarget);
				counterTarget++;
			}
		}
		
		// fill the matrix
		this.matrix = new Correspondence[counterSource + counterTarget][counterSource + counterTarget];
		this.distances = new double[counterSource + counterTarget][counterSource + counterTarget];
		this.indices = new int[counterSource + counterTarget][counterSource + counterTarget];
		this.coords = new Coord[mapping.size()];
		for (int x = 0; x < counterSource; x++) {
			for (int y = 0; y < counterTarget; y++) {
				this.matrix[x][y] = null;
			}
		}
		int x, y;
		int i = 0;
		for (Correspondence c : mapping) {
			
			sourceUri = c.getSourceEntityUri();
			targetUri = c.getTargetEntityUri();
			x = sourceEntities.get(sourceUri);
			y = targetEntities.get(targetUri);
			// System.out.println("xxx => " + "x=" + x + " y=" + y);
			this.coords[i] = new Coord(x,y);
			this.matrix[x][y] = c;
			this.indices[x][y] = i;
			i++;
		}
		this.numOfSourceEntities = counterSource;
		this.numOfTargetEntities = counterTarget;
		this.numOfEntities = this.numOfSourceEntities + this.numOfTargetEntities;
		this.numOfCorrespondences = mapping.size();
		this.computeDistances();
	}

	
	public String toString() {
		return "Matrix (" + (numOfSourceEntities + numOfTargetEntities) + " x " + (numOfSourceEntities + numOfTargetEntities) + ") representation of a mapping with " + numOfCorrespondences + " correspondences";
	}

	
	public Correspondence getCorrespondence(int x, int y) {
		return this.matrix[x][y];
	}
	

	public Integer getIndex(int x, int y) {
		return this.indices[x][y];
	}
	
	public double[][] getDistances() {
		return this.distances;
	}
	
	
	// ************************************************
	// ************** PRIVATE PLAYGROUND **************
	// ************************************************
	
	private void computeDistances() {
		for (int x = 0; x < this.numOfEntities; x++) {
			for (int y = 0; y < this.numOfEntities; y++) {
				// System.out.println("---" +  this.numOfEntities + "::: x=" +  x + " y=" + y);
				if (this.matrix[x][y] == null) {
					this.distances[x][y] = 1.0;
				}
				else {
					this.distances[x][y] = 1.0 -  this.matrix[x][y].getConfidence();
				}
			}
		}	
	}

	public Coord getCoord(Integer index) {
		return this.coords[index];
	}


	public boolean containsCorrespondence(int x, int y) {
		return (this.matrix[x][y] != null) ? true : false;
	}



	

	


}

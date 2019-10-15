/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   JaccardVM.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package fr.inrialpes.exmo.ontosim.vector;

public class JaccardVM extends VectorMeasure {

	/**
	 * Compute jaccard similarity. Do not use frenquency but only presence/lack
	 * @param v1, @param v2: two vectors of dimensions
	 * 
	 * @return jaccard similarity between v1 and v2
	 */
	public double getMeasureValue(double[] v1, double[] v2) {
		int sum = 0;
		int union=0;
		for (int i=0 ; i<v1.length ; i++ ) {
			if ((v1[i] > 0)&&(v2[i]>0))
				sum++;
			if ((v1[i] > 0)||(v2[i]>0))
				union++;
		}
		if (union==0)
		    return 0;
		return ((double) sum)/union;
	}

	public double getDissim(double[] v1, double[] v2) {
		return 1-getMeasureValue(v1,v2);
	}

	public double getSim(double[] v1, double[] v2) {
		return getMeasureValue(v1,v2);
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.similarity;
	}

}

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


package de.unima.alcomox.exceptions;


/**
* A correspondence exception handles every type of problems related to
* correspondences and their internal components.
*/
public class CorrespondenceException extends AlcomoException {
		
	public static final int INVALID_SEMANTIC_RELATION = 1;
	public static final int INVALID_CONFIDENCE_VALUE = 2;
	public static final int MISSING_NAMESPACE = 3;
	public static final int NON_REFERING = 4;
	

	public CorrespondenceException(int generalDescriptionId, String specificDescription, Exception e) {
		this(generalDescriptionId, specificDescription);
		this.catchedException = e;
	}	
	
	public CorrespondenceException(int generalDescriptionId, String specificDescription) {
		this(generalDescriptionId);
		this.specificDescription = specificDescription;
	}
	
	public CorrespondenceException(int generalDescriptionId) {
		super("Correspondence-Exception");
		switch (generalDescriptionId) {
		case INVALID_SEMANTIC_RELATION:
			this.generalDescription = "An invalid (not available) semantic relation occured";
			break;
		case INVALID_CONFIDENCE_VALUE:
			this.generalDescription = "An invalid confidence value occured (must double in range 0.0 to 1.0)";
			break;
		case MISSING_NAMESPACE:
			this.generalDescription = "An entity reference without namespace occured";
			break;
		case NON_REFERING:
			this.generalDescription = "A non refering entity reference has been detected";
			break;
		default:
			this.generalDescription = "General description is missing";
			break;
		}
	}
	
	private static final long serialVersionUID = 1L;
	
}

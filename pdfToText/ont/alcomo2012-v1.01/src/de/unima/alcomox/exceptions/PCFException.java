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
* An PCF exception handles problems related to parameter (P) like imvalid
* combinations of parameters, as well as things related to the control flow (CF)
* like the invalid order of method calls.
*/
public class PCFException extends AlcomoException {

	public static final int MISSING_PARAM = 1;
	public static final int INVALID_PARAM = 2;
	public static final int INVALID_PARAM_COMBINATION = 3;
	public static final int INVALID_OPERATION = 4;
	public static final int MISSING_BINDING = 5;
	
	public PCFException(int generalDescriptionId, String specificDescription, Exception e) {
		this(generalDescriptionId, specificDescription);
		this.catchedException = e;
	}	
	
	public PCFException(int generalDescriptionId, String specificDescription) {
		this(generalDescriptionId);
		this.specificDescription = specificDescription;
	}
	
	public PCFException(int generalDescriptionId) {
		super("PCF-Exception (= ParameterControlFlow)");
		switch (generalDescriptionId) {
		case MISSING_PARAM:
			this.generalDescription = "necessary parameter has not been initialized";
			break;
		case INVALID_PARAM:
			this.generalDescription = "invalid value for some parameter has been chosen";
			break;
		case INVALID_PARAM_COMBINATION:
			this.generalDescription = "invalid combination of parameter values has been chosen";
			break;
		case INVALID_OPERATION:
			this.generalDescription = "trying to perform an invalid operation";
			break;
		case MISSING_BINDING:
			this.generalDescription = "binding missing or cannot be resolved";
			break;
		default:
			this.generalDescription = "general description is missing";
			break;
		}
	}
	
	private static final long serialVersionUID = 1L;


}

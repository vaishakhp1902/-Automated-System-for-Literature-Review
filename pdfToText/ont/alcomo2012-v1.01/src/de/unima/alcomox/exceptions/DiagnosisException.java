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
* A diagnosis exception handles every type of problems related to
* diagnosis with the human in the loop.
*/
public class DiagnosisException extends AlcomoException {

	public static final int IO_ERROR = 2;
	public static final int INVALID_FORMAT = 3;

	public DiagnosisException(int generalDescriptionId, String specificDescription, Exception e) {
		this(generalDescriptionId, specificDescription);
		this.catchedException = e;
	}	
	
	public DiagnosisException(int generalDescriptionId, String specificDescription) {
		this(generalDescriptionId);
		this.specificDescription = specificDescription;
	}
	
	public DiagnosisException(int generalDescriptionId) {
		super("Diagnosis-Exception");
		switch (generalDescriptionId) {
		case IO_ERROR:
			this.generalDescription = "IO-operation caused an error";
			break;
		case INVALID_FORMAT:
			this.generalDescription = "invalid format in";
			break;
		default:
			this.generalDescription = "general description is missing";
			break;
		}
	}
	
	private static final long serialVersionUID = 1L;


	
}

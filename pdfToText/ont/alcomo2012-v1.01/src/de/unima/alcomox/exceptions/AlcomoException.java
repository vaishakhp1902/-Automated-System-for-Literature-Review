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
* Every problem that might occur and cannot be solved is thrown as
* instance of this class. Also any exception thrown internally is captured
* and transformed into an alcomo exception.
*/
public abstract class AlcomoException extends Exception {
	
	protected String mainDescription;
	protected String generalDescription;
	protected String specificDescription;
	protected Exception catchedException;

	public AlcomoException(String mainDescription) {
		this.mainDescription = mainDescription;
	}



	/**
	* Returns a human understandable string representation.
	* 
	* @return A string representation of this exception. 
	*/
	public String toString() {
		String repr = "\n";
		repr += "Alcomo-Exception caused by " + this.mainDescription + "\n";
		repr += "General: " + this.generalDescription + "\n";
		if (this.specificDescription != null) {
			repr += "Specific: " + this.specificDescription + "\n";
		}
		if (this.catchedException != null) {
			repr += "Caught exception: " + this.catchedException + ".\n";
		}
		return repr;
	}


	
	private static final long serialVersionUID = 1L;
	
}

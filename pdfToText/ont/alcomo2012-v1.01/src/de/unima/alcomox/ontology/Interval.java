package de.unima.alcomox.ontology;

/**
 * An interval represents a sub branch of classes in an interval tree.
 *
 */
public class Interval implements Comparable<Interval> {
	
	private int lower;
	private int upper;
	
	/**
	* Constructs an interval be explicitly defining the lower border, while
	* the upper border is set to -1 to mark the interval as an open interval.
	* 
	* @param lower The lower border (inclusive) of the interval.
	*/
	public Interval(int lower) {
		this.lower = lower;	
		this.upper = -1;
	}
	
	public Interval(int lower, int upper) {
		this.lower = lower;	
		this.upper = upper;
	}
	
	public void setLower(int lower) {
		this.lower = lower;
	}
	
	public void setUpper(int upper) {
		this.upper = upper;
	}
	
	public int getLower() {
		return this.lower;
	}
	
	public int getUpper() {
		return this.upper;
	}
	
	public boolean isClosed() {
		if (this.upper == -1) return false;
		else return true;
	}

	public int getId() {
		return this.lower;		
	}
	
	public String toString() {
		return "[" + this.lower + "," + this.upper + "]";
	}
	
	public boolean equals(Object that) {
		if (that instanceof Interval) {
			Interval thatInterval = (Interval)that;
			return (thatInterval.lower == this.lower && thatInterval.lower == this.lower);
		}
		return false;
	}
	
	/**
     * Compares this and that interval and checks whether this overlaps with that (see also 
     * the cases in the in line comments). The method is symmetric.
     * 
	 * @param that Another interval.
	 * @return True if this interval has a common element with that interval, false otherwise.
	 */
	public boolean overlaps(Interval that) {
		if (!this.isClosed() || !that.isClosed()) return false;
		// this ------
		// that   ------
		if (this.upper >= that.lower && this.upper <= that.upper) {
			return true;
		}
		// this   ------
		// that ------
		if (this.lower >= that.lower && this.lower <= that.upper) {
			return true;
		}
		// this ------
		// that  --- 
		// the other direction has already been captured in the two cases above
		if (this.contains(that)) {
			return true;
		}
		return false;
	}
	
	/**
     * Compares this and that interval and checks whether this interval contains that interval. In case that
     * one of the intervals is not yet closed the method returns false.
     * 
	 * @param that Another interval.
	 * @return True if this contains that, false otherwise.
	 */
	public boolean contains(Interval that) {
		if (!this.isClosed() || !that.isClosed()) return false;
		if (this.lower <= that.lower && this.upper >= that.upper) return true;
		return false;
	}

	/**
	* Checks whether an index is contained in this interval.
	* 
	* @param index Some index used in an interval tree.
	* @return True if the index is contained in this interval, false otherwise.
	*/
	public boolean contains(int index) {
		if (index >= this.lower && index <= this.upper) return true;
		return false;
	}
	
	


	@Override
	public int compareTo(Interval that) {
		if (this.lower < that.lower) return -1;
		else if(this.lower > that.lower) return 1;
		return 0;
	}
	

}

package de.unima.alcomox.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

public class IConcept {
	
	private int id;
	
	private ArrayList<Interval> subIntervals;
	private ArrayList<Interval> disIntervals;
	
	private HashSet<IConcept> parents;
	private HashSet<IConcept> children;
	
	private OWLClass concept;
	
	
	public IConcept(OWLClass concept, int counter) {
		this.concept = concept;
		this.parents = new HashSet<IConcept>();
		this.children = new HashSet<IConcept>();
		this.subIntervals = new ArrayList<Interval>();
		this.disIntervals = new ArrayList<Interval>();
		this.id = counter;
	}
	
	public void addParent(IConcept concept) {
		this.parents.add(concept);
	}
	
	public void addChild(IConcept concept) {
		this.children.add(concept);
	}
	
	public Set<IConcept> getChildren() {
		return this.children;
	}
	
	public int getId() {
		return this.id;	
	}
	
	public String toString() {
		return "(" + this.id + ") " + this.concept.getIRI().getFragment() + " -> " + this.getIntervalRepresentation(); 
	}
	
	public void displaySubtree(int depth) {
		System.out.println(getIndent(depth) + this);
		for (IConcept child : this.children) {
			child.displaySubtree(depth + 1);
		}
	}
	
	private String getIntervalRepresentation() {
		String rep = "subs: ";
		for (Interval interval : this.subIntervals) {
			rep += interval + " ";
		}
		rep += "  dis: ";
		for (Interval interval : this.disIntervals) {
			rep += interval + " ";
		}
		return rep;
	}
	
	private String getIndent(int depth) {
		String indent = "";
		for (int i = 0; i < depth; i++) {
			indent = indent + "   ";
		}
		return indent;
	}


	public void openInterval(int index) {
		Interval interval = new Interval(index);
		this.subIntervals.add(interval);
	}

	
	public void closeInterval(int index) {
		Interval interval = this.subIntervals.get(0);
		interval.setUpper(index);
	}

	public ArrayList<Interval> getSubIntervals() {
		return this.subIntervals;
	}
	
	public ArrayList<Interval> getDisIntervals() {
		return this.disIntervals;
	}

	public void addSubIntervals(ArrayList<Interval> intervals) {
		this.addIntervals(intervals, this.subIntervals);
	}
	
	public void addDisIntervals(ArrayList<Interval> intervals) {
		this.addIntervals(intervals, this.disIntervals);
	}

	private void addIntervals(ArrayList<Interval> intervals, ArrayList<Interval> pintervals) {
		for (int i = 0; i < intervals.size(); i++) {
			Interval ii = intervals.get(i);
			boolean insert = true;
			boolean superInserted = false;
			for (int j = 0; j < pintervals.size(); j++) {
				Interval jj = pintervals.get(j);
				// new interval is superinterval
				if (ii.contains(jj)) {
					if (superInserted) {
						pintervals.remove(j);
						j--;
					}
					else {
						pintervals.set(j, ii);
						superInserted = true;
						insert = false;
					}
				}
				// new interval is sub intervall 
				else if (jj.contains(ii)) insert = false;
			}
			if (insert) pintervals.add(ii);
		}
	}

	public boolean isSubClassOf(IConcept that) {
		for (Interval interval : that.getSubIntervals()) {
			if (interval.contains(this.getId())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasCommonSubClassWith(IConcept that) {
		for (Interval i1 : this.getSubIntervals()) {
			for (Interval i2 : that.getSubIntervals()) {
				if (i1.overlaps(i2)) {
					return true;
				}
			}
		}
		return false;
	}

	
	public boolean hasCommonSubDisjointClassWith(IConcept that) {
		for (Interval i1 : this.getSubIntervals()) {
			for (Interval i2 : that.getDisIntervals()) {
				if (i1.overlaps(i2)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isDisjointWith(IConcept ic2) {
		for (Interval i : this.getDisIntervals()) {
			if (i.contains(ic2.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	* Sorts the subsumption and disjointness intervals and joins them if possible. 
	*/
	public void refine() {
		Collections.sort(this.disIntervals);
		this.joinIntervals(this.disIntervals);
		Collections.sort(this.subIntervals);
		this.joinIntervals(this.subIntervals);
	}
	
	private void joinIntervals(ArrayList<Interval> intervals) {
		for (int i = 1; i < intervals.size(); i++) {
			if (intervals.get(i-1).getUpper() == intervals.get(i).getLower() - 1) {
				Interval joined = new Interval(intervals.get(i-1).getLower(), intervals.get(i).getUpper());
				intervals.set(i-1, joined);
				intervals.remove(i);
				i--;
			}
		}
	}
	


}

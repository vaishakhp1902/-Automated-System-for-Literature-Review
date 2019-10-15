package de.unima.alcomox.ontology;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.util.AlcomoLogger;

public class IOntology extends LocalOntology {
	

	// private static int displayCounter = 1;
	private HashMap<OWLClass, IConcept> hashedIConcepts;
	private HashMap<Integer, IConcept> hashedByIdIConcepts;
	// private HashMap<OWLClass, OWLClass> hashedComplements;
	private static int traversalCounter = 1;
	
	public static void main(String[] args) throws AlcomoException {
		Settings.BLACKBOX_REASONER = Settings.BlackBoxReasoner.HERMIT;
		IOntology itr = new IOntology("examples/cmt.owl");
		itr.intitIntervalTree();
	}

	/**
	* Initializes an IOntology by setting up an additional interval tree,
	* that represents very fast index structures highly beneficial in the
	* pattern-based reasoning.
	* 22
	* @param ep The Extraction that this IOntology is bound to.
	*/
	public void init(ExtractionProblem ep) throws OntologyException {
		super.init(ep);	
		AlcomoLogger.takeTime("init interval tree I");
		this.intitIntervalTree();
	}
	
	public IOntology(String filepathOrUrl) throws AlcomoException {
		super(filepathOrUrl);
		this.hashedIConcepts = new HashMap<OWLClass, IConcept>();
		this.hashedByIdIConcepts = new HashMap<Integer, IConcept>();
	}
	
	
	public boolean isISubClassOfClass(OWLClass c1, OWLClass c2) {
		IConcept ic1 = this.hashedIConcepts.get(c1);
		IConcept ic2 = this.hashedIConcepts.get(c2);
		return ic1.isSubClassOf(ic2);
	}
	
	
	public boolean isIDisjointClassWithClass(OWLClass c1, OWLClass c2) {
		IConcept ic1 = this.hashedIConcepts.get(c1);
		IConcept ic2 = this.hashedIConcepts.get(c2);
		return ic1.isDisjointWith(ic2);
	}
	
	/**
	* Checks whether two classes have a common named sublass (except NOTHING).
	* Should run in average in something between O(1) and O(b) where b is the
	* average branching factor.
	* 
	* @param c1 The first class.
	* @param c2 The second class
	* @return True if both classes have a common subclass, false otherwise.
	*/
	public boolean hasICommonSubClass(OWLClass c1, OWLClass c2) {
		IConcept ic1 = this.hashedIConcepts.get(c1);
		IConcept ic2 = this.hashedIConcepts.get(c2);
		return ic1.hasCommonSubClassWith(ic2);
	}	
	
	/**
	* Checks whether there exists a class that is both a subclass of c1 and
	* at the same time a class that is disjoint with c2. 
	* 
	* @param c1 The class for which subclasses are implicitly checked.
	* @param c2 The class for which disjoint classes are implicitly checked.
	* @return True, if there is a class that is a subclass of c1 and disjoint with c2.
	*/
	public boolean hasICommonSubDisjointClass(OWLClass c1, OWLClass c2) {
		IConcept ic1 = this.hashedIConcepts.get(c1);
		IConcept ic2 = this.hashedIConcepts.get(c2);
		return ic1.hasCommonSubDisjointClassWith(ic2);	
	}
	
	
	// ***************************************************************
	// *** PRIVATE PLAYGROUND ****************************************
	// ***************************************************************
	
	/**
	 * Inits the representation as an interval tree for simple subsumption.
	 */
	public void intitIntervalTree() throws OntologyException {
		
		this.initReasoner();
		// System.out.println("building interval tree ...");
		this.buildTree(this.THING, null);
		IConcept root = this.hashedIConcepts.get(this.THING);
		this.extendByStatedDisjointness();
		this.extendTreeByDerivedDisjointness(root);
		this.extendTreeByUnionOfDisjointness();
		this.extendTreeByDerivedDisjointness(root);
		this.refineTree();
		// root.displaySubtree(0);
		
		
		// System.out.println("... building interval tree finished!");		
	}
	


	/**
	* Builds up a subsumption tree starting at a given concept.
	* Iterates via depth first search through the subsumption tree.
	* 
	* @param concept The concept that is the root of the tree. 
	* @param concept The super concept of the concept.  
	*/
	private IConcept buildTree(OWLClass concept, OWLClass superConcept) {
		IConcept ic = null;
		IConcept superIc = null;
		if (this.NOTHING.equals(concept)) return null;
		if (superConcept != null) superIc = this.hashedIConcepts.get(superConcept);
		// default case: graph is a tree
		if (!this.hashedIConcepts.containsKey(concept)) {
			ic = new IConcept(concept, traversalCounter);
			this.hashedIConcepts.put(concept, ic);
			this.hashedByIdIConcepts.put(ic.getId(), ic);
			ic.openInterval(traversalCounter);
			setParentChildRelation(ic, superIc);
			traversalCounter++;
			Set<OWLClass> subConcepts = this.reasoner.getSubClasses(concept, true).getFlattened();
			for (OWLClass subConcept : subConcepts) { 
				IConcept conceptMH = this.buildTree(subConcept, concept);
				// if there is a return value, this refer to a cycle that has
				// been blocked in previous step, subclass interval is directly added
				if (conceptMH != null) ic.addSubIntervals(conceptMH.getSubIntervals());
				for (IConcept child : ic.getChildren()) {
					if (child.getSubIntervals().size() > 1) {
						ic.addSubIntervals(child.getSubIntervals());
					}
				}
			}
			// now index also each equivalent class by letting it refer to the same index
			Set<OWLClass> equivConcepts = this.reasoner.getEquivalentClasses(concept).getEntities();
			for (OWLClass equivConcept : equivConcepts) {
				if (!equivConcept.equals(concept)) {
					this.hashedIConcepts.put(equivConcept, ic);
				}
			}
			ic.closeInterval(traversalCounter - 1);
			// in the case of a "pure tree" null is returned
			return null;
		}
		// exceptional case: graph is not a tree
		// a concept has more than one parent
		else {
			ic = this.hashedIConcepts.get(concept);
			setParentChildRelation(ic, superIc);
			// stop the recursion here
			return ic;
		}

	}
	
	/**
	* Extends the tree by adding all stated disjointness intervals.
	*/
	private void extendByStatedDisjointness() {
		// variant that uses no reasoner at all
		// taking care of disjointness axioms
		Set<OWLDisjointClassesAxiom> disAxioms = this.ontology.getAxioms(AxiomType.DISJOINT_CLASSES);
		// System.out.println("number of disjointness axioms before: " + disAxioms.size());
		// taking care of disjointness in disjoint union axioms
		Set<OWLDisjointUnionAxiom> disUAxioms = this.ontology.getAxioms(AxiomType.DISJOINT_UNION);
		for (OWLDisjointUnionAxiom disUAxiom : disUAxioms) {
			disAxioms.add(disUAxiom.getOWLDisjointClassesAxiom());
		}
		// now add for each disjoint pair the subinterval of the other class as disjoint interval
		for (OWLDisjointClassesAxiom disAxiom : disAxioms) {
			Set<OWLClassExpression> conceptExpressions = disAxiom.getClassExpressions();
			for (OWLClassExpression conceptEx1 : conceptExpressions) {
				for (OWLClassExpression conceptEx2 : conceptExpressions) {
					// do not add disjointness for something and itself
					if (conceptEx1.equals(conceptEx2)) continue;
					extendByStatedDisjointness(conceptEx1, conceptEx2);
				}
			}
		}
	}
	
	/**
	* Extends the tree by adding all disjointness information that follows from expanding
	* axioms with unionOf statements.
	* 
	*/
	private void extendTreeByUnionOfDisjointness() {
		Set<OWLAxiom> axioms = this.ontology.getAxioms();
		// collecting all object union description
		Set<OWLObjectUnionOf> objectUnionExpressions = new HashSet<OWLObjectUnionOf>();
		for (OWLAxiom axiom : axioms) {
			Set<OWLClassExpression> expressions = axiom.getNestedClassExpressions();
			for (OWLClassExpression expression : expressions) {
				if (expression instanceof OWLObjectUnionOf) {
					objectUnionExpressions.add((OWLObjectUnionOf)expression);
				}
			}
		}
		// transfer in sets of owl classes and do the disjointness extension
		for (OWLObjectUnionOf unionOf : objectUnionExpressions) {
			Set<OWLClass> unionOfClassSet = new HashSet<OWLClass>();
			Set<OWLClassExpression> unionOfExpressionSet = unionOf.asDisjunctSet();
			// first check if each involved entity is named class
			boolean containsNonClass = false;
			for (OWLClassExpression unionOfExpression : unionOfExpressionSet) {
				if (unionOfExpression instanceof OWLClass) {
					unionOfClassSet.add((OWLClass)unionOfExpression);
				}
				else {
					containsNonClass = true;
					break;
					
				}
			}
			// if each involved class is a named class
			if (!containsNonClass) {
				ArrayList<Interval> intersectionIntervals = null;
				//System.out.println(">>> detected unionof that consist of only OWLClasses:");
				for (OWLClass unionOfClass : unionOfClassSet) {
					// System.out.println(">>> >>> " +  unionOfClass);
					IConcept unionOfIConcept = this.hashedIConcepts.get(unionOfClass);
					if (intersectionIntervals == null) {
						intersectionIntervals = new ArrayList<Interval>();
						intersectionIntervals.addAll(unionOfIConcept.getDisIntervals());
					}
					else {
						reduceToIntersection(intersectionIntervals, unionOfIConcept.getDisIntervals());
					}
				}				
				if (intersectionIntervals != null) {
					/*
					for (Interval interval : intersectionIntervals) {
						System.out.println(">>> >>> interval ::: " + interval);
					}
					*/
					Set<OWLClass> subclasses = this.reasoner.getSubClasses(unionOf, true).getFlattened();
					// add disjointness information too each subclass
					for (OWLClass subclass : subclasses) {
						// System.out.println(">>>  adding to " +  subclass +  " " + intersectionIntervals.size() + " disjoint intervalls");
						
						IConcept iconcept = this.hashedIConcepts.get(subclass);
						iconcept.addDisIntervals(intersectionIntervals);
					}
					// add disjointess information to each class specifeid by the id in the disjointness intervals
					for (Interval interval : intersectionIntervals) {
						int id = interval.getId();
						IConcept iconcept = this.hashedByIdIConcepts.get(id);
						if (iconcept != null) {
							for (OWLClass subclass : subclasses) {
								IConcept disiconcept = this.hashedIConcepts.get(subclass);
								iconcept.addDisIntervals(disiconcept.getSubIntervals());
							}
						}
					}
				}
			}
		}
	}
	

	
	
	
	/**
	* 
	* @param conceptEx1
	* @param conceptEx2
	*/
	private void extendByStatedDisjointness(OWLClassExpression conceptEx1, OWLClassExpression conceptEx2) {
		//System.out.println();
		if (!(conceptEx1 instanceof OWLClass)) {
			// System.out.println("XXX " + conceptEx1 );
			Set<OWLClass> subclasses = this.reasoner.getSubClasses(conceptEx1, true).getFlattened();
			subclasses.addAll(this.reasoner.getEquivalentClasses(conceptEx1).getEntities());
			for (OWLClass subclass : subclasses) {
				this.extendByStatedDisjointness(subclass, conceptEx2);
			}
			return;
		}
		if (!(conceptEx2 instanceof OWLClass)) {
			Set<OWLClass> subclasses = this.reasoner.getSubClasses(conceptEx2, true).getFlattened();
			subclasses.addAll(this.reasoner.getEquivalentClasses(conceptEx2).getEntities());
			for (OWLClass subclass : subclasses) {
				this.extendByStatedDisjointness(conceptEx1, subclass);
			}
			return;
		}
		OWLClass casted1 = (OWLClass)conceptEx1;
		OWLClass casted2 = (OWLClass)conceptEx2;
		if (casted1.equals(casted2)) return;
		IConcept icasted1 = this.hashedIConcepts.get(casted1);
		IConcept icasted2 = this.hashedIConcepts.get(casted2);
		if (icasted1 != null && icasted2 != null) {
			icasted1.addDisIntervals(icasted2.getSubIntervals());
		}
	}
	
	
	private void refineTree() {
		for (IConcept iconcept : this.hashedIConcepts.values()) {
			iconcept.refine();
		}
		
	}
	
	/**
	* Extends stated disjointness statement of a tree specified by iconcept as root
	* by the disjointness that can be derived from stated disjointness and
	* subsumption hierarchy. Makes a recursive call to all children.
	* 
	* @param iconcept The root of the tree.
	*/
	private void extendTreeByDerivedDisjointness(IConcept iconcept) {
		// System.out.println("scanning " + iconcept);
		for(IConcept child : iconcept.getChildren()) {
			if (child == null) continue;
			child.addDisIntervals(iconcept.getDisIntervals());
			this.extendTreeByDerivedDisjointness(child);
		}
	}	
	
	/**
	 * Simply helper methods that established the relations between child and parent
	 * in case that the parent is not null. Take care that the method is never called
	 * if the child is null.
	 *  
	 */
	private void setParentChildRelation(IConcept ic, IConcept superIc) {
		if (superIc != null) {
			superIc.addChild(ic);
			ic.addParent(superIc);
		}
	}
	
	
	/**
	* Removes from the core list all intervals that are not in the some list.
	*  
	* @param core The ArrayList that is replaced 
	* @param some
	* @return
	*/
	private static void reduceToIntersection(ArrayList<Interval> core, ArrayList<Interval> some) {
		for (int i = 0; i < core.size(); i++) {
			boolean remove = true;
			for (int j = 0; j < some.size(); j++) {
				if (core.get(i).equals(some.get(j))) {
					remove = false;
					continue;
				}
				else if (some.get(j).contains(core.get(i))) {
					remove = false;
					continue;
				}
				else if (core.get(i).contains(some.get(j))) {
					core.add(some.get(j));
				}
			}
			if (remove) {
				core.remove(i);
				i--;
			}
		}
	}
}

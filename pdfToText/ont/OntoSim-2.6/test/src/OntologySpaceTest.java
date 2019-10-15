import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontosim.OntologySpaceMeasure;
import fr.inrialpes.exmo.ontosim.entity.EntityLexicalMeasure;
import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontosim.set.MaxCoupling;



public class OntologySpaceTest {
    
    LoadedOntology<?> o101;
    LoadedOntology<?> o103;
    LoadedOntology<?> o301;
    LoadedOntology<?> o302;
    LoadedOntology<?> anatomy;
    
    @Test(groups = { "full", "noling" })
    public void initOnto() throws OntowrapException, URISyntaxException {
	//OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi10.OWLAPIOntologyFactory");
	//OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory");
	o101 = OntologyFactory.getFactory().loadOntology(new URI("file:examples/101.rdf"));
	o103 = OntologyFactory.getFactory().loadOntology(new URI("file:examples/103.rdf"));
	o301 = OntologyFactory.getFactory().loadOntology(new URI("file:examples/301.rdf"));
	o302 = OntologyFactory.getFactory().loadOntology(new URI("file:examples/302.rdf"));
	anatomy = OntologyFactory.getFactory().loadOntology(new URI("file:examples/nci_anatomy_2008.owl"));
    }
    
    @Test(groups = { "full", "ontospace", "noling"}, dependsOnMethods = {"initOnto"})
    public void entityLexical() {
	OntologySpaceMeasure m = new OntologySpaceMeasure(new MaxCoupling<Entity<?>>(new EntityLexicalMeasure()));
	//System.out.println(m.getSim(anatomy, o101));
	assertEquals(m.getSim(o101, o101),1.);
	assertEquals(m.getSim(o103, o103),1.);
	
	//HashSet<?> o1ent = new HashSet<Object>(o302.getEntities());
	//HashSet<?> o2ent = new HashSet<Object>(o103.getEntities());
	//System.out.println("taille o1 : "+o1ent.size());
	//System.out.println("taille o2 : " +o2ent.size());
	/*for (Object obj : o1ent) {
	    System.out.println(obj);
	    try {
		for (String s : o101.getEntityAnnotations(obj))
		 System.out.println("\t"+s);
	    } catch (OntowrapException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}*/
	/*for (Object obj : o2ent) {
	    System.out.println(obj);
	}*/
	//System.out.println(m.getSim(o101, o103));
	assertEquals(m.getSim(o101, o103),1.);
	
	//System.out.println(m.getSim(o101, o301));
	assertEquals(String.valueOf(m.getSim(o101, o301)).substring(0,7),"0.83598");//""0.30249");
	
	//System.out.println(m.getSim(o101, o302));
	assertEquals(String.valueOf(m.getSim(o101, o302)).substring(0,7),"0.77957");//"0.22053");
	/*assertTrue(m.getSim(o101, o301)>0.216 && m.getSim(o101, o301)<0.217); //0.216
	assertTrue(m.getSim(o101, o302)>0.1685 && m.getSim(o101, o302)<0.1686);*/
    } 
    
    @Test(groups = { "full", "ontospace", "noling"}, dependsOnMethods = {"initOnto"})
    public void OLA() {
	
	/*OntologySpaceMeasure m = new OntologySpaceMeasure(new MaxCoupling(new OLAEntitySim()));
	assertEquals(m.getSim(o101, o101),1.);
	assertEquals(m.getSim(o103, o103),1.);
	assertTrue(m.getSim(o101, o103)>0.986 && m.getSim(o101, o103)<0.987);
	
	assertTrue(m.getSim(o101, o301)>0.216 && m.getSim(o101, o301)<0.217); //0.216
	assertTrue(m.getSim(o101, o302)>0.1685 && m.getSim(o101, o302)<0.1686);*/
    }
    
}

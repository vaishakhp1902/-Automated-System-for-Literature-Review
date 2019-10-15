import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontosim.util.LuceneAnalyser;


public class LuceneAnalyserTest {

    
    @Test(groups = { "full", "noling" })
    public void englishAnalyser() {
	String s = "Apache Lucene is an open source project available for free download. Please use the links on the right to access Lucene.";
	Collection<String> terms =LuceneAnalyser.getTerms(s, null);
	Collection<String> reference = new ArrayList<String>();
	Collections.addAll(reference, "apach", "lucen", "open", "sourc", "project", "avail", "free", "download", "pleas", "us", "link", "right", "access", "lucen");
	Assert.assertEquals(terms, reference);
    }
}

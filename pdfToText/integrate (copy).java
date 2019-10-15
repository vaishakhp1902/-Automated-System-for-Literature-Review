import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;


public class integrate {

	public static void main(String args[]){
		
		OntModel model = ModelFactory.createOntologyModel();

		OntModel outputmodel = ModelFactory.createOntologyModel(); 

		InputStream ins = FileManager.get().open("/home/godass69/Downloads/converter.rdf");
		model.read(ins,null);
		
		
		String input = "Phishing";
		int flag = 1;
		do{
			
			String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
					+"select ?p ?r ?s where {?p ?q \""+input+"\"@en." +
					"?p skos:broader ?r." +
					"?r <http://www.w3.org/2004/02/skos/core#prefLabel> ?s."+
		    		"}";
			//System.out.println(query);
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				
				ResultSet results = qexec.execSelect() ;
				if(results.hasNext()){
					flag = 1;
				}else{
					flag = 0;
				}
				while (results.hasNext())
				{
					
					QuerySolution soln = results.nextSolution() ;
					
					//System.out.println(soln);
					String a = soln.get("r").toString();
					String b = soln.get("p").toString();
					String c = soln.get("s").toString();
					c = c.substring(0,c.length()-3);

					OntClass parent = outputmodel.createClass(a);
					OntClass child = outputmodel.createClass(b);
					child.addSuperClass(parent);
					input = c;
					
					
				}
			}
		}while(flag != 0);
		String fileName = "/home/godass69/Desktop/t/text.txt";
		try {
			FileWriter out = new FileWriter( fileName );
			outputmodel.write(out,"RDF/XML");
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//model.write(System.out,"RDF/XML");
		//String x = outputmodel.write(System.out,"RDF/XML").toString();
		//System.out.println(x);
	}
		
}


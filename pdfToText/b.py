from SPARQLWrapper import SPARQLWrapper, JSON

sparql = SPARQLWrapper("http://dbpedia.org/sparql")
sparql.setQuery("""
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX quepy: <http://www.machinalis.com/quepy#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX dbpprop: <http://dbpedia.org/property/>
PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>
SELECT ?x1 WHERE {
    ?x0 rdf:type foaf:Person.
    ?x0 rdfs:label "Rajinikanth"@en.
    ?x0 rdfs:residence ?x1.
}
""")
sparql.setReturnFormat(JSON)
results = sparql.query().convert()
print(results)
for result in results["results"]["bindings"]:
    print('%s: %s' % (result["x"]["xml:residence"], result["x"]["value"]))

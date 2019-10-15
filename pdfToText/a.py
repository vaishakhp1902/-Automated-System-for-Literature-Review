import os
from SPARQLWrapper import SPARQLWrapper, JSON

sparql = SPARQLWrapper("http://dbpedia.org/sparql")

f = open('keywords.txt','r')
inp = f.readlines()
f.close()

for i in inp:
	print(i)

	inpu = "\""+i+"\""

	sparql.setQuery("""
	   Select ?d WHERE{

	?a ?b """+inpu+"""@en.
	?a <http://purl.org/dc/terms/subject> ?d.
	}
	""")



	sparql.setReturnFormat(JSON)
	results = sparql.query()
	counter = 1
	send = ""

	for r in results:
		if counter > 3:	
			x = str(r)
			x = x[x.find("Category:")+9:x.find(" }}")-1]
			if x.find("(") != -1:
				x = x[:x.find("(")]
			send=send+x+":"
		
		
		counter = counter + 1

	send = send[:-1]
	send = inpu+":"+send

	cmd = "javac -cp "+".:./lib/*"+" integrate.java"

	os.system(cmd)  # returns the exit code in unix


	cmd = "java -cp "+".:./lib/*"+" integrate "+ send

	os.system(cmd)  # returns the exit code in unix


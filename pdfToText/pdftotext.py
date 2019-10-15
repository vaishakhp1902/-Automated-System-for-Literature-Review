from tika import parser
import re

path_of_file= 'c.pdf'
	
raw = parser.from_file(path_of_file)



f=open("text2.txt", "r+")
text = str(raw['content'].encode("utf-8"));
#text = text.replace('-\\n', '')
#text = text.replace('\\n', ' ')

f.write(text)

f.close()

#with open('text.txt', 'r') as file :
#	filedata = file.read()

# Replace the target string
	#filedata = filedata.replace('\\n', '')
	#filedata = filedata.replace('\\xc3\\xa2', 'asd')
	#x=filedata.encode("latin-1").decode('utf-8')
	


# Write the file out again
#with open('text.txt', 'w') as file:
	
#	file.write(filedata)
  
buffered = 0
  
with open('text2.txt', 'r') as file :
	filedata = file.read()
	
	
#	print(filedata.find("Categories and Subject Descriptors"))
	if filedata.lower().find("abstract") != -1 and filedata.find("Categories and Subject Descriptors") != -1:
			buffered = 1
			start = filedata.lower().find("abstract")+len("abstract")
			end = filedata.find("Categories and Subject Descriptors")
	
	elif filedata.lower().find("abstract") != -1 and filedata.find("Index Terms") != -1:
		buffered = 2
		start = filedata.lower().find("abstract")+len("abstract")
		end = filedata.find("Index Terms")
		#result = re.search('abstract(.*)keywords', filedata.lower())
	
	elif filedata.lower().find("abstract") != -1 and filedata.lower().find("keywords",filedata.lower().find("abstract")+len("abstract")) != -1:
		buffered = 3
		start = filedata.lower().find("abstract")+len("abstract")
		end = filedata.lower().find("keywords",filedata.lower().find("abstract")+len("abstract"))
		#result = re.search('abstract(.*)keywords', filedata.lower())
	elif filedata.lower().find("abstract") != -1 and filedata.lower().find("introduction",filedata.lower().find("abstract")+len("abstract")) != -1:
	
			buffered = 4
			start = filedata.lower().find("abstract")+len("abstract")
			end = filedata.lower().find("introduction",filedata.lower().find("abstract")+len("abstract"))
			#result = re.search('abstract(.*)introduction', filedata)
	#result = re.search('Abstract(.*)1 INTRODUCTION', s)
	

	
if buffered == 1 or buffered == 2 or buffered == 3 or buffered == 4:
	print(buffered)
	print(start)
	print(end)
	#with open('text.txt', 'w') as file:
		#file.write(result.group(1))
		#file.write(filedata[start:end])
else:	
	print("else part")
	#with open('text.txt', 'w') as file:
		#file.write("")

	

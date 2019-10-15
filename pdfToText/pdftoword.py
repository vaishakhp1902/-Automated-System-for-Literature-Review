from tika import parser
import re

path_of_file= '11.pdf'
	
raw = parser.from_file(path_of_file)



f=open("text.txt", "r+")
text = str(raw['content'].encode("utf-8"));
text = text.replace('\\n', '')

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
  
with open('text.txt', 'r') as file :
	filedata = file.read()
	
	
	if filedata.lower().find("abstract") != -1 and filedata.lower().find("keywords") != -1:
		buffered = 1
		start = filedata.lower().find("abstract")+len("abstract")
		end = filedata.lower().find("keywords")
		#result = re.search('abstract(.*)keywords', filedata.lower())
	else:
		if filedata.lower().find("abstract") != -1 and filedata.lower().find("introduction") != -1:
			buffered = 2
			start = filedata.lower().find("abstract")+len("abstract")
			end = filedata.lower().find("introduction")
			#result = re.search('abstract(.*)introduction', filedata)
	#result = re.search('Abstract(.*)1 INTRODUCTION', s)
	

	
if buffered == 1 or buffered == 2:
	print(buffered)
	with open('text.txt', 'w') as file:
		#file.write(result.group(1))
		file.write(filedata[start:end])
else:	
	print("else part")
	with open('text.txt', 'w') as file:
		file.write("")

	

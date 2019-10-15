import re
from array import *

f=open("Stop_Words.txt", "r")
text =f.read()
pattern = re.compile('(\w+? JJ\n)*(\w+ NN\w?\n)+', re.DOTALL)
#without grouping = (\w+? JJ\n)*(\w+ NN\w?)+
result = pattern.finditer(text)
list=[]
for element in result:
	r = element.group()
	words = r.split('\n')
	temp=[]
	for i in range(0,len(words)):
		if(words[i] != ""):
			temp.append(words[i])
	list.append(temp)

for items in list:
	for words in items:
		if(words.find("NN") != -1):
			list.append(words)

for it in list:
	print(it)
	
	

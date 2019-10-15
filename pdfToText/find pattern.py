import re
from array import *

f=open("key_list_tagged.txt", "r")
text =f.read()
pattern = re.compile('(\w+? JJ\w?\n)*(\w+ NN\w?\n)+', re.DOTALL)
#without grouping = (\w+? JJ\n)*(\w+ NN\w?)+
result = pattern.finditer(text)
lists=[]
for element in result:
	r = element.group()
	words = r.split('\n')
	temp=[]
	for i in range(0,len(words)):
		if(words[i] != ""):
			temp.append(words[i])
	lists.append(temp)

for items in lists:
	for words in items:
		if(words.find("NN") != -1):
			lists.append(words)

for it in lists:
	print(it)
	print("\n")
	

import os.path
from rake_nltk import Rake

def removeStopwords(wordlist, stopwords):
    return [w for w in wordlist if w not in stopwords]


count = 0


f = open("abstract.txt",'r')

text = f.read()

abstract = text

s = ""

lists = text.split('.')

phrases = ['in this paper','in this ','in this study','our research','this paper proposes','this novel','this paper','this paper aimed','we describe','this article', 'this article extends','this work examined','the present study','we present','the objective of this paper','we examine','this paper surveys','this paper conducted', 'this paper presents','we develop a novel approach','we develop','in this paper,we have proposed','this paper describe','in this domain', 'method proposes', 'proposed' , 'study' ]

selected = []
for line in lists:
	for phrase in phrases:
		lower = line.lower()
		index = lower.find(phrase)
		if index != -1:
			selected.append(line)
			break
for i in selected:
	s = s+i+"."
text = s

if text == "":
	text = abstract
f=open("p_statment.txt", "w+")

f.write(text)

f.close()


r=Rake()
r.extract_keywords_from_text(text)

lists = (r.get_ranked_phrases())

astext = ""

f = open("stopwords.txt",'r')
words = f.read()
s = words.split("\n")

f = open("keywords.txt",'w+')
f.write('')
f.close()
for items in lists:
	wordlist = items.split(" ")
	text1 = removeStopwords(wordlist,s)
	text2 = ""
	for i in text1:
		text2 = text2+i+" "
	
	f = open("keywords.txt",'a')
	f.write(text2[:-1]+'\n')
	f.close()
	
	
	



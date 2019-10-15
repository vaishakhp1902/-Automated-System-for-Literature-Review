#!/usr/local/bin/python
# coding: utf-8
from rake_nltk import Rake

f = open("abstract.txt",'r')

text = f.read()



r=Rake()
r.extract_keywords_from_text(text)

list = (r.get_ranked_phrases())
for items in list:
	print(items)
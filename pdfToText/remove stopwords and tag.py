import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from collections import OrderedDict

f=open("key_list_tagged.txt", "r")
sent = f.read()
f.close()

stop_words = set(stopwords.words('english'))
word_tokens = word_tokenize(sent)
filtered_sentence = [w for w in word_tokens if not w in stop_words]
filtered_sentence = []
for w in word_tokens:
    if w not in stop_words:
        filtered_sentence.append(w)

filtered_sentence=list(OrderedDict.fromkeys(filtered_sentence))
tag = nltk.pos_tag(filtered_sentence)
fh = open("key_list_tagged.txt", "w+")
for i in range(0,len(filtered_sentence)):
	fh.write((tag[i][0])+" "+(tag[i][1])+"\n")
fh.close()

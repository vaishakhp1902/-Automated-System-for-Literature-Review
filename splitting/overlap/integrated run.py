import os

os.system('python "interval slice.py"')
print("\n\nRemoving stopwords and tagging")
os.system('python "remove stopwords and tag.py"')
print("\n\nFinding phrases according to JJ*N+")
os.system('python "find pattern.py"')
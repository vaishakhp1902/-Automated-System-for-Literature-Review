import os

import os.path

import sys
pdf = 'd.pdf'

print('Taking image of the first page of the PDF file...\n')
os.system('python3 "storing pic of first page of pdf.py" '+pdf)

if os.path.exists('out.jpg') == False :
	print("Error in taking pic of the first page of the pdf")
	sys.exit()

print('drawing bounding boxes around the abstract of the page...\n')
os.system('python3 "drawing boxes and storing.py"')

if os.path.exists('img_1.jpg') == False :
	print("Error in Drawing bounding box")
	sys.exit()

print('Recognizing abstract from the image...\n')
os.system('python3 "recognize text from image.py"')

print('Finding problem statement from the abstract...\n')
os.system('python3 "problem_statement.py"')
	
print('Storing images of all the pages of the PDF...\n')
os.system('python3 "storing pic of everything from of pdf.py" d.pdf')

print('Finding exact keywords...\n')
os.system('python3 "finding occurence of keywords.py"')
'''
print('Removing temp files...')
os.remove('abstract.txt')

os.remove('out_text.txt')

os.remove('p_statment.txt')
'''

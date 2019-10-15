from pdf2image import convert_from_path
import sys


filein = sys.argv[1]
pages = convert_from_path(filein, 500)
i=0
for page in pages:
    if i == 1:
	    break
    page.save('out.jpg', 'JPEG')
    i = i + 1	

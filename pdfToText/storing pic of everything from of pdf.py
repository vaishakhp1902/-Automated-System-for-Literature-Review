from pdf2image import convert_from_path
import sys
import os


files = sys.argv[1]
pages = convert_from_path(files, 500)
i=1

for page in pages:
    filename = "out_"+str(i)+".jpg"
    page.save(filename, 'JPEG')
    i = i + 1	

i-=1

print('Writing text from all the images to text file...\n')
os.system('python3 "recognize everything from pdf.py" ' + str(i))

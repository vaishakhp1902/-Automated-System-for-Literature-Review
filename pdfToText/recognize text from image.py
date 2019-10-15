from PIL import Image
import pytesseract
#pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files (x86)\Tesseract-OCR\tesseract.exe"
text = str(((pytesseract.image_to_string(Image.open('img_1.jpg')))))
#text = str(((pytesseract.image_to_string(Image.open('img_1.jpg'))).encode('utf-8')))
text = text.replace('-\n', '')
text = text.replace('\n', ' ')
start = 0
end = len(text)
index = text.find('©')
if(index != -1):
	end = index

f=open("abstract.txt", "w+")

final = text[start:end]

f.write(final)

f.close()

from pydub import AudioSegment
import speech_recognition as sr
import os


audio = AudioSegment.from_wav("../../1.WAV")

n = len(audio)

counter = 1

interval = 5 * 1000
overlap = 1.5 * 1000
start = 0
end = 0
flag = 0
fh = open("recognized.txt", "w+")
counter = 1
for i in range(0,2*n,interval):
	
	time = i
	if i == 0:
		start = 0
		end = interval
	else:
		start = end - overlap
		end = start + interval 

	if end >= n:
		end = n
		flag = 1
		
	chunk = audio[start:end]
		
	chunk.export('chunk.wav',format="wav")
	
	AUDIO_FILE = 'chunk.wav'
	
	print("Processing chunk "+str(counter)+". Start= "+str(start)+" end= "+str(end))
	counter = counter + 1
	
	
	r = sr.Recognizer()
	with sr.AudioFile(AUDIO_FILE) as source:
			audio_listened = r.listen(source)
	try:    
		rec =  r.recognize_google(audio_listened)
		fh.write(rec+" ")
	except sr.UnknownValueError:
		print("Google Speech Recognition could not understand audio")
	except sr.RequestError as e:
		print("Could not request results from Google Speech Recognition service; {0}".format(e))
	if flag == 1:
		os.remove('chunk.wav')
		fh.close()
		break;
		

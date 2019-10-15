from pydub import AudioSegment
from pydub.silence import split_on_silence
import os, shutil
import speech_recognition as sr
from os import path
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
folder = 'A:/SPEECH TO TEXT/newf'
for the_file in os.listdir(folder):
    file_path = os.path.join(folder, the_file)
    try:
        if os.path.isfile(file_path):
            os.unlink(file_path)
        elif os.path.isdir(file_path): shutil.rmtree(file_path)
        print('Deleted everything!')
    except Exception as e:
        print(e)
sound = AudioSegment.from_mp3("1.WAV")
f=open("decoded.txt", "a+")
chunks = split_on_silence(sound,min_silence_len=280,silence_thresh=-33,keep_silence=150)
for i, chunk in enumerate(chunks):
    print(i)
    print("\n")
    chunk.export(folder+"/chunk{0}.wav".format(i), format="wav")
    AUDIO_FILE = path.join(path.dirname(path.realpath(__file__)), folder+"/chunk{0}.wav".format(i))
    r = sr.Recognizer()
    with sr.AudioFile(AUDIO_FILE) as source:
        print("Listening...")
        audio = r.record(source)  # read the entire audio file
        f.write((r.recognize_google(audio) +" "))
f.close();



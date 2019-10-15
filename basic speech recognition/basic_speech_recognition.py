#!/usr/bin/env python3
# Requires PyAudio and PySpeech.
 
import speech_recognition as sr

AUDIO_FILE = ("1.wav")


# Record Audio

r = sr.Recognizer()
#with sr.Microphone() as source:
with sr.AudioFile(AUDIO_FILE) as source:
    print("Listening...")
    audio = r.listen(source)
 
# Speech recognition using Google Speech Recognition
try:
    # for testing purposes, we're just using the default API key
    # to use another API key, use `r.recognize_google(audio, key="GOOGLE_SPEECH_RECOGNITION_API_KEY")`
    # instead of `r.recognize_google(audio)`
    print("Speech recognized as : \n" + r.recognize_google(audio,config))
except sr.UnknownValueError:
    print("Google Speech Recognition could not understand audio")
except sr.RequestError as e:
    print("Could not request results from Google Speech Recognition service; {0}".format(e))
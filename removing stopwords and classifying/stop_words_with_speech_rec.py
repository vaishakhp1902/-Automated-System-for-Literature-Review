import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
import speech_recognition as sr
AUDIO_FILE = ("../1.wav")
r = sr.Recognizer()
#with sr.Microphone() as source:
with sr.AudioFile(AUDIO_FILE) as source:
    print("Listning...")
    audio = r.listen(source)
 # Speech recognition using Google Speech Recognition
try:
    # for testing purposes, we're just using the default API key
    # to use another API key, use `r.recognize_google(audio, key="GOOGLE_SPEECH_RECOGNITION_API_KEY")`
    # instead of `r.recognize_google(audio)`
    sent = r.recognize_google(audio)
    stop_words = set(stopwords.words('english'))
    word_tokens = word_tokenize(sent)
    filtered_sentence = [w for w in word_tokens if not w in stop_words]
    filtered_sentence = []
    for w in word_tokens:
        if w not in stop_words:
            filtered_sentence.append(w)
    tag = nltk.pos_tag(filtered_sentence)
    
    fh = open("Stop_Words.txt", "a")
    for i in range(0,len(filtered_sentence)):
        fh.write(' '.join(tag[i])+"\n")
    fh.close()
 
except sr.UnknownValueError:
    print("Google Speech Recognition could not understand audio")
except sr.RequestError as e:
    print("Could not request results from Google Speech Recognition service; {0}".format(e))

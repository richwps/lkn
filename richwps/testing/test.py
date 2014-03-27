#!/bin/py
import requests
import urllib
import time
ENDPOINT = 'http://192.168.56.102:8080/wps/WebProcessingService'
HEADERS = {'Content-Type': 'application/xml'}

def doPost(XMLFILE):
	print(""+XMLFILE+"\n")
	XML = open(XMLFILE).read()
	start_time = time.time()
	r =  requests.post(ENDPOINT, data=XML, headers=HEADERS)
	stop_time = time.time()
	print("reponse ")
	print("status: "+ str(r.status_code))
	if r.status_code is 200:
		# reads normal an chunked messages likewise
		contents = ''.join(r.iter_content(224))
		
		if "exception" in contents:
			print("TEST FAILED!")	
			print("body: "+contents)
			return 
	else:
		print("TEST FAILED!")
		print("body: "+contents)
		return 
	print("TEST OK!")
	#text_file = open("Output.txt", "w")
	#text_file.write(contents)
	#text_file.close()
	exectime  = stop_time-start_time
	print("REQUEST TOOK: "+str(exectime)+" seconds")
	print("\n\n\n")


#doPost("integerlisttest.xml")
#doPost("observationfeaturecollectionlisttest.xml")
doPost("intersectionfeaturecollectionlisttest.xml")
#doPost("intersectionfeaturecollectionlistgeneratortest.xml")

#!/bin/py
import requests
import urllib
import time
ENDPOINT = 'http://192.168.56.102:8080/wps/WebProcessingService'
HEADERS = {'Content-Type': 'application/xml'}
FAIL = '\033[91m'
OKGREEN = '\033[92m'
ENDC = '\033[0m'

def doPost(XMLFILE, store=False, printOut=False, printHeader=False):
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
			print(FAIL+"TEST FAILED!"+ENDC+", - exception raised.")	
			print("body: "+contents)
			return 
		elif len(contents) is 0:
			print(FAIL+"TEST FAILED!"+ENDC+" - empty body.")	
			print("body: "+contents)
			return 
	else:
		print(FAIL+"TEST FAILED!"+ENDC)	
		print("body: "+contents)
		return 
	print(OKGREEN+"TEST OK!"+ENDC)
	if store is True:
		text_file = open(XMLFILE+".OUT", "w")
		text_file.write(contents)
		text_file.close()
	if printOut is True:
		print("body: "+contents)

	exectime  = stop_time-start_time
	print("REQUEST TOOK: "+str(exectime)+" seconds")
	print("\n\n\n")


#doPost("integerlisttest.xml")
#doPost("observationfeaturecollectionlisttest.xml")
#doPost("intersectionfeaturecollectionlisttest.xml")
#doPost("intersectionfeaturecollectionlistgeneratortest.xml")
#doPost("macrophyteassesment.xml")
#doPost("macrophyteassesmentwfsreferences.xml", False, True)
#doPost("macrophyteassesmenthttpreferences.xml", False, True)
doPost("selectreportingareanf.xml", False, True)
doPost("selectreportingareadi.xml", False, True)

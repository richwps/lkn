#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
	Small testsuite for richwps execute calls.
	Takes prepared requests (see folder /requests) and embeds data (see folder /data).
	This tool then commences HTTP POST with requests and roughly validates the outcome.
"""
__author__ = 'dalcacer'
__version__ = '0.2'

import requests
import urllib
import time
import re
import os

#ENDPOINT = 'http://192.168.56.101:8080/wps/WebProcessingService'
ENDPOINT = "http://52n.edvsz.hs-osnabrueck.de/wps/WebProcessingService"
HEADERS = {'Content-Type': 'application/xml'}

# colors
FAIL = '\033[91m'
OKGREEN = '\033[92m'
ENDC = '\033[0m'

def doPost(XMLFILE, store=False, printOut=False, printHeader=False):
	"""
	HTTP-POSTs the contents of an xmlfile to the above described endpoint.
	"""
	# prepare the request.
	XMLPATH = os.path.join('requests',XMLFILE)
	XML = open(XMLPATH).read()
	
	print("\n\n"+XMLPATH)

	# if necessary substitue variables ($$msrld5.json$$) with real data.
	XML = expand(XML)
	#perform request
	start_time = time.time()
	r =  requests.post(ENDPOINT, data=XML, headers=HEADERS)
	stop_time = time.time()
	
	print("status: "+ str(r.status_code))
	contents = ''.join(r.iter_content(224))
	
	if r.status_code is 200:
		# reads normal and chunked messages likewise
		
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
	print("\n")

def expand(XML):
	"""
	expands occurences of #filename.ext# and replaces them with real
	data, which can be found in the folder \data.
	"""
	vars = re.findall(r'\#\w*.\w*\#', XML)
	for s in vars:
	 	DATAPATH = os.path.join('data',s[1:-1])
		DATA = open(DATAPATH).read()
		XML = re.sub(s,DATA, XML)
	return XML


# Test custom datatypes
#doPost("integerlisttest.xml", False, False, True)
#doPost("observationfeaturecollectionlisttest.xml", False, False, True)
#doPost("intersectionfeaturecollectionlisttest.xml")
#doPost("intersectionfeaturecollectionlistgeneratortest.xml")

#doPost("selectreportingareanf.xml", False, False, True)
#doPost("selectreportingareadi.xml", False, False, True)
#doPost("selectreportingareadi-kvp.xml")
#doPost("selecttopography.xml", True, True, True)
doPost("selecttopographykvp.xml", False, False, True)
#doPost("intersectdi.xml", False, False, True)
#doPost("macrophyteassessment.xml", False, True, True)
#doPost("macrophyteassessmentkvpreferences.xml", False, True, True)
#doPost("macrophyteassessment.xml", False, False, True)
#doPost("macrophyteassessmentchain.xml", False, True, True)

#not working, yet
#doPost("macrophyteassessmentreferences.xml", False, True, True)
#doPost("macrophyteassessment-rola.xml", False, False, True)


#doPost("macrophyteassesmentwfsreferences.xml", False, True)
#doPost("macrophyteassesmenthttpreferences.xml", False, True)

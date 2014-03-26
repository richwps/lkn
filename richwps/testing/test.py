#!/bin/py
import requests
import urllib

ENDPOINT = 'http://192.168.56.102:8080/wps/WebProcessingService'


INTEGERLISTXML = open('./integerlisttest.xml').read()
HEADERS = {'Content-Type': 'application/xml'}

def doPost(XMLFILE):
	print("\n\n\n"+XMLFILE+"\n\n\n")
	XML = open(XMLFILE).read()
	r =  requests.post(ENDPOINT, data=XML, headers=HEADERS)
	print("reponse:")
	print("status"+ str(r.status_code))
	if r.status_code is 200:
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


doPost("integerlisttest.xml")
doPost("observationfeaturecollectionlisttest.xml")

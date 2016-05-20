import json
import requests
import time

ip="http://10.110.110.83"
headers ={'Content-type':'application/json','Accept':'text/plain'}

url = ip+'/api/3T4xrbTSJXLwJG8BqQkwa7MjF4fSniJLMpV-GELV/lights/1/state'
try:
        while True:
                pdata = {"on":True, "sat":254, "bri":50,"hue":25500}

                r = requests.put(url,data=json.dumps(pdata),headers=headers)

                print (r.text)

                time.sleep(0.5)

                pdata = {"on":False, "sat":254, "bri":50,"hue":25500}

                r = requests.put(url,data=json.dumps(pdata),headers=headers)
                print (r.text)
                time.sleep(0.5)
                

                pdata = {"on":True, "sat":100, "bri":70,"hue":12371}

                r = requests.put(url,data=json.dumps(pdata),headers=headers)

                print (r.text)

                time.sleep(0.5)

                pdata = {"on":False, "sat":100, "bri":70,"hue":12371}

                r = requests.put(url,data=json.dumps(pdata),headers=headers)
                print (r.text)
                time.sleep(0.5)
                

except KeyboardInterrupt:
        print ("End")

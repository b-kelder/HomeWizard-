import paho.mqtt.client as mqtt
import urllib.request, json
import socket

import hashlib
import base64

import sys, getopt

# Tries to logon to a homewizard account and returns a json object with the data
# Returns None on error
def homewizard_logon(username, password):
    url = "https://cloud.homewizard.com/account/login"

    authInfo = username + ":" + hashlib.sha1(password.encode("ascii")).hexdigest()
    req = urllib.request.Request(
    url, 
    data=None, 
    headers={
            'Authorization' : 'Basic ' + base64.b64encode(authInfo.encode("ascii")).decode("utf-8")
        }
    )

    try:
        response = urllib.request.urlopen(req)
    except:
        print("Could not reach https://cloud.homewizard.com/account/login")
    else:
        result = response.read().decode("utf-8")
        data = json.loads(result)
        return data

    return None

# Connects to a homewizard via the internet using the account's username and password
# if local is True then it will try to find a homewizard on the local network
# and connect directly via url. In this case password should be the homewizard's password
# Returns None on failure
def homewizard_connect(username, password, local=False):
    if(local):
        try:
            response = urllib.request.urlopen("http://gateway.homewizard.nl/discovery.php")
        except urllib.request.URLError:
            print("Could not reach http://gateway.homewizard.nl/discovery.php")
            return None
        else:
            data = json.loads(response.read().decode("utf-8"))
            if(data["status"] == "ok" and data["ip"] != ""):
                url = "http://" + data["ip"] + "/" + password
                print("Got local HomeWizard url", url)
                return url
    else:
        jsonData = homewizard_logon(username, password)
        if(jsonData != None):
            if(jsonData["status"] == "ok"):
                sessionid = jsonData["session"]
                serial = jsonData["response"][0]["serial"]

                url = "https://cloud.homewizard.com/forward/" + sessionid + "/" + serial
                print("Got HomeWizard url", url)
                return url
            else:
                print("Logon failed with error code", jsonData["error"], jsonData["errorMessage"])
        return None



# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print("Connected with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    #client.subscribe("$SYS/broker/log/M/#")
    client.subscribe(homewizardBaseTopic + "/#")
    client.subscribe(homewizardBaseTopic)

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    if(msg.topic.startswith(homewizardBaseTopic)):
        if(msg.payload.startswith(b"RETURN:")):
            # Return value from a homewizard?
            # How did this end up here?
            print("Return message recieved")
            print(msg.topic, str(msg.payload))
        else:
            # Not a return, request homewizard data
            # TODO: THREADS/CALLBACK
            # url is base url plus topic minus the base topic
            print("Recieved message for HomeWizard")
            print(msg.topic, str(msg.payload))
            try:
                url = homewizardBaseUrl + msg.topic.replace(homewizardBaseTopic, "") + "/" + msg.payload.decode("utf-8")
                response = urllib.request.urlopen(url)
                # data = json.loads(response.read().decode("utf-8"))
                # TODO: QoS?
                #publish result on base return topic with same topic as incoming message
                client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), response.read().decode("utf-8"))
            except urllib.request.URLError:
                print("HomeWizard url could not be reached")

#CODE START

#parse params
argv = sys.argv[1:]

# HomeWizard base topic. MUST NOT END WITH A FORWARD SLASH
homewizardBaseTopic = "HMWZ"
homewizardBaseReturnTopic = "HMWZRETURN"

username = None#"bram.kelder@student.stenden.com"
password = None#"1234567890"
local = False
brokerAddr = None#"10.110.111.141"

try:
    opts, args = getopt.getopt(argv,"hu:p:lb:")
except getopt.GetoptError:
    print("INPUT ERROR")
    sys.exit()
else:
    for opt, arg in opts:
        if opt == '-h':
            print("TODO: Help")
            sys.exit()
        elif opt in ("-u"):
            username = arg
        elif opt in ("-p"):
            password = arg
        elif opt in ("-l"):
            local = True
        elif opt in ("-b"):
            brokerAddr = arg
    if username is not None:
        if password is None:
            print("Password required for username", username)
            sys.exit()
    else:
        if local is not None:
            if password is None:
                print("Password required for local HomeWizard")
                sys.exit()
        else:
            print("Username and password required")
            sys.exit()
        
    if brokerAddr is None:
        print("MQTT Broker address required")
        sys.exit()

homewizardBaseUrl = homewizard_connect(username, password, local)
if homewizardBaseUrl is None:
    print("Could not connect to HomeWizard")
    sys.exit()

print("Snake Hydra Protocol Translator - V0.1")
print("--------------------------------------")

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# Start the loop
try:
    client.connect(brokerAddr, 1883, 60)
    client.loop_forever()
except socket.gaierror:
    print("Could not connect to server at", brokerAddr)
    print("Possible soloutions:")
    print("Check network connection")
    print("Check server address")
except ConnectionAbortedError:
    print("Connection aborted")
except:
    print("An unexpected error occured")

# Test?
# client.publish(homewizardBaseTopic + "get-status")





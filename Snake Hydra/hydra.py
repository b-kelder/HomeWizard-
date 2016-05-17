# MQTT/HomeWizard
import paho.mqtt.client as mqtt
import urllib.request, json
# Errors
import socket
# Timestamps
import time
import datetime
# HomeWizard auth info
import hashlib
import base64
# Async message handling
import threading
# Launch param parsing
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
                #TODO: Support for accounts with multiple HomeWizards?
                sessionid = jsonData["session"]
                serial = jsonData["response"][0]["serial"]

                url = "https://cloud.homewizard.com/forward/" + sessionid + "/" + serial
                print("Got HomeWizard url", url)
                return url
            else:
                print("Logon failed with error code", jsonData["error"], jsonData["errorMessage"])
        return None

# Returns a string with the current date and time
def get_time_string():
    return datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')


# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print(get_time_string(), "Connected to MQTT server with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    client.subscribe(homewizardBaseTopic + "/#")
    client.subscribe(homewizardBaseTopic)
    client.subscribe(hydraStatusTopic)

# Tries to access a HomeWizard url based on topic and payload of the message
# When successful the result is published on the return topic
# Will try to reconnect if connection to the HomeWizard is lost
# attempt is the amount of attempts we're at
def message_handler(client, userdata, msg, attempt):
    global homewizardBaseUrl

    # Look, we tried 7 times already, it's not happening
    if(attempt > 7):
        print(get_time_string(), "Hit attempt limit for message at", msg.topic, str(msg.payload))
        return
    
    try:
        url = homewizardBaseUrl + msg.topic.replace(homewizardBaseTopic, "") + "/" + msg.payload.decode("utf-8")
        response = urllib.request.urlopen(url)    
    except urllib.request.URLError:
        print(get_time_string(), "HomeWizard url could not be reached for", msg.topic, str(msg.payload))
    else:
        # TODO: QoS?
        # Publish result on base return topic with same topic as incoming message
        print(get_time_string(), "Processed message for HomeWizard at", msg.topic, str(msg.payload))
        result = response.read().decode("utf-8")
        jsonData = json.loads(result)
        
        if(jsonData["status"] == "failed"):
            print(get_time_string(), "Failed request at", msg.topic, str(msg.payload), "with error", jsonData["error"], jsonData["errorMessage"])
            # Try to reconnect
            # TODO: This may happen at multiple threads at the same time, which is redundant. Join threads first or something.
            homewizardBaseUrl = homewizard_connect(username, password, local)
            if homewizardBaseUrl is None:
                print(get_time_string(), "Could not reconnect to HomeWizard")
            else:
                # Try again after the reconnect
                print(get_time_string(), "Reconnected to HomeWizard, retrying message, attempt", attempt + 1)
                message_handler(client, userdata, msg, attempt + 1)
        else:
            client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), result)

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    if(msg.topic.startswith(homewizardBaseTopic)):
        if(msg.payload.startswith(b"RETURN:")):
            pass
        else:
            # Not a return, request homewizard data
            # url is base url plus topic minus the base topic
            print(get_time_string(), "Recieved message for HomeWizard at", msg.topic, str(msg.payload))
            # Launch thread
            threading.Thread(target=message_handler, args=(client, userdata, msg, 1)).start()
    elif(msg.topic.startswith(hydraStatusTopic)):
        # Respond to hail with HYDRA to indicate we are still running
        if(msg.payload.startswith(b"HAIL")):
            print(get_time_string(), "Responding to hail")
            client.publish(hydraStatusTopic, "HYDRA")


# ------------------------------------------------------#
#                                                       #
#                        GLOBALS                        #
#                                                       #
# ------------------------------------------------------#

# HomeWizard base topics
homewizardBaseTopic = "HMWZ"
homewizardBaseReturnTopic = "HMWZRETURN"

# Hydra status topic
hydraStatusTopic = "HYDRA"

# Data
username = None
password = None
local = False
brokerAddr = None

# ------------------------------------------------------#
#                                                       #
#                   Script code start                   #
#                                                       #
# ------------------------------------------------------#
                
print("Snake Hydra Protocol Translator - V0.3")
print("--------------------------------------")

# Parse params
argv = sys.argv[1:]

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

# Start MQTT client

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# TODO: Proper error handling
try:
    client.connect(brokerAddr, 1883, 60)
except socket.gaierror:
    print("Could not connect to server at", brokerAddr)
    print("Possible soloutions:")
    print("Check network connection")
    print("Check server address")
except ConnectionAbortedError:
    print("Connection aborted")
else:
    client.loop_forever()





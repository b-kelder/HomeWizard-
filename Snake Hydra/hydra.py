# MQTT/HomeWizard
import paho.mqtt.client as mqtt
import urllib.request, json
# Errors
import socket
import http.client
# Timestamps
import time
import datetime
# HomeWizard auth info
import hashlib
import base64
# Async message handling
import threading
import queue
# Launch param parsing
import sys, getopt
# Hue
import phue

BASE_QOS = 2
# TODO: Remove this
HARDCODED_CONNECT = True


# Returns a string with the current date and time
def get_time_string():
    return datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')

# Returns the serial of a HomeWizard cloud URL
def get_serial(url):
    return url.rsplit('/', 1)[-1]

#
# Tries to logon to a homewizard account and returns an object with the json result
# Returns None on error
#
def homewizard_logon(username, password):
    url = "https://cloud.homewizard.com/account/login"

    try:
        authInfo = username + ":" + hashlib.sha1(password.encode("ascii")).hexdigest()
        req = urllib.request.Request(
        url, 
        data=None, 
        headers={
                'Authorization' : 'Basic ' + base64.b64encode(authInfo.encode("ascii")).decode("utf-8")
            }
        )
    except:
        print("Could not create authInfo")
    else:
        try:
            response = urllib.request.urlopen(req)
        except:
            print("Could not reach https://cloud.homewizard.com/account/login")
        else:
            result = response.read().decode("utf-8")
            data = json.loads(result)
        return data

    return None


#
# Connects to a homewizard via the internet using the account's username and password
# if local is True then it will try to find a homewizard on the local network
# and connect directly via url. In this case password should be the homewizard's password
# Returns None on failure
#
def homewizard_connect(username, password, local=False, ip=None):
    # TODO: Remove this
    if(HARDCODED_CONNECT):
        #return "http://192.168.1.104/1234567890"
        pass

    if(local):
        if ip is None:
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
            url = "http://" + ip + "/" + password
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

#
# Client connected (CONNACK recieved) callback
#
def on_connect(client, userdata, flags, rc):
    print(get_time_string(), "Connected to MQTT server with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    client.subscribe(homewizardBaseTopic + "/#")
    client.subscribe(homewizardBaseTopic)
    client.subscribe(hydraStatusTopic)
    client.subscribe(hydraAuthTopic)
    client.subscribe(hueBaseTopic + "/#")
    client.subscribe(hueBaseTopic)

#
# PUBLISH Message recieved callback
#
def on_message(client, userdata, msg):
    global messageQueue
    global connectAuthThread
    
    if(msg.topic.startswith(homewizardBaseTopic)):
        print(get_time_string(), "Recieved message for HomeWizard at", msg.topic, str(msg.payload))
        # Process each message in a seperate thread to minimise waiting for IO (url requests)
        if homewizardBaseUrl is not None:
            threading.Thread(target = process_message, args = (client, msg, 1)).start()
        else:
            publish_fail_msg(client, msg, 4)
            
    elif(msg.topic.startswith(hydraStatusTopic)):
        # get-status returns some data about our status
        if(msg.payload.decode("utf-8") == "get-status"):
            serial = ""
            if homewizardBaseUrl is not None:
                serial = get_serial(homewizardBaseUrl)
            client.publish(hydraStatusTopic + "/results", json.dumps({
                'status': 'ok',
                'request': {
                    'route': 'hydrastatus'},
                'serial': serial}))
    
    elif(msg.topic.startswith(hydraAuthTopic)):
        # Only accept an auth message if we aren't processing one yet
        if((connectAuthThread is None) or (connectAuthThread.is_alive() == False)):
            connectAuthThread = threading.Thread(target = process_auth_request, args = (client, msg))
            connectAuthThread.start()

    elif(msg.topic.startswith(hueBaseTopic)):
        threading.Thread(target = process_hue_message, args = (client, msg)).start()

        
#
# Publish a message containing a json string with error data
# This is used when trying to log into the HomeWizard
#
def publish_auth_fail_msg(client, error):
    # The format of this object is similar to the HomeWizard's own errors
    errorMsg = {70: 'Could not login to HomeWizard',
                71: 'Invalid payload',
                72: 'Already connected to HomeWizard'}
    
    data = {'status': 'failed_hydra',
            'error': error,
            'errorMessage': errorMsg[error],
            'request':{'route': 'hydralogin'}}
    string = json.dumps(data)
    client.publish(hydraAuthTopic + "/results", string, BASE_QOS)


#
# Publish a message containing a json string with error data
# This is send when we have an error when processing a message
# and we have no response from the HomeWizard
#
def publish_fail_msg(client, msg, error):
    # The format of this object is similar to the HomeWizard's own errors
    errorMsg = {1: 'HomeWizard url could not be reached',
                2: 'Cannot login to HomeWizard',
                3: 'Connection to HomeWizard was lost',
                4: 'Not logged in to any HomeWizard'}
    
    data = {'status': 'failed_hydra',
            'error': error,
            'errorMessage': errorMsg[error],
            'request':{'route':msg.topic.replace(homewizardBaseTopic, "")}}
    string = json.dumps(data)
    client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), string, BASE_QOS)


#
# Processes a HomeWizard login request
# Publishes errors and success on hydraAuthTopic/results
#
def process_auth_request(client, msg):
    global homewizardBaseUrl
    global reconnectThread
    global connectAuthThread

    global username
    global password

    # Wait for a reconnect if there is any running to prevent race conditions
    if((reconnectThread is not None) and (reconnectThread.is_alive())):
        reconnectThread.join()
    reconnectThread = threading.current_thread()
    
    try:
        loginData = json.loads(msg.payload.decode("utf-8"))

        if(loginData["type"] == "login"):
            print(get_time_string(), "Recieved connection request for HomeWizard")
            if homewizardBaseUrl is None:
                urlResult = homewizard_connect(loginData["email"], loginData["password"], False)
                if urlResult is None:
                    print(get_time_string(), "Could not connect to HomeWizard")
                    publish_auth_fail_msg(client, 70)
                else:
                    # Publish result
                    data = {'status': 'ok',
                        'request':{'route': 'hydralogin'},
                        'serial': get_serial(urlResult)}
                    string = json.dumps(data)
                    client.publish(hydraAuthTopic + "/results", string, BASE_QOS)                

                    # Update globals
                    homewizardBaseUrl = urlResult
                    username = loginData["email"]
                    password = loginData["password"]
            else:
                print(get_time_string(), "Already connected to HomeWizard!")
                publish_auth_fail_msg(client, 72)
                
        elif(loginData["type"] == "disconnect"):
            print(get_time_string(), "Recieved disconnect request for HomeWizard")
            client.publish(hydraAuthTopic + "/results", json.dumps({
                'status': 'ok',
                'request': {
                    'route': 'hydradisconnect'},
                'serial': ''}))
            homewizardBaseUrl = None
        else:
            print(get_time_string(), "Invalid login payload")
            publish_auth_fail_msg(client, 71)
    except:
        print(get_time_string(), "Invalid login payload")
        publish_auth_fail_msg(client, 71)


#
# Tries to reconnect to a HomeWizard using the current globals and updates them accordingly
# This is a function so it can be put in a thread
#
def homewizard_reconnect():
    global homewizardBaseUrl
    homewizardBaseUrl = homewizard_connect(username, password, local, ip)


#
# Connect to a Hue Bridge
#
def hue_connect(ip):
    global hueBridge
    hueBridge = phue.Bridge(ip)
    try:
         # Note: If Hue data is saved, then this will always succeed, even if the Hue isn't actually on the network!
        hueBridge.connect()             # Press on the Hue button before this line runs
    except:
        print("Error connecting to Hue", ip, "Check if it's connected to the network and make sure the button is pressed.")
        errorData = {
            "status": "hydra_failed",
            "error": 91,
            "errorMessage": "Connecting to Hue failed",
            "request":{"route": "hueconnect"}}
        client.publish(hueBaseReturnTopic + "/connect", json.dumps(errorData))
    else:
        errorData = {
            "status": "ok",
            "request":{"route": "hueconnect"},
            "username": hueBridge.username}
        client.publish(hueBaseReturnTopic + "/connect", json.dumps(errorData))


#
# Processes an incoming message for the Philips Hue
#
# Actions:
# get-lights                    returns a list of all lights
# set-light                     sets light settings, takes a json object with params for set_light function
# set-name                      sets light name, takes a json object with light id and name
# connect                       connects to a Hue bridge with the IP in the payload
#
def process_hue_message(client, msg):
    action = msg.topic.rsplit('/',1)[1] # Last part of topic is the action
    if(action == "connect"):
        print("Processing HUE connect message")
        hue_connect(msg.payload.decode("utf-8"))    # Connect to a Hue bridge
        return
    if(hueBridge is not None):
        print("Processing HUE message")
        print(msg.payload.decode("utf-8"))
        if(action == "get-lights"):
            try:
                lights = hueBridge.get_light_objects("id")  # Get Hue lights data
            except Exception as e:
                print("Error on HUE get-lights")
            else:
                lightData = []
                for index in lights:
                    l = lights[index]
                    # Make a nice package of the light's status
                    # TODO: Add proper support for other light types
                    if(l.type == "Extended color light"):
                        ld = {"name": l.name,
                              "id": index,
                              "type": l.type,
                              "on": l.on,
                              "colormode": l.colormode,
                              "brightness": l.brightness,
                              "hue": l.hue,
                              "saturation": l.saturation,
                              "xy": l.xy,
                              "colortemp": l.colortemp
                              }
                    else:
                        ld = {"name": l.name,
                              "id": index,
                              "type": l.type,
                              "on": l.on,
                              "brightness": l.brightness
                              }
                    lightData.append(ld)
                jsonData = json.dumps(lightData)
                client.publish(hueBaseReturnTopic + "/" + action, jsonData)
        elif(action == "set-light"):                # Payload has to be a json string containing first and second param 
            try:                                    # hueBridge.set_light(lights, command)
                jsonData = json.loads(msg.payload.decode("utf-8"))
                lights = jsonData["lights"]
                command = jsonData["command"]

                hueBridge.set_light(lights, command)
            except Exception as e:
                print("Error on HUE set-light payload")
        elif(action == "set-name"):                # Payload has to be a json object with light (id) and name (new name)
            try:
                jsonData = json.loads(msg.payload.decode("utf-8"))
                light = jsonData["light"]
                name = jsonData["name"]

                hueBridge.set_light(light, "name", name)
            except Exception as e:
                print("Error on HUE set-name payload")
    else:
        # Not connected to HUE
        print("Processing HUE message, but no Hue")
        errorData = {
            "status": "hydra_failed",
            "error": 99,
            "errorMessage": "Not connected to Hue bridge",
            "request":{"route": action}}
        client.publish(hueBaseReturnTopic + "/" + action, json.dumps(errorData))

#
# Tries to access a HomeWizard url based on topic and payload of the message
# When successful the result is published on the return topic
# Will try to reconnect if connection to the HomeWizard is lost
#
def process_message(client, msg, attempt):
    global homewizardBaseUrl
    global reconnectThread

    # Fail 3: Several attempts failed, just end here
    if(attempt > 3):
        print(get_time_string(), "Hit attempt limit for message at", msg.topic, str(msg.payload))
        publish_fail_msg(client, msg, 3)
        return
    
    try:
        # url consists of topic plus payload. Keep this in mind when sending messages. Slash is added between payload and topic.
        url = homewizardBaseUrl + msg.topic.replace(homewizardBaseTopic, "") + "/" + msg.payload.decode("utf-8")
        response = urllib.request.urlopen(url)    
    except urllib.request.URLError:
        # Fail 1: Can not reach url
        print(get_time_string(), "HomeWizard url could not be reached for", msg.topic, str(msg.payload))
        publish_fail_msg(client, msg, 1)
    except http.client.RemoteDisconnected as e:
        print(get_time_string(), e)
        publish_fail_msg(client, msg, 3)
    else:
        # Publish result on base return topic with same topic as incoming message
        print(get_time_string(), "Processed message for HomeWizard at", msg.topic, str(msg.payload))
        result = response.read().decode("utf-8")
        jsonData = json.loads(result)
        
        if(jsonData["status"] == "failed"):
            if "errorMessage" not in jsonData:
                # Testing shows this is porbably a bad URL, not a connection error
                # We should publish the response anyway
                # TODO: More testing
                print(get_time_string(), "Failed request at", msg.topic, str(msg.payload), "with error", jsonData["error"])
                client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), result, BASE_QOS)
            else:
                # Probably a connection error, try to reconnect
                print(get_time_string(), "Failed request at", msg.topic, str(msg.payload), "with error", jsonData["error"], jsonData["errorMessage"])

                # Wait until reconnect is finished
                if((reconnectThread is None) or (reconnectThread.is_alive() == False)):
                    reconnectThread = threading.Thread(target = homewizard_reconnect)
                    reconnectThread.start()
                    # Give reconnectThread time to start
                    time.sleep(0.1)
                reconnectThread.join()
                
                if homewizardBaseUrl is None:
                    # Fail 2: Can't reconnect
                    print(get_time_string(), "Could not reconnect to HomeWizard")
                    publish_fail_msg(client, msg, 2)
                else:
                    # Try again after the reconnect
                    print(get_time_string(), "Reconnected to HomeWizard at attempt", attempt, ", retrying message")
                    process_message(client, msg, attempt + 1)
        else:
            client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), result, BASE_QOS)


# ------------------------------------------------------#
#                                                       #
#                        GLOBALS                        #
#                                                       #
# ------------------------------------------------------#

# HomeWizard base topics
homewizardBaseTopic = "HYDRA/HMWZ"
homewizardBaseReturnTopic = "HYDRA/HMWZRETURN"
hydraStatusTopic = "HYDRA/STATUS"
hydraAuthTopic = "HYDRA/AUTH"
hueBaseTopic = "HYDRA/HUE"
hueBaseReturnTopic = "HYDRA/HUERETURN"

homewizardBaseUrl = None

# Data
hueIp = None
hueBridge = None

username = None
password = None
local = False
ip = None
brokerAddr = None
brokerPort = None
brokerUser = None
brokerPass = None
tls = False
certFile = None


# Thread for connecting to the HomeWizard
connectAuthThread = None
# Used to prevent multiple message threads from reconnecting at the same time
reconnectThread = None


# ------------------------------------------------------#
#                                                       #
#                   Script code start                   #
#                                                       #
# ------------------------------------------------------#

if __name__ == '__main__':
    
    
    print("Snake Hydra Protocol Translator - V0.45")
    print("--------------------------------------")

    # Parse params
    argv = sys.argv[1:]

    try:
        opts, args = getopt.getopt(argv,"hb:s:x:p:i:l")#u:p:l
    except getopt.GetoptError:
        print("Type -h for help")
        sys.exit()
    else:
        for opt, arg in opts:
            try:
                if opt == '-h':
                    print("Snake Hydra help")
##                    print("-u USERNAME -- Username for the HomeWizard account")
                    print("-p PASSWORD -- Password for the HomeWizard (account)")
                    print("-b IP:PORT  -- IP and Port for MQTT broker")
                    print("-s PATH     -- Path to MQTT server TSL certificate")
                    print("-x USER:PW  -- Username and password for MQTT server")
                    print("-l          -- Connection to HomeWizard local instead of via cloud")
                    print("-i IP       -- Optional HomeWizard IP for local connection")
                    sys.exit()
##                elif opt in ("-u"):
##                    username = arg
                elif opt in ("-p"):
                    password = arg
                elif opt in ("-l"):
                    local = True
                elif opt in ("-b"):
                    brokerAddr = arg.split(':')[0]
                    brokerPort = int(arg.split(':')[1])
                elif opt in ("-s"):
                    certFile = arg
                    tls = True
                elif opt in ("-x"):
                    brokerUser = arg.split(':')[0]
                    brokerPass = arg.split(':')[1]
                elif opt in ("-i"):
                    ip = arg;
            except:
                print("Something went wrong trying to parse the arguments. Please check and try again")
                print("Type -h for help")
                sys.exit()

        if local:
            if password is None:
                print("Password required for local HomeWizard")
                sys.exit()

##        if username is not None:
##            if password is None:
##                print("Password required for username", username)
##                sys.exit()
##        else:
##            if local is not None:
##                if password is None:
##                    print("Password required for local HomeWizard")
##                    sys.exit()
##            else:
##                print("Username and password required")
##                sys.exit()
            
        if brokerAddr is None:
            print("MQTT Broker address and port IP:PORT required")
            sys.exit()

    # Local connect
    if local:
        if ip is None:
            homewizardBaseUrl = homewizard_connect(None, password, local)
            if homewizardBaseUrl is None:
                print("Could not connect to HomeWizard")
                sys.exit()
        else:
            homewizardBaseUrl = homewizard_connect(None, password, local, ip)
            if homewizardBaseUrl is None:
                print("Could not connect to HomeWizard at ip", ip)
                sys.exit()

    #homewizardBaseUrl = homewizard_connect(username, password, local)
    #if homewizardBaseUrl is None:
    #    print("Could not connect to HomeWizard")
    #    sys.exit()

    # MQTT client setup
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    if brokerUser is not None:
        client.username_pw_set(brokerUser, brokerPass)

    try:
        if(tls):
            # Get absolute certificate file path
            import os
            script_dir = os.path.dirname(__file__)
            client.tls_set(os.path.join(script_dir, certFile))
            print("Using cert", certFile)
            
        client.connect(brokerAddr, brokerPort, 60)
    except socket.gaierror:
        print("Could not connect to server at", brokerAddr)
        print("Possible soloutions:")
        print("Check network connection")
        print("Check server address")
    except ConnectionAbortedError:
        print("Connection aborted")
    except Exception as e:
        print("Encountered unexpected exception on startup")
        print(e)
    else:
        client.loop_start()

        # User input
        inputstring = ""
        inp_norm = inputstring.lower()
        while(inp_norm not in('quit', 'exit')):
            inputstring = input("")
            inp_norm = inputstring.lower()
            
            if(inp_norm == "status"):
                print("Status report")
                print("URL:", homewizardBaseUrl)
                if(hueBridge is not None):
                    print("Hue:", hueBridge.ip)
                else:
                    print("Hue: Not connected")
                print("broker:", brokerAddr, ":", brokerPort)
            else:
                # DEBUG: Treat input as python code
                try:
                    exec(inputstring)
                except Exception as e:
                    print(e)
        #
        client.disconnect()




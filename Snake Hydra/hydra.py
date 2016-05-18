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


# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    global messageQueue
    if(msg.topic.startswith(homewizardBaseTopic)):
        print(get_time_string(), "Recieved message for HomeWizard at", msg.topic, str(msg.payload))
        #threading.Thread(target=process_message, args=(client, userdata, msg, 1)).start()
        # Put it in the queue
        messageQueue.put(msg)
    elif(msg.topic.startswith(hydraStatusTopic)):
        # Respond to STS with HYD to indicate we are still running
        if(msg.payload.startswith(b"STS")):
            print(get_time_string(), "Responding to STS request")
            client.publish(hydraStatusTopic, "HYD")

    
# Publish a message containing a json string with error data
# This is send when we have an error when processing a message
# and we have no response from the HomeWizard
def publish_fail_msg(client, msg, error):
    # The format of this json object is similar to the HomeWizard's own errors
    errorMsg = {1: 'Url does not exist',
                2: 'Cannot login to HomeWizard',
                3: 'Connection to HomeWizard was lost'}
    
    data = {'status': 'failed_hydra',
            'error': error,
            'errorMessage': errorMsg[error],
            'request':{'route':msg.topic.replace(homewizardBaseTopic, "")}}
    string = json.dumps(data)
    client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), string)


# Loop that handles processing messages in the queue
# Spawns threads to handle each message
def message_processing_loop(client, msgQueue):
    global homewizardBaseUrl    
    while True:
        # Get a message or block until one is available
        message = msgQueue.get()
        threading.Thread(target = process_message, args = (client, message, 1)).start()


# Tries to reconnect to a HomeWizard using the current globals and updates them accordingly
# This is a function so it can be put in a thread
def homewizard_reconnect():
    global homewizardBaseUrl
    homewizardBaseUrl = homewizard_connect(username, password, local)

# Tries to access a HomeWizard url based on topic and payload of the message
# When successful the result is published on the return topic
# Will try to reconnect if connection to the HomeWizard is lost
def process_message(client, msg, attempt):
    global homewizardBaseUrl
    global reconnectThread
    
    # FAIL STATE 3: ATTEMPT LIMIT REACHED
    if(attempt > 3):
        print(get_time_string(), "Hit attempt limit for message at", msg.topic, str(msg.payload))
        publish_fail_msg(client, msg, 3)
        return
    
    try:
        # url is base url plus topic minus the base topic
        url = homewizardBaseUrl + msg.topic.replace(homewizardBaseTopic, "") + "/" + msg.payload.decode("utf-8")
        response = urllib.request.urlopen(url)    
    except urllib.request.URLError:
        # FAIL STATE 1: URL DOES NOT EXIST
        print(get_time_string(), "HomeWizard url could not be reached for", msg.topic, str(msg.payload))
        publish_fail_msg(client, msg, 1)
    except http.client.RemoteDisconnected as e:
        print(get_time_string(), e)
        publish_fail_msg(client, msg, 3)
    else:
        # TODO: QoS?
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
                client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), result)
            else:
                # Probably a connection error, try to reconnect
                print(get_time_string(), "Failed request at", msg.topic, str(msg.payload), "with error", jsonData["error"], jsonData["errorMessage"])

                # Wait until reconnect is finished
                if((reconnectThread is None) or (reconnectThread.is_alive() == False)):
                    reconnectThread = threading.Thread(target = homewizard_reconnect)
                    reconnectThread.start()
                reconnectThread.join()
                
                if homewizardBaseUrl is None:
                    # FAIL STATE 2: FAILED AND CAN'T LOGIN
                    print(get_time_string(), "Could not reconnect to HomeWizard")
                    publish_fail_msg(client, msg, 2)
                else:
                    # Try again after the reconnect
                    print(get_time_string(), "Reconnected to HomeWizard at attempt", attempt, ", retrying message")
                    process_message(client, msg, attempt + 1)
        else:
            client.publish(homewizardBaseReturnTopic + msg.topic.replace(homewizardBaseTopic, ""), result)


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
brokerPort = None
brokerUser = None
brokerPass = None
tls = False
certFile = None

messageQueue = queue.Queue()
messageThread = None

# Thread for connecting to the HomeWizard
# Used to prevent multiple message threads from reconnecting at the same time
reconnectThread = None

# ------------------------------------------------------#
#                                                       #
#                   Script code start                   #
#                                                       #
# ------------------------------------------------------#

if __name__ == '__main__':
    print("Snake Hydra Protocol Translator - V0.3")
    print("--------------------------------------")

    # Parse params
    argv = sys.argv[1:]

    try:
        opts, args = getopt.getopt(argv,"hlu:p:b:s:x:")
    except getopt.GetoptError:
        print("Type -h for help")
        sys.exit()
    else:
        for opt, arg in opts:
            try:
                if opt == '-h':
                    print("Snake Hydra help")
                    print("-u USERNAME -- Username for the HomeWizard account")
                    print("-p PASSWORD -- Password for the HomeWizard (account)")
                    print("-b IP:PORT  -- IP and Port for MQTT broker")
                    print("-s PATH     -- Path to MQTT server TSL certificate")
                    print("-x USER:PW  -- Username and password for MQTT server")
                    print("-l          -- Connection to HomeWizard local instead of via cloud")
                    sys.exit()
                elif opt in ("-u"):
                    username = arg
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
            except:
                print("Something went wrong trying to parse the arguments. Please check and try again")
                print("Type -h for help")
                sys.exit()
                
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
            print("MQTT Broker address and port IP:PORT required")
            sys.exit()

    homewizardBaseUrl = homewizard_connect(username, password, local)
    if homewizardBaseUrl is None:
        print("Could not connect to HomeWizard")
        sys.exit()

    # Start MQTT client
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    if brokerUser is not None:
        client.username_pw_set(brokerUser, brokerPass)

    # TODO: Proper error handling
    # TODO: Port via arguments
    try:
        if(tls):
            ####
            import os
            script_dir = os.path.dirname(__file__) #<-- absolute dir the script is in
            client.tls_set(os.path.join(script_dir, certFile))
            print("Using cert", certFile)
            ####
        client.connect(brokerAddr, brokerPort, 60)
    except socket.gaierror:
        print("Could not connect to server at", brokerAddr)
        print("Possible soloutions:")
        print("Check network connection")
        print("Check server address")
    except ConnectionAbortedError:
        print("Connection aborted")
    else:
        messageThread = threading.Thread(target=message_processing_loop, args=(client, messageQueue))
        messageThread
        messageThread.start()
        client.loop_forever()





import paho.mqtt.client as mqtt
import urllib.request, json
import sys, getopt

# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print("Conected with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    #client.subscribe("$SYS/broker/log/M/#")
    client.subscribe(homewizardBaseReturnTopic + "/#")
    client.subscribe(homewizardBaseReturnTopic)
    client.subscribe(hydraStatusTopic)

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    # Return value from a homewizard
    
    string = msg.payload.decode("utf-8")

    if(msg.topic.startswith(homewizardBaseReturnTopic)):
        print("Message recieved on topic", msg.topic)
        print("")
        try:
            import json
            data = json.loads(string)
            print("Status:", data["status"])
            print("Request:", data["request"]["route"])
            print("RAW:")
        except:
            print("An error occurred while trying to parse as JSON")
        print(string)
    if(msg.topic.startswith(hydraStatusTopic)):
        if(string == "HYD"):
            print("Hydra detected")
    

# Stress TEST
def stress_test(topic, msg, amount, delay):
    for i in range(0, amount):
        client.publish(topic, msg)
        time.sleep(delay)
    print("Finished stress test")
    
argv = sys.argv[1:]
brokerAddr = None
brokerPort = None
brokerUser = None
brokerPass = None
tls = False
certFile = None

try:
    opts, args = getopt.getopt(argv,"hb:s:x:")
except getopt.GetoptError:
    print("Type -h for help")
    sys.exit()
else:
    for opt, arg in opts:
        try:
            if opt == '-h':
                print("Clienttest help")
                print("-b IP:PORT  -- IP and Port for MQTT broker")
                print("-s PATH     -- Path to MQTT server TSL certificate")
                print("-x USER:PW  -- Username and password for MQTT server")
                sys.exit()
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
    if brokerAddr is None:
        print("MQTT Broker address required")
        sys.exit()

# HomeWizard base topics.
homewizardBaseTopic = "HMWZ"
homewizardBaseReturnTopic = "HMWZRETURN"

# Hydra status topic
hydraStatusTopic = "HYDRA"

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
if brokerUser is not None:
    client.username_pw_set(brokerUser, brokerPass)


# Start the loop
try:
    if(tls):
        ####
        import os
        script_dir = os.path.dirname(__file__) #<-- absolute dir the script is in
        client.tls_set(os.path.join(script_dir, certFile))
        print("Using cert", certFile)
        ####
    client.connect(brokerAddr, brokerPort, 60)
except:
    print("Oops")
else:
    client.loop_start()

# TODO: Optimize
import time
time.sleep(0.2)

#client.publish(homewizardBaseTopic, "get-status")
#client.publish(homewizardBaseTopic, "get-sensors")
#client.publish(homewizardBaseTopic, "telist")
#client.publish(homewizardBaseTopic, "swlist")
#client.publish(homewizardBaseTopic, "gplist")
#client.publish(homewizardBaseTopic + "/wea", "get")
inputstring = ""
while inputstring.upper() != "EXIT":
    inputstring = input("")
    if inputstring.upper() == "STS":
        print("Hailing hydra...")
        client.publish(hydraStatusTopic, "STS")
    elif inputstring.upper() == "STATUS":
        print("Requesting status...")
        client.publish(homewizardBaseTopic, "get-status")
    elif inputstring.upper() == "SENSORS":
        print("Requesting sensors...")
        client.publish(homewizardBaseTopic, "get-sensors")
    elif inputstring.upper() == "STRESS":
        stress_test(homewizardBaseTopic, "get-sensors", 10, 0)
    else:
        try:
            exec(inputstring)
        except:
            print("An error occured while parsing")


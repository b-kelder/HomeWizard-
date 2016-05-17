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
    print("Message recieved on topic", msg.topic)
    print("")
    string = msg.payload.decode("utf-8")

    if(msg.topic.startswith(homewizardBaseReturnTopic)):
        import json
        data = json.loads(string)
        print("Status:", data["status"])
        print("Request:", data["request"]["route"])
        print("RAW:")
    print(string)

    
argv = sys.argv[1:]
brokerAddr = None#"10.110.111.141"
try:
    opts, args = getopt.getopt(argv,"hb:")
except getopt.GetoptError:
    print("INPUT ERROR")
    sys.exit()
else:
    for opt, arg in opts:
        if opt == '-h':
            print("TODO: Help")
            sys.exit()
        if opt == '-b':
            brokerAddr = arg
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

# Start the loop
client.connect(brokerAddr, 1883, 60)
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
    if inputstring.upper() == "HAIL":
        print("Hailing hydra...")
        client.publish(hydraStatusTopic, "HAIL")
    elif inputstring.upper() == "STATUS":
        print("Requesting status...")
        client.publish(homewizardBaseTopic, "get-status")
    elif inputstring.upper() == "SENSORS":
        print("Requesting sensors...")
        client.publish(homewizardBaseTopic, "get-sensors")
    else:
        try:
            exec(inputstring)
        except:
            print("An error occured while parsing")


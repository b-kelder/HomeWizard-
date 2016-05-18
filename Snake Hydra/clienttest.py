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
        try:
            import json
            data = json.loads(string)
            print("Status:", data["status"])
            print("Request:", data["request"]["route"])
            print("RAW:")
        except:
            print("An error occurred while trying to parse as JSON")
    print(string)

# Stress TEST
def stress_test(topic, msg, amount, delay):
    for i in range(0, amount):
        client.publish(topic, msg)
        time.sleep(delay)
    print("Finished stress test")
    
argv = sys.argv[1:]
brokerAddr = None#"10.110.111.141"
tls = False
certFile = None

try:
    opts, args = getopt.getopt(argv,"hb:s:")
except getopt.GetoptError:
    print("INPUT ERROR")
    sys.exit()
else:
    for opt, arg in opts:
        if opt == '-h':
            print("TODO: Help")
            sys.exit()
        elif opt == '-b':
            brokerAddr = arg
        elif opt in ("-s"):
            certFile = arg
            tls = True
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
client.username_pw_set("app", "Jr3NKrKA3wcLVy5CMuhZv4kZ")


# Start the loop
try:
    port = 1883
    if(tls):
        port = 8883
        ####
        import os
        script_dir = os.path.dirname(__file__) #<-- absolute dir the script is in
        client.tls_set(os.path.join(script_dir, certFile))
        print("Using cert", certFile)
        ####
    client.connect(brokerAddr, port, 60)
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


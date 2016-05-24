import paho.mqtt.client as mqtt
import json
# Launch param parsing
import sys, getopt
# Import hydra so we can reuse it's generic stuff
import hydra


#
# Updates the base urls, topics and subscriptions according to a HomeWizard cloud url
#
def set_topics(serial = None):

    # Clean up subscriptions
    try:
        # This won't work the first time because all these are None
        client.unsubscribe(hydra.homewizardBaseReturnTopic + "/#")
        client.unsubscribe(hydra.homewizardBaseReturnTopic)
        client.unsubscribe(hydra.hydraStatusTopic + "/results")
        client.unsubscribe(hydra.hydraAuthTopic + "/results")
    except:
        pass
    
    
    # Reset all 
    hydra.homewizardBaseTopic = "HYDRA/HMWZ"
    hydra.homewizardBaseReturnTopic = "HYDRA/HMWZRETURN"
    hydra.hydraStatusTopic = "HYDRA/STATUS"
    hydra.hydraAuthTopic = "HYDRA/AUTH"

    serial = None

    if serial is not None:
            
        hydra.homewizardBaseTopic += "/" + serial
        hydra.homewizardBaseReturnTopic += "/" + serial
        hydra.hydraStatusTopic += "/" + serial
        hydra.hydraAuthTopic += "/" + serial

    # Resub
    client.subscribe(hydra.homewizardBaseReturnTopic + "/#")
    client.subscribe(hydra.homewizardBaseReturnTopic)
    client.subscribe(hydra.hydraStatusTopic + "/results")
    client.subscribe(hydra.hydraAuthTopic + "/results")

    #print(hydra.homewizardBaseTopic)
    #print(hydra.homewizardBaseReturnTopic)
    #print(hydra.hydraStatusTopic)
    #print(hydra.hydraAuthTopic)


# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print("Conected with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    #client.subscribe("$SYS/broker/log/M/#")
    client.subscribe(hydra.homewizardBaseReturnTopic + "/#")
    client.subscribe(hydra.homewizardBaseReturnTopic)
    client.subscribe(hydra.hydraStatusTopic + "/results")
    client.subscribe(hydra.hydraAuthTopic + "/results")

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    # Return value from a homewizard
    
    string = msg.payload.decode("utf-8")

    if(msg.topic.startswith(hydra.homewizardBaseReturnTopic)):
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
    elif(msg.topic.startswith(hydra.hydraStatusTopic)):
        print(msg.payload.decode("utf-8"))
    elif(msg.topic.startswith(hydra.hydraAuthTopic)):
        import json
        data = json.loads(string)
        if(data["status"] == "ok"):
            if(data["request"]["route"] == "hydradisconnect"):
                print("Disconnected HomeWizard")
            elif(data["request"]["route"] == "hydralogin"):
                print("Login successful")
            # Check for serial
            if data["serial"]:
                set_topics(data["serial"])
            else:
                set_topics(None)
        else:
            print(data["errorMessage"])
    else:
        print(string)


# Logs in to hydra/homewizard
def login(email, password):
    data = {'type': 'login', 'email':email, 'password':password}
    string = json.dumps(data)
    client.publish(hydra.hydraAuthTopic, string)

# Tell hydra to disconnect
def disconnect():
    data = {'type': 'disconnect', 'email': '', 'password': ''}
    string = json.dumps(data)
    client.publish(hydra.hydraAuthTopic, string)

# Stress test
def stress_test(topic, msg, amount, delay):
    for i in range(0, amount):
        client.publish(topic, msg)
        time.sleep(delay)
    print("Finished stress test")


argv = sys.argv[1:]

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
                hydra.brokerAddr = arg.split(':')[0]
                hydra.brokerPort = int(arg.split(':')[1])
            elif opt in ("-s"):
                hydra.certFile = arg
                hydra.tls = True
            elif opt in ("-x"):
                hydra.brokerUser = arg.split(':')[0]
                hydra.brokerPass = arg.split(':')[1]
        except:
            print("Something went wrong trying to parse the arguments. Please check and try again")
            print("Type -h for help")
            sys.exit()
    if hydra.brokerAddr is None:
        print("MQTT Broker address required")
        sys.exit()


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
if hydra.brokerUser is not None:
    client.username_pw_set(hydra.brokerUser, hydra.brokerPass)
set_topics()

# Start the loop
try:
    if(hydra.tls):
        ####
        import os
        script_dir = os.path.dirname(__file__) #<-- absolute dir the script is in
        client.tls_set(os.path.join(script_dir, hydra.certFile))
        print("Using cert", hydra.certFile)
        ####
    client.connect(hydra.brokerAddr, hydra.brokerPort, 60)
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
        client.publish(hydra.hydraStatusTopic, "get-status")
    elif inputstring.upper() == "STATUS":
        print("Requesting status...")
        client.publish(hydra.homewizardBaseTopic, "get-status")
    elif inputstring.upper() == "SENSORS":
        print("Requesting sensors...")
        client.publish(hydra.homewizardBaseTopic, "get-sensors")
    elif inputstring.upper() == "STRESS":
        stress_test(homewizardBaseTopic, "get-sensors", 10, 0)
    elif inputstring.upper() == "LOGIN":
        login("bram.kelder@student.stenden.com", "1234567890")
    elif inputstring.upper() == "DISCONNECT":
        disconnect()
    else:
        try:
            exec(inputstring)
        except Exception as e:
            print(e)


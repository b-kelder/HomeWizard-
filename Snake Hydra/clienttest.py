import paho.mqtt.client as mqtt
import urllib.request, json

# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print("Conected with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    #client.subscribe("$SYS/broker/log/M/#")
    client.subscribe(homewizardBaseReturnTopic + "/#")
    client.subscribe(homewizardBaseReturnTopic)

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    # Return value from a homewizard
    print("Return message recieved on topic", msg.topic)
    print("")
    string = msg.payload.decode("utf-8")
    import json
    data = json.loads(string)
    print("Status:", data["status"])
    print("Request:", data["request"]["route"])
    print("RAW:")
    print(string)

    


# HomeWizard base topics.
homewizardBaseTopic = "HMWZ"
homewizardBaseReturnTopic = "HMWZRETURN"

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# Start the loop
client.connect("10.110.111.141", 1883, 60)
client.loop_start()

# TODO: Optimize
import time
time.sleep(0.2)

client.publish(homewizardBaseTopic, "get-status")
client.publish(homewizardBaseTopic, "get-sensors")
#client.publish(homewizardBaseTopic, "telist")
#client.publish(homewizardBaseTopic, "swlist")
#client.publish(homewizardBaseTopic, "gplist")
#client.publish(homewizardBaseTopic + "/wea", "get")



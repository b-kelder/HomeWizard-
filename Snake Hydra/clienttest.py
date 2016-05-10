import paho.mqtt.client as mqtt
import urllib.request, json

# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print("Connected with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    #client.subscribe("$SYS/broker/log/M/#")
    client.subscribe(homewizardBaseTopic + "/#")

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    if(msg.payload.startswith(b"RETURN:")):
        # Return value from a homewizard?
        print("Return message recieved")
        print(msg.topic, str(msg.payload))
        
        import json
        string = msg.payload[7:].decode("utf-8")
        print(string)
        data = json.loads(string)
        print(data["time"])
        print(data["status"])


# HomeWizard base topic.
homewizardBaseTopic = "HMWZ"

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# Start the loop
client.connect("test.mosquitto.org", 1883, 60)
client.loop_start()

# TODO: Optimize
import time
time.sleep(0.2)

client.publish(homewizardBaseTopic, "get-sensors")

time.sleep(0.2)
# Switch test
client.publish(homewizardBaseTopic + "/sw/1", "on")

time.sleep(0.2)
client.publish(homewizardBaseTopic + "/sw/1", "off")

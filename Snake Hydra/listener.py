import paho.mqtt.client as mqtt
import urllib.request, json

# Client connected (CONNACK recieved) callback
def on_connect(client, userdata, flags, rc):
    print("Connected with result code", str(rc))

    # subscribe here to make sure we resub after a reconnect
    #client.subscribe("$SYS/broker/log/M/#")
    client.subscribe(homewizardBaseTopic + "#")

# PUBLISH Message recieved callback
def on_message(client, userdata, msg):
    if(msg.payload.startswith(b"RETURN:")):
        # Return value from a homewizard?
        print("Return message recieved")
        print(msg.topic, str(msg.payload))


# HomeWizard base topic. MUST END WITH A FORWARD SLASH
homewizardBaseTopic = "HMWZ/"


client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# Start the loop
client.connect("test.mosquitto.org", 1883, 60)
client.loop_forever()


# Test?





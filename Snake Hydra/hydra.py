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
    if(msg.topic.startswith(homewizardBaseTopic)):
        if(msg.payload.startswith(b"RETURN:")):
            # Return value from a homewizard?
            pass
            # How did this end up here?
            # print("Return message recieved")
            # print(msg.topic, str(msg.payload))
        else:
            # Not a return, request homewizard data
            # TODO: THREADS/CALLBACK
            # url is base url plus topic minus the base topic
            print("Recieved message for HomeWizard")
            print(msg.topic, str(msg.payload))
            url = homewizardBaseUrl + msg.topic.replace(homewizardBaseTopic, "")
            response = urllib.request.urlopen(url)
            # data = json.loads(response.read().decode("utf-8"))
            # TODO: QoS?
            client.publish(msg.topic, "RETURN:" + response.read().decode("utf-8"))

# HomeWizard base topic. MUST END WITH A FORWARD SLASH
homewizardBaseTopic = "HMWZ/"
# HomeWizard base url. MUST END WITH A FORWARD SLASH
homewizardBaseUrl = "http://localhost/homewizard/"

print("Snake Hydra Protocol Translator - V0.1")
print("--------------------------------------")

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# Start the loop
client.connect("test.mosquitto.org", 1883, 60)
client.loop_forever()



# Test?
# client.publish(homewizardBaseTopic + "get-status")





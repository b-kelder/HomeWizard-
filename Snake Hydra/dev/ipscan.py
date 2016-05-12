import urllib.request

with open("ips.txt") as file:
    lines = [line.rstrip('\n').strip() for line in file]
    
for line in lines:
    try:
        url = "http://" + line;
        response = urllib.request.urlopen(url)
        
        # data = json.loads(response.read().decode("utf-8"))
        # TODO: QoS?
    except urllib.request.URLError:
        print(line + " could not be reached")
    else:
        print("-----------------------------------------------")
        print("FOUND SERVER ON IP", line)
        print("")
        print("")
        print(response.read().decode("utf-8"))
        print("")
        print("")

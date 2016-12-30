########### Python 2.7 #############
import httplib, urllib, base64
import sys
import json
image1 = sys.argv[1]
image2 = sys.argv[2]
encoded_string = ""
with open(image1, "rb") as image_file:
    encoded_string1 = image_file.read()

with open(image2, "rb") as image_file:
    encoded_string2 = image_file.read()

# print encoded_string
headers = {
    # Request headers
    'Content-Type': 'application/octet-stream',
    'Ocp-Apim-Subscription-Key': '508d0bd1ffb04071a74d01fcbb16224f',
}

params = urllib.urlencode({
    # Request parameters
    'returnFaceId': 'true',
    'returnFaceLandmarks': 'false',
    # 'returnFaceAttributes': '{string}',
})

# try:
conn = httplib.HTTPSConnection('api.projectoxford.ai')
conn.request("POST", "/face/v1.0/detect?%s" % params, encoded_string1, headers)
response = conn.getresponse()
data = response.read()
data = data.replace('[', '')
data = data.replace(']', '')
data = json.loads(data)
faceId1 = str(data['faceId'])
conn.close()

conn = httplib.HTTPSConnection('api.projectoxford.ai')
conn.request("POST", "/face/v1.0/detect?%s" % params, encoded_string2, headers)
response = conn.getresponse()
data = response.read()
data = data.replace('[', '')
data = data.replace(']', '')
data = json.loads(data)
faceId2 = str(data['faceId'])
conn.close()

headers = {
    # Request headers
    'Content-Type': 'application/json',
    'Ocp-Apim-Subscription-Key': '508d0bd1ffb04071a74d01fcbb16224f',
}

params = urllib.urlencode({
})

body = {
    "faceId1": faceId1,
    "faceId2": faceId2,
}

try:
    conn = httplib.HTTPSConnection('api.projectoxford.ai')
    conn.request("POST", "/face/v1.0/verify?%s" % params, str(body), headers)
    response = conn.getresponse()
    data = response.read()
    data = data.replace('[', '')
    data = data.replace(']', '')
    data = json.loads(data)    
    conn.close()
except Exception as e:
    print("[Errno {0}] {1}".format(e.errno, e.strerror))

isIdentical = data['isIdentical'] # bool
confidence = data['confidence'] # float

# except Exception as e:
    # print("[Errno {0}] {1}".format(e.errno, e.strerror))

####################################

########### Python 3.2 #############
# import http.client, urllib.request, urllib.parse, urllib.error, base64

# headers = {
#     # Request headers
#     'Content-Type': 'application/json',
#     'Ocp-Apim-Subscription-Key': '{subscription key}',
# }

# params = urllib.parse.urlencode({
#     # Request parameters
#     'returnFaceId': 'true',
#     'returnFaceLandmarks': 'false',
#     'returnFaceAttributes': '{string}',
# })

# try:
#     conn = http.client.HTTPSConnection('api.projectoxford.ai')
#     conn.request("POST", "/face/v1.0/detect?%s" % params, "{body}", headers)
#     response = conn.getresponse()
#     data = response.read()
#     print(data)
#     conn.close()
# except Exception as e:
#     print("[Errno {0}] {1}".format(e.errno, e.strerror))

# ####################################
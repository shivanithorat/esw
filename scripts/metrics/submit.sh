#!/usr/bin/env bash

for i in {1..10000}
do
curl -X POST \
  http://localhost:8090/post-endpoint \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 922383f7-8934-437d-abe0-67a72295c5d2' \
  -H 'cache-control: no-cache' \
  -H 'X-Real-IP: 10.131.20.155' \
  -d '{
  "_type": "ComponentCommand",
  "componentId": {
    "prefix": "CSW.ncc.trombone",
    "componentType": "hcd"
  },
  "command": {
    "_type": "Submit",
    "controlCommand": {
      "_type": "Observe",
      "source": "CSW.ncc.trombone",
      "commandName": "immediate",
      "maybeObsId": [
        "obs001"
      ],
      "paramSet": []
    }
  }
}'

sleep 1
done

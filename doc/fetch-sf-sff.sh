curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/service-function:service-functions" -X GET | json_pp; echo;
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/service-function-forwarder:service-function-forwarders" -X GET | json_pp; echo; 

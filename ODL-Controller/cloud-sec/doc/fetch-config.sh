#!/bin/bash
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/opendaylight-inventory:nodes" -X GET | json_pp; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/policy:tenants" -X GET | json_pp; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operational/endpoint:endpoints" -X GET | json_pp; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/service-function-path:service-function-paths" -X GET | json_pp; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/service-function-chain:service-function-chains" -X GET | json_pp; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/service-function:service-functions" -X GET | json_pp; echo;
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/service-function-forwarder:service-function-forwarders" -X GET | json_pp; echo; 

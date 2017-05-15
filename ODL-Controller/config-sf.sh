#!/bin/bash

if [ -z $1 ]
then
	echo "Usage : $0 <controller-ip>"
fi

CONTROLLER=$1

curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/config/cloud-sec:service-function-registry" -X POST \
	-d '{"service-function":[{"address":"192.168.33.12","name":"firewall"}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/config/cloud-sec:service-function-registry" -X POST \
	-d '{"service-function":[{"address":"192.168.33.14","name":"dpi"}]}'
sleep 3
curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST \
	-d '{"service-function-forwarder":[{"address":"192.168.33.11","name":"sff1","ovs-bridge":"sw2","service-functions":[{"sf-name":"firewall"}]}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST \
	-d '{"service-function-forwarder":[{"address":"192.168.33.13","name":"sff2","ovs-bridge":"sw4","service-functions":[{"sf-name":"dpi"}]}]}'

curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/operations/cloud-sec:create-function-path" -X POST -d '{"input":{"unused":"test"}}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/operations/cloud-sec:create-tunnel" -X POST -d '{"input":{"unused":"test"}}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/operations/cloud-sec:create-tenant" -X POST -d '{"input":{"unused":"test"}}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://$CONTROLLER:8181/restconf/operations/cloud-sec:create-endpoints" -X POST -d '{"input":{"unused":"test"}}'

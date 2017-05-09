#!/bin/bash
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry" -X POST \
	-d '{"service-function":[{"address":"172.24.110.14","name":"firewall"}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry" -X POST \
	-d '{"service-function":[{"address":"172.24.110.16","name":"dpi"}]}'
sleep 3
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST \
	-d '{"service-function-forwarder":[{"address":"172.24.110.13","name":"sff1","ovs-bridge":"sw2","service-functions":[{"sf-name":"firewall"}]}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST \
	-d '{"service-function-forwarder":[{"address":"172.24.110.15","name":"sff2","ovs-bridge":"sw4","service-functions":[{"sf-name":"dpi"}]}]}'

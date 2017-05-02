curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry" -X POST -d '{"service-function":[{"address":"10.0.0.1","name":"test-sf1"}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry" -X PUT  -d '{"service-function-registry":{"service-function":[{"address":"10.0.0.2","name":"test-sf1"}]}}'

curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST -d '{"service-function-forwarder":[{"address":"10.0.1.1","name":"test-sff1","ovs-bridge":"sw1","service-functions":[{"sf-name":"test-sf1"}]}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X PUT  -d '{"service-function-forwarder-registry":{"service-function-forwarder":[{"address":"10.0.1.2","name":"test-sff1","ovs-bridge":"sw1","service-functions":[{"sf-name":"test-sf1"}]}]}}'

curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry/service-function-forwarder/test-sff1" -X DELETE
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry/service-function/test-sf1" -X DELETE

# Cloud Sec demo

## Intro

This demo aims at creating an environment in which we have a fully working GBP+SFC infrastructure with the cloud-sec plugin loaded.

It relies on vagrant for machines provisionning (we created a vagrant box using a jessie 64 with python and predictable interfaces names), ansible for softwares installation (openvswitch, docker, opendaylight, ...) and two dockeres to build the plugin and openvswitch.

## Usage

First, we need to build OpenVSwitch and Cloud-Sec, then to instantiate the VMs, and finally to configure them, launch and test the demo.

### Build dependencies

It takes roughtly 20 minutes to build the plugin and 15 to build OVS on a i7 + 16Go RAM.

```
cd Common/DockerBuildOVS
docker build -t build_ovs .
mkdir build
docker run --rm --name build_ovs -v `pwd`/build:/build build_ovs
cd ../../
```

```
cd Common/DockerBuildPlugin
docker build -t build_plugin .
mkdir build
docker run --rm --name build_plugin -v `pwd`/build:/build build_plugin
cd ../../
```

```
cd Common/DockerBuildGBP
docker build -t build_gbp .
mkdir build
docker run --rm --name build_gbp -v `pwd`/build:/build build_gbp
cd ../../
```

Then we need to link the packages to the build folder of the Ansible folder.

```
mkdir Ansible/builds
cd Common
ln -s $PWD/DockerBuildPlugin/build/cloudsec ../Ansible/builds/
ln -s $PWD/DockerBuildOVS/build/*.deb ../Ansible/builds/
ln -s $PWD/DockerBuildGBP/build/*.jar ../Ansible/builds/
cd ../
```

Download the ODL prebuilt package :

```
cd Ansible/builds
wget https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/integration/distribution-karaf/0.6.0-Carbon/distribution-karaf-0.6.0-Carbon.tar.gz
cd ../../
```

### Instantiate VMs

```
cd Common
vagrant up
```

### Configure using Ansible

```
cd Ansible
ansible -i hosts -m ping all -u root
```

Verify that all hosts answer to pings before going on with the script.

```
ansible-playbook -i hosts -u root -s install.yml
../Common/reboot.sh
ansible-playbook -i hosts -u root -s config.yml
```

### Start the Demo

All those commands are done on the ODL controller at cloud7 (`ssh root@192.168.33.16`).

```
cd /root/distribution-karaf-0.6.0-Carbon
screen -mS odl-controller bin/start

opendaylight> feature:install odl-groupbasedpolicy-ofoverlay odl-groupbasedpolicy-ui odl-restconf
opendaylight> repo-add mvn:com.orange.cloudsec/cloud-sec-features/0.1.0-SNAPSHOT/xml/features
opendaylight> feature:install odl-cloud-sec odl-cloud-sec-api

<Ctrl-A><Ctrl-D>

curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry" -X POST \
  -d '{"service-function":[{"address":"192.168.33.12","name":"firewall"}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-registry" -X POST \
  -d '{"service-function":[{"address":"192.168.33.14","name":"dpi"}]}'

sleep 3
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST \
  -d '{"service-function-forwarder":[{"address":"192.168.33.11","name":"sff1","ovs-bridge":"sw2","service-functions":[{"sf-name":"firewall"}]}]}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/config/cloud-sec:service-function-forwarder-registry" -X POST \
  -d '{"service-function-forwarder":[{"address":"192.168.33.13","name":"sff2","ovs-bridge":"sw4","service-functions":[{"sf-name":"dpi"}]}]}'

sleep 3
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-function-path" -X POST -d '{"input":{"unused":"test"}}'; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-tunnel" -X POST -d '{"input":{"unused":"test"}}'; echo

sleep 3
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-tenant" -X POST -d '{"input":{"unused":"test"}}'; echo
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-endpoints" -X POST -d '{"input":{"unused":"test"}}'; echo
```

At this point, the demo should be ready.

### Testing

#### Ping test

```
ssh root@192.168.33.10
cloud1# docker attach h35-2
h35-2# ping 10.0.36.4
**SHOULD WORK**
# Detach from docker using <Ctrl + P> <Ctrl + Q>
cloud1# exit
```

#### HTTP test

```
ssh root@192.168.33.15
cloud6# docker attach h36-4
h36-4# python -m SimpleHTTPServer 80
<Ctrl-P><Ctrl-Q>
cloud6# exit
ssh root@192.168.33.10
cloud1# docker attach h35-2
h35-2# curl http://10.0.36.4
**SHOUD RETURN THE FOLDERS IN / OF h36-4**
# Detach from docker using <Ctrl + P> <Ctrl + Q>
cloud1# exit
```

### Clean

A script has been written to clean the demonstration : 

```
Common/clean-ansible.sh
```

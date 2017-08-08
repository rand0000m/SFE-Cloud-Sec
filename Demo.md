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
ansible-playbook -i hosts -u root -s config.yml
ansible-playbook -i hosts -u root -s launch.yml
```

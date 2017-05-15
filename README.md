# Security Agent Demo

To launch the virtual machines, a VagrantFile has been written in [Common](Common/).

The demo has to be started in X steps :

1. Start the VMs
2. Install the VMs according to function (SFC/SFF/SF)
3. Configure the VMs
4. Configure the SDN controller
5. Test that the demo is working

## Topology

```
Network drawing
```

## Steps

### Starting the VMs

You may change the number of VMs you wish to start by changing `NUM_NODES` in the [VagrantFile](Common/VagrantFile).

```
cd Common
vagrant up
```

### Installing the VMs

Execute the script `install.sh` of the function folder you wish to install for each VM :

```
ssh root@192.168.33.10 /vagrant/SFC/install.sh
ssh root@192.168.33.11 /vagrant/SFF/install.sh
ssh root@192.168.33.12 /vagrant/SF/install.sh
ssh root@192.168.33.13 /vagrant/SFF/install.sh
ssh root@192.168.33.14 /vagrant/SF/install.sh
ssh root@192.168.33.15 /vagrant/SFC/install.sh
ssh root@192.168.33.16 /vagrant/ODL-Controller/install.sh
```

### Configuring the VMs

Use the `config.sh` scripts :

```
ssh root@192.168.33.10 /vagrant/SFC/config.sh --controller=192.168.33.16 --bridge=sw1 --datapath-id=1
ssh root@192.168.33.11 /vagrant/SFF/config.sh 192.168.33.16
ssh root@192.168.33.12 /vagrant/SF/config.sh sw3
ssh root@192.168.33.13 /vagrant/SFF/config.sh 192.168.33.16
ssh root@192.168.33.14 /vagrant/SF/config.sh sw5
ssh root@192.168.33.15 /vagrant/SFC/config.sh --controller=192.168.33.16 --bridge=sw6 --datapath-id=6

ssh root@192.168.33.10 /vagrant/SFC/add-cont.sh --name=h35-2 --ip 10.0.35.2/24 --mac=00:00:00:00:35:02 --broadcast=10.0.35.255 --router-ip=10.0.35.1 --switch=sw1
ssh root@192.168.33.10 /vagrant/SFC/add-cont.sh --name=h35-3 --ip 10.0.35.3/24 --mac=00:00:00:00:35:03 --broadcast=10.0.35.255 --router-ip=10.0.35.1 --switch=sw1
ssh root@192.168.33.15 /vagrant/SFC/add-cont.sh --name=h35-2 --ip 10.0.35.4/24 --mac=00:00:00:00:35:04 --broadcast=10.0.35.255 --router-ip=10.0.35.1 --switch=sw6
ssh root@192.168.33.15 /vagrant/SFC/add-cont.sh --name=h35-2 --ip 10.0.35.5/24 --mac=00:00:00:00:35:05 --broadcast=10.0.35.255 --router-ip=10.0.35.1 --switch=sw6

ssh root@192.168.33.10 /vagrant/SFC/add-cont.sh --name=h36-2 --ip 10.0.36.2/24 --mac=00:00:00:00:36:02 --broadcast=10.0.36.255 --router-ip=10.0.36.1 --switch=sw1
ssh root@192.168.33.10 /vagrant/SFC/add-cont.sh --name=h36-3 --ip 10.0.36.3/24 --mac=00:00:00:00:36:03 --broadcast=10.0.36.255 --router-ip=10.0.36.1 --switch=sw1
ssh root@192.168.33.15 /vagrant/SFC/add-cont.sh --name=h36-2 --ip 10.0.36.4/24 --mac=00:00:00:00:36:04 --broadcast=10.0.36.255 --router-ip=10.0.36.1 --switch=sw6
ssh root@192.168.33.15 /vagrant/SFC/add-cont.sh --name=h36-2 --ip 10.0.36.5/24 --mac=00:00:00:00:36:05 --broadcast=10.0.36.255 --router-ip=10.0.36.1 --switch=sw6
```

### Configuring the SDN Controller

```
ssh root@192.168.33.16 /vagrant/ODL-Controller/config.sh
```

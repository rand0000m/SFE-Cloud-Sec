#!/bin/bash

apt install -y vim dkms git uuid-runtime
if [ -z $1 ]
then
	cd /vagrant/builds
	sudo dpkg -i openvswitch-common_2.5.90-1_amd64.deb openvswitch-switch_2.5.90-1_amd64.deb openvswitch-datapath-dkms_2.5.90-1_all.deb 
else
	mkdir ovs
	cd ovs
	sudo apt-get install -y git autoconf libtool build-essential initramfs-tools firmware-linux-free irqbalance linux-headers-3.16 python-six python-pip graphviz debhelper dh-autoreconf libssl-dev python-all python-qt4 python-twisted-conch python-zopeinterface dkms uuid-runtime vim
	git clone https://github.com/yyang13/ovs_nsh_patches.git
	git clone https://github.com/openvswitch/ovs.git
	cd ovs
	git reset --hard 7d433ae57ebb90cd68e8fa948a096f619ac4e2d8
	cp ../ovs_nsh_patches/*.patch ./
	git am *.patch
	dpkg-checkbuilddeps 
	DEB_BUILD_OPTIONS='parallel=8 nocheck' fakeroot debian/rules binary
	cd ..
	sudo dpkg -i openvswitch-common_2.5.90-1_amd64.deb openvswitch-switch_2.5.90-1_amd64.deb openvswitch-datapath-dkms_2.5.90-1_all.deb 
fi

apt -f -y install

sudo apt-get -y install \
	apt-transport-https \
	ca-certificates \
	curl \
	software-properties-common
curl -fsSL https://download.docker.com/linux/debian/gpg | sudo apt-key add -
echo "deb https://download.docker.com/linux/debian jessie stable" >> /etc/apt/sources.list
sudo apt-get update
sudo apt-get -y install docker-ce
sudo systemctl start docker

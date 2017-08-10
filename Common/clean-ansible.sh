#!/bin/bash

ssh root@192.168.33.16 "/root/distribution-karaf-0.6.0-Carbon/bin/client 'shutdown -f -h -cc 0'"

ssh root@192.168.33.10 /tmp/del-cont.sh --name=h35-2 --switch=bb34
ssh root@192.168.33.10 /tmp/del-cont.sh --name=h35-3 --switch=bb34
ssh root@192.168.33.15 /tmp/del-cont.sh --name=h35-4 --switch=b96c
ssh root@192.168.33.15 /tmp/del-cont.sh --name=h35-5 --switch=b96c
ssh root@192.168.33.10 /tmp/del-cont.sh --name=h36-2 --switch=bb34
ssh root@192.168.33.10 /tmp/del-cont.sh --name=h36-3 --switch=bb34
ssh root@192.168.33.15 /tmp/del-cont.sh --name=h36-4 --switch=b96c
ssh root@192.168.33.15 /tmp/del-cont.sh --name=h36-5 --switch=b96c

ssh root@192.168.33.10 "ovs-vsctl del-br bb34; ovs-vsctl del-manager;"
ssh root@192.168.33.11 "ovs-vsctl del-br sw2; ovs-vsctl del-manager;" 
ssh root@192.168.33.12 "ovs-vsctl del-br be9c;" 
ssh root@192.168.33.13 "ovs-vsctl del-br sw4; ovs-vsctl del-manager;" 
ssh root@192.168.33.14 "ovs-vsctl del-br b40e;" 
ssh root@192.168.33.15 "ovs-vsctl del-br b96c; ovs-vsctl del-manager;" 


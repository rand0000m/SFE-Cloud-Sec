#!/bin/bash

usage(){
	echo "Usage : $0 <switch> <SFF-address>"
	exit 1
}

if [ -z "$1" ]; then
	usage
fi
if [ -z "$1" ]; then
	usage
fi

SWITCH=$1

SFF=$2

SFF_HEX_IP=$(python -c "import binascii; import socket; print '0x' + binascii.hexlify(socket.inet_aton('$SFF')).upper()");

sudo ovs-ofctl --strict del-flows $SWITCH priority=0
sudo ovs-ofctl add-flow $SWITCH "priority=1000,nsi=255 actions=move:NXM_NX_NSH_MDTYPE[]->NXM_NX_NSH_MDTYPE[],move:NXM_NX_NSH_NP[]->NXM_NX_NSH_NP[],move:NXM_NX_NSP[]->NXM_NX_NSP[],load:254->NXM_NX_NSI[],move:NXM_NX_NSH_C1[]->NXM_NX_NSH_C1[],move:NXM_NX_NSH_C2[]->NXM_NX_NSH_C2[],move:NXM_NX_TUN_ID[0..31]->NXM_NX_TUN_ID[0..31],load:$SFF_HEX_IP->NXM_NX_TUN_IPV4_DST[],load:0x4->NXM_NX_TUN_GPE_NP[],IN_PORT" -OOpenFlow13
sudo ovs-ofctl add-flow $SWITCH "priority=1000,nsi=254 actions=move:NXM_NX_NSH_MDTYPE[]->NXM_NX_NSH_MDTYPE[],move:NXM_NX_NSH_NP[]->NXM_NX_NSH_NP[],move:NXM_NX_NSP[]->NXM_NX_NSP[],load:253->NXM_NX_NSI[],move:NXM_NX_NSH_C1[]->NXM_NX_NSH_C1[],move:NXM_NX_NSH_C2[]->NXM_NX_NSH_C2[],move:NXM_NX_TUN_ID[0..31]->NXM_NX_TUN_ID[0..31],load:$SFF_HEX_IP->NXM_NX_TUN_IPV4_DST[],load:0x4->NXM_NX_TUN_GPE_NP[],IN_PORT" -OOpenFlow13

/*
 * Copyright © 2017 Mathieu Rousse and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.orange.cloudsec.impl;

import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.JProtocol;
import org.jnetpcap.protocol.lan.Ethernet;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CloudSecProvider implements DataTreeChangeListener<Node>, AutoCloseable, PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    private final DataBroker dataBroker;
    private final NotificationService notificationService;
    private final SalFlowService salFlowService;
    private PacketProcessingService packetProcessingService;
    private final RpcProviderRegistry rpcProviderRegistry;

    private List<Switch> switches;

    public CloudSecProvider(final DataBroker dataBroker,
                            final NotificationService notificationService,
                            final SalFlowService salFlowService,
                            final RpcProviderRegistry rpcProviderRegistry) {

        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.salFlowService = salFlowService;
        this.rpcProviderRegistry = rpcProviderRegistry;

        switches = new ArrayList<>();
    }

    private void registerInventoryChangeListener(){
        InstanceIdentifier<Node> path = InstanceIdentifier.create(Nodes.class)
                .child(Node.class);
        final DataTreeIdentifier<Node> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, path);
        try {
            LOG.info("Cloud : Registering on {}", treeId);
            dataBroker.registerDataTreeChangeListener(treeId, this);
        } catch (final Exception e){
            LOG.warn("Cloud : Registration failed :/");
        }
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        registerInventoryChangeListener();
        this.packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);
        notificationService.registerNotificationListener(this);
        LOG.info("CloudSecProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("CloudSecProvider Closed");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> collection) {
        // Parcours de l'ensemble des modifications (create/update/delete)
        for(DataTreeModification<Node> mod : collection){
            DataObjectModification<Node> rootNode = mod.getRootNode();

            if(rootNode.getModificationType() == DataObjectModification.ModificationType.WRITE){
                if(rootNode.getDataBefore() == null){ // Si le node est créé
                    Switch newNode = new Switch(mod.getRootPath().getRootIdentifier(), dataBroker);
                    switches.add(newNode);
                    LOG.info("Node {} created", mod.getRootPath().getRootIdentifier());
                }
            }else if(rootNode.getModificationType() == DataObjectModification.ModificationType.DELETE){
                // Lors de la suppression d'un node, retrait du datastore configuration
                Switch toDelete = Switch.getSwitchByIid(switches, mod.getRootPath().getRootIdentifier());
                if(toDelete != null)
                    toDelete.killMe();
                LOG.info("Node {} has been deleted", mod.getRootPath().getRootIdentifier());
            }else{
                // Cas ne devant pas se présenter
                LOG.warn("Something changed, but I'm not quite sure what...");
            }
        }
    }


    @Override
    public void onPacketReceived(PacketReceived packetReceived) {
        // Identification du Switch recevant le paquet
        NodeKey nodeKey = null;
        Iterable<InstanceIdentifier.PathArgument> pathArgs = packetReceived.getIngress().getValue().getPathArguments();
        for(InstanceIdentifier.PathArgument pathArgument: pathArgs){
            if(pathArgument instanceof InstanceIdentifier.IdentifiableItem){
                if(((InstanceIdentifier.IdentifiableItem) pathArgument).getKey() instanceof NodeKey)
                    nodeKey = (NodeKey) ((InstanceIdentifier.IdentifiableItem) pathArgument).getKey();
            }
        }
        InstanceIdentifier<Node> node =
                InstanceIdentifier.create(Nodes.class)
                .child(Node.class, nodeKey);

        Switch sw = Switch.getSwitchByIid(switches, node);

        // Parsing du paquet
        byte[] payload = packetReceived.getPayload();
        JPacket packet = new JMemoryPacket(JProtocol.ETHERNET_ID, payload);

        //Ignore LLDP packets, or you will be in big trouble
        Ethernet eth = new Ethernet();
        if (!packet.hasHeader(JProtocol.ETHERNET_ID) || packet.getHeader(eth).type() == 0x88cc) {
            return;
        }

        // Emission du paquet sur tous les ports sauf le port entrant
        NodeConnectorRef egress;
        for(NodeConnector nc : sw.connectors){
            if(packetReceived.getIngress() != nc){
                egress = new NodeConnectorRef(
                        node.child(NodeConnector.class, nc.getKey()));
                TransmitPacketInput input = new TransmitPacketInputBuilder()
                        .setPayload(packetReceived.getPayload())
                        .setNode(new NodeRef(node))
                        .setIngress(packetReceived.getIngress())
                        .setEgress(egress)
                        .build();
                packetProcessingService.transmitPacket(input);
            }
        }

        LOG.info("Transmitted packet from {} to {} - EtherType {} - Ethernet : {} - IP4 : {} - IP6 : {}",
                bytesToHex(packet.getHeader(eth).source()),
                bytesToHex(packet.getHeader(eth).destination()),
                Integer.toHexString(packet.getHeader(eth).type() & 0xffff),
                packet.hasHeader(JProtocol.ETHERNET_ID),
                packet.hasHeader(JProtocol.IP4_ID),
                packet.hasHeader(JProtocol.IP6_ID));
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    @org.jetbrains.annotations.NotNull
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
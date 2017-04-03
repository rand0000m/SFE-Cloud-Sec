/*
 * Copyright © 2017 Mathieu Rousse and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.orange.cloudsec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.JProtocol;
import org.opendaylight.controller.config.yang.md.sal.binding.NotificationProviderServiceServiceInterface;
import org.opendaylight.controller.liblldp.Ethernet;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class CloudSecProvider implements DataTreeChangeListener<Node>, AutoCloseable, PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    private final DataBroker dataBroker;
    private final NotificationService notificationService;
    private final SalFlowService salFlowService;
    private PacketProcessingService packetProcessingService;
    private final RpcProviderRegistry rpcProviderRegistry;

    private AtomicLong flowIdInc = new AtomicLong(0);

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
        // On veut flood le paquet
        // La méthode pour envoyer un paquet est "packetProcessingService.transmitPacket(TransmitPacketInput input)
        // Il faut renseigner le NodeConnectorRef du port expéditeur (ingress)
        // Et le NodeConnectorRef du port sortant (egress)

        //Ignore LLDP packets, or you will be in big trouble
        /*byte[] etherTypeRaw = PacketParsingUtils.extractEtherType(notification.getPayload());
        int etherType = (0x0000ffff & ByteBuffer.wrap(etherTypeRaw).getShort());
        if (etherType == 0x88cc) {
            return;
        }*/
        NodeKey nodeKey = null;
        Iterable<InstanceIdentifier.PathArgument> pathArgs = packetReceived.getIngress().getValue().getPathArguments();
        for(InstanceIdentifier.PathArgument pathArgument: pathArgs){
            if(pathArgument instanceof InstanceIdentifier.IdentifiableItem){
                if(((InstanceIdentifier.IdentifiableItem) pathArgument).getKey() instanceof NodeKey)
                    nodeKey = (NodeKey) ((InstanceIdentifier.IdentifiableItem) pathArgument).getKey();
            }
        }
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> node =
                InstanceIdentifier.create(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class, nodeKey);

        NodeConnectorRef egress;
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> dataObjectOptional = null;
            dataObjectOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, node).get();
            if(dataObjectOptional.isPresent()){
                Node dataNode = dataObjectOptional.get();
                for(NodeConnector nc : dataNode.getNodeConnector()){
                    if(packetReceived.getIngress() != nc){
                        egress = new NodeConnectorRef(
                                node.child(NodeConnector.class, nc.getKey()));
                        TransmitPacketInput input = new TransmitPacketInputBuilder()
                                .setPayload(packetReceived.getPayload())
                                .setNode(new NodeRef(egress.getValue().firstIdentifierOf(Node.class)))
                                .setIngress(packetReceived.getIngress())
                                .setEgress(egress)
                                .build();
                        packetProcessingService.transmitPacket(input);
                    }
                }
                Ethernet etherPacket = new Ethernet();
                byte[] payload = packetReceived.getPayload();
                etherPacket.deserialize(payload, 0, payload.length);

                JPacket packet = new JMemoryPacket(JProtocol.ETHERNET_ID, payload);

                LOG.info("Transmitted packet from {} to {} - EtherType {} - Ethernet : {} - IP4 : {} - IP6 : {}",
                        bytesToHex(etherPacket.getSourceMACAddress()),
                        bytesToHex(etherPacket.getDestinationMACAddress()),
                        Integer.toHexString(etherPacket.getEtherType() & 0xffff),
                        packet.hasHeader(JProtocol.ETHERNET_ID),
                        packet.hasHeader(JProtocol.IP4_ID),
                        packet.hasHeader(JProtocol.IP6_ID));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
/*
 * Copyright © 2017 Mathieu Rousse and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.orange.cloudsec.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.config.yang.md.sal.binding.NotificationProviderServiceServiceInterface;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CloudSecProvider implements DataTreeChangeListener<Node>, AutoCloseable, PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    private final DataBroker dataBroker;
    private final NotificationService notificationService;
    private final SalFlowService salFlowService;
    private PacketProcessingService packetProcessingService;
    private final RpcProviderRegistry rpcProviderRegistry;

    private AtomicLong flowIdInc = new AtomicLong(0);

    private Map<InstanceIdentifier<Node>,Switch> switches;

    public CloudSecProvider(final DataBroker dataBroker,
                            final NotificationService notificationService,
                            final SalFlowService salFlowService,
                            final RpcProviderRegistry rpcProviderRegistry) {

        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.salFlowService = salFlowService;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    private void registerTopologyChangeListener(){
        InstanceIdentifier<Node> path = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Node.class);
        final DataTreeIdentifier<Node> treeId = new DataTreeIdentifier<Node>(LogicalDatastoreType.OPERATIONAL, path);
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
        registerTopologyChangeListener();
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
        List<Flow> addFlows = new ArrayList<>();
        List<InstanceIdentifier<Flow>> addFlowPaths = new ArrayList<>();
        List<InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node>> deleteFlowPaths = new ArrayList<>();

        for(DataTreeModification<Node> mod : collection){
            DataObjectModification<Node> rootNode = mod.getRootNode();
            if(rootNode.getModificationType() == DataObjectModification.ModificationType.WRITE){
                if(rootNode.getDataBefore() == null){
                    FlowId flowId = new FlowId(Long.toString(flowIdInc.incrementAndGet()));
                    String nodeName = mod.getRootNode().getDataAfter().getKey().getNodeId().getValue();
                    InstanceIdentifier<Flow> flowPath = InstanceIdentifier.create(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes.class)
                            .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                                    new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey(
                                            new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId(nodeName)))
                            .augmentation(FlowCapableNode.class)
                            .child(Table.class, new TableKey(new Short("0")))
                            .child(Flow.class, new FlowKey(flowId));
                    addFlowPaths.add(flowPath);
                    addFlows.add(buildAllToCtrlFlow(flowId));
                    LOG.info("Node {} created", mod.getRootPath().getRootIdentifier());
                }
            }else if(rootNode.getModificationType() == DataObjectModification.ModificationType.DELETE){
                String nodeName = mod.getRootNode().getDataBefore().getKey().getNodeId().getValue();
                InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> node = InstanceIdentifier.create(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes.class)
                        .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                                new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey(
                                        new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId(nodeName)));
                deleteFlowPaths.add(node);
                LOG.info("Node {} has been deleted", mod.getRootPath().getRootIdentifier());
            }
        }
        deleteFromStore(deleteFlowPaths);
        pushToStore(addFlows, addFlowPaths);
    }

    private Flow buildAllToCtrlFlow(FlowId flowId){
        FlowBuilder allToCtrlFlow = new FlowBuilder()
                .setTableId(new Short("0"))
                .setFlowName("allPacketsToCtrl")
                .setId(flowId)
                .setKey(new FlowKey(flowId));

        MatchBuilder matchBuilder = new MatchBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Integer.valueOf(0xffff));
        Uri controllerPort = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(controllerPort);

        ActionBuilder ab = new ActionBuilder();
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));

        List<Action> actionList = new ArrayList<Action>();
        actionList.add(ab.build());

        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);

        allToCtrlFlow
                .setMatch(matchBuilder.build())
                .setInstructions(isb.build())
                .setPriority(10000)
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(0)
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return allToCtrlFlow.build();
    }

    private <T extends DataObject> void pushToStore(List<T> objects, List<InstanceIdentifier<T>> objectsPaths){
        if(objects.isEmpty()){
            LOG.info("No objects to add");
            return;
        }
        if(objects.size() != objectsPaths.size()){
            LOG.error("Inconsistent objects/objectsPath list provided");
            return;
        }
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        for(int i = 0; i < objects.size(); i++) {
            T obj = objects.get(i);
            InstanceIdentifier<T> objPath = objectsPaths.get(i);
            transaction.put(LogicalDatastoreType.CONFIGURATION, objPath, obj, true);
        }
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = transaction.submit();
        try {
            commitFuture.checkedGet();
            LOG.warn("Transaction success : {}", commitFuture);
        } catch (Exception e) {
            LOG.error("Transaction failed with error {}", e.getMessage());
            transaction.cancel();
        }
    }

    private <T extends DataObject> void deleteFromStore(List<InstanceIdentifier<T>> objects){
        if(objects.isEmpty()){
            LOG.info("No objects to delete");
            return;
        }
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        for(InstanceIdentifier<T> obj : objects)
            transaction.delete(LogicalDatastoreType.CONFIGURATION, obj);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = transaction.submit();

        try {
            commitFuture.checkedGet();
            LOG.warn("Transaction success : {}", commitFuture);
        } catch (Exception e) {
            LOG.error("Transaction failed with error {}", e.getMessage());
            transaction.cancel();
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
        /*InstanceIdentifier<Node> node = InstanceIdentifier.create(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class,
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey(
                                new org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId(nodeName)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(new Short("0")))
                .child(Flow.class, new FlowKey(flowId));


        //Construct input for RPC call to packet processing service
        TransmitPacketInput input = new TransmitPacketInputBuilder()
                .setPayload(packetReceived.getPayload())
                .setNode(ingressNodeRef)
                .setIngress(packetReceived.getIngress())
                .setEgress(egressNodeConnectorRef)
                .build();
        packetProcessingService.transmitPacket(input);*/
        Iterable<InstanceIdentifier.PathArgument> a = packetReceived.getIngress().getValue().getPathArguments();
        (packetReceived.getIngress().getValue().getPath().get(1).getKey() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey) == true
    }
}
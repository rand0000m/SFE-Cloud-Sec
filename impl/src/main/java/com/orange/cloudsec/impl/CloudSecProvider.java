/*
 * Copyright © 2017 Mathieu Rousse and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.orange.cloudsec.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

public class CloudSecProvider implements DataTreeChangeListener<Node>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationService;
    private final SalFlowService salFlowService;

    public CloudSecProvider(final DataBroker dataBroker,
		    final NotificationPublishService notificationService,
		    final SalFlowService salFlowService) {

        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.salFlowService = salFlowService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
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
        LOG.info("Cloud : Data Changed :)");
    }
}

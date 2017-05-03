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
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.CloudSecService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.ServiceFunctionForwarderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.ServiceFunctionRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.service.function.registry.ServiceFunction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CloudSecProvider implements DataTreeChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    private final DataBroker dataBroker;
    private final NotificationService notificationService;
    private final SalFlowService salFlowService;
    private final RpcProviderRegistry rpcProviderRegistry;

    private CloudConfig<ServiceFunction> sfDB;
    private CloudConfig<ServiceFunctionForwarder> sffDB;

    public CloudSecProvider(final DataBroker dataBroker,
                            final NotificationService notificationService,
                            final SalFlowService salFlowService,
                            final RpcProviderRegistry rpcProviderRegistry) {

        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.salFlowService = salFlowService;
        this.rpcProviderRegistry = rpcProviderRegistry;

        sfDB = new CloudConfig<>(dataBroker);
        sffDB = new CloudConfig<>(dataBroker);

        rpcProviderRegistry.addRpcImplementation(CloudSecService.class, sfDB);
    }

    private void registerInventoryChangeListener(){
        InstanceIdentifier<ServiceFunction> pathSF = InstanceIdentifier.create(ServiceFunctionRegistry.class)
                .child(ServiceFunction.class);
        InstanceIdentifier<ServiceFunctionForwarder> pathSFF = InstanceIdentifier.create(ServiceFunctionForwarderRegistry.class)
                .child(ServiceFunctionForwarder.class);
        final DataTreeIdentifier<ServiceFunction> idSFTree = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, pathSF);
        final DataTreeIdentifier<ServiceFunctionForwarder> idSFFTree = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, pathSFF);
        try {
            LOG.info("Cloud : Registering on {}", idSFTree);
            dataBroker.registerDataTreeChangeListener(idSFTree, this);
            LOG.info("Cloud : Registering on {}", idSFFTree);
            dataBroker.registerDataTreeChangeListener(idSFFTree, this);
        } catch (final Exception e){
            LOG.warn("Cloud : Registration failed :/");
        }
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        registerInventoryChangeListener();
        LOG.info("CloudSecProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("CloudSecProvider Closed");
    }

    @Override
    public void onDataTreeChanged(Collection collection) {
        /* AJOUT D'UNE SF :
    "service-function": [
        {
            "nsh-aware": "true",
            "name": "dpi",
            "type": "service-function-type:dpi",
            "ip-mgmt-address": "10.0.0.1",
            "sf-data-plane-locator": [
                {
                    "name": "1",
                    "service-function-forwarder": "SFF1",
                    "ip": "10.0.0.2",
                    "port": "6633",
                    "transport": "service-locator:vxlan-gpe"
                }
            ]
        }
    ]
         */
        for(Object a: collection){
            if(a instanceof DataTreeModification) {
                DataTreeModification b = (DataTreeModification) a;
                DataObject dataObj = b.getRootNode().getDataAfter();
                if(b.getRootPath().getRootIdentifier().getTargetType() == ServiceFunctionForwarder.class) {
                    LOG.warn("Modification sur une SFF !");
                    sffDB.dataTreeChanged(b);
                }
                if(b.getRootPath().getRootIdentifier().getTargetType() == ServiceFunction.class) {
                    LOG.warn("Modification sur une SF !");
                    sfDB.dataTreeChanged(b);
                }
            }
        }
    }
}
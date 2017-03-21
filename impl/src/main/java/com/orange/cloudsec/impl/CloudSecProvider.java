/*
 * Copyright © 2017 Mathieu Rousse and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.orange.cloudsec.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudSecProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    private final DataBroker dataBroker;
    private final NotificationProviderService notificationService;
    private final SalFlowService salFlowService;

    public CloudSecProvider(final DataBroker dataBroker,
		    final NotificationProviderService notificationService,
		    final SalFlowService salFlowService) {

        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.salFlowService = salFlowService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("CloudSecProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("CloudSecProvider Closed");
    }
}

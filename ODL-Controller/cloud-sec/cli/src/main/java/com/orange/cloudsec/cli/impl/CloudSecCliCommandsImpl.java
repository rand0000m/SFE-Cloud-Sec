/*
 * Copyright © 2017 Mathieu Rousse and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.orange.cloudsec.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.orange.cloudsec.cli.api.CloudSecCliCommands;

public class CloudSecCliCommandsImpl implements CloudSecCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(CloudSecCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public CloudSecCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("CloudSecCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}
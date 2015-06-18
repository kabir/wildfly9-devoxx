/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.kabir.example.log.file.viewer;


import java.io.BufferedInputStream;
import java.io.InputStream;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.as.controller.client.OperationResponse;
import org.jboss.dmr.ModelNode;

/**
 * @author Kabir Khan
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /*
         * Do a /subsystem=logging/log-file=server.log:read-attribute(name=stream)
         * which does not look very informative in the CLI
         */
        final ModelNode op = new ModelNode();
        op.get("operation").set("read-attribute");
        ModelNode addr = new ModelNode();
        addr.add("subsystem", "logging");
        addr.add("log-file", "server.log");
        op.get("address").set(addr);
        op.get("name").set("stream");


        try (final ModelControllerClient client =
                     ModelControllerClient.Factory.create("localhost", 9990);) {
            OperationResponse response =
                    client.executeOperation(
                            Operation.Factory.create(op),
                            OperationMessageHandler.DISCARD);
            //Validate response and get stream id
            ModelNode result = response.getResponseNode();
            if (!"success".equals(result.get("outcome").asString())) {
                throw new RuntimeException(result.get("failure-description").asString());
            }
            String streamId = result.get("result").asString();


            System.out.println();
            System.out.println();
            System.out.println("================== LOG FILE CONTENTS");

            //Consume the stream
            try (final InputStream in = new BufferedInputStream(
                    response.getInputStream(streamId).getStream());) {
                int i = in.read();
                while (i != -1) {
                    System.out.print((char) i);
                    i = in.read();
                }
            }
        }
    }
}

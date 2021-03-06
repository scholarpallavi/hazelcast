/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client;

import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.spi.ProxyService;
import com.hazelcast.spi.impl.PortableDistributedObjectEvent;

public class DistributedObjectListenerRequest extends CallableClientRequest implements Portable, RetryableRequest {

    public DistributedObjectListenerRequest() {
    }

    @Override
    public Object call() throws Exception {
        ProxyService proxyService = clientEngine.getProxyService();
        String registrationId = proxyService.addProxyListener(new MyDistributedObjectListener());
        endpoint.setDistributedObjectListener(registrationId);
        return registrationId;
    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public int getFactoryId() {
        return ClientPortableHook.ID;
    }

    @Override
    public int getClassId() {
        return ClientPortableHook.LISTENER;
    }

    private class MyDistributedObjectListener implements DistributedObjectListener {
        @Override
        public void distributedObjectCreated(DistributedObjectEvent event) {
            send(event);
        }

        @Override
        public void distributedObjectDestroyed(DistributedObjectEvent event) {
        }

        private void send(DistributedObjectEvent event) {
            if (endpoint.live()) {
                PortableDistributedObjectEvent portableEvent = new PortableDistributedObjectEvent(
                        event.getEventType(), event.getDistributedObject().getName(), event.getServiceName());
                endpoint.sendEvent(portableEvent, getCallId());
            }
        }
    }
}

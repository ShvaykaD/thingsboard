/**
 * Copyright © 2016-2020 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.mqtt.attributes.updates;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.TransportPayloadType;
import org.thingsboard.server.common.data.device.profile.MqttTopics;
import org.thingsboard.server.mqtt.attributes.AbstractMqttAttributesIntegrationTest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public abstract class AbstractMqttAttributesUpdatesIntegrationTest extends AbstractMqttAttributesIntegrationTest {

    private static final String RESPONSE_ATTRIBUTES_PAYLOAD_DELETED = "{\"deleted\":[\"attribute5\"]}";

    private static String getResponseGatewayAttributesUpdatedPayload() {
        return "{\"device\":\"" + "Gateway Device Subscribe to attribute updates" + "\"," +
                "\"data\":{\"attribute1\":\"value1\",\"attribute2\":true,\"attribute3\":42.0,\"attribute4\":73,\"attribute5\":{\"someNumber\":42,\"someArray\":[1,2,3],\"someNestedObject\":{\"key\":\"value\"}}}}";
    }

    private static String getResponseGatewayAttributesDeletedPayload() {
        return "{\"device\":\"" + "Gateway Device Subscribe to attribute updates" + "\",\"data\":{\"deleted\":[\"attribute5\"]}}";
    }

    @Before
    public void beforeTest() throws Exception {
        processBeforeTest("Test Subscribe to attribute updates", "Gateway Test Subscribe to attribute updates", TransportPayloadType.JSON);
    }

    @After
    public void afterTest() throws Exception {
        processAfterTest();
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServer() throws Exception {
        processTestSubscribeToAttributesUpdates();
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServerGateway() throws Exception {
        processGatewayTestSubscribeToAttributesUpdates();
    }

    protected void processTestSubscribeToAttributesUpdates() throws Exception {

        MqttAsyncClient client = getMqttAsyncClient(accessToken);

        TestMqttCallback onUpdateCallback = getTestMqttCallback();
        client.setCallback(onUpdateCallback);

        client.subscribe(MqttTopics.DEVICE_ATTRIBUTES_TOPIC, MqttQoS.AT_MOST_ONCE.value());

        Thread.sleep(2000);

        doPostAsync("/api/plugins/telemetry/DEVICE/" + savedDevice.getId().getId() + "/attributes/SHARED_SCOPE", POST_ATTRIBUTES_PAYLOAD, String.class, status().isOk());
        onUpdateCallback.getLatch().await(3, TimeUnit.SECONDS);

        validateUpdateAttributesResponse(onUpdateCallback);

        TestMqttCallback onDeleteCallback = getTestMqttCallback();
        client.setCallback(onDeleteCallback);

        doDelete("/api/plugins/telemetry/DEVICE/" + savedDevice.getId().getId() + "/SHARED_SCOPE?keys=attribute5", String.class);
        onDeleteCallback.getLatch().await(3, TimeUnit.SECONDS);

        validateDeleteAttributesResponse(onDeleteCallback);
    }

    protected void validateUpdateAttributesResponse(TestMqttCallback callback) throws InvalidProtocolBufferException {
        assertNotNull(callback.getPayloadBytes());
        String s = new String(callback.getPayloadBytes(), StandardCharsets.UTF_8);
        assertEquals(POST_ATTRIBUTES_PAYLOAD, s);
    }

    protected void validateDeleteAttributesResponse(TestMqttCallback callback) throws InvalidProtocolBufferException {
        assertNotNull(callback.getPayloadBytes());
        String s = new String(callback.getPayloadBytes(), StandardCharsets.UTF_8);
        assertEquals(s, RESPONSE_ATTRIBUTES_PAYLOAD_DELETED);
    }

    protected void processGatewayTestSubscribeToAttributesUpdates() throws Exception {

        MqttAsyncClient client = getMqttAsyncClient(gatewayAccessToken);

        TestMqttCallback onUpdateCallback = getTestMqttCallback();
        client.setCallback(onUpdateCallback);

        Device device = new Device();
        device.setName("Gateway Device Subscribe to attribute updates");
        device.setType("default");

        byte[] connectPayloadBytes = getConnectPayloadBytes();

        publishMqttMsg(client, connectPayloadBytes);

        Thread.sleep(1000);

        Device savedDevice = doGet("/api/tenant/devices?deviceName=" + "Gateway Device Subscribe to attribute updates", Device.class);
        assertNotNull(savedDevice);

        client.subscribe(MqttTopics.GATEWAY_ATTRIBUTES_TOPIC, MqttQoS.AT_MOST_ONCE.value());

        Thread.sleep(2000);

        doPostAsync("/api/plugins/telemetry/DEVICE/" + savedDevice.getId().getId() + "/attributes/SHARED_SCOPE", POST_ATTRIBUTES_PAYLOAD, String.class, status().isOk());
        onUpdateCallback.getLatch().await(3, TimeUnit.SECONDS);

        validateGatewayUpdateAttributesResponse(onUpdateCallback);

        TestMqttCallback onDeleteCallback = getTestMqttCallback();
        client.setCallback(onDeleteCallback);

        doDelete("/api/plugins/telemetry/DEVICE/" + savedDevice.getId().getId() + "/SHARED_SCOPE?keys=attribute5", String.class);
        onDeleteCallback.getLatch().await(3, TimeUnit.SECONDS);

        validateGatewayDeleteAttributesResponse(onDeleteCallback);

    }

    protected void validateGatewayUpdateAttributesResponse(TestMqttCallback callback) throws InvalidProtocolBufferException {
        assertNotNull(callback.getPayloadBytes());
        String s = new String(callback.getPayloadBytes(), StandardCharsets.UTF_8);
        assertEquals(getResponseGatewayAttributesUpdatedPayload(), s);
    }

    protected void validateGatewayDeleteAttributesResponse(TestMqttCallback callback) throws InvalidProtocolBufferException {
        assertNotNull(callback.getPayloadBytes());
        String s = new String(callback.getPayloadBytes(), StandardCharsets.UTF_8);
        assertEquals(s, getResponseGatewayAttributesDeletedPayload());
    }

    protected byte[] getConnectPayloadBytes() {
        String connectPayload = "{\"device\":\"" + "Gateway Device Subscribe to attribute updates" + "\"}";
        return connectPayload.getBytes();
    }


    private TestMqttCallback getTestMqttCallback() {
        CountDownLatch latch = new CountDownLatch(1);
        return new TestMqttCallback(latch);
    }

    private void publishMqttMsg(MqttAsyncClient client, byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(payload);
        client.publish(MqttTopics.GATEWAY_CONNECT_TOPIC, message);
    }

    protected static class TestMqttCallback implements MqttCallback {

        private final CountDownLatch latch;
        private Integer qoS;
        private byte[] payloadBytes;

        TestMqttCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        int getQoS() {
            return qoS;
        }

        byte[] getPayloadBytes() {
            return payloadBytes;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        @Override
        public void connectionLost(Throwable throwable) {
        }

        @Override
        public void messageArrived(String requestTopic, MqttMessage mqttMessage) throws Exception {
            qoS = mqttMessage.getQos();
            payloadBytes = mqttMessage.getPayload();
            latch.countDown();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }
}

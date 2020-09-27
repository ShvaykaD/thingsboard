/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.mqtt.telemetry.timeseries;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.TransportPayloadType;
import org.thingsboard.server.common.data.device.profile.MqttTopics;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.gen.transport.TransportApiProtos;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.mqtt.telemetry.AbstractMqttTelemetryIntegrationTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public abstract class AbstractMqttTimeseriesJsonIntegrationTest extends AbstractMqttTimeseriesIntegrationTest {

    private static final String POST_DATA_TELEMETRY_TOPIC = "data/telemetry";

    @Before
    public void beforeTest() throws Exception {
        processBeforeTest("Test Post Telemetry device json payload", "Test Post Telemetry gateway json payload", TransportPayloadType.JSON, POST_DATA_TELEMETRY_TOPIC, null);
    }

    @After
    public void afterTest() throws Exception {
        processAfterTest();
    }

    @Test
    public void testPushMqttTelemetry() throws Exception {
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3", "key4", "key5");
        processTelemetryTest(POST_DATA_TELEMETRY_TOPIC, expectedKeys, PAYLOAD_VALUES_STR_V_1.getBytes(), false);
    }

    @Test
    public void testPushMqttTelemetryWithTs() throws Exception {
        String payloadStr = "{\"ts\": 10000, \"values\": " + PAYLOAD_VALUES_STR_V_1 + "}";
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3", "key4", "key5");
        processTelemetryTest(POST_DATA_TELEMETRY_TOPIC, expectedKeys, payloadStr.getBytes(), true);
    }

    @Test
    public void testPushMqttTelemetryGateway() throws Exception {
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3", "key4", "key5");
        String deviceName1 = "Device A";
        String deviceName2 = "Device B";
        String payload = getGatewayTelemetryJsonPayload(deviceName1, deviceName2, "10000", "20000");
        processGatewayTelemetryTest(MqttTopics.GATEWAY_TELEMETRY_TOPIC, expectedKeys, payload.getBytes(), deviceName1, deviceName2);
    }

    @Test
    public void testGatewayConnect() throws Exception {
        String payload = "{\"device\":\"Device A\"}";
        MqttAsyncClient client = getMqttAsyncClient(gatewayAccessToken);
        publishMqttMsg(client, payload.getBytes(), MqttTopics.GATEWAY_CONNECT_TOPIC);

        Thread.sleep(2000);

        String deviceName = "Device A";
        Device device = doGet("/api/tenant/devices?deviceName=" + deviceName, Device.class);
        assertNotNull(device);
    }
}

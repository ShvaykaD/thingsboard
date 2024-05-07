/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.dao.service.DaoSqlTest;
import org.thingsboard.server.service.ruleengine.RuleEngineCallService;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DaoSqlTest
public class RuleEngineControllerTest extends AbstractControllerTest {

    private static final String REQUEST_BODY = "{\"temperature\":23}";

    @SpyBean
    private RuleEngineCallService ruleEngineCallService;

    @Test
    public void testHandleRuleEngineRequestWithMsgOriginatorUser() throws Exception {
        loginSysAdmin();
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, TenantId.SYS_TENANT_ID, null, TbMsgMetaData.EMPTY, REQUEST_BODY);

        doAnswer(invocation -> {
            Consumer<TbMsg> consumer = invocation.getArgument(4);
            consumer.accept(msg);
            return null;
        }).when(ruleEngineCallService).processRestApiCallToRuleEngine(eq(TenantId.SYS_TENANT_ID), any(UUID.class), any(TbMsg.class), anyBoolean(), any());

        MvcResult result = doPost("/api/rule-engine/", (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo(REQUEST_BODY);
        ArgumentCaptor<TbMsg> captor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ruleEngineCallService).processRestApiCallToRuleEngine(eq(TenantId.SYS_TENANT_ID), any(), captor.capture(), eq(false), any());
        TbMsg tbMsg = captor.getValue();
        assertThat(tbMsg.getData()).isEqualTo(REQUEST_BODY);
        assertThat(tbMsg.getType()).isEqualTo(msg.getType());
    }

    @Test
    public void testHandleRuleEngineRequestWithMsgOriginatorDevice() throws Exception {
        loginTenantAdmin();
        Device device = createDevice("Test", "123");
        String deviceIdStr = String.valueOf(device.getId().getId());
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, device.getId(), null, TbMsgMetaData.EMPTY, REQUEST_BODY);
       mockSuccessfulRestApiCallToRuleEngine(msg);

        MvcResult result = doPost("/api/rule-engine/DEVICE/" + deviceIdStr, (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo(REQUEST_BODY);
        ArgumentCaptor<TbMsg> captor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ruleEngineCallService).processRestApiCallToRuleEngine(eq(tenantId), any(), captor.capture(), eq(false), any());
        TbMsg tbMsg = captor.getValue();
        assertThat(tbMsg.getData()).isEqualTo(REQUEST_BODY);
        assertThat(tbMsg.getType()).isEqualTo(msg.getType());
    }

    @Test
    public void testHandleRuleEngineRequestWithMsgOriginatorDeviceAndSpecifiedTimeout() throws Exception {
        loginTenantAdmin();
        Device device = createDevice("Test", "123");
        String deviceIdStr = String.valueOf(device.getId().getId());
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, device.getId(), null, TbMsgMetaData.EMPTY, REQUEST_BODY);
        mockSuccessfulRestApiCallToRuleEngine(msg);

        MvcResult result = doPost("/api/rule-engine/DEVICE/" + deviceIdStr + "/15000", (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo(REQUEST_BODY);
        ArgumentCaptor<TbMsg> captor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ruleEngineCallService).processRestApiCallToRuleEngine(eq(tenantId), any(), captor.capture(), eq(false), any());
        TbMsg tbMsg = captor.getValue();
        assertThat(tbMsg.getData()).isEqualTo(REQUEST_BODY);
        assertThat(tbMsg.getType()).isEqualTo(msg.getType());
    }

    @Test
    public void testHandleRuleEngineRequestWithMsgOriginatorDeviceAndResponseIsNull() throws Exception {
        loginTenantAdmin();
        Device device = createDevice("Test", "123");
        String deviceIdStr = String.valueOf(device.getId().getId());
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, device.getId(), null, TbMsgMetaData.EMPTY, REQUEST_BODY);
        mockSuccessfulRestApiCallToRuleEngine(msg);

        MvcResult result = doPost("/api/rule-engine/DEVICE/" + deviceIdStr + "/15000", (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        ArgumentCaptor<TbMsg> captor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ruleEngineCallService).processRestApiCallToRuleEngine(eq(tenantId), any(), captor.capture(), eq(false), any());
        TbMsg tbMsg = captor.getValue();
        assertThat(tbMsg.getData()).isEqualTo(REQUEST_BODY);
        assertThat(tbMsg.getType()).isEqualTo(msg.getType());
    }

    @Test
    public void testHandleRuleEngineRequestWithMsgOriginatorDeviceAndSpecifiedQueue() throws Exception {
        loginTenantAdmin();
        Device device = createDevice("Test", "123");
        String deviceIdStr = String.valueOf(device.getId().getId());
        TbMsg msg = TbMsg.newMsg("HighPriority", TbMsgType.REST_API_REQUEST, device.getId(), null, TbMsgMetaData.EMPTY, REQUEST_BODY);
        mockSuccessfulRestApiCallToRuleEngine(msg);

        MvcResult result = doPost("/api/rule-engine/DEVICE/" + deviceIdStr + "/HighPriority/1000", (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo(REQUEST_BODY);
        ArgumentCaptor<TbMsg> captor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ruleEngineCallService).processRestApiCallToRuleEngine(eq(tenantId), any(), captor.capture(), eq(true), any());
        TbMsg tbMsg = captor.getValue();
        assertThat(tbMsg.getData()).isEqualTo(REQUEST_BODY);
        assertThat(tbMsg.getType()).isEqualTo(msg.getType());
        assertThat(tbMsg.getQueueName()).isEqualTo(msg.getQueueName());
    }

    @Test
    public void testHandleRuleEngineRequestWithInvalidRequestBody() throws Exception {
        loginSysAdmin();

        doPost("/api/rule-engine/", (Object) "@")
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Invalid request body")));

        verifyNoMoreInteractions(ruleEngineCallService);
    }

    @Test
    public void testHandleRuleEngineRequestWithAuthorityCustomerUser() throws Exception {
        loginTenantAdmin();
        Device device = createDevice("test", "123");
        String deviceIdStr = String.valueOf(device.getId().getId());
        doPost("/api/customer/" + customerId.getId() + "/device/" + deviceIdStr).andReturn();
        loginCustomerUser();

        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, device.getId(), null, TbMsgMetaData.EMPTY, REQUEST_BODY);
        mockSuccessfulRestApiCallToRuleEngine(msg);

        MvcResult result = doPost("/api/rule-engine/DEVICE/" + deviceIdStr, (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo(REQUEST_BODY);
        ArgumentCaptor<TbMsg> captor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ruleEngineCallService).processRestApiCallToRuleEngine(eq(tenantId), any(), captor.capture(), eq(false), any());
        TbMsg tbMsg = captor.getValue();
        assertThat(tbMsg.getData()).isEqualTo(REQUEST_BODY);
        assertThat(tbMsg.getType()).isEqualTo(msg.getType());
    }

    @Test
    public void testHandleRuleEngineRequestWithoutPermission() throws Exception {
        loginTenantAdmin();
        Device device = createDevice("test", "123");
        String deviceIdStr = String.valueOf(device.getId().getId());
        loginCustomerUser();

        MvcResult result = doPost("/api/rule-engine/DEVICE/" + deviceIdStr, (Object) REQUEST_BODY).andReturn();

        ResponseEntity response = (ResponseEntity) result.getAsyncResult();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(Objects.requireNonNull(response.getBody()).toString()).isEqualTo("You don't have permission to perform this operation!");
        verify(ruleEngineCallService, never()).processRestApiCallToRuleEngine(any(), any(), any(), anyBoolean(), any());
    }

    @Test
    public void testHandleRuleEngineRequestUnauthorized() throws Exception {
        doPost("/api/rule-engine/", (Object) REQUEST_BODY)
                .andExpect(status().isUnauthorized())
                .andExpect(statusReason(containsString("Authentication failed")));
    }

    private void mockSuccessfulRestApiCallToRuleEngine(TbMsg msg) {
        doAnswer(invocation -> {
            Consumer<TbMsg> consumer = invocation.getArgument(4);
            consumer.accept(msg);
            return null;
        }).when(ruleEngineCallService).processRestApiCallToRuleEngine(eq(tenantId), any(UUID.class), any(TbMsg.class), anyBoolean(), any());
    }
}

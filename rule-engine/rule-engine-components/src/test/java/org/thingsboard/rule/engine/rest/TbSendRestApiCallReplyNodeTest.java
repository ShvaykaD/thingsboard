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
package org.thingsboard.rule.engine.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.RuleEngineRpcService;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TbSendRestApiCallReplyNodeTest {

    private static final DeviceId DEVICE_ID = new DeviceId(UUID.fromString("212445ad-9852-4bfd-819d-6b01ab6ee6b6"));

    private TbSendRestApiCallReplyNode node;
    private TbSendRestApiCallReplyNodeConfiguration config;
    
    @Mock
    private TbContext ctxMock;
    @Mock
    private RuleEngineRpcService rpcServiceMock;

    @BeforeEach
    void setUp() throws TbNodeException {
        node = new TbSendRestApiCallReplyNode();
        config = new TbSendRestApiCallReplyNodeConfiguration().defaultConfiguration();
        var configuration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));
        node.init(ctxMock, configuration);
    }

    @Test
    void givenValidRestApiRequest_whenOnMsg_thenTellSuccess() {
        when(ctxMock.getRpcService()).thenReturn(rpcServiceMock);
        String data = """
                {
                "temperature": 23,
                }
                """;
        Map<String, String> metadata = Map.of(
                "requestUUID", "80b7883b-7ec6-4872-9dd3-b2afd5660fa6",
                "serviceId", "tb-core-0");
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, DEVICE_ID, new TbMsgMetaData(metadata), data);

        node.onMsg(ctxMock, msg);

        UUID requestUUID = UUID.fromString("80b7883b-7ec6-4872-9dd3-b2afd5660fa6");
        verify(rpcServiceMock).sendRestApiCallReply(eq("tb-core-0"), eq(requestUUID), eq(msg));
        verify(ctxMock).tellSuccess(eq(msg));
    }

    @Test
    void givenRequestIdIsNotPresent_whenOnMsg_thenTellFailure() {
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, DEVICE_ID, TbMsgMetaData.EMPTY, TbMsg.EMPTY_STRING);

        node.onMsg(ctxMock, msg);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(ctxMock).tellFailure(eq(msg), captor.capture());
        Throwable throwable = captor.getValue();
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Request id is not present in the metadata!");
    }

    @Test
    void givenRequestBodyIsEmpty_whenOnMsg_thenTellFailure() {
        Map<String, String> metadata = Map.of("requestUUID", "80b7883b-7ec6-4872-9dd3-b2afd5660fa6");
        TbMsg msg = TbMsg.newMsg(null, TbMsgType.REST_API_REQUEST, DEVICE_ID, new TbMsgMetaData(metadata), TbMsg.EMPTY_STRING);

        node.onMsg(ctxMock, msg);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(ctxMock).tellFailure(eq(msg), captor.capture());
        Throwable throwable = captor.getValue();
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Request body is empty!");
    }
}

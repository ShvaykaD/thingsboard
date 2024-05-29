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
package org.thingsboard.server.actors.rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.TbActorRef;
import org.thingsboard.server.actors.ruleChain.DefaultTbContext;
import org.thingsboard.server.actors.ruleChain.RuleChainOutputMsg;
import org.thingsboard.server.actors.ruleChain.RuleNodeCtx;
import org.thingsboard.server.actors.ruleChain.RuleNodeToRuleChainTellNextMsg;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.data.msg.TbNodeConnectionType;
import org.thingsboard.server.common.data.rule.RuleNode;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.common.msg.TbMsgProcessingStackItem;
import org.thingsboard.server.common.msg.queue.ServiceType;
import org.thingsboard.server.common.msg.queue.TbMsgCallback;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import org.thingsboard.server.queue.common.SimpleTbQueueCallback;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ExtendWith(MockitoExtension.class)
public class DefaultTbContextTest {

    private final String EXCEPTION_MSG = "Some runtime exception!";
    private final RuntimeException EXCEPTION = new RuntimeException(EXCEPTION_MSG);

    private final TenantId TENANT_ID = TenantId.fromUUID(UUID.fromString("c7bf4c85-923c-4688-a4b5-0f8a0feb7cd5"));
    private final RuleNodeId RULE_NODE_ID = new RuleNodeId(UUID.fromString("1ca5e2ef-1309-41d9-bafa-709e9df0e2a6"));
    private final RuleChainId RULE_CHAIN_ID = new RuleChainId(UUID.fromString("b87c4123-f9f2-41a6-9a09-e3a5b6580b11"));

    @Mock
    private ActorSystemContext mainCtxMock;
    @Mock
    private RuleNodeCtx nodeCtxMock;
    @Mock
    private TbActorRef chainActorMock;
    @InjectMocks
    private DefaultTbContext defaultTbContext;

    @MethodSource
    @ParameterizedTest
    public void givenDebugModeOptionsAndTbNodeConnectionsSet_whenTellNext_thenVerify(
            boolean defaultDebugMode, boolean debugRuleNodeFailures, Set<String> relationTypes) {
        // GIVEN
        var callbackMock = mock(TbMsgCallback.class);
        var msg = getTbMsgWithCallback(callbackMock);
        var ruleNode = new RuleNode(RULE_NODE_ID);
        ruleNode.setRuleChainId(RULE_CHAIN_ID);
        ruleNode.setDebugMode(defaultDebugMode);

        boolean persistDebugOutput = defaultDebugMode || debugRuleNodeFailures;
        if (persistDebugOutput) {
            given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        }
        if (!defaultDebugMode) {
            given(nodeCtxMock.isDebugRuleNodeFailures()).willReturn(debugRuleNodeFailures);
        }
        given(nodeCtxMock.getSelf()).willReturn(ruleNode);
        given(nodeCtxMock.getChainActor()).willReturn(chainActorMock);

        // WHEN
        defaultTbContext.tellNext(msg, relationTypes);

        // THEN
        then(nodeCtxMock).should().getChainActor();
        then(nodeCtxMock).shouldHaveNoMoreInteractions();
        var expectedRuleNodeToRuleChainTellNextMsg = new RuleNodeToRuleChainTellNextMsg(
                RULE_CHAIN_ID,
                RULE_NODE_ID,
                relationTypes,
                msg,
                null);
        then(chainActorMock).should().tell(expectedRuleNodeToRuleChainTellNextMsg);
        then(chainActorMock).shouldHaveNoMoreInteractions();
        then(callbackMock).should().onProcessingEnd(RULE_NODE_ID);
        then(callbackMock).shouldHaveNoMoreInteractions();

        if (defaultDebugMode) {
            relationTypes.forEach(relationType ->
                    then(mainCtxMock).should().persistDebugOutput(TENANT_ID, RULE_NODE_ID, msg, relationType, null)
            );
            then(mainCtxMock).shouldHaveNoMoreInteractions();
            return;
        }
        if (debugRuleNodeFailures && relationTypes.contains(TbNodeConnectionType.FAILURE)) {
            then(mainCtxMock).should().persistDebugOutput(TENANT_ID, RULE_NODE_ID, msg, TbNodeConnectionType.FAILURE, null);
            then(mainCtxMock).shouldHaveNoMoreInteractions();
            return;
        }
        then(mainCtxMock).shouldHaveNoInteractions();
    }

    @MethodSource
    @ParameterizedTest
    public void givenDebugModeOptionsAndTbNodeConnection_whenOutput_thenVerify(
            boolean defaultDebugMode, boolean debugRuleNodeFailures, String connectionType) {
        // GIVEN
        var msgMock = mock(TbMsg.class);
        var ruleNode = new RuleNode(RULE_NODE_ID);
        ruleNode.setRuleChainId(RULE_CHAIN_ID);
        ruleNode.setDebugMode(defaultDebugMode);

        given(msgMock.popFormStack()).willReturn(new TbMsgProcessingStackItem(RULE_CHAIN_ID, RULE_NODE_ID));
        boolean hasFailureAndDebugFailuresIsEnabled = debugRuleNodeFailures
                                                      && TbNodeConnectionType.FAILURE.equals(connectionType);
        if (defaultDebugMode || hasFailureAndDebugFailuresIsEnabled) {
            given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        }
        if (!defaultDebugMode) {
            given(nodeCtxMock.isDebugRuleNodeFailures()).willReturn(debugRuleNodeFailures);
        }
        given(nodeCtxMock.getSelf()).willReturn(ruleNode);
        given(nodeCtxMock.getChainActor()).willReturn(chainActorMock);

        // WHEN
        defaultTbContext.output(msgMock, connectionType);

        // THEN
        then(msgMock).should().popFormStack();
        then(nodeCtxMock).should().getChainActor();
        then(nodeCtxMock).shouldHaveNoMoreInteractions();
        var expectedRuleChainOutputMsg = new RuleChainOutputMsg(
                RULE_CHAIN_ID,
                RULE_NODE_ID,
                connectionType,
                msgMock);
        then(chainActorMock).should().tell(expectedRuleChainOutputMsg);
        then(chainActorMock).shouldHaveNoMoreInteractions();

        if (defaultDebugMode) {
            then(mainCtxMock).should().persistDebugOutput(TENANT_ID, RULE_NODE_ID, msgMock, connectionType);
            then(mainCtxMock).shouldHaveNoMoreInteractions();
            return;
        }
        if (hasFailureAndDebugFailuresIsEnabled) {
            then(mainCtxMock).should().persistDebugOutput(TENANT_ID, RULE_NODE_ID, msgMock, connectionType);
            then(mainCtxMock).shouldHaveNoMoreInteractions();
            return;
        }
        then(mainCtxMock).shouldHaveNoInteractions();
    }

    @MethodSource
    @ParameterizedTest
    public void givenDebugModeOptionsAndTbNodeConnectionAndEmptyStack_whenOutput_thenVerifyMsgAck(
            boolean defaultDebugMode, String connectionType) {
        // GIVEN
        var msgMock = mock(TbMsg.class);
        var ruleNode = new RuleNode(RULE_NODE_ID);
        ruleNode.setRuleChainId(RULE_CHAIN_ID);
        ruleNode.setDebugMode(defaultDebugMode);

        given(msgMock.popFormStack()).willReturn(null);
        TbMsgCallback callbackMock = mock(TbMsgCallback.class);
        given(msgMock.getCallback()).willReturn(callbackMock);
        if (defaultDebugMode) {
            given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        }
        given(nodeCtxMock.getSelf()).willReturn(ruleNode);

        // WHEN
        defaultTbContext.output(msgMock, connectionType);

        // THEN
        then(msgMock).should().popFormStack();
        if (defaultDebugMode) {
            then(mainCtxMock).should().persistDebugOutput(TENANT_ID, RULE_NODE_ID, msgMock, "ACK", null);
        }
        then(callbackMock).should().onProcessingEnd(RULE_NODE_ID);
        then(callbackMock).should().onSuccess();
    }

    @MethodSource
    @ParameterizedTest
    public void givenDebugModeOptions_whenEnqueueForTellFailure_thenVerify(
            boolean defaultDebugMode, boolean debugRuleNodeFailures) {
        // GIVEN
        var msg = getTbMsgWithQueueName();
        var tpi = new TopicPartitionInfo(DataConstants.MAIN_QUEUE_TOPIC, TENANT_ID, 0, true);
        var ruleNode = new RuleNode(RULE_NODE_ID);
        ruleNode.setRuleChainId(RULE_CHAIN_ID);
        ruleNode.setDebugMode(defaultDebugMode);
        var tbClusterServiceMock = mock(TbClusterService.class);

        given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        given(nodeCtxMock.getSelf()).willReturn(ruleNode);
        given(mainCtxMock.resolve(any(ServiceType.class), anyString(), any(TenantId.class), any(EntityId.class))).willReturn(tpi);
        if (!defaultDebugMode) {
            given(nodeCtxMock.isDebugRuleNodeFailures()).willReturn(debugRuleNodeFailures);
        }
        given(mainCtxMock.getClusterService()).willReturn(tbClusterServiceMock);

        // WHEN
        defaultTbContext.enqueueForTellFailure(msg, EXCEPTION);

        // THEN
        then(mainCtxMock).should().resolve(ServiceType.TB_RULE_ENGINE, DataConstants.MAIN_QUEUE_NAME, TENANT_ID, TENANT_ID);
        TbMsg expectedTbMsg = TbMsg.newMsg(msg, msg.getQueueName(), RULE_CHAIN_ID, RULE_NODE_ID);
        if (defaultDebugMode || debugRuleNodeFailures) {
            ArgumentCaptor<TbMsg> tbMsgCaptor = ArgumentCaptor.forClass(TbMsg.class);
            then(mainCtxMock).should().persistDebugOutput(eq(TENANT_ID), eq(RULE_NODE_ID), tbMsgCaptor.capture(), eq(TbNodeConnectionType.FAILURE), isNull(), eq(EXCEPTION_MSG));
            TbMsg actualTbMsg = tbMsgCaptor.getValue();
            assertThat(actualTbMsg).usingRecursiveComparison()
                    .ignoringFields("id", "ctx")
                    .isEqualTo(expectedTbMsg);
        }
        then(mainCtxMock).should().getClusterService();
        then(mainCtxMock).shouldHaveNoMoreInteractions();

        ArgumentCaptor<ToRuleEngineMsg> toRuleEngineMsgCaptor = ArgumentCaptor.forClass(ToRuleEngineMsg.class);
        then(tbClusterServiceMock).should().pushMsgToRuleEngine(eq(tpi), notNull(UUID.class), toRuleEngineMsgCaptor.capture(), notNull(SimpleTbQueueCallback.class));
        ToRuleEngineMsg actualToRuleEngineMsg = toRuleEngineMsgCaptor.getValue();
        assertThat(actualToRuleEngineMsg).usingRecursiveComparison()
                .ignoringFields("tbMsg_")
                .isEqualTo(TransportProtos.ToRuleEngineMsg.newBuilder()
                        .setTenantIdMSB(TENANT_ID.getId().getMostSignificantBits())
                        .setTenantIdLSB(TENANT_ID.getId().getLeastSignificantBits())
                        .setTbMsg(TbMsg.toByteString(expectedTbMsg))
                        .setFailureMessage(EXCEPTION_MSG)
                        .addAllRelationTypes(List.of(TbNodeConnectionType.FAILURE)).build());
        then(tbClusterServiceMock).shouldHaveNoMoreInteractions();
    }

    @MethodSource
    @ParameterizedTest
    public void givenDebugModeOptions_whenEnqueueForTellNext_thenVerify(
            boolean defaultDebugMode, boolean debugRuleNodeFailures, String connectionType) {
        // GIVEN
        var msg = getTbMsgWithQueueName();
        var tpi = new TopicPartitionInfo(DataConstants.MAIN_QUEUE_TOPIC, TENANT_ID, 0, true);
        var ruleNode = new RuleNode(RULE_NODE_ID);
        ruleNode.setRuleChainId(RULE_CHAIN_ID);
        ruleNode.setDebugMode(defaultDebugMode);
        var tbClusterServiceMock = mock(TbClusterService.class);

        given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        given(nodeCtxMock.getSelf()).willReturn(ruleNode);
        given(mainCtxMock.resolve(any(ServiceType.class), anyString(), any(TenantId.class), any(EntityId.class))).willReturn(tpi);
        if (!defaultDebugMode) {
            given(nodeCtxMock.isDebugRuleNodeFailures()).willReturn(debugRuleNodeFailures);
        }
        given(mainCtxMock.getClusterService()).willReturn(tbClusterServiceMock);

        // WHEN
        defaultTbContext.enqueueForTellNext(msg, connectionType);

        // THEN
        then(mainCtxMock).should().resolve(ServiceType.TB_RULE_ENGINE, DataConstants.MAIN_QUEUE_NAME, TENANT_ID, TENANT_ID);
        TbMsg expectedTbMsg = TbMsg.newMsg(msg, msg.getQueueName(), RULE_CHAIN_ID, RULE_NODE_ID);
        if (defaultDebugMode) {
            ArgumentCaptor<TbMsg> tbMsgCaptor = ArgumentCaptor.forClass(TbMsg.class);
            then(mainCtxMock).should().persistDebugOutput(eq(TENANT_ID), eq(RULE_NODE_ID), tbMsgCaptor.capture(), eq(connectionType), isNull(), isNull());
            TbMsg actualTbMsg = tbMsgCaptor.getValue();
            assertThat(actualTbMsg).usingRecursiveComparison()
                    .ignoringFields("id", "ctx")
                    .isEqualTo(expectedTbMsg);
        }
        then(mainCtxMock).should().getClusterService();
        then(mainCtxMock).shouldHaveNoMoreInteractions();

        ArgumentCaptor<ToRuleEngineMsg> toRuleEngineMsgCaptor = ArgumentCaptor.forClass(ToRuleEngineMsg.class);
        then(tbClusterServiceMock).should().pushMsgToRuleEngine(eq(tpi), notNull(UUID.class), toRuleEngineMsgCaptor.capture(), notNull(SimpleTbQueueCallback.class));
        ToRuleEngineMsg actualToRuleEngineMsg = toRuleEngineMsgCaptor.getValue();
        assertThat(actualToRuleEngineMsg).usingRecursiveComparison()
                .ignoringFields("tbMsg_")
                .isEqualTo(TransportProtos.ToRuleEngineMsg.newBuilder()
                        .setTenantIdMSB(TENANT_ID.getId().getMostSignificantBits())
                        .setTenantIdLSB(TENANT_ID.getId().getLeastSignificantBits())
                        .setTbMsg(TbMsg.toByteString(expectedTbMsg))
                        .addAllRelationTypes(List.of(connectionType)).build());
        then(tbClusterServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void givenInvalidMsg_whenEnqueueForTellFailure_thenDoNothing() {
        // GIVEN
        var msgMock = mock(TbMsg.class);
        var tpi = new TopicPartitionInfo(DataConstants.MAIN_QUEUE_TOPIC, TENANT_ID, 0, true);

        given(msgMock.getOriginator()).willReturn(TENANT_ID);
        given(msgMock.getQueueName()).willReturn(DataConstants.MAIN_QUEUE_NAME);
        given(msgMock.isValid()).willReturn(false);
        given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        given(mainCtxMock.resolve(any(ServiceType.class), anyString(), any(TenantId.class), any(EntityId.class))).willReturn(tpi);

        // WHEN
        defaultTbContext.enqueueForTellFailure(msgMock, EXCEPTION);

        // THEN
        then(msgMock).should(times(2)).getQueueName();
        then(msgMock).should().getOriginator();
        then(msgMock).should().isValid();
        then(msgMock).shouldHaveNoMoreInteractions();

        then(mainCtxMock).should().resolve(ServiceType.TB_RULE_ENGINE, DataConstants.MAIN_QUEUE_NAME, TENANT_ID, TENANT_ID);
        then(mainCtxMock).shouldHaveNoMoreInteractions();

        then(nodeCtxMock).should(times(2)).getTenantId();
        then(nodeCtxMock).shouldHaveNoMoreInteractions();
        then(chainActorMock).shouldHaveNoInteractions();
    }

    @MethodSource
    @ParameterizedTest
    public void givenDebugModeOptions_whenTellFailure_thenVerify(
            boolean defaultDebugMode, boolean debugRuleNodeFailures) {
        // GIVEN
        var msg = getTbMsg();
        var ruleNode = new RuleNode(RULE_NODE_ID);
        ruleNode.setRuleChainId(RULE_CHAIN_ID);
        ruleNode.setDebugMode(defaultDebugMode);

        boolean persistDebugOutput = defaultDebugMode || debugRuleNodeFailures;
        if (persistDebugOutput) {
            given(nodeCtxMock.getTenantId()).willReturn(TENANT_ID);
        }
        if (!defaultDebugMode) {
            given(nodeCtxMock.isDebugRuleNodeFailures()).willReturn(debugRuleNodeFailures);
        }
        given(nodeCtxMock.getSelf()).willReturn(ruleNode);
        given(nodeCtxMock.getChainActor()).willReturn(chainActorMock);

        // WHEN
        defaultTbContext.tellFailure(msg, EXCEPTION);

        // THEN
        then(nodeCtxMock).should().getChainActor();
        then(nodeCtxMock).shouldHaveNoMoreInteractions();
        var expectedRuleNodeToRuleChainTellNextMsg = new RuleNodeToRuleChainTellNextMsg(
                RULE_CHAIN_ID,
                RULE_NODE_ID,
                Collections.singleton(TbNodeConnectionType.FAILURE),
                msg,
                EXCEPTION_MSG
        );
        then(chainActorMock).should().tell(expectedRuleNodeToRuleChainTellNextMsg);
        then(chainActorMock).shouldHaveNoMoreInteractions();

        if (persistDebugOutput) {
            then(mainCtxMock).should().persistDebugOutput(TENANT_ID, RULE_NODE_ID, msg, TbNodeConnectionType.FAILURE, EXCEPTION);
            then(mainCtxMock).shouldHaveNoMoreInteractions();
            return;
        }
        then(mainCtxMock).shouldHaveNoInteractions();
    }

    private static Stream<Arguments> givenDebugModeOptionsAndTbNodeConnectionsSet_whenTellNext_thenVerify() {
        return Stream.of(
                Arguments.of(true, false, Set.of(TbNodeConnectionType.FAILURE, TbNodeConnectionType.OTHER)),
                Arguments.of(false, true, Set.of(TbNodeConnectionType.FAILURE, TbNodeConnectionType.FALSE)),
                Arguments.of(false, false, Set.of(TbNodeConnectionType.FAILURE, TbNodeConnectionType.TRUE)),
                Arguments.of(true, false, Set.of(TbNodeConnectionType.SUCCESS))
        );
    }

    private static Stream<Arguments> givenDebugModeOptionsAndTbNodeConnection_whenOutput_thenVerify() {
        return Stream.of(
                Arguments.of(true, false, TbNodeConnectionType.FAILURE),
                Arguments.of(false, true, TbNodeConnectionType.FAILURE),
                Arguments.of(false, false, TbNodeConnectionType.FAILURE),
                Arguments.of(true, false, TbNodeConnectionType.FAILURE),
                // same debug mode options but with Success connection
                Arguments.of(true, false, TbNodeConnectionType.SUCCESS),
                Arguments.of(false, true, TbNodeConnectionType.SUCCESS),
                Arguments.of(false, false, TbNodeConnectionType.SUCCESS),
                Arguments.of(true, false, TbNodeConnectionType.SUCCESS)
        );
    }

    private static Stream<Arguments> givenDebugModeOptionsAndTbNodeConnectionAndEmptyStack_whenOutput_thenVerifyMsgAck() {
        return Stream.of(
                Arguments.of(true, TbNodeConnectionType.FAILURE),
                Arguments.of(false, TbNodeConnectionType.FAILURE),
                Arguments.of(false, TbNodeConnectionType.FAILURE),
                Arguments.of(true, TbNodeConnectionType.FAILURE),
                // same debug mode options but with Success connection
                Arguments.of(true, TbNodeConnectionType.SUCCESS),
                Arguments.of(false, TbNodeConnectionType.SUCCESS),
                Arguments.of(false, TbNodeConnectionType.SUCCESS),
                Arguments.of(true, TbNodeConnectionType.SUCCESS)
        );
    }

    private static Stream<Arguments> givenDebugModeOptions_whenEnqueueForTellFailure_thenVerify() {
        return Stream.of(
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false),
                Arguments.of(true, false)
        );
    }

    private static Stream<Arguments> givenDebugModeOptions_whenEnqueueForTellNext_thenVerify() {
        return Stream.of(
                Arguments.of(true, false, TbNodeConnectionType.OTHER),
                Arguments.of(false, true, TbNodeConnectionType.TRUE),
                Arguments.of(false, false, TbNodeConnectionType.FALSE),
                Arguments.of(true, false, TbNodeConnectionType.SUCCESS)
        );
    }

    private static Stream<Arguments> givenDebugModeOptions_whenTellFailure_thenVerify() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false)
        );
    }

    private TbMsg getTbMsgWithCallback(TbMsgCallback callback) {
        return TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, TENANT_ID, TbMsgMetaData.EMPTY, TbMsg.EMPTY_STRING, callback);
    }

    private TbMsg getTbMsgWithQueueName() {
        return TbMsg.newMsg(DataConstants.MAIN_QUEUE_NAME, TbMsgType.POST_TELEMETRY_REQUEST, TENANT_ID, TbMsgMetaData.EMPTY, TbMsg.EMPTY_STRING);
    }

    private TbMsg getTbMsg() {
        return TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, TENANT_ID, TbMsgMetaData.EMPTY, TbMsg.EMPTY_STRING);
    }

}

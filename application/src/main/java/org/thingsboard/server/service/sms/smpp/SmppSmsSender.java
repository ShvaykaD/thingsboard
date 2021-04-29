/**
 * Copyright © 2016-2021 The Thingsboard Authors
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
package org.thingsboard.server.service.sms.smpp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.thingsboard.rule.engine.api.sms.exception.SmsException;
import org.thingsboard.server.common.data.sms.config.SmppSmsProviderConfiguration;
import org.thingsboard.server.service.sms.AbstractSmsSender;

import java.io.IOException;

@Slf4j
public class SmppSmsSender extends AbstractSmsSender {

    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    private SMPPSession session;

    public SmppSmsSender(SmppSmsProviderConfiguration config) {
        String host = config.getSmppHost();
        int port = config.getSmppPort();
        String username = config.getUsername();
        String password = config.getPassword();
        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Invalid SMPP sms provider configuration: host, port, username and password should be specified!");
        }
        this.session = initSession(host, port, username, password);
    }

    @Override
    public int sendSms(String numberTo, String message) throws SmsException {
//        // TODO: 3/2/21 Add implementation
//        if (session != null) {
//            try {
//                RegisteredDelivery registeredDelivery = new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT);
//                String cmt = session.submitShortMessage("CMT",
//                        TypeOfNumber.UNKNOWN,
//                        NumberingPlanIndicator.UNKNOWN,
//                        "1616",
//                        TypeOfNumber.UNKNOWN,
//                        NumberingPlanIndicator.UNKNOWN,
//                        numberTo,
//                        new ESMClass(),
//                        (byte) 0,
//                        (byte) 1,
//                        TIME_FORMATTER.format(new Date()),
//                        null,
//                        registeredDelivery,
//                        (byte) 0, DataCodings.ZERO, (byte) 0, message.getBytes());
//                log.info("cmt: [{}]", cmt);
//            } catch (Exception e) {
//                throw new SmsSendException(e.getMessage());
//            }
//        }
        return 0;
    }

    @Override
    public void destroy() {
        if (session != null) {
            session.unbindAndClose();
        }
    }

    private SMPPSession initSession(String host, int port, String username, String password) {
        SMPPSession session = new SMPPSession();
        try {
            String systemId = session.connectAndBind(host, port, new BindParameter(BindType.BIND_TX, username, password, null, TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));
            log.info("Registered SMPP session with system id [{}]", systemId);
            return session;
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error occurred during SMPP connection to server: [" + host + ":" + port + "]", e);
        }
    }
}

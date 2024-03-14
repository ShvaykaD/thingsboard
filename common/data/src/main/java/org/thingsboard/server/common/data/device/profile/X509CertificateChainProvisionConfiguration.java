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
package org.thingsboard.server.common.data.device.profile;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.DeviceProfileProvisionType;

@Data
@NoArgsConstructor
public class X509CertificateChainProvisionConfiguration implements DeviceProfileProvisionConfiguration {

    private String provisionDeviceSecret;
    private String certificateRegExPattern;
    private boolean allowCreateNewDevicesByX509Certificate;

    @Override
    public DeviceProfileProvisionType getType() {
        return DeviceProfileProvisionType.X509_CERTIFICATE_CHAIN;
    }

}

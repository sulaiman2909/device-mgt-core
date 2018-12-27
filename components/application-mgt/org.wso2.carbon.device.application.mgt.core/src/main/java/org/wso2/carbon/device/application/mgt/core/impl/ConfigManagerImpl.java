/* Copyright (c) 2018, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.application.mgt.core.impl;

import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ConfigManager;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;

public class ConfigManagerImpl implements ConfigManager {

    private UIConfiguration uiConfiguration;

    public ConfigManagerImpl(UIConfiguration config) {
        this.uiConfiguration = config;
    }

    @Override public UIConfiguration getUIConfiguration() throws ApplicationManagementException {
        return this.uiConfiguration;
    }
}

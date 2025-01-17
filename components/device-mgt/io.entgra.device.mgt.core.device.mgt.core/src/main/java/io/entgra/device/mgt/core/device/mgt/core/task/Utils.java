/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.task;

import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static Map<String, Long> getTenantedTaskOperationMap(Map<Integer, Map<String, Map<String, Long>>> map,
                                                                String deviceType) {
        Map<String, Long> taskMap = new HashMap<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (map.containsKey(tenantId)) {
            if (map.get(tenantId).containsKey(deviceType)) {
                return map.get(tenantId).get(deviceType);
            } else {
                Map<String, Map<String, Long>> existingTenantMap = map.get(tenantId);
                existingTenantMap.put(deviceType, taskMap);
                return taskMap;
            }
        } else {
            HashMap<String, Map<String, Long>> typeMap = new HashMap<>();
            typeMap.put(deviceType, taskMap);
            map.put(tenantId, typeMap);
            return taskMap;
        }
    }

    public static boolean getIsTenantedStartupConfig(Map<Integer, List<String>> map, String deviceType) {
        List<String> deviceTypes;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (map.containsKey(tenantId)) {
            if (map.get(tenantId).contains(deviceType)) {
                return false;
            } else {
                deviceTypes = map.get(tenantId);
                deviceTypes.add(deviceType);
                map.put(tenantId, deviceTypes);
                return true;
            }
        } else {
            deviceTypes = new ArrayList<>();
            deviceTypes.add(deviceType);
            map.put(tenantId, deviceTypes);
            return true;
        }
    }

}

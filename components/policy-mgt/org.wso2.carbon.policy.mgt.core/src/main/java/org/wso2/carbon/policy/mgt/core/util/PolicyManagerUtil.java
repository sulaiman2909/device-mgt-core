/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.policy.mgt.core.util;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.policy.mgt.CorrectiveAction;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.config.tenant.PlatformConfigurationManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.PolicyTransformException;
import org.wso2.carbon.policy.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.policy.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.policy.mgt.core.impl.PolicyAdministratorPointImpl;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.*;

public class PolicyManagerUtil {

    public static final String GENERAL_CONFIG_RESOURCE_PATH = "general";
    public static final String MONITORING_FREQUENCY = "notifierFrequency";
    private static final Log log = LogFactory.getLog(PolicyManagerUtil.class);

    public static Document convertToDocument(File file) throws PolicyManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new PolicyManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    /**
     * Resolve data source from the data source definition
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration " +
                    "is null and thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource =
                        PolicyManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = PolicyManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    public static String makeString(List<Integer> values) {

        StringBuilder buff = new StringBuilder();
        for (int value : values) {
            buff.append(value).append(",");
        }
        buff.deleteCharAt(buff.length() - 1);
        return buff.toString();
    }

    public static Operation transformPolicy(Policy policy) throws PolicyTransformException {
        List<ProfileFeature> effectiveFeatures = policy.getProfile().getProfileFeaturesList();

        PolicyOperation policyOperation = new PolicyOperation();
        policyOperation.setEnabled(true);
        policyOperation.setType(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.POLICY);
        policyOperation.setCode(PolicyOperation.POLICY_OPERATION_CODE);
        /*if (policy.getPolicyType() != null &&
                PolicyManagementConstants.GENERAL_POLICY_TYPE.equals(policy.getPolicyType()) &&
                policy.getCorrectiveActions() != null) {
            for (CorrectiveAction correctiveAction : policy.getCorrectiveActions()) {
                if (PolicyManagementConstants.POLICY_CORRECTIVE_ACTION_TYPE
                        .equalsIgnoreCase(correctiveAction.getActionType())) {
                    PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
                    try {
                        Policy correctivePolicy = pap.getPolicy(correctiveAction.getPolicyId());
                        if (correctivePolicy == null || !PolicyManagementConstants.CORRECTIVE_POLICY_TYPE
                                .equalsIgnoreCase(correctivePolicy.getPolicyType() )) {
                            String msg = "No corrective policy was found for the policy " + policy.getPolicyName() +
                                    " and policy ID " + policy.getId();
                            log.error(msg);
                            throw new PolicyTransformException(msg);
                        } else {
                            List<ProfileOperation> correctiveProfileOperations = createProfileOperations(
                                    correctivePolicy.getProfile().getProfileFeaturesList());
                            ProfileFeature correctivePolicyFeature = new ProfileFeature();
                            correctivePolicyFeature.setProfileId(correctivePolicy.getProfileId());
                            correctivePolicyFeature.setContent(new Gson().toJson(correctiveProfileOperations));
                            correctivePolicyFeature.setDeviceType(correctivePolicy.getProfile().getDeviceType());
                            correctivePolicyFeature.setFeatureCode(
                                    PolicyManagementConstants.CORRECTIVE_POLICY_FEATURE_CODE);
                            correctivePolicyFeature.setId(correctivePolicy.getId());
                            effectiveFeatures.add(correctivePolicyFeature);
                        }
                    } catch (PolicyManagementException e) {
                        String msg = "Error occurred while retrieving corrective policy for policy " +
                                     policy.getPolicyName() + " and policy ID " + policy.getId();
                        log.error(msg, e);
                        throw new PolicyTransformException(msg, e);
                    }
                    // Currently only supported POLICY corrective action type so the break is added. This should be
                    // removed when we start supporting other corrective action types
                    break;
                }
            }
        }*/

        policyOperation.setProfileOperations(createProfileOperations(effectiveFeatures));
        if (policy.getPolicyType() != null &&
                PolicyManagementConstants.GENERAL_POLICY_TYPE.equals(policy.getPolicyType())) {
            setCorrectiveActions(effectiveFeatures, policyOperation, policy);
        }
        policyOperation.setPayLoad(policyOperation.getProfileOperations());
        return policyOperation;
    }

    private static void setCorrectiveActions(List<ProfileFeature> features,
                                             PolicyOperation policyOperation, Policy policy) throws PolicyTransformException {
        List<ProfileFeature> effectiveFeatures = new ArrayList<>(features);
        for (ProfileFeature effectiveFeature : features) {
            if (effectiveFeature.getCorrectiveActions() != null) {
                for (CorrectiveAction correctiveAction : effectiveFeature.getCorrectiveActions()) {
                    if (PolicyManagementConstants.POLICY_CORRECTIVE_ACTION_TYPE
                            .equalsIgnoreCase(correctiveAction.getActionType())) {
                        PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
                        try {
                            Policy correctivePolicy = pap.getPolicy(correctiveAction.getPolicyId());
                            if (correctivePolicy == null || !PolicyManagementConstants.CORRECTIVE_POLICY_TYPE
                                    .equalsIgnoreCase(correctivePolicy.getPolicyType() )) {
                                String msg = "No corrective policy was found for the policy " + policy.getPolicyName() +
                                        " and policy ID " + policy.getId();
                                log.error(msg);
                                throw new PolicyTransformException(msg);
                            } else {
                                List<ProfileOperation> correctiveProfileOperations = createProfileOperations(
                                        correctivePolicy.getProfile().getProfileFeaturesList());
                                ProfileFeature correctivePolicyFeature = new ProfileFeature();
                                correctivePolicyFeature.setProfileId(correctivePolicy.getProfileId());
                                correctivePolicyFeature.setContent(new Gson().toJson(correctiveProfileOperations));
                                correctivePolicyFeature.setDeviceType(correctivePolicy.getProfile().getDeviceType());
                                correctivePolicyFeature.setFeatureCode(
                                        PolicyManagementConstants.CORRECTIVE_POLICY_FEATURE_CODE);
                                correctivePolicyFeature.setId(correctivePolicy.getId());
                                List<ProfileOperation> profileOperations = policyOperation.getProfileOperations();
                                effectiveFeatures.add(correctivePolicyFeature);
                            }
                        } catch (PolicyManagementException e) {
                            String msg = "Error occurred while retrieving corrective policy for policy " +
                                    policy.getPolicyName() + " and policy ID " + policy.getId();
                            log.error(msg, e);
                            throw new PolicyTransformException(msg, e);
                        }
                        // Currently only supported POLICY corrective action type so the break is added. This should be
                        // removed when we start supporting other corrective action types
                        break;
                    }
                }
            }
        }
    }


    public static List<ProfileOperation> createProfileOperations(List<ProfileFeature> effectiveFeatures) {
        List<ProfileOperation> profileOperations = new ArrayList<>();
        for (ProfileFeature feature : effectiveFeatures) {
            ProfileOperation profileOperation = new ProfileOperation();
            profileOperation.setCode(feature.getFeatureCode());
            profileOperation.setEnabled(true);
            profileOperation.setStatus(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Status.PENDING);
            profileOperation.setType(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.PROFILE);
            profileOperation.setPayLoad(feature.getContent());
            profileOperations.add(profileOperation);
        }
        return  profileOperations;
    }


    public static byte[] getBytes(Object obj) throws java.io.IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        bos.close();
        byte[] data = bos.toByteArray();
        return data;
    }

    public static boolean convertIntToBoolean(int x) {

        return x == 1;
    }


//    public static Cache getCacheManagerImpl() {
//        return Caching.getCacheManagerFactory()
//                .getCacheManager(PolicyManagementConstants.DM_CACHE_MANAGER).getCache(PolicyManagementConstants
//                        .DM_CACHE);
//    }


    public static Cache<Integer, Policy> getPolicyCache(String name) {
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<Integer, Policy>getCache(name) :
                Caching.getCacheManager().<Integer, Policy>getCache(name);
    }

    public static Cache<Integer, List<Policy>> getPolicyListCache(String name) {
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<Integer, List<Policy>>getCache(name) :
                Caching.getCacheManager().<Integer, List<Policy>>getCache(name);
    }

    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().getCacheManager(
                PolicyManagementConstants.DM_CACHE_MANAGER);
    }


    public static HashMap<Integer, Device> covertDeviceListToMap(List<Device> devices) {

        HashMap<Integer, Device> deviceHashMap = new HashMap<>();
        for (Device device : devices) {
            deviceHashMap.put(device.getId(), device);
        }
        return deviceHashMap;
    }


    public static int getMonitoringFrequency() throws PolicyManagementException {

        PlatformConfigurationManagementService configMgtService = new PlatformConfigurationManagementServiceImpl();
        PlatformConfiguration tenantConfiguration;
        int monitoringFrequency = 0;
        try {
            tenantConfiguration = configMgtService.getConfiguration(GENERAL_CONFIG_RESOURCE_PATH);
            List<ConfigurationEntry> configuration = tenantConfiguration.getConfiguration();

            if (configuration != null && !configuration.isEmpty()) {
                for (ConfigurationEntry cEntry : configuration) {
                    if (MONITORING_FREQUENCY.equalsIgnoreCase(cEntry.getName())) {
                        if (cEntry.getValue() == null) {
                            throw new PolicyManagementException("Invalid value, i.e. '" + cEntry.getValue() +
                                    "', is configured as the monitoring frequency");
                        }
                        monitoringFrequency = (int) (Double.parseDouble(cEntry.getValue().toString()) + 0.5d);
                    }
                }
            }

        } catch (ConfigurationManagementException e) {
            log.error("Error while getting the configurations from registry.", e);
        }

        if (monitoringFrequency == 0) {
            PolicyConfiguration policyConfiguration = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig().getPolicyConfiguration();
            monitoringFrequency = policyConfiguration.getMonitoringFrequency();
        }

        return monitoringFrequency;
    }


    public static Map<Integer, DeviceGroup> convertDeviceGroupMap(List<DeviceGroup> deviceGroups) {
        Map<Integer, DeviceGroup> groupMap = new HashMap<>();
        for (DeviceGroup dg: deviceGroups){
            groupMap.put(dg.getGroupId(), dg);
        }
        return groupMap;
    }
}

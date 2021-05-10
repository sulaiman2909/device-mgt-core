/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.application.extension;

//import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.application.extension.bean.APIRegistrationProfile;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import org.wso2.carbon.apimgt.application.extension.util.APIManagerUtil;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
//import org.wso2.carbon.apimgt.integration.client.OAuthRequestInterceptor;
//import org.wso2.carbon.apimgt.integration.client.store.StoreClient;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.APIInfo;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.APIList;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.Application;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.ApplicationInfo;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.ApplicationKey;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.ApplicationKeyGenerateRequest;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.ApplicationList;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.Subscription;
//import org.wso2.carbon.apimgt.integration.generated.client.store.model.SubscriptionList;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.config.ui.UIConfiguration;
import org.wso2.carbon.device.mgt.core.config.ui.UIConfigurationManager;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementProviderServiceImpl implements APIManagementProviderService {

    private static final Log log = LogFactory.getLog(APIManagementProviderServiceImpl.class);
    private static final String CONTENT_TYPE = "application/json";
    private static final int MAX_API_PER_TAG = 200;
    private static final String APP_TIER_TYPE = "application";
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();

    @Override
    public boolean isTierLoaded() {
//        StoreClient storeClient = APIApplicationManagerExtensionDataHolder.getInstance().getIntegrationClientService()
//                .getStoreClient();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getTenantDomain();
//        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
//        try {
        try {
            APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
        } catch (APIManagementException e) {
            log.error("APIs not ready", e);
        }
        //            storeClient.getIndividualTier().tiersTierLevelTierNameGet(ApiApplicationConstants.DEFAULT_TIER,
//                    APP_TIER_TYPE,
//                    tenantDomain, CONTENT_TYPE, null, null);
            return true;
//        } catch (FeignException e) {
//            log.error("Feign Exception", e);
//            if (e.status() == 401) {
//                OAuthRequestInterceptor oAuthRequestInterceptor = new OAuthRequestInterceptor();
//                String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
//                oAuthRequestInterceptor.removeToken(username, tenantDomain);
//                try {
//                    storeClient.getIndividualTier().tiersTierLevelTierNameGet(ApiApplicationConstants.DEFAULT_TIER,
//                            APP_TIER_TYPE, tenantDomain, CONTENT_TYPE, null, null);
//                } catch (FeignException ex) {
//                    log.error("Invalid Attempt : " + ex);
//                }
//            }
//        } catch (Exception e) {
//            log.error("APIs not ready", e);
//        }
//        return false;
    }

    @Override
    public void removeAPIApplication(String applicationName, String username) throws APIManagerException {

//        StoreClient storeClient = APIApplicationManagerExtensionDataHolder.getInstance().getIntegrationClientService()
//                .getStoreClient();
//        ApplicationList applicationList = storeClient.getApplications()
//                .applicationsGet("", applicationName, 1, 0, CONTENT_TYPE, null);
        try {
            APIConsumer apiConsumer = API_MANAGER_FACTORY.getAPIConsumer(username);
            Application application = apiConsumer.getApplicationsByName(username, applicationName, "");
            if (application != null) {
//                ApplicationInfo applicationInfo = applicationList.getList().get(0);
//                storeClient.getIndividualApplication().applicationsApplicationIdDelete(applicationInfo.getApplicationId(),
//                        null, null);
                apiConsumer.removeApplication(application, username);
            }
        } catch (APIManagementException e) {
            //todo:amalka
            e.printStackTrace();
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String tags[],
                                                                             String keyType, String username,
                                                                             boolean isAllowedAllDomains,
            String validityTime, String scopes) throws APIManagerException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isEmpty(username)) {
            username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        }
        try {
            APIConsumer apiConsumer = API_MANAGER_FACTORY.getAPIConsumer(username);
            Application application = apiConsumer.getApplicationsByName(username, applicationName, "");

            int applicationId = 0;
            Subscriber subscriber = null;
            if (application == null) {
                subscriber = apiConsumer.getSubscriber(username);
                if (subscriber == null) {
                    // create subscriber
                    apiConsumer.addSubscriber(username, "");
                    subscriber = apiConsumer.getSubscriber(username);
                }
                //create application
                application = new Application(applicationName, subscriber);
                application.setTier(ApiApplicationConstants.DEFAULT_TIER);
                application.setGroupId("");
                applicationId = apiConsumer.addApplication(application, username);
            } else {
                applicationId = application.getId();
                subscriber = apiConsumer.getSubscriber(username);
            }

            Set<SubscribedAPI> subscribedAPIs =
                    apiConsumer.getSubscribedAPIsByApplicationId(subscriber, applicationId, "");

            log.info("Already subscribed API count: " + subscribedAPIs.size());

            // subscribe to apis.
            Set<String> tempApiIds = new HashSet<>();
            if (tags != null && tags.length > 0) {
                for (String tag : tags) {
                    Set<API> apisWithTag = apiConsumer.getAPIsWithTag(tag, tenantDomain);
                    if (apisWithTag == null || apisWithTag.size() == 0) {
                        apisWithTag = apiConsumer.getAPIsWithTag(tag, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    }

                    if (apisWithTag != null && apisWithTag.size() > 0) {
                        for (API apiInfo : apisWithTag) {
                            String id = apiInfo.getId().getProviderName().replace("@", "-AT-")
                                    + "-" + apiInfo.getId().getName() + "-" + apiInfo.getId().getVersion();
                            // todo: amalka will this break old apis?
                            boolean subscriptionExist = false;
                            if (subscribedAPIs.size() > 0) {
                                for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                                    if (String.valueOf(subscribedAPI.getApiId().toString()).equals(id)) {
                                        subscriptionExist = true;
                                        break;
                                    }
                                }
                            }
                            if (!subscriptionExist && !tempApiIds.contains(id)) {
                                ApiTypeWrapper apiTypeWrapper = new ApiTypeWrapper(apiInfo);
                                apiTypeWrapper.setTier(ApiApplicationConstants.DEFAULT_TIER);
                                apiConsumer.addSubscription(apiTypeWrapper, username, applicationId, "");
                                tempApiIds.add(id);
                            }
                        }
                    }
                }
            }
            //end of subscription

            List<APIKey> applicationKeys = application.getKeys();
            if (applicationKeys != null) {
                for (APIKey applicationKey : applicationKeys) {
                    if (keyType.equals(applicationKey.getType())) {
                        if (applicationKey.getConsumerKey() != null && !applicationKey.getConsumerKey().isEmpty()) {
                            ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                            apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
                            apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());
                            return apiApplicationKey;
                        }
                    }
                }
            }

            List<String> allowedDomains = new ArrayList<>();
            if (isAllowedAllDomains) {
                allowedDomains.add(ApiApplicationConstants.ALLOWED_DOMAINS);
            } else {
                allowedDomains.add(APIManagerUtil.getTenantDomain());
            }

            APIAdmin apiAdmin = new APIAdminImpl();
            String keyManagerId = null;
            try {
                List<KeyManagerConfigurationDTO> keyManagerConfigurations = apiAdmin
                        .getKeyManagerConfigurationsByTenant(tenantDomain);
                if (keyManagerConfigurations != null) {
                    for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
                        keyManagerId = keyManagerConfigurationDTO.getUuid();
                    }
                }
                String jsonString = "{\"grant_types\":\"refresh_token,urn:ietf:params:oauth:grant-type:saml2-bearer," +
                        "password,client_credentials,iwa:ntlm,urn:ietf:params:oauth:grant-type:jwt-bearer\"," +
                        "\"additionalProperties\":\"{\\\"application_access_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"user_access_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"refresh_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"id_token_expiry_time\\\":\\\"N\\/A\\\"}\"," +
                        "\"username\":\"" + username + "\"}";

                // if scopes not defined
                if (StringUtils.isEmpty(scopes)) {
                    UIConfigurationManager uiConfigurationManager = UIConfigurationManager.getInstance();
                    UIConfiguration uiConfiguration = uiConfigurationManager.getUIConfig();
                    List<String> scopeList = uiConfiguration.getScopes();

                    if (scopeList != null && scopeList.size() > 0) {
                        StringBuilder builder = new StringBuilder();
                        for (String scope : scopeList) {
                            String tmpScope = scope + " ";
                            builder.append(tmpScope);
                        }
                        scopes = builder.toString();
                    }

                    if (StringUtils.isEmpty(scopes)) {
                        scopes = scopes.trim();
                    } else {
                        scopes = "default";
                    }
                }

                Map<String, Object> keyDetails = apiConsumer
                        .requestApprovalForApplicationRegistration(username, applicationName, keyType, "",
                                allowedDomains.toArray(new String[allowedDomains.size()]), validityTime, scopes, "",
                                jsonString, keyManagerId, tenantDomain);

                if (keyDetails != null) {
                    ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                    apiApplicationKey.setConsumerKey((String) keyDetails.get("consumerKey"));
                    apiApplicationKey.setConsumerSecret((String) keyDetails.get("consumerSecret"));
                    return apiApplicationKey;
                }
                throw new APIManagerException("Failed to generate keys for tenant: " + tenantDomain);
            } catch (APIManagementException e) {
                throw new APIManagerException("Failed to create api application for tenant: " + tenantDomain, e);
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to create api application for tenant: " + tenantDomain, e);
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String tags[],
                                                                             String keyType, String username,
                                                                             boolean isAllowedAllDomains,
                                                                             String validityTime)
            throws APIManagerException {
        return this.generateAndRetrieveApplicationKeys(applicationName, tags, keyType, username,
                isAllowedAllDomains, validityTime, null);
    }

    @Override
    public AccessTokenInfo getAccessToken(String scopes, String[] tags, String applicationName, String tokenType,
            String validityPeriod) throws APIManagerException {
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            ApiApplicationKey clientCredentials = getClientCredentials(tenantDomain, tags, applicationName, tokenType,
                    validityPeriod);

            if (clientCredentials == null) {
                String msg = "Oauth Application creation is failed.";
                log.error(msg);
                throw new APIManagerException(msg);
            }

            String user =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() + "@" + PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getTenantDomain(true);

            JWTClientManagerService jwtClientManagerService = APIApplicationManagerExtensionDataHolder.getInstance()
                    .getJwtClientManagerService();
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            AccessTokenInfo accessTokenForAdmin = jwtClient
                    .getAccessToken(clientCredentials.getConsumerKey(), clientCredentials.getConsumerSecret(), user,
                            scopes);

            return accessTokenForAdmin;
        } catch (JWTClientException e) {
            String msg = "JWT Error occurred while registering Application to get access token.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (APIManagerException e) {
            String msg = "Error occurred while getting access tokens.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (UserStoreException e) {
            String msg = "User management exception when getting client credentials.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }

    /**
     * Get Client credentials
     * @param tenantDomain Tenant Domain
     * @param tags Tags
     * @param applicationName Application Name
     * @param tokenType Token Type
     * @param validityPeriod Validity Period
     * @return {@link ApiApplicationKey}
     * @throws APIManagerException if error occurred while generating access token
     * @throws UserStoreException if error ocurred while getting admin username.
     */
    private ApiApplicationKey getClientCredentials(String tenantDomain, String[] tags, String applicationName,
            String tokenType, String validityPeriod) throws APIManagerException, UserStoreException {

        APIRegistrationProfile registrationProfile = new APIRegistrationProfile();
        registrationProfile.setAllowedToAllDomains(false);
        registrationProfile.setMappingAnExistingOAuthApp(false);
        registrationProfile.setTags(tags);
        registrationProfile.setApplicationName(applicationName);

        ApiApplicationKey info = null;
        if (tenantDomain == null || tenantDomain.isEmpty()) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                            .getAdminUserName());

            if (registrationProfile.getUsername() == null || registrationProfile.getUsername().isEmpty()) {
                info = generateAndRetrieveApplicationKeys(registrationProfile.getApplicationName(),
                        registrationProfile.getTags(), tokenType, null,
                        registrationProfile.isAllowedToAllDomains(), validityPeriod);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return info;
    }
}

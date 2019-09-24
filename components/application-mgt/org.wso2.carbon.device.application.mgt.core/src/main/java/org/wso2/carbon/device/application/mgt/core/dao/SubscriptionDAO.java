/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;
import java.util.Map;

/**
 * This interface provides the list of operations that are supported with subscription database.
 *
 */
public interface SubscriptionDAO {

    List<Integer> addDeviceSubscription(String subscribedBy, List<Integer> deviceIds, String subscribedFrom,
            String installStatus, int releaseId, int tenantId ) throws ApplicationManagementDAOException;

    List<Integer> updateDeviceSubscription(String updateBy, List<Integer> deviceIds, boolean isUnsubscribed,
            String actionTriggeredFrom, String installStatus, int releaseId, int tenantId)
            throws ApplicationManagementDAOException;

    void addOperationMapping (int operationId, List<Integer> deviceSubscriptionId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between user and the application which the application is installed on. This mapping will be
     * added when an enterprise installation triggered to the user.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param users list of user names of the users whose devices are subscribed to the application
     * @param releaseId id of the {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementDAOException If unable to add a mapping between device and application
     */
    void addUserSubscriptions(int tenantId, String subscribedBy, List<String> users, int releaseId)
            throws ApplicationManagementDAOException;

    void addRoleSubscriptions(int tenantId, String subscribedBy, List<String> roles, int releaseId)
            throws ApplicationManagementDAOException;

    void addGroupSubscriptions(int tenantId, String subscribedBy, List<String> groups, int releaseId)
            throws ApplicationManagementDAOException;

    List<DeviceSubscriptionDTO> getDeviceSubscriptions(int appReleaseId, int tenantId) throws
            ApplicationManagementDAOException;

    Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> deviceIds, int tenantId) throws
            ApplicationManagementDAOException;

    List<String> getSubscribedUserNames(List<String> users, int tenantId) throws
            ApplicationManagementDAOException;

    List<String> getSubscribedRoleNames(List<String> roles, int tenantId) throws
            ApplicationManagementDAOException;

    List<String> getSubscribedGroupNames(List<String> groups, int tenantId) throws
            ApplicationManagementDAOException;

    void updateSubscriptions(int tenantId, String updateBy, List<String> paramList,
            int releaseId, String subType, String action) throws ApplicationManagementDAOException;

    List<Integer> getSubscribedDeviceIds(List<Integer> deviceIds, int applicationReleaseId, int tenantId)
            throws ApplicationManagementDAOException;

    List<Integer> getDeviceSubIdsForOperation (int operationId, int tenantId) throws ApplicationManagementDAOException;

    boolean updateDeviceSubStatus(int deviceId, List<Integer> deviceSubIds, String status, int tenantcId)
            throws ApplicationManagementDAOException;

    /**
     * this method is used to get the details of users
     * @param tenantId tenant id
     * @param offsetValue offsetValue
     * @param limitValue limitValue
     * @param appReleaseId appReleaseId
     * @return subscribedUsers
     * @throws ApplicationManagementDAOException throws {@link ApplicationManagementDAOException} if
     * connections establishment fails.
     */
    List<String> getAppSubscribedUsers(int offsetValue, int limitValue, int appReleaseId,
                                       int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * this method is used to get the details of roles
     * @param tenantId tenant id
     * @param offsetValue offsetValue
     * @param limitValue limitValue
     * @param appReleaseId appReleaseId
     * @return subscribedRoles
     * @throws ApplicationManagementDAOException throws {@link ApplicationManagementDAOException} if
     * connections establishment fails.
     */
    List<String> getAppSubscribedRoles(int offsetValue, int limitValue, int appReleaseId,
                                       int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * this method is used to get the details of groups
     * @param tenantId tenant id
     * @param offsetValue offsetValue
     * @param limitValue limitValue
     * @param appReleaseId appReleaseId
     * @return subscribedUsers
     * @throws ApplicationManagementDAOException throws {@link ApplicationManagementDAOException} if
     * connections establishment fails.
     */
    List<String> getAppSubscribedGroups(int offsetValue, int limitValue, int appReleaseId,
                                        int tenantId)
            throws ApplicationManagementDAOException;
}

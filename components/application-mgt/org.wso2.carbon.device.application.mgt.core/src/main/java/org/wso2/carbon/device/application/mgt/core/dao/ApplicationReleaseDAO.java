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

import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * This is responsible for Application Release related DAO operations.
 */
public interface ApplicationReleaseDAO {

    /**
     * To create an Application release.
     *
     * @param applicationRelease Application Release that need to be created.
     * @return Unique ID of the relevant release.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationRelease createRelease(ApplicationRelease applicationRelease, int appId, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To get a release details with the particular version.
     * @param applicationName name of the application to get the release.
     * @param versionName Name of the version
     * @param applicationType Type of the application release
     * @param releaseType type of the release
     * @param tenantId tenantId of the application

     * @return ApplicationRelease for the particular version of the given application
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationRelease getRelease(String applicationName,String applicationType, String versionName,
            String releaseType, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To get all the releases of a particular application.
     *
     * @param applicationId Id of the Application
     * @param tenantId tenant id of the application
     * @return list of the application releases
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    List<ApplicationRelease> getReleases(int applicationId, int tenantId) throws
            ApplicationManagementDAOException;

    /**
     * To update an Application release.
     *
     * @param applicationRelease ApplicationRelease that need to be updated.
     * @param applicationId      Id of the application.
     * @param tenantId           Id of the tenant
     * @return the updated Application Release
     * @throws ApplicationManagementDAOException Application Management DAO Exception
     */
    ApplicationRelease updateRelease(int applicationId, ApplicationRelease applicationRelease, int tenantId) throws
                                                                                                             ApplicationManagementDAOException;

    /**
     * To update an Application release.
     * @param uuid UUID of the ApplicationRelease that need to be updated.
     * @param rating given stars for the application.
     * @param ratedUsers number of users who has rated for the application release.
     * @throws ApplicationManagementDAOException Application Management DAO Exception
     */
    void updateRatingValue(String uuid, double rating, int ratedUsers) throws ApplicationManagementDAOException;

    /**
     * To retrieve rating of an application release.
     *
     * @param uuid UUID of the application Release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Rating getRating(String uuid, int tenantId) throws ApplicationManagementDAOException;


    /**
     * To delete a particular release.
     *
     * @param id      ID of the Application which the release need to be deleted.
     * @param version Version of the Application Release
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteRelease(int id, String version) throws ApplicationManagementDAOException;

    /**
     * To get release details of a specific application.
     *
     * @param applicationId ID of the application.
     * @param releaseUuid UUID of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationRelease getReleaseByIds(int applicationId, String releaseUuid, int tenantId) throws
                                                                                            ApplicationManagementDAOException;

}

/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistory;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.common.search.PropertyMap;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceCompliance;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationList;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationRequest;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementServiceImpl implements DeviceManagementService {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    private static final Log log = LogFactory.getLog(DeviceManagementServiceImpl.class);

    @GET
    @Path("/{type}/{id}/status")
    @Override
    public Response isEnrolled(@PathParam("type") String type, @PathParam("id") String id) {
        boolean result;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
        try {
            result = DeviceMgtAPIUtils.getDeviceManagementService().isEnrolled(deviceIdentifier);
            if (result) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking enrollment status of the device.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    public Response getDevices(
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("user") String user,
            @QueryParam("userPattern") String userPattern,
            @QueryParam("role") String role,
            @QueryParam("ownership") String ownership,
            @QueryParam("status") String status,
            @QueryParam("excludeStatus") String excludeStatus,
            @QueryParam("groupId") int groupId,
            @QueryParam("since") String since,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("requireDeviceInfo") boolean requireDeviceInfo,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(role)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Request contains both name and role " +
                                "parameters. Only one is allowed " +
                                "at once.").build()).build();
            }
//            RequestValidationUtil.validateSelectionCriteria(type, user, roleName, ownership, status);
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();
            if (deviceAccessAuthorizationService == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Device access authorization service is " +
                                "failed").build()).build();
            }
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;
            DeviceList devices = new DeviceList();

            if (name != null && !name.isEmpty()) {
                request.setDeviceName(name);
            }
            if (type != null && !type.isEmpty()) {
                request.setDeviceType(type);
            }
            if (ownership != null && !ownership.isEmpty()) {
                RequestValidationUtil.validateOwnershipType(ownership);
                request.setOwnership(ownership);
            }
            if (status != null && !status.isEmpty()) {
                RequestValidationUtil.validateStatus(status);
                request.setStatus(status);
            }
            if (excludeStatus != null && !excludeStatus.isEmpty()) {
                RequestValidationUtil.validateStatus(excludeStatus);
                request.setExcludeStatus(excludeStatus);
            }
            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

            if (groupId != 0) {
                try {
                    int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                    UserStoreManager userStoreManager = DeviceMgtAPIUtils.getRealmService()
                            .getTenantUserRealm(tenantId).getUserStoreManager();
                    String[] userRoles = userStoreManager.getRoleListOfUser(authorizedUser);
                    boolean isPermitted = false;
                    if (deviceAccessAuthorizationService.isDeviceAdminUser()) {
                        isPermitted = true;
                    } else {
                        List<String> roles = DeviceMgtAPIUtils.getGroupManagementProviderService().getRoles(groupId);
                        for (String userRole : userRoles) {
                            if (roles.contains(userRole)) {
                                isPermitted = true;
                                break;
                            }
                        }
                        if (!isPermitted) {
                            DeviceGroup deviceGroup = DeviceMgtAPIUtils.getGroupManagementProviderService()
                                    .getGroup(groupId, false);
                            if (deviceGroup != null && authorizedUser.equals(deviceGroup.getOwner())) {
                                isPermitted = true;
                            }
                        }
                    }
                    if (isPermitted) {
                        request.setGroupId(groupId);
                    } else {
                        return Response.status(Response.Status.FORBIDDEN).entity(
                                new ErrorResponse.ErrorResponseBuilder().setMessage("Current user '" + authorizedUser
                                        + "' doesn't have enough privileges to list devices of group '"
                                        + groupId + "'").build()).build();
                    }
                } catch (GroupManagementException | UserStoreException e) {
                    throw new DeviceManagementException(e);
                }
            }
            if (role != null && !role.isEmpty()) {
                request.setOwnerRole(role);
            }
            authorizedUser = MultitenantUtils.getTenantAwareUsername(authorizedUser);
            // check whether the user is device-mgt admin
            if (deviceAccessAuthorizationService.isDeviceAdminUser() || request.getGroupId() > 0) {
                if (user != null && !user.isEmpty()) {
                    request.setOwner(MultitenantUtils.getTenantAwareUsername(user));
                } else if (userPattern != null && !userPattern.isEmpty()) {
                    request.setOwnerPattern(userPattern);
                }
            } else {
                if (user != null && !user.isEmpty()) {
                    user = MultitenantUtils.getTenantAwareUsername(user);
                    if (user.equals(authorizedUser)) {
                        request.setOwner(user);
                    } else {
                        String msg = "User '" + authorizedUser + "' is not authorized to retrieve devices of '" + user
                                + "' user";
                        log.error(msg);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(
                                new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage(msg).build()).build();
                    }
                } else {
                    request.setOwner(authorizedUser);
                }
            }

            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
                }
                request.setSince(sinceDate);
                if (requireDeviceInfo) {
                    result = dms.getAllDevices(request);
                } else {
                    result = dms.getAllDevices(request, false);
                }

                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else if (since != null && !since.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(since);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'since' filter").build()).build();
                }
                request.setSince(sinceDate);
                if (requireDeviceInfo) {
                    result = dms.getAllDevices(request);
                } else {
                    result = dms.getAllDevices(request, false);
                }
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    devices.setList(new ArrayList<Device>());
                    devices.setCount(0);
                    return Response.status(Response.Status.OK).entity(devices).build();
                }
            } else {
                if (requireDeviceInfo) {
                    result = dms.getAllDevices(request);
                } else {
                    result = dms.getAllDevices(request, false);
                }
                int resultCount = result.getRecordsTotal();
                if (resultCount == 0) {
                    Response.status(Response.Status.OK).entity(devices).build();
                }
            }

            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking device access authorization";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    @Path("/user-devices")
    public Response getDeviceByUser(@QueryParam("requireDeviceInfo") boolean requireDeviceInfo,
                                    @QueryParam("offset") int offset,
                                    @QueryParam("limit") int limit) {

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        PaginationResult result;
        DeviceList devices = new DeviceList();

        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        request.setOwner(currentUser);

        try {
            if (requireDeviceInfo) {
                result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesOfUser(request);
            } else {
                result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesOfUser(request, false);
            }
            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @DELETE
    @Consumes(MediaType.WILDCARD)
    @Override
    @Path("/type/{device-type}/id/{device-id}")
    public Response deleteDevice(@PathParam("device-type") String deviceType,
                                 @PathParam("device-id") String deviceId) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, true);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            boolean response = deviceManagementProviderService.disenrollDevice(deviceIdentifier);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while deleting device of type : " + deviceType + " and " +
                    "ID : " + deviceId;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()
            ).build();
        }
    }

    @POST
    @Override
    @Path("/type/{device-type}/id/{device-id}/rename")
    public Response renameDevice(Device device, @PathParam("device-type") String deviceType,
                                 @PathParam("device-id") String deviceId) {
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            Device persistedDevice = deviceManagementProviderService.getDevice(new DeviceIdentifier
                    (deviceId, deviceType), true);
            persistedDevice.setName(device.getName());
            boolean response = deviceManagementProviderService.modifyEnrollment(persistedDevice);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (DeviceManagementException e) {
            log.error("Error encountered while updating device of type : " + deviceType + " and " +
                    "ID : " + deviceId);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage("Error while updating " +
                            "device of type " + deviceType + " and ID : " + deviceId).build()).build();
        }
    }

    @GET
    @Path("/{type}/{id}")
    @Override
    public Response getDevice(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @QueryParam("owner") @Size(max = 100) String owner,
            @QueryParam("ownership") @Size(max = 100) String ownership,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Device device;
        Date sinceDate = null;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            // check whether the user is authorized
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser)) {
                String msg = "User '" + authorizedUser + "' is not authorized to retrieve the given device id '" + id + "'";
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_UNAUTHORIZED).setMessage(msg).build()).build();
            }

            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceIdentifier(deviceIdentifier);

            if (!StringUtils.isBlank(ifModifiedSince)){
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                    deviceData.setLastModifiedDate(sinceDate);
                } catch (ParseException e) {
                    String msg = "Invalid date string is provided in 'If-Modified-Since' header";
                    log.error(msg, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            }

            if (!StringUtils.isBlank(owner)){
                deviceData.setDeviceOwner(owner);
            }
            if (!StringUtils.isBlank(ownership)){
                deviceData.setDeviceOwnership(ownership);
            }
            device = dms.getDevice(deviceData, true);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking the device authorization.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (device == null) {
            if (sinceDate != null) {
                return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                        "after the timestamp provided in 'If-Modified-Since' header").build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_NOT_FOUND).setMessage("Requested device of type '" +
                            type + "', which carries id '" + id + "' does not exist").build()).build();
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @Path("/{deviceType}/{deviceId}/location-history")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getDeviceLocationInfo(@PathParam("deviceType") String deviceType,
                                          @PathParam("deviceId") String deviceId,
                                          @QueryParam("from") long from, @QueryParam("to") long to) {

        List<DeviceLocationHistory> deviceLocationHistory;
        String errorMessage;

        try {
            RequestValidationUtil.validateDeviceIdentifier(deviceType, deviceId);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(deviceType);

            if (deviceAccessAuthorizationService == null) {
                errorMessage = "Device access authorization service is failed";
                log.error(errorMessage);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(errorMessage).build()).build();
            }
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser)) {
                String msg = "User '" + authorizedUser + "' is not authorized to retrieve the given device id '" +
                        deviceId + "'";
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage(msg).build()).build();
            }
            if (from == 0 || to == 0) {
                errorMessage = "Invalid values for from/to";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage)).build();
            }

            deviceLocationHistory = dms.getDeviceLocationInfo(deviceIdentifier, from, to);

        } catch (DeviceManagementException e) {
            errorMessage = "Error occurred while fetching the device information.";
            log.error(errorMessage, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(errorMessage).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            errorMessage = "Error occurred while checking the device authorization.";
            log.error(errorMessage, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(errorMessage).build()).build();
        } catch (InputValidationException e){
            errorMessage = "Invalid device Id or device type";
            log.error(errorMessage, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage)).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocationHistory).build();
    }

    @GET
    @Path("/type/any/id/{id}")
    @Override
    public Response getDeviceByID(
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("requireDeviceInfo") boolean requireDeviceInfo) {
        Device device;
        try {
            RequestValidationUtil.validateDeviceIdentifier("any", id);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

            Date sinceDate = null;
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    String message = "Error occurred while parse the since date.Invalid date string is provided in " +
                                 "'If-Modified-Since' header";
                    log.error(message, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
                }
            }
            if (sinceDate != null) {
                device = dms.getDevice(id, sinceDate, requireDeviceInfo);
                if (device == null) {
                    String message = "No device is modified after the timestamp provided in 'If-Modified-Since' header";
                    log.error(message);
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                     "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else {
                device = dms.getDevice(id, requireDeviceInfo);
            }
            if (device == null) {
                String message = "Device does not exist with id '" + id + "'";
                log.error(message);
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage(message).build()).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, device.getType());
            // check whether the user is authorized
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser)) {
                String message = "User '" + authorizedUser + "' is not authorized to retrieve the given " +
                                 "device id '" + id + "'";
                log.error(message);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage(message).build()).build();
            }
        } catch (DeviceManagementException e) {
            String message = "Error occurred while fetching the device information.";
            log.error(message, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(message).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String message = "Error occurred while checking the device authorization.";
            log.error(message, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(message).build()).build();
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @GET
    @Path("/{type}/{id}/location")
    @Override
    public Response getDeviceLocation(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        DeviceInformationManager informationManager;
        DeviceLocation deviceLocation;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(id);
            deviceIdentifier.setType(type);
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceLocation = informationManager.getDeviceLocation(deviceIdentifier);

        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device location.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocation).build();

    }


    @GET
    @Path("/{type}/{id}/info")
    @Override
    public Response getDeviceInformation(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        DeviceInformationManager informationManager;
        DeviceInfo deviceInfo;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(id);
            deviceIdentifier.setType(type);
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceInfo = informationManager.getDeviceInfo(deviceIdentifier);

        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device information of id : " + id + " type : " + type;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceInfo).build();

    }

    @GET
    @Path("/{type}/{id}/features")
    @Override
    public Response getFeaturesOfDevice(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<Feature> features = new ArrayList<>();
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            FeatureManager fm;
            try {
                fm = dms.getFeatureManager(type);
            } catch (DeviceTypeNotFoundException e) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder()
                                .setMessage("No device type found with name '" + type + "'").build()).build();
            }
            if (fm != null) {
                features = fm.getFeatures();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of features of '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(features).build();
    }

    @POST
    @Path("/search-devices")
    @Override
    public Response searchDevices(@QueryParam("offset") int offset,
                                  @QueryParam("limit") int limit, SearchContext searchContext) {
        SearchManagerService searchManagerService;
        List<Device> devices;
        DeviceList deviceList = new DeviceList();
        try {
            searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
            devices = searchManagerService.search(searchContext);
        } catch (SearchMgtException e) {
            String msg = "Error occurred while searching for devices that matches the provided selection criteria";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        deviceList.setList(devices);
        deviceList.setCount(devices.size());
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @POST
    @Path("/query-devices")
    @Override
    public Response queryDevicesByProperties(@QueryParam("offset") int offset,
                                             @QueryParam("limit") int limit, PropertyMap map) {
        List<Device> devices;
        DeviceList deviceList = new DeviceList();
        try {
            if(map.getProperties().isEmpty()){
                if (log.isDebugEnabled()) {
                    log.debug("No search criteria defined when querying devices.");
                }
                return Response.status(Response.Status.BAD_REQUEST).entity("No search criteria defined.").build();
            }
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            devices = dms.getDevicesBasedOnProperties(map.getProperties());
            if(devices == null || devices.isEmpty()){
                if (log.isDebugEnabled()) {
                    log.debug("No Devices Found for criteria : " + map);
                }
                return Response.status(Response.Status.NOT_FOUND).entity("No device found matching query criteria.").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while searching for devices that matches the provided device properties";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        deviceList.setList(devices);
        deviceList.setCount(devices.size());
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @GET
    @Path("/{type}/{id}/applications")
    @Override
    public Response getInstalledApplications(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        List<Application> applications;
        ApplicationManagementProviderService amc;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            amc = DeviceMgtAPIUtils.getAppManagementService();
            applications = amc.getApplicationListForDevice(new DeviceIdentifier(id, type));
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{type}/{id}/operations")
    @Override
    public Response getDeviceOperations(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("owner") String owner,
            @QueryParam("ownership") String ownership) {
        OperationList operationsList = new OperationList();
        RequestValidationUtil.validateOwnerParameter(owner);
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        request.setOwner(owner);
        PaginationResult result;
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            if (!StringUtils.isBlank(ownership)) {
                request.setOwnership(ownership);
            }
            result = dms.getOperations(new DeviceIdentifier(id, type), request);
            operationsList.setList((List<? extends Operation>) result.getData());
            operationsList.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(operationsList).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{type}/{id}/effective-policy")
    @Override
    public Response getEffectivePolicyOfDevice(@PathParam("type") @Size(max = 45) String type,
                                               @PathParam("id") @Size(max = 45) String id,
                                               @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            Policy policy = policyManagementService.getAppliedPolicyToDevice(new DeviceIdentifier(id, type));

            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("{type}/{id}/compliance-data")
    public Response getComplianceDataOfDevice(@PathParam("type") @Size(max = 45) String type,
                                              @PathParam("id") @Size(max = 45) String id) {

        RequestValidationUtil.validateDeviceIdentifier(type, id);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        Policy policy;
        NonComplianceData complianceData;
        DeviceCompliance deviceCompliance = new DeviceCompliance();

        try {
            policy = policyManagementService.getAppliedPolicyToDevice(new DeviceIdentifier(id, type));
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }

        if (policy == null) {
            deviceCompliance.setDeviceID(id);
            deviceCompliance.setComplianceData(null);
            return Response.status(Response.Status.OK).entity(deviceCompliance).build();
        } else {
            try {
                policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
                complianceData = policyManagementService.getDeviceCompliance(
                        new DeviceIdentifier(id, type));
                deviceCompliance.setDeviceID(id);
                deviceCompliance.setComplianceData(complianceData);
                return Response.status(Response.Status.OK).entity(deviceCompliance).build();
            } catch (PolicyComplianceException e) {
                String error = "Error occurred while getting the compliance data.";
                log.error(error, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(error).build()).build();
            }
        }
    }

    /**
     * Change device status.
     *
     * @param type       Device type
     * @param id         Device id
     * @param newsStatus Device new status
     * @return {@link Response} object
     */
    @PUT
    @Path("/{type}/{id}/changestatus")
    public Response changeDeviceStatus(@PathParam("type") @Size(max = 45) String type,
                                       @PathParam("id") @Size(max = 45) String id,
                                       @QueryParam("newStatus") EnrolmentInfo.Status newsStatus) {
        RequestValidationUtil.validateDeviceIdentifier(type, id);
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, false);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            boolean response = deviceManagementProviderService.changeDeviceStatus(deviceIdentifier, newsStatus);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while changing device status of type : " + type + " and " +
                    "device id : " + id;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/{type}/operations")
    public Response addOperation(@PathParam("type") String type, @Valid OperationRequest operationRequest) {
        try {
            if (operationRequest == null || operationRequest.getDeviceIdentifiers() == null
                    || operationRequest.getOperation() == null) {
                String errorMessage = "Operation cannot be empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device Type is invalid";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            Operation.Type operationType = operationRequest.getOperation().getType();
            if (operationType == Operation.Type.COMMAND || operationType == Operation.Type.CONFIG || operationType == Operation.Type.PROFILE) {
                DeviceIdentifier deviceIdentifier;
                List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                for (String deviceId : operationRequest.getDeviceIdentifiers()) {
                    deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(deviceId);
                    deviceIdentifier.setType(type);
                    deviceIdentifiers.add(deviceIdentifier);
                }
                Operation operation;
                if (operationType == Operation.Type.COMMAND) {
                    Operation commandOperation = operationRequest.getOperation();
                    operation = new CommandOperation();
                    operation.setType(Operation.Type.COMMAND);
                    operation.setCode(commandOperation.getCode());
                    operation.setEnabled(commandOperation.isEnabled());
                    operation.setStatus(commandOperation.getStatus());

                } else if (operationType == Operation.Type.CONFIG) {
                    Operation configOperation = operationRequest.getOperation();
                    operation = new ConfigOperation();
                    operation.setType(Operation.Type.CONFIG);
                    operation.setCode(configOperation.getCode());
                    operation.setEnabled(configOperation.isEnabled());
                    operation.setPayLoad(configOperation.getPayLoad());
                    operation.setStatus(configOperation.getStatus());

                } else {
                    Operation profileOperation = operationRequest.getOperation();
                    operation = new ProfileOperation();
                    operation.setType(Operation.Type.PROFILE);
                    operation.setCode(profileOperation.getCode());
                    operation.setEnabled(profileOperation.isEnabled());
                    operation.setPayLoad(profileOperation.getPayLoad());
                    operation.setStatus(profileOperation.getStatus());
                }
                String date = new SimpleDateFormat(DATE_FORMAT_NOW).format(new Date());
                operation.setCreatedTimeStamp(date);
                Activity activity = DeviceMgtAPIUtils.getDeviceManagementService().addOperation(type, operation,
                        deviceIdentifiers);
                return Response.status(Response.Status.CREATED).entity(activity).build();
            } else {
                String message = "Only Command and Config operation is supported through this api";
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(message).build();
            }

        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Override
    @Path("/type/{type}/status/{status}/count")
    public Response getDeviceCountByStatus(@PathParam("type") String type, @PathParam("status") String status) {
        int deviceCount;
        try {
            deviceCount = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCountOfTypeByStatus(type, status);
            return Response.status(Response.Status.OK).entity(deviceCount).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while retrieving device count.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @GET
    @Override
    @Path("/type/{type}/status/{status}/ids")
    public Response getDeviceIdentifiersByStatus(@PathParam("type") String type, @PathParam("status") String status) {
        List<String> deviceIds;
        try {
            deviceIds = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceIdentifiersByStatus(type, status);
            return Response.status(Response.Status.OK).entity(deviceIds.toArray(new String[0])).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while obtaining list of devices";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @PUT
    @Override
    @Path("/type/{type}/status/{status}")
    public Response bulkUpdateDeviceStatus(@PathParam("type") String type, @PathParam("status") String status,
                                           @Valid List<String> deviceList) {
        try {
            DeviceMgtAPIUtils.getDeviceManagementService().bulkUpdateDeviceStatus(type, deviceList, status);
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while updating device status in bulk.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
        return Response.status(Response.Status.OK).build();
    }
}

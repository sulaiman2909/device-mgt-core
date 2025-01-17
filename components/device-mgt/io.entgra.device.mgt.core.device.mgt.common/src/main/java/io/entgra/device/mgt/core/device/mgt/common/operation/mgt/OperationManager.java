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
package io.entgra.device.mgt.core.device.mgt.common.operation.mgt;

import io.entgra.device.mgt.core.device.mgt.common.*;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.InvalidDeviceException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;

import java.util.List;

/**
 * This represents the Device Operation management functionality which should be implemented by
 * the device type plugins.
 */
public interface OperationManager {

    /**
     * Method to add a operation to a device or a set of devices.
     *
     * @param operation Operation to be added
     * @param devices   List of DeviceIdentifiers to execute the operation
     * @return Activity object corresponds to the added operation.
     * @throws OperationManagementException If some unusual behaviour is observed while adding the operation
     * @throws InvalidDeviceException       If addOperation request contains Invalid DeviceIdentifiers.
     */
    Activity addOperation(Operation operation, List<DeviceIdentifier> devices) throws OperationManagementException,
            InvalidDeviceException;

    void addTaskOperation(List<Device> devices, Operation operation) throws OperationManagementException;

    void addTaskOperation(String deviceType, Operation operation, DynamicTaskContext dynamicTaskContext) throws OperationManagementException;

    /**
     * Method to retrieve the list of all operations to a device.
     *
     * @param deviceId - Device Identifier of the device
     * @return A List of operations applied to the given device-id.
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException;

    /**
     * Method to retrieve all the operations applied to a device with pagination support.
     *
     * @param deviceId DeviceIdentifier of the device
     * @param request  PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException;
    /**
     * Method to retrieve the list of operations placed for device with specified status.
     *
     * @param deviceId  - Device Identifier of the device
     * @param status    - Status of the operation
     * @return A List of operations applied to the given device-id.
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    List<? extends Operation> getOperations(DeviceIdentifier deviceId, Operation.Status status)
            throws OperationManagementException;

    /**
     * Method to retrieve the list of available operations to a device.
     *
     * @param deviceId DeviceIdentifier of the device
     * @return A List of pending operations.
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId) throws OperationManagementException;

    List<? extends Operation> getPendingOperations(Device device) throws OperationManagementException;

    Operation getNextPendingOperation(DeviceIdentifier deviceId, long notNowOperationFrequency)
            throws OperationManagementException;

    Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException;

    void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException;

    void updateOperation(int enrolmentId, Operation operation, DeviceIdentifier deviceId) throws OperationManagementException;

    Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException;

    List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
                                                             Operation.Status status)
            throws OperationManagementException, DeviceManagementException;

    Operation getOperation(int operationId) throws OperationManagementException;

    Activity getOperationByActivityId(String activity) throws OperationManagementException;

    List<Activity> getOperationByActivityIds(List<String> idList) throws OperationManagementException;

    Activity getOperationByActivityIdAndDevice(String activity, DeviceIdentifier deviceId)
            throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException;

    List<Activity> getActivities(ActivityPaginationRequest activityPaginationRequest) throws OperationManagementException;

    int getActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException;

    List<DeviceActivity> getDeviceActivities(ActivityPaginationRequest activityPaginationRequest) throws OperationManagementException;

    int getDeviceActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException;

    List<Activity> getFilteredActivities(String operationCode, int limit, int offset) throws OperationManagementException;

    int getTotalCountOfFilteredActivities(String operationCode) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset) throws OperationManagementException;

    int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException;

    int getActivityCountUpdatedAfterByUser(long timestamp, String user) throws OperationManagementException;

    /**
     * retrive the push notification strategy.
     * @return NotificationStrategy
     */
    NotificationStrategy getNotificationStrategy();

    /**
     * Check if an operation exists for a given device identifier and operation id
     *
     * @param deviceId Device identifier of the device
     * @param operationId Id of the operation
     * @return true if operation already exists, else false
     * @throws {@link OperationManagementException}
     */
    boolean isOperationExist(DeviceIdentifier deviceId, int operationId) throws OperationManagementException;

    List<Activity> getActivities(List<String> deviceTypes, String operationCode, long updatedSince, String operationStatus)
            throws OperationManagementException;
}

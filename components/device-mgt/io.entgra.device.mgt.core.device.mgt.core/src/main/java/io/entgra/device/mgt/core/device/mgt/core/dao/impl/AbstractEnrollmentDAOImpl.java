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
package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.EnrollmentDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractEnrollmentDAOImpl implements EnrollmentDAO {

    @Override
    public EnrolmentInfo addEnrollment(int deviceId, EnrolmentInfo enrolmentInfo,
                                       int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT(DEVICE_ID, OWNER, OWNERSHIP, STATUS, " +
                    "DATE_OF_ENROLMENT, DATE_OF_LAST_UPDATE, TENANT_ID) VALUES(?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            Timestamp enrollmentTime = new Timestamp(new Date().getTime());
            stmt.setInt(1, deviceId);
            stmt.setString(2, enrolmentInfo.getOwner());
            stmt.setString(3, enrolmentInfo.getOwnership().toString());
            stmt.setString(4, enrolmentInfo.getStatus().toString());
            stmt.setTimestamp(5, enrollmentTime);
            stmt.setTimestamp(6, enrollmentTime);
            stmt.setInt(7, tenantId);
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int enrolmentId = rs.getInt(1);
                enrolmentInfo.setId(enrolmentId);
                enrolmentInfo.setDateOfEnrolment(enrollmentTime.getTime());
                enrolmentInfo.setDateOfLastUpdate(enrollmentTime.getTime());
                addDeviceStatus(enrolmentId, enrolmentInfo.getStatus());
                return enrolmentInfo;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DeviceManagementDAOException("Error occurred while adding enrolment configuration", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int updateEnrollment(EnrolmentInfo enrolmentInfo, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET OWNERSHIP = ?, STATUS = ?, DATE_OF_LAST_UPDATE = ? " +
                    "WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, enrolmentInfo.getOwnership().toString());
            stmt.setString(2, enrolmentInfo.getStatus().toString());
            stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
            stmt.setInt(4, enrolmentInfo.getId());
            stmt.setInt(5, tenantId);
            int updatedCount = stmt.executeUpdate();
            if (updatedCount == 1){
                addDeviceStatus(enrolmentInfo.getId(), enrolmentInfo.getStatus());
            }
            return updatedCount;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating enrolment configuration", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean updateEnrollmentStatus(List<EnrolmentInfo> enrolmentInfos) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean status = false;
        int updateStatus = -1;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET STATUS = ? WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            if (conn.getMetaData().supportsBatchUpdates()) {
                for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                    stmt.setString(1, enrolmentInfo.getStatus().toString());
                    stmt.setInt(2, enrolmentInfo.getId());
                    stmt.addBatch();
                }
                updateStatus = stmt.executeBatch().length;
            } else {
                for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                    stmt.setString(1, enrolmentInfo.getStatus().toString());
                    stmt.setInt(2, enrolmentInfo.getId());
                    updateStatus = stmt.executeUpdate();
                }
            }
            if (updateStatus > 0) {
                status = true;
                for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                    addDeviceStatus(enrolmentInfo);
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating enrolment status of given device-list.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return status;
    }

    @Override
    public int removeEnrollment(int deviceId, String currentOwner,
                                int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int status = -1;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setInt(1, deviceId);
            stmt.setString(2, currentOwner);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                status = 1;
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private int getCountOfDevicesOfOwner(String owner, int tenantID) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = this.getConnection();
            String checkQuery = "SELECT COUNT(ID) AS COUNT FROM DM_ENROLMENT WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setString(1, owner);
            stmt.setInt(2, tenantID);
            rs = stmt.executeQuery();
            if(rs.next()){
                count = rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while trying to get device " +
                    "count of Owner : "+owner, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return count;
    }

    @Override
    public boolean setStatus(String currentOwner, EnrolmentInfo.Status status,
                             int tenantId) throws DeviceManagementDAOException {
        return setStatusAllDevices(currentOwner, status, tenantId);
    }

    @Override
    public boolean setStatusAllDevices(String currentOwner, EnrolmentInfo.Status status, int tenantId)
            throws DeviceManagementDAOException{
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp updateTime = new Timestamp(new Date().getTime());
        if(getCountOfDevicesOfOwner(currentOwner, tenantId) > 0){
            try {
                conn = this.getConnection();
                String sql = "UPDATE DM_ENROLMENT SET STATUS = ?, DATE_OF_LAST_UPDATE = ? WHERE OWNER = ? AND TENANT_ID = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, status.toString());
                stmt.setTimestamp(2, updateTime);
                stmt.setString(3, currentOwner);
                stmt.setInt(4, tenantId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
            } finally {
                DeviceManagementDAOUtil.cleanupResources(stmt, null);
            }
            return addDeviceStatus(currentOwner, status, tenantId);
        } else {
            return false;
        }
    }

    @Override
    public boolean setStatus(int enrolmentID, EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp updateTime = new Timestamp(new Date().getTime());
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET STATUS = ?, DATE_OF_LAST_UPDATE = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setTimestamp(2, updateTime);
            stmt.setInt(3, enrolmentID);
            stmt.setInt(4, tenantId);
            int updatedRowCount = stmt.executeUpdate();
            if (updatedRowCount != 1){
                throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment: "+
                        updatedRowCount + " rows were updated instead of one row!!!");
            }
            // save the device status history
            addDeviceStatus(enrolmentID, status);
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    private boolean addDeviceStatus(EnrolmentInfo config) throws DeviceManagementDAOException {
        return addDeviceStatus(config.getId(), config.getStatus());
    }

    private boolean addDeviceStatus(String currentOwner, EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        String changedBy = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (changedBy == null){
            changedBy = DeviceManagementConstants.MaintenanceProperties.MAINTENANCE_USER;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<int[]> enrolmentInfoList = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentOwner);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int enrolmentId = rs.getInt("ID");
                int deviceId = rs.getInt("DEVICE_ID");
                enrolmentInfoList.add(new int[]{enrolmentId, deviceId});
            }
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            Timestamp updateTime = new Timestamp(new Date().getTime());
            sql = "INSERT INTO DM_DEVICE_STATUS (ENROLMENT_ID, DEVICE_ID, STATUS, UPDATE_TIME, CHANGED_BY) VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (conn.getMetaData().supportsBatchUpdates()) {
                    for(int[] info: enrolmentInfoList){
                        ps.setInt(1, info[0]);
                        ps.setInt(2, info[1]);
                        ps.setString(3, status.toString());
                        ps.setTimestamp(4, updateTime);
                        ps.setString(5, changedBy);
                        ps.addBatch();
                    }
                    int[] batchResult = ps.executeBatch();
                    for (int i : batchResult) {
                        if (i == 0 || i == Statement.SUCCESS_NO_INFO || i == Statement.EXECUTE_FAILED) {
                            return false;
                        }
                    }
                } else {
                    for(int[] info: enrolmentInfoList){
                        ps.setInt(1, info[0]);
                        ps.setInt(2, info[1]);
                        ps.setString(3, status.toString());
                        ps.setTimestamp(4, updateTime);
                        ps.setString(5, changedBy);
                        ps.execute();
                    }

                }
            }

        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolments " +
                    "information of owner '" + currentOwner + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return true;
    }

    private boolean addDeviceStatus(int enrolmentId, EnrolmentInfo.Status status) throws DeviceManagementDAOException {
        Connection conn;
        String changedBy = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (changedBy == null){
            changedBy = DeviceManagementConstants.MaintenanceProperties.MAINTENANCE_USER;
        }
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            // get the device id and last udpated status from the device status table
            String sql = "SELECT DEVICE_ID, STATUS FROM DM_DEVICE_STATUS WHERE ENROLMENT_ID = ? ORDER BY UPDATE_TIME DESC LIMIT 1";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            ResultSet rs = stmt.executeQuery();
            int deviceId = -1;
            EnrolmentInfo.Status previousStatus = null;
            if (rs.next()) {
                // if there is a record corresponding to the enrolment we save the status and the device id
                previousStatus = EnrolmentInfo.Status.valueOf(rs.getString("STATUS"));
                deviceId = rs.getInt("DEVICE_ID");
            }
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            // if there was no record for the enrolment or the previous status is not the same as the current status
            // we'll add a record
            if (previousStatus == null || previousStatus != status){
                if (deviceId == -1) {
                    // we need the device id in order to add a new record, therefore we get it from the enrolment table
                    sql = "SELECT DEVICE_ID FROM DM_ENROLMENT WHERE ID = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, enrolmentId);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        deviceId = rs.getInt("DEVICE_ID");
                    } else {
                        // if there were no records corresponding to the enrolment id this is a problem. i.e. enrolment
                        // id is invalid
                        throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment: no record for enrolment id " + enrolmentId);
                    }
                    DeviceManagementDAOUtil.cleanupResources(stmt, null);
                }

                sql = "INSERT INTO DM_DEVICE_STATUS (ENROLMENT_ID, DEVICE_ID, STATUS, UPDATE_TIME, CHANGED_BY) VALUES(?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                Timestamp updateTime = new Timestamp(new Date().getTime());
                stmt.setInt(1, enrolmentId);
                stmt.setInt(2, deviceId);
                stmt.setString(3, status.toString());
                stmt.setTimestamp(4, updateTime);
                stmt.setString(5, changedBy);
                stmt.execute();
            } else {
                // no need to update status since the last recorded status is the same as the current status
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }
    @Override
    public EnrolmentInfo.Status getStatus(int deviceId, String currentOwner,
                                          int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo.Status status = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT STATUS FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, currentOwner);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                status = EnrolmentInfo.Status.valueOf(rs.getString("STATUS"));
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public EnrolmentInfo getEnrollment(int deviceId, String currentOwner,
                                       int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, currentOwner);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                enrolmentInfo = this.loadEnrolment(rs);
            }
            return enrolmentInfo;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolment " +
                    "information of user '" + currentOwner + "' upon device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public EnrolmentInfo getEnrollment(int deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND TENANT_ID = ? " +
                    "ORDER BY DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                enrolmentInfo = this.loadEnrolment(rs);
            }
            return enrolmentInfo;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolment " +
                    "information of device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<EnrolmentInfo> getEnrollmentsOfUser(int deviceId, String user, int tenantId)
            throws DeviceManagementDAOException {
        List<EnrolmentInfo> enrolmentInfos = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, user);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                enrolmentInfo = this.loadEnrolment(rs);
                enrolmentInfos.add(enrolmentInfo);
            }
            return enrolmentInfos;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolments " +
                    "information of user '" + user + "' upon device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean updateOwnerOfEnrollment(List<Device> devices, String owner, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            boolean updateStatus = true;
            String sql = "UPDATE DM_ENROLMENT "
                    + "SET OWNER = ?, IS_TRANSFERRED = ?, DATE_OF_LAST_UPDATE = ? "
                    + "WHERE ID = ? AND TENANT_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (conn.getMetaData().supportsBatchUpdates()) {
                    for (Device device : devices) {
                        ps.setString(1, owner);
                        ps.setBoolean(2, device.getEnrolmentInfo().isTransferred());
                        ps.setTimestamp(3, new Timestamp(new Date().getTime()));
                        ps.setInt(4, device.getEnrolmentInfo().getId());
                        ps.setInt(5, tenantId);
                        ps.addBatch();
                    }
                    int[] batchResult = ps.executeBatch();
                    for (int i : batchResult) {
                        if (i == 0 || i == Statement.SUCCESS_NO_INFO || i == Statement.EXECUTE_FAILED) {
                            updateStatus = false;
                            break;
                        }
                    }
                } else {
                    for (Device device : devices) {
                        ps.setString(1, owner);
                        ps.setBoolean(2, device.getEnrolmentInfo().isTransferred());
                        ps.setInt(3, device.getId());
                        ps.setInt(4, tenantId);
                        if (ps.executeUpdate() == 0) {
                            updateStatus = false;
                            break;
                        }
                    }
                }
            }
            return updateStatus;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while obtaining the DB connection to update the "
                    + "owner of the device enrollment.", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    private EnrolmentInfo loadEnrolment(ResultSet rs) throws SQLException {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner(rs.getString("OWNER"));
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
        enrolmentInfo.setTransferred(rs.getBoolean("IS_TRANSFERRED"));
        enrolmentInfo.setDateOfEnrolment(rs.getTimestamp("DATE_OF_ENROLMENT").getTime());
        enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
        enrolmentInfo.setId(rs.getInt("ID"));
        return enrolmentInfo;
    }

}
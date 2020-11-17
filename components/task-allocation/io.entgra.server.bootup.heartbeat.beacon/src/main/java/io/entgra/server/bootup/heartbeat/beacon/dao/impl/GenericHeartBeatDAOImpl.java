/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.server.bootup.heartbeat.beacon.dao.impl;

import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatBeaconDAOFactory;
import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatDAO;
import io.entgra.server.bootup.heartbeat.beacon.dao.exception.HeartBeatDAOException;
import io.entgra.server.bootup.heartbeat.beacon.dao.util.HeartBeatBeaconDAOUtil;
import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents implementation of HeartBeatDAO
 */
public class GenericHeartBeatDAOImpl implements HeartBeatDAO {

    @Override
    public String recordServerCtx(ServerContext ctx) throws HeartBeatDAOException {
        PreparedStatement stmt = null;
        String uuid = null;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();

            String sql;
            sql = "INSERT INTO SERVER_HEART_BEAT_EVENTS(HOST_NAME, MAC, UUID, SERVER_PORT) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"UUID"});
            stmt.setString(1, ctx.getHostName());
            stmt.setString(2, ctx.getMacAddress());
            stmt.setString(3, UUID.randomUUID().toString());
            stmt.setInt(4, ctx.getCarbonServerPort());

            stmt.executeUpdate();
            ResultSet result = stmt.getGeneratedKeys();
            if (result.next()){
                uuid = result.getString("UUID");
            }
        } catch (SQLException e) {
            throw new HeartBeatDAOException("Error occurred while persisting server context for : '" +
                                            "mac '" + ctx.getMacAddress() + "' " +
                                            "hostname : '" + ctx.getHostName() + "' ", e);
        } finally {
            HeartBeatBeaconDAOUtil.cleanupResources(stmt, null);
        }
        return uuid;
    }

    @Override
    public boolean recordHeatBeat(HeartBeatEvent event) throws HeartBeatDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql;
            sql = "UPDATE SERVER_HEART_BEAT_EVENTS SET LAST_UPDATED_TIMESTAMP = ? WHERE UUID = ?";
            stmt = conn.prepareStatement(sql, new String[]{"ID"});
            stmt.setTimestamp(1, event.getTime());
            stmt.setString(2, event.getServerUUID());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new HeartBeatDAOException("Error occurred while updating heartbeat event against server with UUID : '" +
                                            event.getServerUUID() + "'  and timestamp " + event.getTime(), e);
        } finally {
            HeartBeatBeaconDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public String retrieveExistingServerCtx(ServerContext ctx) throws HeartBeatDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String uuid = null;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql = "SELECT UUID FROM SERVER_HEART_BEAT_EVENTS WHERE HOST_NAME = ? AND MAC = ? AND SERVER_PORT = ?";
            stmt = conn.prepareStatement(sql, new String[]{"UUID"});
            stmt.setString(1, ctx.getHostName());
            stmt.setString(2, ctx.getMacAddress());
            stmt.setInt(3, ctx.getCarbonServerPort());

            resultSet = stmt.executeQuery();
            if (resultSet.next()){
                uuid = resultSet.getString("UUID");
            }
        } catch (SQLException e) {
            throw new HeartBeatDAOException("Error occurred while retrieving meta information for heart beat event from " +
                                            "mac '" + ctx.getMacAddress() + "' " +
                                            "hostname : '" + ctx.getHostName() + "' ", e);
        } finally {
            HeartBeatBeaconDAOUtil.cleanupResources(stmt, resultSet);
        }
        return uuid;
    }

    @Override
    public Map<String, ServerContext> getActiveServerDetails(int elapsedTimeInSeconds) throws HeartBeatDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<String, ServerContext> ctxList = new HashMap<>();
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql = "SELECT (@row_number:=@row_number + 1) AS IDX, UUID, HOST_NAME, MAC, SERVER_PORT from " +
                         "SERVER_HEART_BEAT_EVENTS, (SELECT @row_number:=-1) AS TEMP " +
                         "WHERE LAST_UPDATED_TIMESTAMP >  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? SECOND) " +
                         "ORDER BY UUID";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, elapsedTimeInSeconds);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                ctxList.put(resultSet.getString("UUID"), HeartBeatBeaconDAOUtil.populateContext(resultSet));
            }
        } catch (SQLException e) {
            throw new HeartBeatDAOException("Error occurred while retrieving acting server count with " +
                                            "heartbeat updates within " + elapsedTimeInSeconds + " seconds.", e);
        } finally {
            HeartBeatBeaconDAOUtil.cleanupResources(stmt, resultSet);
        }
        return ctxList;
    }

}

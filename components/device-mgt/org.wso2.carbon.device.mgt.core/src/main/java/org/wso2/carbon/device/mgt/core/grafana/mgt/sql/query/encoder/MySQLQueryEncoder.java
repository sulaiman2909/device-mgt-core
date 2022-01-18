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
 */
package org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.encoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.core.grafana.mgt.sql.connection.GrafanaDatasourceConnectionFactory;
import org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.PreparedQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLQueryEncoder implements QueryEncoder {

    private static final Log log = LogFactory.getLog(MySQLQueryEncoder.class);
    private static final String PREPARED_STATEMENT_STRING_OBJECT_ID_SEPARATOR = ": ";

    private final String databaseName;

    public MySQLQueryEncoder(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String encode(PreparedQuery preparedQuery) throws SQLException, DBConnectionException {
        try {
            Connection con = GrafanaDatasourceConnectionFactory.getConnection(databaseName);
            PreparedStatement stmt = con.prepareStatement(preparedQuery.getPreparedSQL());
            setParameters(stmt, preparedQuery.getParameters());
            return generateQueryFromPreparedStatement(stmt);
        } finally {
            GrafanaDatasourceConnectionFactory.closeConnection(databaseName);
        }
    }

    public void setParameters(PreparedStatement stmt, List<String> parameters)
            throws SQLException {
        int i = 0;
        for (String p : parameters) {
            stmt.setObject(++i, p);
        }
    }

    private String generateQueryFromPreparedStatement(PreparedStatement stmt) {
        String query =  stmt.toString().substring(stmt.toString().indexOf(PREPARED_STATEMENT_STRING_OBJECT_ID_SEPARATOR) +
                PREPARED_STATEMENT_STRING_OBJECT_ID_SEPARATOR.length());
        // remove unnecessary "]" char at the end
        if (query.charAt(query.length() - 1) == ']') {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

}

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

package io.entgra.device.mgt.core.notification.logger;

import io.entgra.device.mgt.core.device.mgt.extensions.logger.LogContext;

public class RoleMgtLogContext extends LogContext {
    private final String userStoreDomain;
    private final String roleName;
    private final String users;
    private final String actionTag;
    private final String userName;
    private final String tenantID;
    private final String tenantDomain;

    private RoleMgtLogContext(Builder builder) {
        this.userStoreDomain = builder.userStoreDomain;
        this.roleName = builder.roleName;
        this.users = builder.users;
        this.actionTag = builder.actionTag;
        this.userName = builder.userName;
        this.tenantID = builder.tenantID;
        this.tenantDomain = builder.tenantDomain;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getUserStoreDomain() {
        return userStoreDomain;
    }

    public String getUsers() {
        return users;
    }

    public String getActionTag() {
        return actionTag;
    }

    public String getUserName() {
        return userName;
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public static class Builder {
        private String userStoreDomain;
        private String roleName;
        private String users;
        private String actionTag;
        private String userName;
        private String tenantID;
        private String tenantDomain;

        public Builder() {
        }

        public String getUserStoreDomain() {
            return userStoreDomain;
        }

        public Builder setUserStoreDomain(String userStoreDomain) {
            this.userStoreDomain = userStoreDomain;
            return this;
        }

        public String getRoleName() {
            return roleName;
        }

        public Builder setRoleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public String getUsers() {
            return users;
        }

        public Builder setUsers(String users) {
            this.users = users;
            return this;
        }

        public String getActionTag() {
            return actionTag;
        }

        public Builder setActionTag(String actionTag) {
            this.actionTag = actionTag;
            return this;
        }

        public String getUserName() {
            return userName;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public String getTenantID() {
            return tenantID;
        }

        public Builder setTenantID(String tenantID) {
            this.tenantID = tenantID;
            return this;
        }

        public String getTenantDomain() {
            return tenantDomain;
        }

        public Builder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public RoleMgtLogContext build() {
            return new RoleMgtLogContext(this);
        }
    }
}
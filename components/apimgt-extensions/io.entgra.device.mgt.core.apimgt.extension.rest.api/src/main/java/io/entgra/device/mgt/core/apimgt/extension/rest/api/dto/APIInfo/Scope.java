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
package io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo;

import java.io.Serializable;
import java.util.*;

public class Scope implements Serializable{

    private static final long serialVersionUID = 1L;

    String key;
    String name;
    List<String> roles;
    String description;
    String id;
    int usageCount;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = removeDuplicatesFromRoleString(roles);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Scope scope = (Scope) o;

        if (id != null ? !id.equals(scope.id) : scope.id != null) return false;
        if (!key.equals(scope.key)) return false;
        if (!name.equals(scope.name)) return false;
        if (roles != null ? !roles.equals(scope.roles) : scope.roles != null) return false;
        return description != null ? description.equals(scope.description) : scope.description == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, name, roles, description, id);
    }

    private static List<String> removeDuplicatesFromRoleString(List<String> roles) {
        Set<String> roleSet = new HashSet<>();
        for(String role : roles) {
            roleSet.add(role.trim());
        }
        return new ArrayList<>(roleSet);
    }
}

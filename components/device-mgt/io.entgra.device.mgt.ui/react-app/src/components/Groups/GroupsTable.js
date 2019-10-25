/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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

import React from "react";
import axios from "axios";
import {message, notification, Table, Typography} from "antd";
import TimeAgo from 'javascript-time-ago'
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'
import {withConfigContext} from "../../context/ConfigContext";
import GroupActions from "./GroupActions";
import AddGroup from "./AddGroup";

const {Text} = Typography;

let config = null;
let apiUrl;

const getTimeAgo = (time) => {
    const timeAgo = new TimeAgo('en-US');
    return timeAgo.format(time);
};

class GroupsTable extends React.Component {
    constructor(props) {
        super(props);
        config =  this.props.context;
        TimeAgo.addLocale(en);
        this.state = {
            data: [],
            pagination: {},
            loading: false,
            selectedRows: []
        };
    }

    columns = [
        {
            title: 'Group Name',
            dataIndex: 'name',
            width: 100,
        },
        {
            title: 'Owner',
            dataIndex: 'owner',
            key: 'owner',
            // render: enrolmentInfo => enrolmentInfo.owner
            // todo add filtering options
        },
        {
            title: 'Description',
            dataIndex: 'description',
            key: 'description',
            //  render: enrolmentInfo => enrolmentInfo.ownership
            // todo add filtering options
        },
        {
            title: 'Action',
            dataIndex: 'id',
            key: 'action',
            render: (id, row) => (
                <span>
                    <GroupActions data={row} fetchGroups={this.fetchGroups}/>
                </span>
            ),
        },
    ];

    rowSelection = {
        onChange: (selectedRowKeys, selectedRows) => {
            this.setState({
                selectedRows: selectedRows
            })
        }
    };

    componentDidMount() {
        this.fetchGroups();
    }

    //fetch data from api
    fetchGroups = (params = {}) => {
        const config = this.props.context;
        this.setState({loading: true});

        // get current page
        const currentPage = (params.hasOwnProperty("page")) ? params.page : 1;

        const extraParams = {
            offset: 10 * (currentPage - 1), //calculate the offset
            limit: 10,
        };

        const encodedExtraParams = Object.keys(extraParams)
            .map(key => key + '=' + extraParams[key]).join('&');

        apiUrl = window.location.origin + config.serverConfig.invoker.uri +
                config.serverConfig.invoker.deviceMgt +
                "/admin/groups?" + encodedExtraParams;

        //send request to the invokerss
        axios.get(apiUrl).then(res => {
            if (res.status === 200) {
                const pagination = {...this.state.pagination};
                this.setState({
                    loading: false,
                    data: res.data.data.deviceGroups,
                    pagination,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:"Error occurred while trying to load device groups.",
                });
            }

            this.setState({loading: false});
        });
    };



    handleTableChange = (pagination, filters, sorter) => {
        const pager = {...this.state.pagination};
        pager.current = pagination.current;
        this.setState({
            pagination: pager,
        });
        this.fetch({
            results: pagination.pageSize,
            page: pagination.current,
            sortField: sorter.field,
            sortOrder: sorter.order,
            ...filters,
        });
    };

    render() {

        const {data, pagination, loading, selectedRows} = this.state;

        return (
            <div>
                <div style={{background: '#f0f2f5'}}>
                    <AddGroup fetchGroups={this.fetchGroups} style={{marginBottom:"10px"}}/>
                </div>
                <div>
                    <Table
                        columns={this.columns}
                        rowKey={record => (record.id)}
                        dataSource={data}
                        pagination={{
                            ...pagination,
                            size: "small",
                            // position: "top",
                            showTotal: (total, range) => `showing ${range[0]}-${range[1]} of ${total} groups`
                            // showQuickJumper: true
                        }}
                        loading={loading}
                        onChange={this.handleTableChange}
                        rowSelection={this.rowSelection}
                    />
                </div>
            </div>
        );
    }
}

export default withConfigContext(GroupsTable);
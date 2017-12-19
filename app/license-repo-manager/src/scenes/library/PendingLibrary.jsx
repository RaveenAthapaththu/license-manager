import React, { Component } from 'react';
import { Card, CardHeader, CardText } from 'material-ui/Card';
import { Table, TableBody, TableRow, TableRowColumn } from 'material-ui/Table';
import LibraryRequest from '../../services/database/LibraryRequest';
import ValidateUser from '../../services/authentication/ValidateUser';

/**
* @class PendingLibrary
* @extends {Component}
* @description Pending libraries
*/
class PendingLibrary extends Component {
    /**
    * @class PendingLibrary
    * @extends {Component}
    * @param {any} props props for constructor
    * @description constructor
    */
    constructor() {
        super();
        this.state = {
            waitingRequests: [],
            userDetails: [],
        };
        this.setPendingLibrary = this.setPendingLibrary.bind(this);
    }
    /**
    * @class PendingLibrary
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
            this.setPendingLibrary(this.state.userDetails.userEmail);
        });
    }
    /**
    * validation function for input repository name
    * @param {String} requestBy requestBye
    */
    setPendingLibrary(requestBy) {
        LibraryRequest.selectLibraryRequestWaitingAndRequestBy(requestBy).then((response) => {
            this.setState(() => {
                return {
                    waitingRequests: response,
                };
            });
        });
    }
    /**
    * @class PendingLibrary
    * @extends {Component}
    * @description render method
    */
    render() {
        const cards = [];
        if (this.state.waitingRequests.length !== 0) {
            const arrayLength = this.state.waitingRequests.length;
            const request = this.state.waitingRequests;
            let i = 0;
            /* eslint-disable */
            for (i = 0; i < arrayLength - 1; i += 2) {
                cards.push(
                    <div className="row" key={i}>
                        <div className="col-md-6">
                            <Card>
                                <CardHeader
                                    title={'Name - ' + request[i].LIBREQUEST_NAME}
                                    actAsExpander={true}
                                    showExpandableButton={true}
                                />
                                <CardText expandable={true}>
                                    <Table>
                                        <TableBody displayRowCheckbox={false}>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Category
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[i].LIBCATEGORY_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Type
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[i].LIBTYPE_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                        </TableBody>
                                    </Table>
                                </CardText>
                            </Card>
                            <br />
                        </div>
                        
                        <div className="col-md-6">
                            <Card>
                                <CardHeader
                                    title={'Name - ' + request[i + 1].LIBREQUEST_NAME}
                                    actAsExpander={true}
                                    showExpandableButton={true}
                                />
                                <CardText expandable={true}>
                                    <Table>
                                        <TableBody displayRowCheckbox={false}>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Category
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[i + 1].LIBCATEGORY_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Type
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[i + 1].LIBTYPE_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                        </TableBody>
                                    </Table>
                                </CardText>
                            </Card>
                            <br />
                        </div>
                    </div>
                );
            }
            if (arrayLength % 2 !== 0) {
                cards.push(
                    <div className="row" key={i}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[arrayLength - 1].LIBREQUEST_NAME}
                                        actAsExpander={true}
                                        showExpandableButton={true}
                                    />
                                    <CardText expandable={true}>
                                        <Table>
                                            <TableBody displayRowCheckbox={false}>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Category
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[arrayLength - 1].LIBCATEGORY_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Type
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[arrayLength - 1].LIBTYPE_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                            </TableBody>
                                        </Table>
                                    </CardText>
                                </Card>
                                <br />
                            </div>
                    </div>
                );
            }
            /* eslint-enable */
        }
        return (
            <div className="container-fluid">
                <h2 className="text-center">Requested Library Requests</h2>
                <br />
                {/* eslint-disable */}
                {cards}
                {/* eslint-enable */}
            </div>
        );
    }
}

export default PendingLibrary;

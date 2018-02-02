import React, { Component } from 'react';
import { Card, CardHeader, CardText } from 'material-ui/Card';
import { Table, TableBody, TableRow, TableRowColumn } from 'material-ui/Table';
import Repository from '../../services/database/Repository';
import ValidateUser from '../../services/authentication/ValidateUser';

/**
* @class PendingRepository
* @extends {Component}
* @description Show pending requests
*/
class PendingRepository extends Component {
    /**
    * @class PendingRequests
    * @extends {Component}
    * @param {any} props props for constructor
    * @description Show pending requests constructor
    */
    constructor() {
        super();
        this.state = {
            pendingRequests: [],
            userDetails: [],
        };
        this.setPendingRequests = this.setPendingRequests.bind(this);
    }
    /**
    * @class PendingRepository
    * @extends {Component}
    * @description Show pending requests componentDidMount
    */
    componentDidMount() {
        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
            this.setPendingRequests(this.state.userDetails.userEmail);
        });
    }
    /**
    * validation function for input repository name
    * @param {String} requestBy requestBye
    */
    setPendingRequests(requestBy) {
        Repository.selectDataFromRequestBy(requestBy).then((response) => {
            this.setState(() => {
                return {
                    pendingRequests: response,
                };
            });
        });
    }
    /**
    * @class PendingRepository
    * @extends {Component}
    * @description Show pending requests render method
    */
    render() {
        const cards = [];
        if (this.state.pendingRequests.length !== 0) {
            const arrayLength = this.state.pendingRequests.length;
            const request = this.state.pendingRequests;
            let i = 0;
            /* eslint-disable */
            for (i = 0; i < arrayLength - 1; i += 2) {
                cards.push(
                    <div className="row" key={i}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[i].REPOSITORY_NAME}
                                        actAsExpander={true}
                                        showExpandableButton={true}
                                    />
                                    <CardText expandable={true}>
                                        <Table>
                                            <TableBody displayRowCheckbox={false}>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Repository Type
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i].REPOSITORYTYPE_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Organization
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i].ORGANIZATION_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Language
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i].REPOSITORY_LANGUAGE}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        License
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i].LICENSE_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Buildable
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {(request[i].REPOSITORY_BUILDABLE) ? 'Yes' : 'No'}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Nexus
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {(request[i].REPOSITORY_NEXUS) ? 'Yes' : 'No'}
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
                                        title={'Name - ' + request[i + 1].REPOSITORY_NAME}
                                        actAsExpander={true}
                                        showExpandableButton={true}
                                    />
                                    <CardText expandable={true}>
                                        <Table>
                                            <TableBody displayRowCheckbox={false}>
                                                <TableRow >
                                                    <TableRowColumn>
                                                        Repository Type
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i + 1].REPOSITORYTYPE_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Organization
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i + 1].ORGANIZATION_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Language
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i + 1].REPOSITORY_LANGUAGE}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        License
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {request[i + 1].LICENSE_NAME}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Buildable
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {(request[i + 1].REPOSITORY_BUILDABLE) ? 'Yes' : 'No'}
                                                    </TableRowColumn>
                                                </TableRow>
                                                <TableRow>
                                                    <TableRowColumn>
                                                        Nexus
                                                    </TableRowColumn>
                                                    <TableRowColumn>
                                                        {(request[i + 1].REPOSITORY_NEXUS) ? 'Yes' : 'No'}
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
                                    title={'Name - ' + request[arrayLength - 1].REPOSITORY_NAME}
                                    actAsExpander={true}
                                    showExpandableButton={true}
                                />
                                <CardText expandable={true}>
                                    <Table>
                                        <TableBody displayRowCheckbox={false}>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Repository Type
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[arrayLength - 1].REPOSITORYTYPE_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Organization
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[arrayLength - 1].ORGANIZATION_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Language
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[arrayLength - 1].REPOSITORY_LANGUAGE}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    License
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {request[arrayLength - 1].LICENSE_NAME}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Buildable
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {(request[arrayLength - 1].REPOSITORY_BUILDABLE) ? 'Yes' : 'No'}
                                                </TableRowColumn>
                                            </TableRow>
                                            <TableRow>
                                                <TableRowColumn>
                                                    Nexus
                                                </TableRowColumn>
                                                <TableRowColumn>
                                                    {(request[arrayLength - 1].REPOSITORY_NEXUS) ? 'Yes' : 'No'}
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
                <h2 className="text-center">Requested Repository Requests</h2>
                <br />
                {cards}
            </div>
        );
    }
}

export default PendingRepository;

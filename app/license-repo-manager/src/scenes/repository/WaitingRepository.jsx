import React, { Component } from 'react';
import { Link } from 'react-router';
import { Card, CardActions, CardHeader, CardText } from 'material-ui/Card';
import { Table, TableBody, TableRow, TableRowColumn } from 'material-ui/Table';
import FlatButton from 'material-ui/FlatButton';
import Repository from '../../services/database/Repository';

/**
* @class WaitingRepository
* @extends {Component}
* @description Waiting requests
*/
class WaitingRepository extends Component {
    /**
    * @class WaitingRepository
    * @extends {Component}
    * @param {any} props props for constructor
    * @description constructor
    */
    constructor() {
        super();
        this.state = {
            waitingRequests: [],
        };
    }
    /**
    * @class WaitingRepository
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        Repository.selectWaitingRequests().then((response) => {
            this.setState(() => {
                return {
                    waitingRequests: response,
                };
            });
        });
    }
    /**
    * @class WaitingRepository
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
                        <Link to={'/app/acceptRepository?repositoryId=' + request[i].REPOSITORY_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[i].REPOSITORY_NAME}
                                        subtitle={'Requested by - ' + request[i].REPOSITORY_REQUEST_BY}
                                        actAsExpander={true}
                                        showExpandableButton={true}
                                    />
                                    <CardActions>
                                        <FlatButton label="More" />
                                    </CardActions>
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
                        </Link>
                        <Link to={'/app/acceptRepository?repositoryId=' + request[i + 1].REPOSITORY_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[i + 1].REPOSITORY_NAME}
                                        subtitle={'Requested by - ' + request[i + 1].REPOSITORY_REQUEST_BY}
                                        actAsExpander={true}
                                        showExpandableButton={true}
                                    />
                                    <CardActions>
                                        <FlatButton label="More" />
                                    </CardActions>
                                    <CardText expandable={true}>
                                        <Table>
                                            <TableBody displayRowCheckbox={false}>
                                                <TableRow>
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
                        </Link>
                    </div>
                );
            }
            if (arrayLength % 2 !== 0) {
                cards.push(
                    <div className="row" key={i}>
                        <Link to={'/app/acceptRepository?repositoryId=' + request[arrayLength - 1].REPOSITORY_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[arrayLength - 1].REPOSITORY_NAME}
                                        subtitle={'Requested by - ' + request[arrayLength - 1].REPOSITORY_REQUEST_BY}
                                        actAsExpander={true}
                                        showExpandableButton={true}
                                    />
                                    <CardActions>
                                        <FlatButton label="More" />
                                    </CardActions>
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
                        </Link>
                    </div>
                );
            }
            /* eslint-enable */
        }
        return (
            <div className="container-fluid">
                <h2 className="text-center">Requested Repositories For The Approval</h2>
                <br />
                {cards}
            </div>
        );
    }
}

export default WaitingRepository;

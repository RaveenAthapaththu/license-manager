import React, { Component } from 'react';
import { Link } from 'react-router';
import { Card, CardActions, CardHeader, CardText } from 'material-ui/Card';
import { Table, TableBody, TableRow, TableRowColumn } from 'material-ui/Table';
import FlatButton from 'material-ui/FlatButton';
import LibraryRequest from '../../services/database/LibraryRequest';

/**
* @class WaitingLibrary
* @extends {Component}
* @description Show waiting libraries for acceptance
*/
class WaitingLibrary extends Component {
    /**
    * @class WaitingLibrary
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
    * @class WaitingLibrary
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        LibraryRequest.selectLibraryWaitingRequests().then((response) => {
            this.setState(() => {
                return {
                    waitingRequests: response,
                };
            });
        });
    }
    /**
    * @class WaitingLibrary
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
                        <Link to={'/app/acceptLibrary?libRequestId=' + request[i].LIBREQUEST_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[i].LIBREQUEST_NAME}
                                        subtitle={'Requested by - ' + request[i].LIBREQUEST_REQUESTED_BY}
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
                        </Link>
                        <Link to={'/app/acceptLibrary?libRequestId=' + request[i + 1].LIBREQUEST_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[i + 1].LIBREQUEST_NAME}
                                        subtitle={'Requested by - ' + request[i + 1].LIBREQUEST_REQUESTED_BY}
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
                        </Link>
                    </div>
                );
            }
            if (arrayLength % 2 !== 0) {
                cards.push(
                    <div className="row" key={i}>
                        <Link to={'/app/acceptLibrary?libRequestId=' + request[arrayLength - 1].LIBREQUEST_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Name - ' + request[arrayLength - 1].LIBREQUEST_NAME}
                                        subtitle={'Requested by - ' + request[arrayLength - 1].LIBREQUEST_REQUESTED_BY}
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
                        </Link>
                    </div>
                );
            }
            /* eslint-enable */
        }
        return (
            <div className="container-fluid">
                <h2 className="text-center">Requested Libraries For The Approval</h2>
                <br />
                {/* eslint-disable */}
                {cards}
                {/* eslint-enable */}
            </div>
        );
    }
}

export default WaitingLibrary;

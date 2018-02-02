import React, { Component } from 'react';
import { Link } from 'react-router';
import { Card, CardActions, CardHeader } from 'material-ui/Card';
import FlatButton from 'material-ui/FlatButton';
import DataManager from '../../services/msf4j/DataManager';

/**
* @class WaitingLicense
* @extends {Component}
* @description Show waiting libraries for acceptance
*/
class WaitingLicense extends Component {
    /**
    * @class WaitingLicense
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
    * @class WaitingLicense
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        DataManager.selectWaitingLicenseRequests().then((response) => {
            this.setState(() => {
                return {
                    waitingRequests: response.data.responseData,
                };
            });
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * @class WaitingLicense
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
                        <Link to={'/app/acceptLicense?productId=' + request[i].LR_PRODUCT_ID + '&requestId=' + request[i].LR_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Product  - ' + request[i].PRODUCT_NAME}
                                        subtitle={'Version - ' + request[i].PRODUCT_VERSION}
                                        actAsExpander={true}
                                    />
                                    <CardActions>
                                        <FlatButton label="More" />
                                    </CardActions>
                                </Card>
                                <br />
                            </div>
                        </Link>
                        <Link to={'/app/acceptLicense?productId=' + request[i + 1].LR_PRODUCT_ID + '&requestId=' + request[i].LR_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Product  - ' + request[i + 1].PRODUCT_NAME}
                                        subtitle={'Version - ' + request[i + 1].PRODUCT_VERSION}
                                        actAsExpander={true}
                                    />
                                    <CardActions>
                                        <FlatButton label="More" />
                                    </CardActions>
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
                        <Link to={'/app/acceptLicense?productId=' + request[arrayLength - 1].LR_PRODUCT_ID + '&requestId=' + request[i].LR_ID}>
                            <div className="col-md-6">
                                <Card>
                                    <CardHeader
                                        title={'Product  - ' + request[arrayLength - 1].PRODUCT_NAME}
                                        subtitle={'Version - ' + request[arrayLength - 1].PRODUCT_VERSION}
                                        actAsExpander={true}
                                    />
                                    <CardActions>
                                        <FlatButton label="More" />
                                    </CardActions>
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

export default WaitingLicense;

import React, { Component } from 'react';

/**
* @class ErrorPage
* @extends {Component}
* @description Get user details
*/
class Alert extends Component {
    /**
    * @class Root
    * @extends {Component}
    * @description Sample React component
    */
    render() {
        const props = this.props;
        return (
            <div className="alert alert-dismissible alert-warning" style={{ display: props.display }}>
                <button type="button" className="close" data-dismiss="alert">&times;</button>
                <strong>
                    {props.message}
                </strong>
            </div>
        );
    }
}
export default Alert;

import React, { Component } from 'react';

/**
* @class Alert
* @extends {Component}
* @description Alert message component
*/
class Alert extends Component {
    /**
    * @class Alert
    * @extends {Component}
    * @description Alert message component render method
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

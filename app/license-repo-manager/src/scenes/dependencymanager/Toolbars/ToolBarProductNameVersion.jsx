import React, { Component } from 'react';
import Chip from 'material-ui/Chip';
import { Toolbar, ToolbarGroup, ToolbarTitle } from 'material-ui/Toolbar';

/**
* @class ToolBarProductNameVersion
* @extends {Component}
* @description Get ProductName&Version
*/
class ToolbarProductNameVersion extends Component {
    /**
    * @class ToolBarProductNameVersion
    * @extends {Component}
    * @description render method
    */
    render() {
        return (
            <div>
                {/* eslint-disable */}
                <Toolbar  style={{ backgroundColor: '#212121' }}>
                    <ToolbarGroup firstChild={false}>
                        <ToolbarTitle text="Product Name :" />
                        <Chip backgroundColor='#FF3D00'>{this.props.nameProduct}</Chip>
                    </ToolbarGroup>
                    <ToolbarGroup firstChild={true}>
                        <ToolbarTitle text="Version :" />
                        <Chip backgroundColor='#FF3D00'>{this.props.versionProduct}</Chip>
                    </ToolbarGroup>
                </Toolbar>
                {/* eslint-enable */}
            </div>
        );
    }
}

export default ToolbarProductNameVersion;

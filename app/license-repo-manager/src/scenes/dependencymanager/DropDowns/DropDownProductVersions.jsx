import React, { Component } from 'react';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import LoadProductVersions from '../../../services/dependencyManager/database/LoadProductVersions';

/**
* @class DropDownProductVersions
* @extends {Component}
* @description Get ProductVersions
*/
class DropDownProductVersions extends Component {
    /**
     * @class DropDownProductVersions
     * @extends {Component}
     * @param {any} props props for constructor
     * @description Sample React component
     */
    constructor(props) {
        super(props);
        this.state = { data: [], value: '' };
        this.loadProductVersionsDropDown = this.loadProductVersionsDropDown.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }
    /**
    * @class DropDownProductVersions
    * @extends {Component}
    * @param {any} props props for componentWillReceiveProps
    * @description componentWillReceiveProps
    */
    componentWillReceiveProps(props) {
        if(props.loadDropDown){//eslint-disable-line
            this.loadProductVersionsDropDown(props.productName);//eslint-disable-line
        }
    }
    /**
    * @class DropDownProductVersions
    * @extends {Component}
    * @param {string} prName product name
    * @description Add the Menu items of the Product Version dropdown
    */
    loadProductVersionsDropDown(prName) {
        LoadProductVersions.productVersionsDropdown(prName).then((response) => {
            let i = 0;
            const array = [];
            if (response.length > 0) {
                for (i; i < response.length; i++) {
                    array.push(<MenuItem value={i} key={i} primaryText={response[i].PRODUCT_VERSION} />);
                }
            } else {
                array[0] = 'Error in loading';
            }
            this.setState({ data: array });
        });
    }
    handleChange(event, index, value) {
        const update = this.props;
        this.setState({ value });
        update.setVersion(this.state.data[value].props.primaryText);
    }
    render() {
        if (this.props.loadDropDown) {
            return (
                <div>
                    <SelectField
                        floatingLabelText="Select Version"
                        maxHeight={300}
                        value={this.state.value}
                        onChange={this.handleChange}
                    >
                        {this.state.data}
                    </SelectField>
                </div>
            );
        } else {
            return (
                <div>
                    <SelectField
                        floatingLabelText="Select Version"
                        maxHeight={300}
                        value={this.state.value}
                        onChange={this.handleChange}
                        disabled={true}//eslint-disable-line
                    >
                        <MenuItem value={0} key={0} primaryText={'-Select-'} />
                    </SelectField>
                </div>
            );
        }
    }
}

export default DropDownProductVersions;

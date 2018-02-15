import React, { Component } from 'react';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import LoadProductVersions from '../../../services/dependencyManager/database/LoadProductVersions';
import LoadComponentVersions from '../../../services/dependencyManager/database/LoadComponentVersions';

const styles = {
    customWidth: {
        width: '100%',
    },
};
/**
* @class DropDownProductOrLibraryVersions
* @extends {Component}
* @description Get ProductVersions
*/
class DropDownProductOrLibraryVersions extends Component {
    /**
     * @class DropDownProductOrLibraryVersions
     * @extends {Component}
     * @param {any} props props for constructor
     * @description Sample React component
     */
    constructor(props) {
        super(props);
        this.state = { data: [], value: '' };
        this.loadProductVersionsDropDown = this.loadProductVersionsDropDown.bind(this);
        this.loadComponentVersionsDropDown = this.loadComponentVersionsDropDown.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }
    /**
    * @class DropDownProductOrLibraryVersions
    * @extends {Component}
    * @param {any} props props for componentWillReceiveProps
    * @description componentWillReceiveProps
    */
    componentWillReceiveProps(props) {
        if(props.loadDropDown){//eslint-disable-line
            if(props.selectedType === 'Component'){//eslint-disable-line
                this.loadComponentVersionsDropDown(props.Name);//eslint-disable-line
            }
            if (props.selectedType === 'Product') {
                this.loadProductVersionsDropDown(props.Name);//eslint-disable-line
            }
        }
    }
    /**
    * @class DropDownProductOrLibraryVersions
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
    /**
    * @class DropDownProductOrLibraryVersions
    * @extends {Component}
    * @param {string} cmName component name
    * @description Add the Menu items of the Component Version dropdown
    */
    loadComponentVersionsDropDown(cmName) {
        LoadComponentVersions.componentVersionsDropdown(cmName).then((response) => {
            let i = 0;
            const array = [];
            if (response.length > 0) {
                for (i; i < response.length; i++) {
                    array.push(<MenuItem value={i} key={i} primaryText={response[i].COMP_VERSION} />);
                }
            } else {
                array[0] = 'Error in loading';
            }
            this.setState({ data: array });
        });
    }
    /**
    * @class DropDownProductOrLibraryVersions
    * @extends {Component}
    * @param {any} event event
    * @param {any} index index
    * @param {any} value value
    * @description handle version select field's changes
    */
    handleChange(event, index, value) {
        const update = this.props;
        this.setState({ value });
        update.setVersion(this.state.data[value].props.primaryText);
    }
    /**
    * @class DropDownProductOrLibraryVersions
    * @extends {Component}
    * @description render method
    */
    render() {
        if (this.props.loadDropDown) {
            return (
                <SelectField
                    floatingLabelText="Select Version"
                    maxHeight={300}
                    value={this.state.value}
                    onChange={this.handleChange}
                >
                    {this.state.data}
                </SelectField>
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
                        style={styles.customWidth}
                    >
                        <MenuItem value={0} key={0} primaryText={'-Select-'} />
                    </SelectField>
                </div>
            );
        }
    }
}

export default DropDownProductOrLibraryVersions;

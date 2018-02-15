import React, { Component } from 'react';
import AutoComplete from 'material-ui/AutoComplete';
import { Card, CardTitle } from 'material-ui/Card';
import LoadLibraryNames from '../../../services/dependencyManager/database/LoadLibraryNames';
import DropDownLibraryVersions from '../DropDowns/DropDownLibraryVersions';
import ToolbarLibraryNameVersion from '../Toolbars/ToolBarLibraryNameVersion';
import ProductComponentsTable from '../Tables/ProductsAndComponentsForLibrary';
import LoadingScreen from '../Common/LoadingScreen';

const array = [];
/**
 * @class ViewByLibrary
 * @extends {Component}
 * @description Sample React component
 */
export default class ViewByLibrary extends Component {
/**
 * @class ViewByLibrary
 * @extends {Component}
 * @param {any} props props for constructor
 * @description Sample React component
 */
    constructor(props) {
        super(props);
        this.state = { data: [],
            searchText: '',
            loadUI: false,
            dataD: [],
            lName: 'No Library is Selected',
            lVersion: 'No Version is Selected',
            lLatest: 'No Version is Selected',
            lArtifactID: 'Makeselections',
            lGroupID: 'Makeselections',
            libType: 'Make selections',
            loadV: false,
            getLatest: false,
            conditionRender: false };
        this.handleUpdateInput = this.handleUpdateInput.bind(this);
        this.setLibraryName = this.setLibraryName.bind(this);
        this.setGroupIDArtifactID = this.setGroupIDArtifactID.bind(this);
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @description componentDidMount load Library Names in the drop down list
    */
    componentDidMount() {
        this.loadLibraryNamesDropDown();
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @param {string} searchTxt typed text
    * @description handle update event of auto complete
    */
    handleUpdateInput(searchTxt) {  //eslint-disable-line
        this.setState({ //eslint-disable-line
            searchText: searchTxt, //eslint-disable-line
            lVersion: 'No Version is Selected',//eslint-disable-line
            conditionRender: false,
            loadV: false,
            getLatest: false,
            lArtifactID: 'Makeselections',
            lGroupID: 'Makeselections',
            libType: 'Make selections',
        });   //eslint-disable-line     
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @description set library Name
    */
    setLibraryName() {  //eslint-disable-line
        const dataArray = this.state.data;
        const ans = dataArray.includes(this.state.searchText, 0);
        if (ans) {
            this.setState({
                lName: this.state.searchText,
                lVersion: 'No Version is Selected',
                conditionRender: false,
                loadV: true,
                getLatest: false,
                lArtifactID: 'Makeselections',
                lGroupID: 'Makeselections',
                libType: 'Make selections',
            });
        }
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @param {string} version library version
    * @description set library version
    */
    setLibraryVersion(version) {  //eslint-disable-line
        this.setState({ //eslint-disable-line
            lVersion: version,//eslint-disable-line
            conditionRender: false,
            loadV: true,
            getLatest: false,
        });   //eslint-disable-line
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @param {string} gID group ID
    * @param {string} aID artifact ID
    * @param {string} lType library type
    * @description set group id artifact id
    */
    setGroupIDArtifactID(gID, aID, lType) {  //eslint-disable-line
        this.setState({
            lArtifactID: aID,
            lGroupID: gID,
            libType: lType,
            conditionRender: true,
            getLatest: true,
        });   //eslint-disable-line
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @description Load the Menu items of the library dropdown
    */
    loadLibraryNamesDropDown() {
        LoadLibraryNames.selectLibraryNamesfortheDropdown().then((response) => {
            let i = 0;
            if (response.length > 0) {
                for (i; i < response.length; i++) {
                    array[i] = response[i].LIB_NAME;
                }
                this.setState({
                    loadUI: true,
                });
            } else {
                array[0] = 'Error in loading';
            }
            this.setState({ data: array });
        });
    }
    /**
    * @class ViewByLibrary
    * @extends {Component}
    * @description render methods
    */
    render() {
        let returnView;
        if (this.state.loadUI) {
            returnView = (
                <div>
                    <div style={{ backgroundColor: '#212121' }}>
                        <div>
                            {/* eslint-disable */}
                            <Card>
                                <h2 className="text-center">View By Library</h2>
                                <CardTitle 
                                    className="text-center"
                                    subtitle="Products and Components using the given Library are displayed" 
                                />
                            </Card>
                            {/* eslint-enable */}
                        </div>
                        <div style={{ backgroundColor: '#212121' }}>
                            <div
                                className="col-xs-6 col-sm-6 col-lg-6 text-right"
                                style={{ backgroundColor: '#212121' }}
                            >
                                <AutoComplete
                                    floatingLabelText="Search Library"
                                    menuStyle={{ maxHeight: '300px', overflowY: 'auto' }}
                                    openOnFocus={true}//eslint-disable-line
                                    searchText={this.state.searchText}
                                    onUpdateInput={this.handleUpdateInput}
                                    onNewRequest={this.setLibraryName}
                                    filter={(searchText, key) => (key.indexOf(searchText) !== -1)}
                                    dataSource={this.state.data}
                                />
                            </div>
                            <div
                                className="col-xs-6 col-sm-6 col-lg-6 text-left"
                                style={{ backgroundColor: '#212121' }}
                            >
                                <DropDownLibraryVersions
                                    loadDropDown={this.state.loadV}
                                    libraryName={this.state.lName}
                                    groupIDartifactID={this.setGroupIDArtifactID.bind(this)}//eslint-disable-line
                                    setVersion={this.setLibraryVersion.bind(this)}//eslint-disable-line
                                />
                            </div>
                        </div>
                        <div>
                            <ToolbarLibraryNameVersion
                                getLatestVersion={this.state.getLatest}
                                nameLibrary={this.state.lName}
                                versionLibrary={this.state.lVersion}
                                libGroupID={this.state.lGroupID}
                                libArtifactID={this.state.lArtifactID}
                                librType={this.state.libType}
                            />
                        </div>
                        <ProductComponentsTable
                            renderCondition={this.state.conditionRender}
                            nameLibrary={this.state.lName}
                            versionLibrary={this.state.lVersion}
                            libGroupID={this.state.lGroupID}
                            libArtifactID={this.state.lArtifactID}
                        />
                    </div>
                </div>
            );
        } else {
            returnView = (
                <div>
                    <LoadingScreen />
                </div>
            );
        }
        return (
            <div style={{ marginTop: -21 }}>
                {returnView}
            </div>
        );
    }
}

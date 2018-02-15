import React from 'react';
import {
    PagingState,
    LocalPaging,
} from '@devexpress/dx-react-grid';
import {
    Grid,
    TableView,
    TableHeaderRow,
    TableColumnResizing,
    PagingPanel,
} from '@devexpress/dx-react-grid-bootstrap3';
import Chip from 'material-ui/Chip';
import LibrariesOfComp
    from '../../../services/dependencyManager/database/LoadComponentLibraries';
import LoadingScreen from '../Common/LoadingScreen';

/* eslint-disable */
const HighlightedTableCell = ({ value, style, colSpan }) => (
    <td
      style={{
        backgroundColor: 'yellow',
      }}
      colSpan={colSpan}
    >
      <span style={{ color: 'black' }}>{value}</span>
    </td>
);
/* eslint-enable */
const columns = [
    { name: 'Count', title: '' },
    { name: 'LibraryName', title: 'The Library Name' },
    { name: 'LibraryType', title: 'The Library Type' },
    { name: 'LibraryVersion', title: 'The Current Version' },
    { name: 'LatestVersion', title: 'The Latest Version' },
    { name: 'ArtifactID', title: 'Artifact ID' },
    { name: 'GroupID', title: 'Group ID' },
];
/**
* @class LibrariesOfComponent
* @extends {Component}
* @description Get libraries of the component
*/
export default class LibrariesOfComponent extends React.PureComponent {
    /**
     * @class LibrariesOfComponent
     * @extends {Component}
     * @param {any} props props for constructor
     * @description Sample React component
     */
    constructor(props) {
        super(props);
        this.state = {
            tRows: [],
            showTable: false,
            columnWidths: {
                Count: 100,
                LibraryName: 400,
                LibraryVersion: 200,
                LibraryType: 200,
                LatestVersion: 300,
                GroupID: 200,
                ArtifactID: 200,
            },
            expandedRows: [],
            CompName: '',
            CompVersion: '',
            numberOfRecords: 0,
        };
        this.loadTable = this.loadTable.bind(this);
        this.tableCellTemplate = ({
            column, value, style, colSpan,
        }) => {
            if (column.name === 'LatestVersion') {
                return <HighlightedTableCell value={value} style={style} colSpan={colSpan} />;
            }
            return undefined;
        };
    }
    /**
    * @class LibrariesOfComponent
    * @extends {Component}
    * @param {any} props props for componentWillReceiveProps
    * @description componentWillReceiveProps
    */
    componentWillReceiveProps(props) {
        if (props.renderCondition) {//eslint-disable-line
            this.setState({
                compName: props.nameComp,//eslint-disable-line
                compVersion: props.versionComp,//eslint-disable-line
                numberOfRecords: 0,
                showTable: false,
            });
            this.loadTable(props.nameComp, props.versionComp);
        }
    }
    /**
    * @class LibrariesOfComponent
    * @extends {Component}
    * @param {string} cName component name
    * @param {string} cVersion component name
    * @description Load libraries of given Component
    */
    loadTable(cName, cVersion) {
        LibrariesOfComp.getLibrariesOfComponent(cName, cVersion).then((response) => {
            let i = 0;
            const array = [];
            if (response.Libraries.length > 0) {
                for (i; i < response.Libraries.length; i++) {
                    array[i] = {
                        Count: i + 1,
                        LibraryName: response.Libraries[i].LIB_NAME,
                        LibraryType: response.Libraries[i].LIB_TYPE,
                        LibraryVersion: response.Libraries[i].LIB_VERSION,
                        LatestVersion: response.Libraries[i].LATEST_VERSION,
                        GroupID: response.Libraries[i].ARTIFACT_ID,
                        ArtifactID: response.Libraries[i].GROUP_ID,
                    };
                }
            } else {
                array[0] = 'No results';
            }
            this.setState({
                tRows: array,
                numberOfRecords: response.Libraries.length,
                showTable: true,
            });
        });
    }
    /**
    * @class LibrariesOfComponent
    * @extends {Component}
    * @description render method
    */
    render() {
        let returnView;
        if (this.props.renderCondition) {
            if (this.state.showTable) {
                returnView = (
                    <div>
                        {this.state.numberOfRecords > 0 ?
                            <div>
                                <Chip>
                                    {this.state.numberOfRecords} results are returned
                                </Chip>
                                <Grid
                                    rows={this.state.tRows}
                                    columns={columns}
                                >
                                    <PagingState
                                        defaultCurrentPage={0}
                                        pageSize={12}
                                    />
                                    <LocalPaging />
                                    <TableView tableCellTemplate={this.tableCellTemplate} />
                                    <TableColumnResizing defaultColumnWidths={this.state.columnWidths} />
                                    <TableHeaderRow allowResizing />
                                    <PagingPanel />
                                </Grid>
                            </div>
                            :
                            <Chip>
                                No Libraries Found
                            </Chip>
                        }
                    </div>
                );
            } else {
                returnView = (
                    <LoadingScreen />
                );
            }
        }
        return (
            <div>
                {returnView}
            </div>
        );
    }
}

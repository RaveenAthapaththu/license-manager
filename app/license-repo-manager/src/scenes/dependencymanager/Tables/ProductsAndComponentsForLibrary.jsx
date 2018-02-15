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
import ProductsComponentsOfLibrary
    from '../../../services/dependencyManager/database/LoadProductsAndComponentsUsingLibrary';
import LoadingScreen from '../Common/LoadingScreen';

const columns = [
    { name: 'Number', title: '' },
    { name: 'ProductName', title: 'The Product Name' },
    { name: 'ProductVersion', title: 'The Product Version' },
    { name: 'ProductLevelDependency', title: 'Product Level Dependency' },
    { name: 'ComponentName', title: 'Component Name' },
    { name: 'ComponentVersion', title: 'Component Version' },
    { name: 'ComponentType', title: 'Component Type' },
];
/**
* @class ProductsAndComponentsForLibrary
* @extends {Component}
* @description Get libraries of product
*/
export default class ProductsAndComponentsForLibrary extends React.PureComponent {
    /**
     * @class ProductsAndComponentsForLibrary
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
                Number: 100,
                ProductName: 350,
                ProductVersion: 160,
                ProductLevelDependency: 200,
                ComponentName: 400,
                ComponentVersion: 200,
                ComponentType: 200,
            },
            expandedRows: [],
            libraryName: '',
            libraryVersion: '',
            loading: '',
            numberOfRecords: 0,
        };
        this.loadTable = this.loadTable.bind(this);
    }
    /**
    * @class ProductsAndComponentsForLibrary
    * @extends {Component}
    * @param {any} props props for componentWillReceiveProps
    * @description componentWillReceiveProps
    */
    componentWillReceiveProps(props) {
        if (props.renderCondition) {//eslint-disable-line
            this.setState({
                libraryName: props.nameLibrary,//eslint-disable-line
                libraryVersion: props.versionLibrary,//eslint-disable-line
                numberOfRecords: 0,
                showTable: false,
            });
            this.loadTable(props.nameLibrary, props.versionLibrary);
        }
    }
    /**
    * @class ProductsAndComponentsForLibrary
    * @extends {Component}
    * @param {string} lName library name
    * @param {string} lVersion library version
    * @description Load Products and Components using the given library
    */
    loadTable(lName, lVersion) {
        ProductsComponentsOfLibrary.getProductsAndComponentsUsingLibrary(lName, lVersion).then((response) => {
            let i = 0;
            const array = [];
            if (response.data.length > 0) {
                for (i; i < response.data.length; i++) {
                    array[i] = {
                        Number: ++i,
                        ProductName: response.data[--i].PRODUCT_NAME,
                        ProductVersion: response.data[i].PRODUCT_VERSION,
                        ProductLevelDependency: response.data[i].PRODUCT_LEVEL_DEPENDENCY,
                        ComponentName: response.data[i].COMPONENT_NAME,
                        ComponentVersion: response.data[i].COMP_VERSION,
                        ComponentType: response.data[i].COMP_TYPE,
                    };
                }
            } else {
                array[0] = 'No results';
            }
            this.setState({
                tRows: array,
                numberOfRecords: response.data.length,
                showTable: true,
            });
        });
    }
    /**
    * @class ProductsAndComponentsForLibrary
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
                                <div>
                                    <Chip>
                                        {this.state.numberOfRecords} results are returned
                                    </Chip>
                                </div>
                                <Grid
                                    rows={this.state.tRows}
                                    columns={columns}
                                >
                                    <PagingState
                                        defaultCurrentPage={0}
                                        pageSize={12}
                                    />
                                    <LocalPaging />
                                    <TableView />
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

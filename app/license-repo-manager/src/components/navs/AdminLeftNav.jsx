import React, { Component } from 'react';
import Paper from 'material-ui/Paper';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import LibraryBooks from 'material-ui/svg-icons/av/library-books';
import ContentCopy from 'material-ui/svg-icons/content/content-copy';
import Add from 'material-ui/svg-icons/content/add';
import ArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import { Link } from 'react-router';

const style = {
    paper: {
        display: 'inline-block',
        float: 'left',
        width: '100%',
        fontSize: '10px',
        fontFamily: 'Helvetica',
        backgroundColor: '#222222',
        overflowY: 'hidden !important',
        marginTop: 0,
        margin: '0px 0px 0px 0px',
    },
    rightIcon: {
        textAlign: 'center',
        lineHeight: '70px',
    },
    menuItem: {
        margin: '-8px 0 -8px 0',
        padding: '0 !important',
        cursor: 'initial !important',
    },
    menu: {
        paddingTop: 0,
        padding: '0px 0px',
        margin: '0px 0px 0px 0px',
    },
    subMenuItem: {
        padding: 0,
        overflowY: 'hidden',
    },
};
/**
* @class UsRooter
* @extends {Component}
* @description Get user details
*/
class AdminLeftNav extends Component {
    /**
    * @class Root
    * @extends {Component}
    * @description Sample React component
    */
    render() {
        return (
            <div>
                <Paper style={style.paper}>
                    <Menu menuItemStyle={style.menu}>
                        <MenuItem
                            className="icon-menu-wrapper"
                            primaryText="Repository"
                            leftIcon={<LibraryBooks />}
                            rightIcon={<ArrowDropRight />}
                            autoWidth='false'
                            menuItems={[
                                <Link to="/app/requestRepository" >
                                    <MenuItem
                                        style={style.subMenuItem}
                                        primaryText="Request"
                                        leftIcon={<Add />}
                                    />
                                </Link>,
                            ]}
                        />
                        <MenuItem
                            primaryText="Library"
                            leftIcon={<ContentCopy />}
                            rightIcon={<ArrowDropRight />}
                            menuItems={[
                                <Link to="/app/requestLibrary" >
                                    <MenuItem primaryText="Request" leftIcon={<Add />} />
                                </Link>,
                            ]}
                        />
                    </Menu>
                </Paper>

            </div>
        );
    }
}

export default AdminLeftNav;

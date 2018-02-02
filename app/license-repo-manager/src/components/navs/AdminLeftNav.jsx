import React, { Component } from 'react';
import Paper from 'material-ui/Paper';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import LibraryBooks from 'material-ui/svg-icons/av/library-books';
import ContentCopy from 'material-ui/svg-icons/content/content-copy';
import Description from 'material-ui/svg-icons/action/description';
import { Link } from 'react-router';
import styles from '../../mystyles';

/**
* @class AdminLeftNav
* @extends {Component}
* @description Admin left nav
*/
class AdminLeftNav extends Component {
    /**
    * @class Root
    * @extends {Component}
    * @description Admin left nav render method
    */
    render() {
        return (
            <Paper style={styles.style.paper}>
                <Menu style={styles.menu}>
                    <Link to="/app/requestRepository" >
                        <MenuItem
                            className="icon-menu-wrapper"
                            primaryText="Repository"
                            leftIcon={<LibraryBooks />}
                        />
                    </Link>
                    <Link to="/app/requestLibrary" >
                        <MenuItem
                            className="icon-menu-wrapper"
                            primaryText="Library"
                            leftIcon={<ContentCopy />}
                        />
                    </Link>
                    <Link to="/app/requestLicense" >
                        <MenuItem
                            className="icon-menu-wrapper"
                            primaryText="License"
                            leftIcon={<Description />}
                        />
                    </Link>
                </Menu>
            </Paper>
        );
    }
}

export default AdminLeftNav;

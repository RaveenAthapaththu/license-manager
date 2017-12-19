import React, { Component } from 'react';
import Paper from 'material-ui/Paper';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import LibraryBooks from 'material-ui/svg-icons/av/library-books';
import ContentCopy from 'material-ui/svg-icons/content/content-copy';
import Add from 'material-ui/svg-icons/content/add';
import ArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import { Link } from 'react-router';
import styles from '../../styles';

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
            <div>
                <Paper style={styles.style.paper}>
                    <Menu>
                        <MenuItem
                            className="icon-menu-wrapper"
                            primaryText="Repository"
                            leftIcon={<LibraryBooks />}
                            rightIcon={<ArrowDropRight />}
                            menuItems={[
                                <Link to="/app/requestRepository" >
                                    <MenuItem
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

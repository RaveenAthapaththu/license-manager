import React, { Component } from 'react';
import Paper from 'material-ui/Paper';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import LibraryBooks from 'material-ui/svg-icons/av/library-books';
import ContentCopy from 'material-ui/svg-icons/content/content-copy';
import Description from 'material-ui/svg-icons/action/description';
import ListIcon from 'material-ui/svg-icons/av/featured-play-list';
import SearchIcon from 'material-ui/svg-icons/action/pageview';
import { List, ListItem } from 'material-ui/List';
import { Link } from 'react-router';
import styles from '../../mystyles';

/**
* @class LeftNav
* @extends {Component}
* @description Admin left nav
*/
class LeftNav extends Component {
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
                    <List>
                        <ListItem
                            primaryText="Dependency"
                            leftIcon={<ListIcon />}
                            style={{ paddingRight: 200 }}
                            initiallyOpen={true}// eslint-disable-line
                            primaryTogglesNestedList={true}// eslint-disable-line
                            nestedItems={[
                                <ListItem
                                    key={1}
                                    primaryText="Library"
                                    leftIcon={<SearchIcon />}
                                    containerElement={<Link to="/app/ViewbyLibrary" />}
                                />,
                                <ListItem
                                    key={2}
                                    primaryText="Product/Component"
                                    leftIcon={<SearchIcon />}
                                    containerElement={<Link to="/app/ViewbyProductComponent" />}
                                />,
                            ]}
                        />
                    </List>
                </Menu>
            </Paper>
        );
    }
}

export default LeftNav;

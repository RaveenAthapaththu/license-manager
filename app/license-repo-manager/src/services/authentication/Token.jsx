import { Component } from 'react';
import Cookies from 'universal-cookie';

/**
* @class Token
* @extends {Component}
* @description Get JWT
*/
class Token extends Component {
    /**
    * get main users
    * @returns {Sting} token
    */
    getToken() {
        const cookies = new Cookies();
        const jwt = cookies.get('jwtCook');//eslint-disable-line
        return jwt;
    }
}

export default (new Token());

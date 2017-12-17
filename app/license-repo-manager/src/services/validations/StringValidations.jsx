import { Component } from 'react';

/**
* @class StringValidations
* @extends {Component}
* @description validate strings
*/
class StringValidations extends Component {
    /**
    * escape charachters
    * @param {String} str validated string
    * @returns {String} str
    */
    setStringToShow(str) {
        str = str.replace(/\\\\n/g, '\n');
        str = str.replace(/\\n/g, '\n');
        str = str.replace(/\\'/g, '\'');
        str = str.replace(/\\;/g, ';');
        return str;
    }
    /**
    * escape charachters
    * @param {String} str validated string
    * @returns {String} str
    */
    escapeCharacters(str) {
        str = str.replace(/\n/g, '\\\\n');
        str = str.replace(/'/g, '\\\\\'');
        str = str.replace(/'/g, '\\\'');
        str = str.replace(/;/g, '\\\\;');
        return str;
    }
}

export default (new StringValidations());

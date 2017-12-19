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
        const token = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IlF6UkJOakEyTWprMU5qQkJRa1EzTlVFM016ZzNRa1F5TXpFd01VRTBRVGRCTXpRMFJFSkROUT09In0.eyJpc3MiOiJ3c28yLm9yZy9wcm9kdWN0cy9hcHBtIiwiZXhwIjoxNTEzNDg3NDc1NTE0LCJzdWIiOiJJUy1XU08yLkNPTS9idWRkaGlrQHdzbzIuY29tQHdzbzJpbnRlcm5hbHN0ZyIsIlN1YmplY3QiOiJJUy1XU08yLkNPTS9idWRkaGlrQHdzbzIuY29tQHdzbzJpbnRlcm5hbHN0ZyIsImF1ZCI6WyJMaWNlbnNlQW5kUmVwb3NpdG9yeU1hbmFnZXItd3NvMmludGVybmFsc3RnLTEuMCIsImNhcmJvblNlcnZlciJdLCJodHRwOi8vd3NvMi5vcmcvY2xhaW1zL2VtYWlsYWRkcmVzcyI6ImJ1ZGRoaWtAd3NvMi5jb20iLCJodHRwOi8vd3NvMi5vcmcvY2xhaW1zL3JvbGUiOiJJUy1XU08yLkNPTS93c28yLmludGVybnMsSVMtV1NPMi5DT00vd3NvMi5zaG9ydHRlcm0tZW1wbG95ZWVzLEludGVybmFsL2V2ZXJ5b25lIn0.XIEZzTso836CJduqtupdLbLSRNuNXLnGUN6TGMRas2ZWtbRFAFWAvU4Q5UkkNpbp911j2J6ge0m49WQkcH0MhMs10Mw060xdWpovpigLlVf1TWFV0WuJyezc33F2_cTkjF0Z-Ww_r2db1J7K7ZoEsEzA-JbD5P89z9dLQpPevto';//eslint-disable-line
        const cookies = new Cookies();
        const jwt = cookies.get('jwtCook');//eslint-disable-line
        return token;
    }
}

export default (new Token());

import React from 'react';
import CircularProgress from 'material-ui/CircularProgress';

const style = {
    container: {
        position: 'center',
    },
    refresh: {
        display: 'inline-block',
        position: 'center',
        marginLeft: '50%',
        marginTop: '10%',
    },
};

const LoadingScreen = () => (
    <div style={style.container}>
        <CircularProgress
            size={80}
            thickness={7}
            style={style.refresh}
        />
    </div>
);

export default LoadingScreen;

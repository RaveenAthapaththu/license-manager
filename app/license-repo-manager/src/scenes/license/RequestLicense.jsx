import React, { Component } from 'react';
import { Link } from 'react-router';
import DropToUpload from 'react-drop-to-upload';
import CloudUpload from 'material-ui/svg-icons/file/cloud-upload';
import Card, { CardMedia, CardActions, CardText } from 'material-ui/Card';
import FlatButton from 'material-ui/FlatButton';
import LinearProgress from 'material-ui/LinearProgress';
import RaisedButton from 'material-ui/RaisedButton';
import Snackbar from 'material-ui/Snackbar';
import Pack from '../../services/msf4j/Pack';
import styles from '../../mystyles';
import ValidateUser from '../../services/authentication/ValidateUser';
import zipfile from '../../assets/images/zip-file.png';

/**
* @class WaitingRequests
* @extends {Component}
* @description Get user details
*/
class RequestLicense extends Component {
    /**
    * @class RequestLicense
    * @extends {Component}
    * @param {any} props props for constructor
    * @description Sample React component
    */
    constructor(props) {
        super(props);
        this.state = {
            openUploadModal: false,
            files: [],
            displayUploadIcon: 'block',
            displayUploadedFile: 'none',
            displayProgress: 'none',
            displayBox: 'block',
            displayNext: 'none',
            completed: 100,
            fileName: '',
            file: '',
            open: false,
            openError: false,
            userDetails: [],
        };
        this.handleDrop = this.handleDrop.bind(this);
        this.uploadPack = this.uploadPack.bind(this);
        this.progress = this.progress.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
        this.handleClickError = this.handleClickError.bind(this);
        this.handleRequestCloseError = this.handleRequestCloseError.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
    }
    /**
    * componentWillMount
    */
    componentWillMount() {
        Pack.validateUser();
        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
        });
    }
    /**
    * componentDidMount
    */
    componentDidMount() {
        this.timer = setTimeout(() => this.progress(5), 1);
    }
    /**
    * componentWillUnmount
    */
    componentWillUnmount() {
        clearTimeout(this.timer);
    }
    /**
    * handleClick
    */
    handleClick() {
        this.setState({
            open: true,
        });
    }
    /**
    * handleClickError
    */
    handleClickError() {
        this.setState({
            openError: true,
        });
    }
    /**
    * handleRequestClose
    */
    handleRequestClose() {
        this.setState({
            open: false,
        });
    }
    /**
    * handleRequestClose
    */
    handleRequestCloseError() {
        this.setState({
            openError: false,
        });
    }
    /**
    * progress
    * @param {any} completed files
    */
    progress(completed) {
        if (completed > 100) {
            this.setState({ completed: 100 });
            clearTimeout(this.timer);
        } else {
            this.setState({ completed });
            const diff = 9;
            let progressVal = 0;
            if ((completed + diff) > 90) {
                progressVal = 90;
            } else {
                progressVal = completed + diff;
            }
            this.timer = setTimeout(() => this.progress(progressVal), 6000);
        }
    }
    /**
    * set teams after selecting organization
    * @param {any} files files
    */
    handleDrop(files) {
        this.setState(() => {
            return {
                displayUploadIcon: 'none',
                displayUploadedFile: 'block',
                fileName: files[0].name,
                file: files[0],
            };
        });
    }
    /**
    * reload page
    */
    reloadPage() {
        window.location.reload();
    }
    /**
    * Upload pack
    * @param {any} files files
    */
    uploadPack() {
        this.setState(() => {
            return {
                completed: 0,
                displayBox: 'none',
                displayProgress: 'block',
            };
        });
        Pack.upload(this.state.file).then((response) => {
            if (response.data.responseType === 'Done') {
                this.progress(101);
                this.handleClick();
                this.setState(() => {
                    return {
                        displayNext: 'block',
                        displayBack: 'none',
                    };
                });
            } else {
                clearTimeout(this.timer);
                this.handleClickError();
                this.setState(() => {
                    return {
                        displayBack: 'block',
                        displayNext: 'none',
                    };
                });
            }
        });
    }
    /**
    * @class WaitingRequests
    * @extends {Component}
    * @description Sample React component
    */
    render() {
        return (
            <div className="container-fluid">
                <h2 className="text-center">Requested License</h2>
                <br />
                <div className="row">
                    <div className="col-md-1" />
                    <div className="col-md-10" style={{ display: this.state.displayBox }}>
                        <DropToUpload
                            onDrop={this.handleDrop}
                            style={styles.dragArea}
                            hoverColor="#ffffff"
                            dropAffect="copy"
                        >
                            <div style={{ display: this.state.displayUploadIcon }}>
                                <CloudUpload style={styles.cloudUploadIcon} />
                                <br />
                                <span><strong>Drop pack here to upload</strong></span>
                            </div>
                            <div style={{ display: this.state.displayUploadedFile }}>
                                <Card style={styles.cloudUploadCard} >
                                    <CardMedia
                                        title="Contemplative Reptile"
                                        mediaStyle={styles.cloudUploadCardMedia}
                                    >
                                        <img src={zipfile} style={styles.zipfile} alt="" />
                                    </CardMedia>
                                    <CardText style={{ marginTop: '52%', color: '#000000' }}>
                                        <strong>{this.state.fileName}</strong>
                                    </CardText>
                                    <CardActions>
                                        <FlatButton
                                            label="Save"
                                            style={styles.zipUploadButtonSave}
                                            labelStyle={styles.zipUploadButtonLabelSave}
                                            onClick={this.uploadPack}
                                        />
                                        <FlatButton
                                            label="Discard"
                                            style={styles.zipUploadButtonDiscard}
                                            labelStyle={styles.zipUploadButtonLabelDiscard}
                                            onClick={this.getPath}
                                        />
                                    </CardActions>
                                </Card>
                            </div>
                        </DropToUpload>
                    </div>
                    <div className="col-md-10" style={{ display: this.state.displayProgress }}>
                        <br />
                        <br />
                        <h4>{this.state.fileName}</h4>
                        <LinearProgress
                            mode="determinate"
                            value={this.state.completed}
                            color="#0033cc"
                            style={styles.progressBar}
                        />
                        <br />
                        <div style={{ display: this.state.displayNext }} >
                            <Snackbar
                                open={this.state.open}
                                message="File added successfully. Click Next to generate license"
                                autoHideDuration={40000}
                                onRequestClose={this.handleRequestClose}
                                bodyStyle={styles.snackBar}
                                contentStyle={styles.snackBarContent}
                            />
                            <Link
                                to={{
                                    pathname: '/app/generateLicense',
                                    query: { packName: this.state.fileName, userEmail: this.state.userDetails.userEmail,
                                    } }}
                            >
                                <RaisedButton
                                    type="button"
                                    label="Next"
                                    style={styles.nextButton}
                                    labelColor='#ffffff'
                                    backgroundColor='#2196F3'
                                />
                            </Link>
                        </div>
                        <div style={{ display: this.state.displayBack }} >
                            <Snackbar
                                open={this.state.openError}
                                message="File added fails."
                                autoHideDuration={40000}
                                onRequestClose={this.handleRequestCloseError}
                                bodyStyle={styles.snackBar}
                                contentStyle={styles.snackBarContent}
                            />
                            <RaisedButton
                                type="button"
                                label="Back"
                                style={styles.nextButton}
                                labelColor='#ffffff'
                                backgroundColor='#BDBDBD'
                                onClick={this.reloadPage}
                            />
                        </div>
                    </div>
                    <div className="col-md-1" />
                </div>
            </div>
        );
    }
}

export default RequestLicense;

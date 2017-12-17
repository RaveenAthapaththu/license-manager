import React, { Component } from 'react';
import { Link } from 'react-router';
import ValidateUser from '../../services/authentication/ValidateUser';
import Repository from '../../services/database/Repository';

/**
* @class SearchRepository
* @extends {Component}
* @description Get user details
*/
class SearchRepository extends Component {
    /**
    * @class SearchRepository
    * @extends {Component}
    * @param {any} props props for constructor
    * @description Sample React component
    */
    constructor(props) {
        super(props);
        this.repo = null;
        this.state = {
            repositoryDetails: [],
            userDetails: [],
            repositoryTable: [],
            repositoryTableDefault: [],
            character:props.location.query.character,// eslint-disable-line
        };
        this.searchRequest = this.searchRequest.bind(this);
        this.showErrorBox = this.showErrorBox.bind(this);
        this.movePage = this.movePage.bind(this);
    }
    /**
    * @class SearchRepository
    * @extends {Component}
    * @description Sample React component
    */
    componentWillMount() {
        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
        });
        Repository.selectAll().then((response) => {
            const tableArray = [];
            let field = [];
            let character = response[0].REPOSITORY_NAME[0];
            let repository = {};
            let i = 0;
            let j = 0;
            /* eslint-disable */
            for (i = 0; i < response.length; i++) {
                repository = response[i];
                if (repository.REPOSITORY_NAME[0].toUpperCase() === character.toUpperCase()) {
                    field.push(
                        <tr
                            id={'demo' + character}
                            className={'active demo' + character}
                            key={repository.REPOSITORY_ID}
                        >
                            <td>{repository.REPOSITORY_NAME}</td>
                            <td>{repository.REPOSITORYTYPE_NAME}</td>
                            <td>{repository.ORGANIZATION_NAME}</td>
                            <td>{repository.LICENSE_NAME}</td>
                            <td>{repository.REPOSITORY_LANGUAGE}</td>
                            <td>{(repository.REPOSITORY_NEXUS) ? ' Yes ' : ' No '}</td>
                            <td>{(repository.REPOSITORY_BUILDABLE) ? ' Yes ' : ' No '}</td>
                            <td>{repository.REPOSITORY_REQUEST_BY}</td>
                            <td><Link to={'/app/showRepository?repositoryId=' + repository.REPOSITORY_ID}>More</Link></td>
                        </tr>
                    );
                    
                } else {
                    tableArray.push({characterIndex: character, data: field});
                    character = response[i].REPOSITORY_NAME[0];
                    field = [];
                    field.push(
                        <tr
                            id={'demo' + character}
                            className={'active demo' + character}
                            key={repository.REPOSITORY_ID}
                        >
                            <td>{repository.REPOSITORY_NAME}</td>
                            <td>{repository.REPOSITORYTYPE_NAME}</td>
                            <td>{repository.ORGANIZATION_NAME}</td>
                            <td>{repository.LICENSE_NAME}</td>
                            <td>{repository.REPOSITORY_LANGUAGE}</td>
                            <td>{(repository.REPOSITORY_NEXUS) ? ' Yes ' : ' No '}</td>
                            <td>{(repository.REPOSITORY_BUILDABLE) ? ' Yes ' : ' No '}</td>
                            <td>{repository.REPOSITORY_REQUEST_BY}</td>
                            <td><Link to={'/app/showRepository?repositoryId=' + repository.REPOSITORY_ID}>More</Link></td>
                        </tr>
                    );
                }
            }
            if (this.state.character === 'null') {
                this.setState(() => {
                    return {
                        repositoryTable: tableArray[0].data,
                    };
                });
            } else {
                for (j = 0; j < tableArray.length; j++) {
                    if (tableArray[j].characterIndex.toUpperCase() === this.state.character.toUpperCase()) {
                        this.setState(() => {
                            return {
                                repositoryTable: tableArray[j].data,
                            };
                        });
                    }
                }
            }
            /* eslint-enable */
            this.setState(() => {
                return {
                    repositoryDetails: response,
                    repositoryTableDefault: tableArray,
                };
            });
        });
    }
    /**
    * showErrorBox
    */
    showErrorBox() {
        this.setState(() => {
            return {
                showErrorBox: 'block',
            };
        });
    }
    /**
    * @param {any} e event
    * accept
    */
    movePage(e) {
        const char = e.target.value;
        this.setState(() => {
            return {
                character: char,
            };
        });
    }
    /**
    * @param {any} e event
    * accept
    */
    searchRequest(e) {
        const tableArray = [];
        const field = [];
        const responseDetails = this.state.repositoryDetails;
        const inputRepositoryNameValue = this.inputRepositoryName.value;
        const inputRepositoryName = new RegExp(inputRepositoryNameValue, 'i');
        let i = 0;
        let name;
        let repository;
        let match;
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        if (this.inputRepositoryName.value.length <= 0) {
            this.setState(() => {
                return {
                    repositoryTable: this.state.repositoryTableDefault,
                };
            });
            return;
        }
        for (i = 0; i < responseDetails.length; i++) {
            repository = responseDetails[i];
            name = String(repository.REPOSITORY_NAME);
            if (inputRepositoryName.test(name)) {
                match = name.match(inputRepositoryName);
                name = name.split(inputRepositoryName);
                /* eslint-disable */
                field.push(
                    <tr id={'demo'} className={'active demo'} key={repository.REPOSITORY_ID}>
                        <td>
                            {name.map((namePart,i)=>(i===0) ? <i>{namePart}</i> : <i><strong style={{ color: 'blue' }}>{match}</strong>{namePart}</i>)}
                        </td>
                        <td>{repository.REPOSITORYTYPE_NAME}</td>
                        <td>{repository.ORGANIZATION_NAME}</td>
                        <td>{repository.LICENSE_NAME}</td>
                        <td>{repository.REPOSITORY_LANGUAGE}</td>
                        <td>{(repository.REPOSITORY_NEXUS) ? ' Yes ' : ' No '}</td>
                        <td>{(repository.REPOSITORY_BUILDABLE) ? ' Yes ' : ' No '}</td>
                        <td>{repository.REPOSITORY_REQUEST_BY}</td>
                        <td><Link to={'/app/showRepository?repositoryId=' + repository.REPOSITORY_ID}>More</Link></td>
                    </tr>
                );
                /* eslint-enable */
            }
        }
        this.setState(() => {
            return {
                repositoryTable: tableArray,
            };
        });
    }
    /**
    * @class SearchRepository
    * @extends {Component}
    * @description Sample React component
    */
    render() {
        return (
            <div className="container-fluid" onSubmit={this.searchRequest}>
                <div className="row" style={{ display: this.state.displayFieldset }}>
                    <div className="row">
                        <br /><br />
                        <div className="form-group">
                            <div className="row">
                                <div className="col-sm-3" />
                                <div className="col-sm-6">
                                    <input
                                        onChange={this.searchRequest}
                                        type="text"
                                        className="form-control"
                                        ref={(c) => { this.inputRepositoryName = c; }}
                                        id="inputRepositoryName"
                                        placeholder="Enter repository name to search"
                                    />
                                </div>
                                <div className="col-sm-3" />
                            </div>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-sm-12">
                            <table className="table table-striped table-hover ">
                                <thead>
                                    <tr className="info">
                                        <th style={{ width: '160px' }}>Name</th>
                                        <th>Type</th>
                                        <th>Organization</th>
                                        <th>License</th>
                                        <th>Language</th>
                                        <th>Nexus</th>
                                        <th>Jenkins</th>
                                        <th>Requested By</th>
                                        <th>More Details</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {/* eslint-disable */}
                                    {(this.state.repositoryTable.length > 0) ? this.state.repositoryTable.map((repository)=>
                                        repository
                                    ):""}
                                    {/* eslint-enable */}
                                </tbody>
                            </table>
                            <br />
                            <ul className="pagination">
                                <li>
                                    <button
                                        onClick={this.movePage}
                                        ref={(c) => { this.inputPrivate = c; }}
                                        value="b"
                                    >
                                        b
                                    </button>
                                </li>
                                <li>
                                    <button
                                        onClick={this.movePage}
                                        ref={(c) => { this.inputPrivate = c; }}
                                        value="c"
                                    >
                                        c
                                    </button>
                                </li>
                                <li><Link to={'/app/searchRepository?character=l'} >l</Link></li>
                                <li><Link to={'/app/searchRepository?character=k'} >k</Link></li>
                                <li><Link to={'/app/searchRepository?character=c'} >c</Link></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default SearchRepository;

var configs = {
    "databaseService": [
        {"host": "https://localhost:9099/"},
        {"basePath":"LMDependencyManager/"},
        {"resourcePaths":[
            {"libraryNames":"library/names"},
            {"libraryVersions":"library/versions/"},
            {"libraryGroupIDArtifactID":"library/artifactIDgroupID/"},
            {"productsAndComponentsUsingLibrary":"library/productsandcomponents/"},
            {"productComponentNames":"productsandcomponents/names"},
            {"componentDetails":"component/details/"},
            {"librariesofComponent":"component/libraries/"},
            {"componentVersion":"component/versions/"},
            {"librariesofProduct":"product/libraries/"},
            {"productVersion":"product/versions/"}
        ]}
    ],
    "routingService": [
        {"host": "https://localhost:9091/"},
        {"basePath":"dependencyManager/"},
        {"resourcePaths":[
            {"requestResolve":"router/"}
        ]}
    ]
};

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.internal.apps.license.manager.client.utils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;
import org.wso2.internal.apps.license.manager.client.exception.LicenseManagerException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Util functions which are required while executing the backend services for license manager.
 */
public class ServiceUtils {

    private static final Logger log = Logger.getLogger(ServiceUtils.class);

    /**
     * Create a trusted http client to initiate a secure connection with micro services.
     *
     * @return closeableHttpClient
     * @throws LicenseManagerException if the connection initiation fails
     */
    public static CloseableHttpClient createTrustedHttpClient() throws LicenseManagerException {

        PropertyReader properties = new PropertyReader();

        // Setting up authentication.
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(properties.getBackendUsername(),
                properties.getBackendPassword());
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Get the keystore file.
        InputStream file = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(properties.getTrustStoreServiceName());
        try {
            // Make the trusted connection.
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(file, properties.getTrustStoreServicePassword().toCharArray());
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, null).build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
            httpClientBuilder.setSSLSocketFactory(sslSocketFactory);
            if (log.isDebugEnabled()) {
                log.debug("A secure connection is established with the micro service. ");
            }
            return httpClientBuilder.setDefaultCredentialsProvider(provider).build();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                KeyManagementException e) {
            throw new LicenseManagerException("Failed to initiate the connection. ", e);
        }

    }
}

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
import org.wso2.internal.apps.license.manager.client.exception.LicenseManagerException;
import org.wso2.internal.apps.license.manager.client.msf4jhttp.PropertyReader;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

public class ServiceUtils {

    public static CloseableHttpClient createTrustedHttpClient() throws LicenseManagerException {

        PropertyReader properties = new PropertyReader();

        // Setting up authentication.
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(properties.getBackendUsername(),
                properties.getBackendPassword());
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        // Create a trusted Https client.
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        InputStream file = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(properties.getTrustStoreServiceName());
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(file, properties.getTrustStoreServicePassword().toCharArray());

            HostnameVerifier allowAllHosts = new NoopHostnameVerifier(); //comment this
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, null).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts); //remove second parameter
            httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);

            return httpClientBuilder.setDefaultCredentialsProvider(provider).build();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            throw new LicenseManagerException("Failed to initiate the connection. ", e);
        }

    }
}

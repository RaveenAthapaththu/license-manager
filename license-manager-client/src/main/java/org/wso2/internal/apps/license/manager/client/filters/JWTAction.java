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

package org.wso2.internal.apps.license.manager.client.filters;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.log4j.Logger;
import org.wso2.internal.apps.license.manager.client.utils.PropertyReader;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is for handling sso configuration.
 */
public class JWTAction implements Filter {

    private static final Logger log = Logger.getLogger(JWTAction.class);
    private static final PropertyReader propertyReader = new PropertyReader();

    /**
     * This method is for get public key
     *
     * @return return for getting public key
     * @throws IOException              if unable to load the file
     * @throws KeyStoreException        if unable to get instance
     * @throws CertificateException     if unable to certify
     * @throws NoSuchAlgorithmException cause by other underlying exceptions(KeyStoreException)
     */
    private static PublicKey getPublicKey() throws IOException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException {

        InputStream file = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(propertyReader.getSsoKeyStoreName());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, propertyReader.getSsoKeyStorePassword().toCharArray());
        Certificate cert = keystore.getCertificate(propertyReader.getSsoCertAlias());
        return cert.getPublicKey();
    }

    public void init(FilterConfig filterConfig) {
        // Do nothing
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String jwt = request.getHeader("X-JWT-Assertion");
        String ssoRedirectUrl = propertyReader.getSsoRedirectUrl();

        if (jwt == null || "".equals(jwt)) {
            if (log.isDebugEnabled()) {
                log.debug("Redirecting to {}");
            }
            response.sendRedirect(ssoRedirectUrl);
            return;
        }

        String username = null;
        String roles = null;

        try {

            SignedJWT signedJWT = SignedJWT.parse(jwt);
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) getPublicKey());

            if (signedJWT.verify(verifier)) {
                if (log.isDebugEnabled()) {
                    log.debug("JWT validation success for token: {}");
                }
                username = signedJWT.getJWTClaimsSet().getClaim("http://wso2.org/claims/emailaddress").toString();
                roles = signedJWT.getJWTClaimsSet().getClaim("http://wso2.org/claims/role").toString();
                if (log.isDebugEnabled()) {
                    log.debug("User = {" + username + "} | Roles = " + roles);
                }
            } else {
                log.error("JWT validation failed for token: {" + jwt + "}");
                response.sendRedirect(ssoRedirectUrl);
                return;
            }
        } catch (ParseException e) {
            log.error("Parsing JWT token failed");
        } catch (JOSEException e) {
            log.error("Verification of jwt failed");
        } catch (Exception e) {
            log.error("Failed to validate the jwt {" + jwt + "}");
        }

        if (username != null && roles != null) {
            request.getSession().setAttribute("user", username);
            request.getSession().setAttribute("roles", roles);
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (ServletException e) {
            log.error("Failed to pass the request, response objects through filters", e);
        }
    }

    public void destroy() {
        // Do nothing
    }
}

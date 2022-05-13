/*
 * Copyright 2022 learn-netty4 Project
 *
 * The learn-netty4 Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.flydean68.ocspstapling;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.operator.DigestCalculator;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 使用BC的OCSPReqBuilder来构建Request
 */

public class OcspRequestBuilder {

    private static final SecureRandom GENERATOR = new SecureRandom();

    private final SecureRandom generator = GENERATOR;

    private final DigestCalculator calculator = new SHA1DigestCalculator();

    private X509Certificate certificate;

    private X509Certificate issuer;


    public OcspRequestBuilder certificate(X509Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    public OcspRequestBuilder issuer(X509Certificate issuer) {
        this.issuer = issuer;
        return this;
    }

    public OCSPReq build() throws OCSPException, IOException, CertificateEncodingException {
        SecureRandom generator = checkNotNull(this.generator, "generator");
        DigestCalculator calculator = checkNotNull(this.calculator, "calculator");
        X509Certificate certificate = checkNotNull(this.certificate, "certificate");
        X509Certificate issuer = checkNotNull(this.issuer, "issuer");

        BigInteger serial = certificate.getSerialNumber();

        CertificateID certId = new CertificateID(calculator,
                new X509CertificateHolder(issuer.getEncoded()), serial);

        OCSPReqBuilder builder = new OCSPReqBuilder();
        builder.addRequest(certId);

        byte[] nonce = new byte[8];
        generator.nextBytes(nonce);

        Extension[] extensions = new Extension[]{
                new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false,
                        new DEROctetString(nonce))};

        builder.setRequestExtensions(new Extensions(extensions));

        return builder.build();
    }
}

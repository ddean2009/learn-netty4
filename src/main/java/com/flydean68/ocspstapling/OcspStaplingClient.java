
package com.flydean68.ocspstapling;

import io.netty.util.CharsetUtil;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 需要证书和private key
 */
@SuppressWarnings("unused")
public class OcspStaplingClient {
    public static void main(String[] args) throws Exception {

        // 加载CA证书链，证书链中只有两个证书，第二个是根证书，根证书中包含OCSP的信息
        X509Certificate[] keyCertChain = parseCertificates(OcspStaplingClient.class, "netty_io_chain.pem");

        X509Certificate certificate = keyCertChain[0];
        X509Certificate issuer = keyCertChain[keyCertChain.length - 1];

        //从根证书中取OCSP信息
        URI uri = OcspUtils.ocspUri(certificate);
        System.out.println("OCSP Responder URI: " + uri);

        if (uri == null) {
            throw new IllegalStateException("The CA/certificate doesn't have an OCSP responder");
        }

        // 创建OCSPReq
        OCSPReq request = new OcspRequestBuilder()
                .certificate(certificate)
                .issuer(issuer)
                .build();

        // 向OCSP responder请求数据
        OCSPResp response = OcspUtils.request(uri, request, 5L, TimeUnit.SECONDS);
        if (response.getStatus() != OCSPResponseStatus.SUCCESSFUL) {
            throw new IllegalStateException("response-status=" + response.getStatus());
        }

        BasicOCSPResp basicResponse = (BasicOCSPResp) response.getResponseObject();
        SingleResp first = basicResponse.getResponses()[0];

        CertificateStatus status = first.getCertStatus();
        System.out.println("Status: " + (status == CertificateStatus.GOOD ? "Good" : status));
        System.out.println("This Update: " + first.getThisUpdate());
        System.out.println("Next Update: " + first.getNextUpdate());

        if (status != null) {
            throw new IllegalStateException("certificate-status=" + status);
        }

        BigInteger certSerial = certificate.getSerialNumber();
        BigInteger ocspSerial = first.getCertID().getSerialNumber();
        if (!certSerial.equals(ocspSerial)) {
            throw new IllegalStateException("Bad Serials=" + certSerial + " vs. " + ocspSerial);
        }
    }

    private static X509Certificate[] parseCertificates(Class<?> clazz, String name) throws Exception {
        InputStream in = clazz.getResourceAsStream(name);
        if (in == null) {
            throw new FileNotFoundException("clazz=" + clazz + ", name=" + name);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, CharsetUtil.US_ASCII))) {
                return parseCertificates(reader);
        } finally {
            in.close();
        }
    }

    private static X509Certificate[] parseCertificates(Reader reader) throws Exception {

        JcaX509CertificateConverter converter = new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider());

        List<X509Certificate> dst = new ArrayList<>();

        try (PEMParser parser = new PEMParser(reader)) {
            X509CertificateHolder holder;

            while ((holder = (X509CertificateHolder) parser.readObject()) != null) {
                X509Certificate certificate = converter.getCertificate(holder);
                if (certificate == null) {
                    continue;
                }

                dst.add(certificate);
            }
        }

        return dst.toArray(new X509Certificate[0]);
    }
}

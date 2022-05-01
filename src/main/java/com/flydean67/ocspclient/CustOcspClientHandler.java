package com.flydean67.ocspclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.ocsp.OcspClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;

import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.math.BigInteger;

@Slf4j
public class CustOcspClientHandler extends OcspClientHandler {

    CustOcspClientHandler(ReferenceCountedOpenSslEngine engine) {
        super(engine);
    }

    @Override
    protected boolean verify(ChannelHandlerContext ctx, ReferenceCountedOpenSslEngine engine) throws Exception {
        byte[] staple = engine.getOcspResponse();
        if (staple == null) {
            throw new IllegalStateException("Server didn't provide an OCSP staple!");
        }

        OCSPResp response = new OCSPResp(staple);
        if (response.getStatus() != OCSPResponseStatus.SUCCESSFUL) {
            return false;
        }

        SSLSession session = engine.getSession();
        X509Certificate[] chain = session.getPeerCertificateChain();
        BigInteger certSerial = chain[0].getSerialNumber();

        BasicOCSPResp basicResponse = (BasicOCSPResp) response.getResponseObject();
        SingleResp first = basicResponse.getResponses()[0];

        CertificateStatus status = first.getCertStatus();
        BigInteger ocspSerial = first.getCertID().getSerialNumber();
        String message = "OCSP status of " + ctx.channel().remoteAddress() +
                "\n  Status: " + (status == CertificateStatus.GOOD ? "Good" : status) +
                "\n  This Update: " + first.getThisUpdate() +
                "\n  Next Update: " + first.getNextUpdate() +
                "\n  Cert Serial: " + certSerial +
                "\n  OCSP Serial: " + ocspSerial;
        log.info("verify message:{}",message);

        return status == CertificateStatus.GOOD && certSerial.equals(ocspSerial);
    }
}


package com.flydean68.ocspstapling;

import io.netty.util.CharsetUtil;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public final class OcspUtils {

    private static final ASN1ObjectIdentifier OCSP_RESPONDER_OID
        = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1").intern();

    private static final String OCSP_REQUEST_TYPE = "application/ocspclient-request";

    private static final String OCSP_RESPONSE_TYPE = "application/ocspclient-response";

    private OcspUtils() {
    }

    /**
     * Returns the OCSP responder {@link URI} or {@code null} if it doesn't have one.
     */
    public static URI ocspUri(X509Certificate certificate) throws IOException {
        byte[] value = certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (value == null) {
            return null;
        }

        ASN1Primitive authorityInfoAccess = JcaX509ExtensionUtils.parseExtensionValue(value);
        if (!(authorityInfoAccess instanceof DLSequence)) {
            return null;
        }

        DLSequence aiaSequence = (DLSequence) authorityInfoAccess;
        DERTaggedObject taggedObject = findObject(aiaSequence, OCSP_RESPONDER_OID, DERTaggedObject.class);
        if (taggedObject == null) {
            return null;
        }

        if (taggedObject.getTagNo() != BERTags.OBJECT_IDENTIFIER) {
            return null;
        }

        byte[] encoded = taggedObject.getEncoded();
        int length = (int) encoded[1] & 0xFF;
        String uri = new String(encoded, 2, length, CharsetUtil.UTF_8);
        return URI.create(uri);
    }

    private static <T> T findObject(DLSequence sequence, ASN1ObjectIdentifier oid, Class<T> type) {
        for (ASN1Encodable element : sequence) {
            if (!(element instanceof DLSequence)) {
                continue;
            }
            DLSequence subSequence = (DLSequence) element;
            if (subSequence.size() != 2) {
                continue;
            }
            ASN1Encodable key = subSequence.getObjectAt(0);
            ASN1Encodable value = subSequence.getObjectAt(1);

            if (key.equals(oid) && type.isInstance(value)) {
                return type.cast(value);
            }
        }

        return null;
    }

    public static OCSPResp request(URI uri, OCSPReq request, long timeout, TimeUnit unit) throws IOException {
        byte[] encoded = request.getEncoded();

        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setConnectTimeout((int) unit.toMillis(timeout));
            connection.setReadTimeout((int) unit.toMillis(timeout));
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("host", uri.getHost());
            connection.setRequestProperty("content-type", OCSP_REQUEST_TYPE);
            connection.setRequestProperty("accept", OCSP_RESPONSE_TYPE);
            connection.setRequestProperty("content-length", String.valueOf(encoded.length));

            try (OutputStream out = connection.getOutputStream()) {
                out.write(encoded);
                out.flush();

                try (InputStream in = connection.getInputStream()) {
                    int code = connection.getResponseCode();
                    if (code != HttpsURLConnection.HTTP_OK) {
                        throw new IOException("Unexpected status-code=" + code);
                    }

                    String contentType = connection.getContentType();
                    if (!contentType.equalsIgnoreCase(OCSP_RESPONSE_TYPE)) {
                        throw new IOException("Unexpected content-type=" + contentType);
                    }

                    int contentLength = connection.getContentLength();
                    if (contentLength == -1) {
                        contentLength = Integer.MAX_VALUE;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        byte[] buffer = new byte[8192];
                        int length;

                        while ((length = in.read(buffer)) != -1) {
                            baos.write(buffer, 0, length);

                            if (baos.size() >= contentLength) {
                                break;
                            }
                        }
                    } finally {
                        baos.close();
                    }
                    return new OCSPResp(baos.toByteArray());
                }
            }
        } finally {
            connection.disconnect();
        }
    }
}

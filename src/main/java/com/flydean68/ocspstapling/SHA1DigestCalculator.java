package com.flydean68.ocspstapling;

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.operator.DigestCalculator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * 从BcX509ExtensionUtils中拷贝的SHA1DigestCalculator
 */
public class SHA1DigestCalculator implements DigestCalculator {
    private ByteArrayOutputStream bOut;

    public SHA1DigestCalculator() {
        this.bOut = new ByteArrayOutputStream();
    }

    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
    }

    public OutputStream getOutputStream() {
        return this.bOut;
    }

    public byte[] getDigest() {
        byte[] var1 = this.bOut.toByteArray();
        this.bOut.reset();
        SHA1Digest var2 = new SHA1Digest();
        var2.update(var1, 0, var1.length);
        byte[] var3 = new byte[var2.getDigestSize()];
        var2.doFinal(var3, 0);
        return var3;
    }
}

package security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

/**
 *
 * @author fazo
 */
public class Signer {

    private PrivateKey key;
    private PublicKey certificate;
    private Signature signer;

    public Signer() {
        this("DSA", "SHA1PRNG", "SHA1withDSA");
    }

    public Signer(PrivateKey key) {
        certificate = null;
        this.key = key;
        initSigner("SHA1withDSA");
    }

    public Signer(PrivateKey key, PublicKey certificate) {
        this(key);
        this.certificate = certificate;
    }

    public Signer(String keyAlgorithm, String randomAlgorithm, String signatureAlgorithm) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-4);
        }
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance(randomAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-5);
        }
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.genKeyPair();
        key = pair.getPrivate();
        initSigner(signatureAlgorithm);
    }

    private void initSigner(String signatureAlgorithm) {
        try {
            signer = Signature.getInstance(signatureAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-6);
        }
    }

    public String sign(String data) {
        return null;
    }

    public String encrypt(String data) {
        return null;
    }

    public PrivateKey getKey() {
        return key;
    }

    public PublicKey getCertificate() {
        return certificate;
    }

    public Signature getSigner() {
        return signer;
    }

}

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

/**
 * This class provides Digital Signnature creation and data Encryption methods
 *
 * @author fazo
 */
public class Signer {

    private PrivateKey key; // the Private Key
    private PublicKey certificate; // the Public Key (if given)
    private Signature signer; // the Signature object

    /**
     * Initializes a new Signer with default settings and using a new key pair
     */
    public Signer() {
        this("DSA", "SHA1PRNG", "SHA1withDSA");
    }

    /**
     * Initializes a new Signer with default settings using the given private
     * key (public key is optional)
     *
     * @param key the private key to use to sign and encrypt
     */
    public Signer(PrivateKey key) {
        certificate = null;
        this.key = key;
        initSigner("SHA1withDSA");
    }

    /**
     * Creates a new Signer using given key pair and default settings
     *
     * @param key
     * @param certificate
     */
    public Signer(PrivateKey key, PublicKey certificate) {
        this(key);
        this.certificate = certificate;
    }

    /**
     * Creates a Signer with new key pair using given algorithms
     *
     * @param keyAlgorithm the algorithm of the keys
     * @param randomAlgorithm the secure random algorithm
     * @param signatureAlgorithm the signature algorithm
     */
    private Signer(String keyAlgorithm, String randomAlgorithm, String signatureAlgorithm) {
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

    /**
     * Initializes the signer object
     *
     * @param signatureAlgorithm algorithm to use
     */
    private void initSigner(String signatureAlgorithm) {
        try {
            signer = Signature.getInstance(signatureAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-6);
        }
    }

    /**
     * Sign the given data and return signature
     *
     * @param data data to sign
     * @return signature
     */
    public String sign(String data) {
        return null;
    }

    /**
     * Encrypt given data
     *
     * @param data the data to encrypt
     * @return encrypted data
     */
    public String encrypt(String data) {
        return null;
    }

    /**
     * Get the private key for this Signer
     *
     * @return PrivateKey object
     */
    public PrivateKey getKey() {
        return key;
    }

    /**
     * Get the public key for this Signer
     *
     * @return PublicKey object
     */
    public PublicKey getCertificate() {
        return certificate;
    }

}

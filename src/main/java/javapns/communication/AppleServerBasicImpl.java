package javapns.communication;

import javapns.communication.exceptions.InvalidKeystoreReferenceException;
import javapns.communication.exceptions.KeystoreException;

import java.io.InputStream;

/**
 * A basic and abstract implementation of the AppleServer interface
 * intended to facilitate rapid deployment.
 *
 * @author Sylvain Pedneault
 */
public abstract class AppleServerBasicImpl implements AppleServer {
    private final String password;
    private final String type;
    private Object keystore;
    private String proxyHost;
    private int proxyPort;

    /**
     * Constructs a AppleServerBasicImpl object.
     *
     * @param keystore The keystore to use (can be a File, an InputStream, a String for a file path, or a byte[] array)
     * @param password The keystore's password
     * @param type     The keystore type (typically PKCS12)
     * @throws KeystoreException thrown if an error occurs when loading the keystore
     */
    protected AppleServerBasicImpl(final Object keystore, final String password, final String type) throws KeystoreException {
        KeystoreManager.validateKeystoreParameter(keystore);
        this.keystore = keystore;
        this.password = password;
        this.type = type;

    /* Make sure that the keystore reference is reusable. */
        this.keystore = KeystoreManager.ensureReusableKeystore(this, this.keystore);
    }

    public InputStream getKeystoreStream() throws InvalidKeystoreReferenceException {
        return KeystoreManager.streamKeystore(keystore);
    }

    public String getKeystorePassword() {
        return password;
    }

    public String getKeystoreType() {
        return type;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxy(final String proxyHost, final int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
}

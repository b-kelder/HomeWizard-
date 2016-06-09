package idu.stenden.inf1i.homewizard;
/*
    From: http://rijware.com/accessing-a-secure-mqtt-broker-with-android
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.util.Log;

import org.spongycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.io.pem.*;


public class SslUtil {

    public static String PASSWORD = "everyoneloveshydra";

    private static SslUtil		mInstance = null;
    private Context					mContext = null;
    private HashMap<Integer, SSLSocketFactory> mSocketFactoryMap = new HashMap<Integer, SSLSocketFactory>();

    public SslUtil(Context context) {
        mContext = context;
    }

    public static SslUtil getInstance( ) {
        if ( null == mInstance ) {
            throw new RuntimeException("first call must be to SslUtility.newInstance(Context) ");
        }
        return mInstance;
    }

    public static SslUtil newInstance( Context context ) {
        if ( null == mInstance ) {
            mInstance = new SslUtil( context );
        }
        return mInstance;
    }

    /*public SSLSocketFactory getSocketFactory (final String caCrtFile, final String crtFile, final String keyFile,
                                              final String password) throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
        X509Certificate caCert = (X509Certificate)reader.readObject();
        reader.close();

        // load client certificate
        reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
        X509Certificate cert = (X509Certificate)reader.readObject();
        reader.close();

        // load client private key
        reader = new PEMReader(
                new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(keyFile)))),
                new PasswordFinder() {
                    @Override
                    public char[] getPassword() {
                        return password.toCharArray();
                    }
                }
        );
        KeyPair key = (KeyPair)reader.readObject();
        reader.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }*/

    public SSLSocketFactory getSocketFactory(int certificateId, String certificatePassword ) {

        SSLSocketFactory result = mSocketFactoryMap.get(certificateId);  	// check to see if already created

        if ( ( null == result) && ( null != mContext ) ) {					// not cached so need to load server certificate

            try {
                /*KeyStore keystoreTrust = KeyStore.getInstance("BKS");		// Bouncy Castle

                keystoreTrust.load(mContext.getResources().openRawResource(certificateId), certificatePassword.toCharArray());

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                trustManagerFactory.init(keystoreTrust);*/

                Security.addProvider(new BouncyCastleProvider());

                // load CA certificate
                /*PemReader reader = new PemReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
                X509Certificate caCert = (X509Certificate)reader.readPemObject();
                reader.close();*/
                java.security.cert.CertificateFactory certificateFactory = java.security.cert.CertificateFactory.getInstance("X.509");
                InputStream inputStream = mContext.getResources().openRawResource(certificateId);
                X509Certificate caCert = (X509Certificate)certificateFactory.generateCertificate(inputStream);
                inputStream.close();

                // CA certificate is used to authenticate server
                KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
                caKs.load(null, null);
                caKs.setCertificateEntry("ca-certificate", caCert);
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(caKs);

                SSLContext sslContext = SSLContext.getInstance("TLSv1");
                sslContext.init(null, tmf.getTrustManagers(), null);
                result = sslContext.getSocketFactory();

                mSocketFactoryMap.put( certificateId, result);	// cache for reuse
            }
            catch ( Exception ex ) {
                // log exception
                Log.e("SslUtil", ex.toString());
            }
        }

        return result;
    }

}
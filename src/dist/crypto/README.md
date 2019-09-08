Place your crypto assets and Java Keystore file (java-keystore.jks) in this directory.

[Follow the docs here for creating a certificate and private key](https://developer.amazon.com/docs/custom-skills/configure-web-service-self-signed-certificate.html#create-a-private-key-and-self-signed-certificate-for-testing)

Use the following `openssl` command to create a PKCS #12 archive file from your private key and certificate. Replace the `private-key.pem` and `certificate.pem` values shown here with the filenames for your key and certificate. Specify a password for the archive when prompted.

```
openssl pkcs12 -keypbe PBE-SHA1-3DES \
               -certpbe PBE-SHA1-3DES \
               -inkey private-key.pem \
               -in certificate.pem \
               -export \
               -out keystore.pkcs12
```

Use the following keytool command to import the PKCS #12 file into a Java KeyStore, specifying a password for both the destination KeyStore and source PKCS #12 archive:

```
$JAVA_HOME/bin/keytool -importkeystore \
           -destkeystore java-keystore.jks \
           -srckeystore keystore.pkcs12 \
           -srcstoretype PKCS12
```

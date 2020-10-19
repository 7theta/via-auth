#!/usr/bin/env bash

# For more details, check out https://github.com/Hakky54/mutual-tls-ssl
#
# curl e.g.
# https://gist.github.com/Hakky54/049299f0874fd4b870257c6458e0dcbd

createCertificatesTrustEachOther() {
    echo 'Starting to create certificates...'

    mkdir -p server/extensions
    mkdir -p root-ca
    mkdir -p client
    cp v3.ext server/extensions/v3.ext

    keytool -genkeypair -keyalg RSA -keysize 2048 -alias server -dname "CN=someone,OU=Somewhere,O=Someplace,C=NL" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore server/identity.jks -storepass secret -keypass secret -deststoretype pkcs12
    keytool -genkeypair -keyalg RSA -keysize 2048 -alias client -dname "CN=$1,OU=Somewhere,O=Someplace,C=NL" -validity 3650 -keystore client/identity.jks -storepass secret -keypass secret -deststoretype pkcs12
    keytool -exportcert -keystore client/identity.jks -storepass secret -alias client -rfc -file client/client.cer
    keytool -exportcert -keystore server/identity.jks -storepass secret -alias server -rfc -file server/server.cer
    keytool -keystore client/truststore.jks -importcert -file server/server.cer -alias server -storepass secret -noprompt
    keytool -keystore server/truststore.jks -importcert -file client/client.cer -alias client -storepass secret -noprompt

    keytool -importkeystore -srckeystore server/truststore.jks -destkeystore server/truststore.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass secret -deststorepass secret -noprompt
    openssl pkcs12 -in server/truststore.p12 -out server/truststore.pem -passin pass:secret -passout pass:secret
    keytool -importkeystore -srckeystore client/identity.jks -destkeystore client/identity.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass secret -deststorepass secret -noprompt
    openssl pkcs12 -in client/identity.p12 -out client/identity.pem -passin pass:secret -passout pass:secret

}

createCertificatesTrustRootCA() {
    echo 'Starting to create certificates...'

    mkdir -p server/extensions
    mkdir -p root-ca
    mkdir -p client

    cp v3.ext server/extensions/v3.ext

    keytool -genkeypair -keyalg RSA -keysize 2048 -alias root-ca -dname "CN=7t,OU=Ontario,O=Ottawa,C=NL" -validity 3650 -keystore root-ca/identity.jks -storepass secret -keypass secret -deststoretype pkcs12
    keytool -genkeypair -keyalg RSA -keysize 2048 -alias server -dname "CN=7t,OU=Ontario,O=Ottawa,C=NL" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore server/identity.jks -storepass secret -keypass secret -deststoretype pkcs12
    keytool -genkeypair -keyalg RSA -keysize 2048 -alias client -dname "CN=$1,OU=Ontario,O=Ottawa,C=NL" -validity 3650 -keystore client/identity.jks -storepass secret -keypass secret -deststoretype pkcs12
    keytool -exportcert -keystore client/identity.jks -storepass secret -alias client -rfc -file client/client.cer
    keytool -exportcert -keystore server/identity.jks -storepass secret -alias server -rfc -file server/server.cer
    keytool -certreq -keystore server/identity.jks -alias server -keypass secret -storepass secret -keyalg rsa -file server/server.csr
    keytool -certreq -keystore client/identity.jks -alias client -keypass secret -storepass secret -keyalg rsa -file client/client.csr
    keytool -importkeystore -srckeystore root-ca/identity.jks -destkeystore root-ca/root-ca.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret
    openssl pkcs12 -in root-ca/root-ca.p12 -out root-ca/root-ca.pem -nokeys -passin pass:secret -passout pass:secret
    openssl pkcs12 -in root-ca/root-ca.p12 -out root-ca/root-ca.key -nocerts -passin pass:secret -passout pass:secret
    openssl x509 -req -in client/client.csr -CA root-ca/root-ca.pem -CAkey root-ca/root-ca.key -CAcreateserial -out client/client-signed.cer -days 1825 -passin pass:secret
    openssl x509 -req -in server/server.csr -CA root-ca/root-ca.pem -CAkey root-ca/root-ca.key -CAcreateserial -out server/server-signed.cer -sha256 -extfile server/extensions/v3.ext -days 1825  -passin pass:secret
    keytool -importkeystore -srckeystore client/identity.jks -destkeystore client/client.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret
    openssl pkcs12 -in client/client.p12 -nodes -out client/client-private.key -nocerts -passin pass:secret
    openssl pkcs12 -export -in client/client-signed.cer -inkey client/client-private.key -out client/client-signed.p12 -name client -passout pass:secret
    keytool -delete -alias client -keystore client/identity.jks -storepass secret
    keytool -importkeystore -srckeystore client/client-signed.p12 -srcstoretype PKCS12 -destkeystore client/identity.jks -srcstorepass secret -deststorepass secret
    keytool -importkeystore -srckeystore server/identity.jks -destkeystore server/server.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret
    openssl pkcs12 -in server/server.p12 -nodes -out server/server-private.key -nocerts -passin pass:secret
    openssl pkcs12 -export -in server/server-signed.cer -inkey server/server-private.key -out server/server-signed.p12 -name server -passout pass:secret
    keytool -delete -alias server -keystore server/identity.jks -storepass secret
    keytool -importkeystore -srckeystore server/server-signed.p12 -srcstoretype PKCS12 -destkeystore server/identity.jks -srcstorepass secret -deststorepass secret
    keytool -keystore client/truststore.jks -importcert -file root-ca/root-ca.pem -alias root-ca -storepass secret -noprompt
    keytool -keystore server/truststore.jks -importcert -file root-ca/root-ca.pem -alias root-ca -storepass secret -noprompt

    keytool -importkeystore -srckeystore server/truststore.jks -destkeystore server/truststore.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass secret -deststorepass secret -noprompt
    openssl pkcs12 -in server/truststore.p12 -out server/truststore.pem -passin pass:secret -passout pass:secret

    keytool -importkeystore -srckeystore client/identity.jks -destkeystore client/identity.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass secret -deststorepass secret -noprompt
    openssl pkcs12 -in client/identity.p12 -out client/identity.pem -passin pass:secret -passout pass:secret

    keytool -importkeystore -srckeystore root-ca/identity.jks -destkeystore root-ca/identity.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass secret -deststorepass secret -noprompt
    openssl pkcs12 -in root-ca/identity.p12 -out root-ca/identity.pem -passin pass:secret -passout pass:secret

}

#Validate if provided argument is present
if [[ -z "$1" ]]; then
    echo "Provide a Common Name (CN) as the first argument. e.g. ./gencerts blackhole"
else
    ./cleanupcerts.sh
    createCertificatesTrustEachOther "$1"

    printf "\nSample usage (after restarting via server):\ncurl -v --insecure --cert client/identity.pem --pass secret --cacert client/truststore.pem https://localhost:3450/\n\n"
fi

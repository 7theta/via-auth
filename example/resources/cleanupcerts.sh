#!/usr/bin/env bash

echo 'Cleaning up existing certificates and keystores'

rm -fv client/client.cer
rm -fv client/client.csr
rm -fv client/client.p12
rm -fv client/client-private.key
rm -fv client/client-signed.cer
rm -fv client/client-signed.p12
rm -fv client/identity.jks
rm -fv client/identity.p12
rm -fv client/identity.pem
rm -fv client/truststore.jks
rm -fv root-ca/root-ca.key
rm -fv root-ca/root-ca.p12
rm -fv root-ca/root-ca.pem
rm -fv root-ca/root-ca.srl
rm -fv root-ca/identity.jks
rm -fv root-ca/identity.p12
rm -fv root-ca/identity.pem
rm -fv server/identity.jks
rm -fv server/server.cer
rm -fv server/server.csr
rm -fv server/server.p12
rm -fv server/server-private.key
rm -fv server/server-signed.cer
rm -fv server/server-signed.p12
rm -fv server/truststore.jks
rm -fv server/truststore.p12
rm -fv server/truststore.pem
rm -rfv server/extensions

rm -rfv server
rm -rfv client
rm -rfv root-ca

echo 'Finished cleanup'

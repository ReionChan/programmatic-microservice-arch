#!/bin/bash
# https://hi.service-now.com/kb_view.do?sysparm_article=KB0852804

# ./import-rsa-keypair-into-keystore.sh docusign-sandbox docusign-demo.jks private-key.key

KEY_ALIAS=$1
KEYSTORE=$2
PRIVATE_KEY=$3

echo -n "Keystore ($KEYSTORE) password:"
read -s KEYSTORE_PASS
echo

# Copy the Private key that was generated from your DocuSign Integration app and make a new file privatekey.key with this private key.
# Create CA signed certificate using private key, please run the below command for this
echo "Creating a CA signed certificate using private key ..."
openssl req -new -x509 -key $PRIVATE_KEY \
    -subj "/C=CO/ST=VAL/L=CALI/O=Capmotion/OU=Security/CN=$KEY_ALIAS" \
    -out $KEY_ALIAS.pem
echo ".............."
echo
echo

# Create PKCS 12 file using your private key and CA signed certificate, please run the below command for this (Set the Password, whenever it asks)
echo "Creating a PKCS 12 file ..."
openssl pkcs12 -export \
    -in $KEY_ALIAS.pem -inkey $PRIVATE_KEY \
    -certfile $KEY_ALIAS.pem \
    -out $KEY_ALIAS.p12 -name $KEY_ALIAS\
    -password pass:$KEYSTORE_PASS
echo ".............."
echo
echo

# Now, Create the JKS file by running the below command
echo "Importing PKCS 12 file into Keystore..."
keytool -importkeystore -v -noprompt \
    -srckeystore $KEY_ALIAS.p12 -srcstorepass $KEYSTORE_PASS \
    -destkeystore $KEYSTORE -deststorepass $KEYSTORE_PASS
echo ".............."
echo
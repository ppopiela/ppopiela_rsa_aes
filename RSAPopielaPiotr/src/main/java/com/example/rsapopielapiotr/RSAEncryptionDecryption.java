package com.example.rsapopielapiotr;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import static java.lang.Math.ceil;


    /*RSA Encryption*/
public class RSAEncryptionDecryption {

    @FXML
    private ComboBox<Integer> rsaKeySize;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    @FXML
    private TextArea publicKeyTextArea,
            privateKeyTextArea,
            enterDataToEncrypt,
            enterDataToDecrypt,
            enterPublicKey,
            enterPublicKeyForFiles,
            EnterPrivateKey,
            EnterPrivateKeyForFiles,
            resultsEncrypt,
            resultsDecrypt;


    public void initialize() {
        ObservableList<Integer> keySizes = FXCollections.observableArrayList(512, 1024, 2048, 4096);
        rsaKeySize.setItems(keySizes);
        rsaKeySize.getSelectionModel().select(0);
    }

    /*Generate keys*/
    @FXML
    public void generateKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(rsaKeySize.getValue());
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();

            publicKeyTextArea.setText(encode(publicKey.getEncoded()));
            privateKeyTextArea.setText(encode(privateKey.getEncoded()));
        } catch (Exception e) {
            publicKeyTextArea.setText(String.valueOf(e));
            privateKeyTextArea.setText(String.valueOf(e));
        }
    }

    private PrivateKey getPrivateKey(String privateKeyString) {
        try {
            byte[] binCpk = Base64.getDecoder().decode(privateKeyString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(binCpk);

            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey getPublicKey(String publicKeyString) {
        try {
            byte[] binCpk = Base64.getDecoder().decode(publicKeyString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(binCpk);

            return keyFactory.generatePublic(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /*Encrypt data*/
    @FXML
    private void onButtonExecuteEncrypt() throws Exception {
        String encryptData = enterDataToEncrypt.getText();

        resultsEncrypt.setText(encrypt(encryptData));
    }

    public double getValueForEncrypt() {
        double value = rsaKeySize.getValue();
        return (value/8)-11;
    }

    public double getValueForDecrypt() {
        double value = rsaKeySize.getValue();
        return value/8;
    }

    private String encrypt(String message) throws Exception {
        if (message.isEmpty()) {
            enterDataToEncrypt.setText("Textfield mustn`t be empty!");
        }

        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey(EnterPrivateKey.getText()));
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);

        return encode(encryptedBytes);
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /*Decrypt data*/
    @FXML
    private void onButtonExecuteDecrypt() throws Exception {
        String decryptData = enterDataToDecrypt.getText();
        resultsDecrypt.setText(decrypt(decryptData));
    }

    public String decrypt(String encryptedMessage) throws Exception {
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, getPublicKey(enterPublicKey.getText()));
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);

        return new String(decryptedMessage, "UTF8");
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    /*Set key sizes*/
    public void setRsaKeySize() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(rsaKeySize.getValue());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /*Encrypt file*/
    @FXML
    private void searchFileByExplorerAndEncryptData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "All Files",
                        "*.*")
        );

        try {
            File file = fileChooser.showOpenDialog(new Stage());
            String path = file.getPath().replace(file.getName(), "");
            byte[] fileToByte = Files.readAllBytes(Path.of(file.getPath()));
            byte[] encryptedBytes = new byte[0];
            int getValueForEncryptInteger = (int) getValueForEncrypt();
            double getValueForEncryptDouble = getValueForEncrypt();
            for(int i = 0; i< ceil(fileToByte.length / getValueForEncryptDouble); i++) {
                byte[] temp = new byte[getValueForEncryptInteger];
                for(int j = i * getValueForEncryptInteger, k = 0; j < i * getValueForEncryptInteger + getValueForEncryptInteger && j < fileToByte.length; j++, k++) {
                    temp[k] = fileToByte[j];
                }
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey(EnterPrivateKeyForFiles.getText()));
                encryptedBytes = concat(encryptedBytes, cipher.doFinal(temp));
                FileOutputStream outputStream = new FileOutputStream(path.concat(file.getName().concat(".enc")));
                outputStream.write(encryptedBytes);
                outputStream.close();
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IOException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void searchFileByExplorerAndDecryptData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "All Files",
                        "*.*")
        );
        try {
            File file = fileChooser.showOpenDialog(new Stage());
            byte[] fileToByte = Files.readAllBytes(Path.of(file.getPath()));
            byte[] encryptedBytes = new byte[0];
            String path = file.getPath().replace(file.getName(), "");

            int getValueForDecryptInteger = (int) getValueForDecrypt();
            double getValueForDecryptDouble = getValueForDecrypt();
            for(int i = 0; i< ceil(fileToByte.length / getValueForDecryptDouble); i++) {
                byte[] temp = new byte[getValueForDecryptInteger];
                for(int j = i * getValueForDecryptInteger, k = 0; j < i * getValueForDecryptInteger + getValueForDecryptInteger && j < fileToByte.length; j++, k++) {
                    temp[k] = fileToByte[j];
                }
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, getPublicKey(enterPublicKeyForFiles.getText()));
                encryptedBytes = concat(encryptedBytes, cipher.doFinal(temp));
                FileOutputStream outputStream = new FileOutputStream(path.concat(file.getName().replace(".enc", ".dec")));
                outputStream.write(encryptedBytes);
                outputStream.close();
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IOException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] concat(byte[]...arrays) {
        int totalLength = 0;
        for (byte[] bytes : arrays) {
            totalLength += bytes.length;
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }
}


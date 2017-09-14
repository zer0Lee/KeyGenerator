package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.json.JSONObject;

public class KeyGenerator {
  private final static int EVALUATE = 0; // License for evaluate
  private final static int BASIC = 1;    // License for basic features
  
  private RSAPrivateKey privateKey;
  private RSAPublicKey publicKey;
  private String superPassword;
  
  public KeyGenerator() throws NoSuchAlgorithmException {
    initializeRsaKeys();
  }
  
  public String getPublicKeyString() {
    return base64Encode(publicKey.getEncoded());
  }
  
  public String getPrivateKeyString() {
    return base64Encode(privateKey.getEncoded());
  }
  
  public String getSuperPassword() {
    return superPassword;
  }
  
  public void generateLicenseFile(
      String licFile, String macAddress, int licType, int expireMonth) throws Exception {
    // Fill in basic license information
    Map<String, String> licenseInformation = new HashMap<>();
    licenseInformation.put("License Type", Integer.toString(licType));
    licenseInformation.put("MAC Hash", md5Hash(macAddress));
    superPassword = licenseInformation.get("MAC Hash").substring(0, 8);
    
    if (licType == BASIC){
      licenseInformation.put("Expire Date", "0000-00-00");
    } else {
      licenseInformation.put("Expire Date", LocalDate.now().plusMonths(expireMonth).toString());
    }
    
    // Convert to JSON string
    JSONObject jsonObject = new JSONObject(licenseInformation);
    String jsonString = jsonObject.toString(4);
    
    System.out.println(jsonString);
    
    // Encrypt the JSON string
    String encryptedLicense = encrypt(jsonString);
    System.out.println(encryptedLicense);
    
    // Save to file
    FileWriter writer = new FileWriter(licFile, false);
    BufferedWriter bufferedWriter = new BufferedWriter(writer);
    bufferedWriter.write(encryptedLicense);
    bufferedWriter.close();
  }
  
  public void loadLicenseFile(String licFile) throws Exception {
    File file = new File(licFile);
    
    // Read from file
    FileReader reader = new FileReader(file);
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line;
    String allLines = "";
    while ((line = bufferedReader.readLine()) != null) {
      allLines += line;
    }
    bufferedReader.close();
    
    System.out.println("\nLoad lic file: " + file.getAbsolutePath());
    System.out.println(allLines);
    
    // Decode and decrypt
    byte[] decoded = Base64.getMimeDecoder().decode(allLines);
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    byte[] cleartext = cipher.doFinal(decoded);
    String jsonString = new String(cleartext);
    System.out.println(jsonString);
    
    // Parse json string
    JSONObject jsonObject = new JSONObject(jsonString);
    String macHash = jsonObject.getString("MAC Hash");
    String licenseType = jsonObject.getString("License Type");
    
    System.out.println("Mac Hash: " + macHash);
    System.out.println("License Type: " + licenseType);
  }
  
  private void initializeRsaKeys() throws NoSuchAlgorithmException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    
    // Initialize
    byte[] seed = new String("I Love Wangfang and Max.Li").getBytes();
    SecureRandom secureRandom = new SecureRandom(seed);
    generator.initialize(2048, secureRandom);
    
    // Generate a key pair
    KeyPair keyPair = generator.generateKeyPair();
    
    // Get keys
    privateKey = (RSAPrivateKey)keyPair.getPrivate();
    publicKey = (RSAPublicKey)keyPair.getPublic();
    
    System.out.println("Public key: ");
    System.out.println(getPublicKeyString());
  }
  
  private String md5Hash(String input) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.reset();
    byte[] nonce = new String("Make@better&world!").getBytes();
    md.update(nonce);
    md.update(input.getBytes());
    byte[] bytes = md.digest();
    
    //This bytes[] has bytes in decimal format;
    //Convert it to hexadecimal format
    StringBuilder sb = new StringBuilder();
    for(int i=0; i< bytes.length ;i++)
    {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
    }
    
    return sb.toString();
  }
  
  private String encrypt(String plaintext) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
    return base64Encode(ciphertext);
  }
  
  private String base64Encode(byte[] input) {
    return Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(input);
  }
}

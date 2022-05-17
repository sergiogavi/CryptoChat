package edu.fje.cryptoproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Chat extends AppCompatActivity {
    Button botojuego,botoregistro;
    ImageView imgUserM,imgUserH;
    TextView tx,byteimg;
    EditText usu;
    EditText pass;
    ArrayList<String> lista = new ArrayList<String>();
    private CoordinatorLayout coordinatorLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        tx = (TextView) findViewById(R.id.texto);
        //coordinatorLayout=findViewById(R.id.coordinatorLayout);
        usu = (EditText) findViewById(R.id.txt_usuario);
        pass = (EditText) findViewById(R.id.txt_password);
        imgUserM = (ImageView) findViewById(R.id.imgM);
        imgUserH = (ImageView) findViewById(R.id.imgH);
        byteimg=(TextView)findViewById(R.id.byteimg);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList <String> imgBytes=new ArrayList<String>();
        ArrayList <String> publicKeyasinc=new ArrayList<String>();
      //  Bitmap srcM= BitmapFactory.decodeFile("src/main/res/drawable-v24 src/main/res/drawable/iconom.png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        botoregistro = (Button) findViewById(R.id.bt_registra);
        botoregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SecretKey sk;

             //  KeyPair kp;
             //  kp=randomGenerate(2048);
             //   byte[] datos= imgBytes.get(0).getBytes();

           //    byte[] resultados=encryptData(datos,kp.getPublic());
               // byte[] encoded = Base64.getEncoder().encode(resultados);
            //     String showimg=new String(resultados);

              //  byte[] datos= imgBytes.get(0).getBytes();

            //    byte[] resultados=encryptData(datos,kp.getPublic());
            //    String showD=new String(resultados);
                //usu.setText(showD);

            //    usu.setText(imgBytes.get(0).getBytes().toString());
                Map<String, Object> user = new HashMap<>();
                user.put("usuario", usu.getText().toString());
                user.put("password", pass.getText().toString());
               // usu.setText(showimg);
                //generación de Hash al usuario y contraseña
                String toHash = usu.getText().toString() + pass.getText().toString();
                sk = passwordKeyGeneration(toHash, 128);
                String passS = usu.getText().toString();
                //   byte[] passbytes=passS.getBytes();
                //Encriptacion asimetrica a la imagen

            //      byte[] imgM= imgUserM.toString().getBytes();
            //       byte[] kpasimetrico=encryptData(imgM,kp.getPublic());
                  // String s = new String(kpasimetrico.toString());
                 //  usu.setText(kp.toString());
                   user.put("hash", sk.toString());
                 user.put("bytesimg",imgBytes.get(0));
                    user.put("publicKey",publicKeyasinc.get(0));
                // user.put("hashCypher",passS);
                // Add a new document with a generated ID
                db.collection("registro")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                //Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Log.w(TAG, "Error adding document", e);
                            }
                        });


                Intent botoregistro = new Intent(Chat.this, MainActivity.class);
                startActivity(botoregistro);
            }


            public SecretKey passwordKeyGeneration(String text, int keySize) {
                SecretKey sKey = null;
                if ((keySize == 128) || (keySize == 192) || (keySize == 256)) {
                    try {
                        byte[] data = text.getBytes("utf8");
                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                        byte[] hash = md.digest(data);

                        byte[] key = Arrays.copyOf(hash, keySize / 8);

                        sKey = new SecretKeySpec(key, "AES");
                        System.out.println("Cifrado en SHA-1: " + key);

                    } catch (Exception ex) {
                        System.err.println("Error generant la clau:" + ex);
                    }

                }
                return sKey;
            }




        });


        imgUserM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyPair kp;
                kp=randomGenerate(2048);
                publicKeyasinc.clear();
                PublicKey pkf=kp.getPublic();
                byte[] encodedpk = Base64.getEncoder().encode(pkf.getEncoded());

               // String encodedString = new String(Base64.getUrlEncoder().encode(bytes));
               //Version anterior a agregar pkey
               // publicKeyasinc.add(encodedpk.toString());
                publicKeyasinc.add(kp.getPublic().toString());
                byte[] imgM= imgUserM.toString().getBytes();
                imgBytes.clear();
                imgBytes.add(imgM.toString());
                byteimg.setText(imgBytes.get(0).toString());

                byte[] datos= imgBytes.get(0).getBytes();

                byte[] resultados=encryptData(datos,kp.getPublic());
                String showD=new String(resultados);

                //SI FALLA ELIMINAR ENCODED
               // byte[] encoded = Base64.getEncoder().encode(showD.getBytes(StandardCharsets.UTF_8));
               // usu.setText(showD);
                imgBytes.clear();
                imgBytes.add(showD.toString());

            //    imgBytes.add(showD);
            //    byteimg.setText(imgBytes.get(0).toString());
            //    usu.setText(showD);
                //byte[] encoded = Base64.getEncoder().encode(resultados);
             //   String showimg=new String(resultados);
           //     usu.setText(datos.toString()+" - "+resultados.toString());
            }

            public KeyPair randomGenerate(int len) {
                KeyPair keys = null;
                try {
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                    keyGen.initialize(len);
                    keys = keyGen.genKeyPair();
                    //usu.setText(keys.toString());

                } catch (Exception ex) {
                    System.err.println("Generador no disponible.");

                }
                return keys;
            }

            public byte[] encryptData(byte[] data, PublicKey pub) {
                byte[] encryptedData = null;
                try {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, pub);
                    encryptedData = cipher.doFinal(data);

                } catch (Exception ex) {
                    System.err.println("Error xifrant: " + ex);

                }
                return encryptedData;
            }
        });


        imgUserH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    byte[] imgH= imgUserH.toString().getBytes();
                //  imgBytes.clear();
                //  imgBytes.add(imgH.toString());
                //  byteimg.setText(imgBytes.get(0).toString());

                publicKeyasinc.clear();
                KeyPair kp;
                kp = randomGenerate(2048);
                publicKeyasinc.add(kp.getPublic().toString());
                byte[] imgH = imgUserH.toString().getBytes();
                imgBytes.clear();
                imgBytes.add(imgH.toString());
                byteimg.setText(imgBytes.get(0).toString());

                byte[] datos = imgBytes.get(0).getBytes();

                byte[] resultados = encryptData(datos, kp.getPublic());
                String showD = new String(resultados);

                // usu.setText(showD);
                imgBytes.clear();
                imgBytes.add(showD);

                //    imgBytes.add(showD);
                //    byteimg.setText(imgBytes.get(0).toString());
                //    usu.setText(showD);
                //byte[] encoded = Base64.getEncoder().encode(resultados);
                //   String showimg=new String(resultados);
                //     usu.setText(datos.toString()+" - "+resultados.toString());
            }

            public KeyPair randomGenerate(int len) {
                KeyPair keys = null;
                try {
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                    keyGen.initialize(len);
                    keys = keyGen.genKeyPair();
                    //usu.setText(keys.toString());

                } catch (Exception ex) {
                    System.err.println("Generador no disponible.");

                }
                return keys;
            }

            public byte[] encryptData(byte[] data, PublicKey pub) {
                byte[] encryptedData = null;
                try {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, pub);
                    encryptedData = cipher.doFinal(data);

                } catch (Exception ex) {
                    System.err.println("Error xifrant: " + ex);

                }
                return encryptedData;
            }

        });


    }
}
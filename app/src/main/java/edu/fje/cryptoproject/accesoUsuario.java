package edu.fje.cryptoproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
public class accesoUsuario extends AppCompatActivity {
    Button botojuego,botoLogin,buttonGet;
    TextView usu,chat,pruebaimg;
    EditText sendmsg;
    Button btSend;
    ImageView icon;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList <String> mensajesBd=new ArrayList<String>();
    ArrayAdapter<Object> adapter = null;
    int countSize=0;
    SecretKey sk=passwordKeyGeneration("user",128);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.accesousuario);
        sendmsg=(EditText)findViewById(R.id.txtmsg);
        usu= (TextView) findViewById(R.id.txtacceso);
        chat= (TextView) findViewById(R.id.txt_chat);
       // pruebaimg=(TextView) findViewById(R.id.pruebaimg);
        btSend=(Button)findViewById(R.id.bt_send);
        buttonGet=(Button)findViewById(R.id.bt_getData);
        icon=(ImageView) findViewById(R.id.icono);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

      //  String msg=sendmsg.getText().toString();

        //Prueba gestión imagenes
        LinearLayout layout = (LinearLayout)findViewById(R.id.linear);
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(R.drawable.logo);
        layout.addView(btn);

       // getBytesImg();
        if(b!=null)
        {
            String j =(String) b.get("usuario");
            usu.setText(j);
        }

       // getBytesImg();

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String msg=sendmsg.getText().toString();
               //recupera lo que habia escrito hace un salto de linea y añade el nuevo contenido

              // chat.setText(chat.getText()+"\n"+msg);

               // SecretKey sk=passwordKeyGeneration("user",128);
                byte[] datos= msg.getBytes();

                byte[] resultados=encryptData(sk,datos);
                byte[] encoded = Base64.getEncoder().encode(resultados);
              //  println(new String(encoded));   // Outputs "SGVsbG8="
                pushBD(new String(encoded));
            }


        });

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("mensajes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                             String m= (String) document.getData().get("mensajes");
                                byte[] decoded = Base64.getDecoder().decode(m);

                                 byte[] mensajesFinal= decryptData(sk,decoded);

                                  String mensajeFinal = new String(mensajesFinal);
                                mensajesBd.add(mensajeFinal);

                            }

                            toPrintData(mensajesBd);
                            mensajesBd.clear();
                        }

                    }

                });

            //Metodo asimetrico
                db.collection("registro").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String bytesImgs = (String) document.getData().get("bytesimg");
                                String pkey = (String) document.getData().get("publicKey");

                                byte[] keyBytes = pkey.getBytes();
                                try {
                                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
                                    //pruebaimg.setText(publicKey.toString());
                                    byte[] resultimg = decryptDataImg(bytesImgs.getBytes(),publicKey);

                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    public static PublicKey convertArrayToPubKey(byte encoded[], String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        return pubKey;
    }

    public KeyPair randomGenerate(int len) {
        KeyPair keys = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(len);
            keys = keyGen.genKeyPair();
        } catch (Exception ex) {
            System.err.println("Generador no disponible.");
        }
        return keys;
    }

public void toPrintData(ArrayList s){
    String textDetails;
    chat.setMovementMethod(new ScrollingMovementMethod());

    String joined = String.join("\n"+usu.getText()+" : ", s);
    Collections.sort(s);
    chat.setText(joined);
    // s.clear();

}
    public static SecretKey passwordKeyGeneration(String text, int keySize) {
        SecretKey sKey = null;
        if ((keySize == 128)||(keySize == 192)||(keySize == 256)) {
            try {
                byte[] data = text.getBytes("utf8");
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] hash = md.digest(data);
                byte[] key = Arrays.copyOf(hash, keySize/8);
                sKey = new SecretKeySpec(key, "AES");
            } catch (Exception ex) {
                System.err.println("Error generant la clau:" + ex);
            }
        } return sKey;
    }

    public byte[] encryptData(SecretKey sKey, byte[] data) {
        byte[] encryptedData = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey);
            encryptedData = cipher.doFinal(data);
        } catch (Exception ex) {
            System.err.println("Error xifrant les dades: " + ex);
        } return encryptedData;
    }

    public byte[] decryptData(SecretKey sKey, byte[] data) {
        byte[] encryptedData = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, sKey);
            encryptedData = cipher.doFinal(data);

        } catch (Exception ex) {
            System.err.println("Error xifrant les dades: " + ex);
        } return encryptedData;
    }

    public void pushBD(String res){
    Map<String, String> mensaje = new HashMap<>();
                mensaje.put("mensajes",res);


    // Add a new document with a generated ID
                db.collection("mensajes")
                        .add(mensaje)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
        @Override
        public void onSuccess(DocumentReference documentReference) {

        }
    })
            .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

        }


    });
    }

    public  byte[] decryptDataImg(byte[] data, PublicKey pub) {
        byte[] encryptedData = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pub);
            encryptedData = cipher.doFinal(data);
        } catch (Exception ex) {
            System.err.println("Error xifrant: " + ex);
            pruebaimg.setText("Error xifrant: " + ex);
        }
        return encryptedData;
    }


}
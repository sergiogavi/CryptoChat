package edu.fje.cryptoproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Login extends AppCompatActivity {
    Button botoCheckLog,botoLogin;
    TextView txlog;
    public static Map<String, String> logs = new HashMap<String, String>();
    EditText usulog;
    EditText passlog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usulog=(EditText) findViewById(R.id.txt_usrlog);
        passlog=(EditText) findViewById(R.id.txt_pass);
        txlog=(TextView)findViewById(R.id.txt_passlog);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final KeyPair[] kpSign = new KeyPair[1];
        botoCheckLog= (Button) findViewById(R.id.bt_autentlog);
        botoCheckLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SecretKey[] skl = new SecretKey[1];
                db.collection("registro").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                logs.put((String) document.getData().get("usuario"),(String) document.getData().get("password"));
                               //Compare hash
                                String hash = (String) document.getData().get("hash");
                                String toHash = usulog.getText().toString() + passlog.getText().toString();
                                skl[0] = passwordKeyGeneration(toHash, 128);
                                String actualHash= skl[0].toString();
                                if(actualHash.equals(hash)){
                                   //Despues de comprobar hash se comprueba además la contraseña y el username

                                    CheckLog();
                                    KeyPair kpSign;
                                    //Firma digital
                                    kpSign=randomGenerate(2048);


                                    PrivateKey privkey= kpSign.getPrivate();
                                    byte[] resultSign=signData(hash.getBytes(),privkey);


                                    //comprobacion firma digital
                                    PublicKey publkey= kpSign.getPublic();
                                   boolean Signature=validateSignature(hash.getBytes(),resultSign.toString().getBytes(),publkey);
                                   if(Signature){
                                       CheckLog();
                                   }
                                }
                            }

                        }
                    }
                });

               // CheckLog();
            }
            public void CheckLog() {
                for (Map.Entry<String, String> entry : logs.entrySet()) {
                    //  System.out.println(entry.getKey() + " = " + entry.getValue());

                    String key = entry.getKey();
                    Object val = entry.getValue();
                    if (key.equals(usulog.getText().toString()) && val.equals(passlog.getText().toString())) {

                        txlog.setText("Correcto, redirigiendo");
                        Intent botoCheckLog = new Intent(Login.this,accesoUsuario.class);
                        botoCheckLog.putExtra("usuario", usulog.getText().toString());
                        startActivity(botoCheckLog);
                        break;
                    } else {
                        txlog.setText("Usuario o contraseña incorrectos, por favor vuelve a intentarlo.");
                    }


                }
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

            public byte[] signData(byte[] data, PrivateKey priv) {
                byte[] signature = null;
                try {
                    Signature signer = Signature.getInstance("SHA1withRSA");
                    signer.initSign(priv);
                    signer.update(data);
                    signature = signer.sign();
                } catch (Exception ex) {
                    System.err.println("Error signant les dades: " + ex);
                }
                return signature;
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

            public boolean validateSignature(byte[] data, byte[] signature, PublicKey pub)
            {
                boolean isValid = false;
                try {
                    Signature signer = Signature.getInstance("SHA1withRSA");
                    signer.initVerify(pub);
                    signer.update(data);
                    isValid = signer.verify(signature);
                } catch (Exception ex) {
                    System.err.println("Error validant les dades: " + ex);
                }
                return isValid;
            }
        });

    }
}
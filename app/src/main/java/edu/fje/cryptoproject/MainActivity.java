package edu.fje.cryptoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button botojuego,botoLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botojuego= (Button) findViewById(R.id.bt_registro);
        botojuego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent botojuego = new Intent(MainActivity.this,Chat.class);
                startActivity(botojuego);
            }
        });

        botoLogin= (Button) findViewById(R.id.bt_login);
        botoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent botoLogin = new Intent(MainActivity.this,Login.class);
                startActivity(botoLogin);
            }
        });
    }
}
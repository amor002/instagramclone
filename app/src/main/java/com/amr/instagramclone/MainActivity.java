package com.amr.instagramclone;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity{

    public TextInputEditText email, password;
    public FirebaseAuth firebaseAuth;

    public static ContentResolver contentResolver;
    public static boolean amrAppeared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        contentResolver = getContentResolver();
        User.resources = getResources();
        getSupportActionBar().hide();

        if(!amrAppeared) {
            amrAppeared = true;
            Toast.makeText(this, "this app was developed by amr", Toast.LENGTH_LONG).show();

        }

        if(!isNetworkConnected()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("info")
                    .setMessage("internt connection is required")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            System.exit(0);
                        }
                    }).create().show();
            return;
        }

        if(!(firebaseAuth.getCurrentUser() == null || firebaseAuth.getCurrentUser().isAnonymous())) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            this.finish();

        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

    }

    public void signIn(View view) {
        if(!isValidEmail(email.getText().toString().trim())) {
            email.setError("please write a valid email");
            if(password.getText().toString().length() == 0) {
                password.setError("password field cannot be empty");
            }
            return;
        }
        if(password.getText().toString().length() == 0) {
            password.setError("password field cannot be empty");
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("signing in");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()) {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    MainActivity.this.finish();

                }else {
                    email.setError("wrong email or password");
                    password.setError("wrong email or password");
                    task.getException().printStackTrace();
                }
            }
        });

    }

    public void signUp(View view) {
        startActivity(new Intent(this, singupActivity.class));
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-0]+)*(\\.[A-Za-z]{2,})$");
        return pattern.matcher(email).matches();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;

    }

}

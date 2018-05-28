package com.amr.instagramclone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.amr.instagramclone.Image;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class singupActivity extends AppCompatActivity {

    public TextInputEditText userName, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);
        getSupportActionBar().hide();

        userName = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

    }

    public void registerAccount(View view) {
        if(TextUtils.isEmpty(userName.getText().toString().trim())) {
            userName.setError("user name cannot be empty");
            return;
        }
        if(TextUtils.isEmpty(email.getText().toString().trim())) {
            email.setError("email cannot be empty");
            return;
        }
        if(TextUtils.isEmpty(password.getText().toString())) {
            password.setError("password field cannot be empty");
            return;
        }
        if(password.getText().toString().trim().length() < 6) {
            password.setError("password cannot be less than 6 characters");
            return;
        }

        final User user = new User(userName.getText().toString().trim(),
                email.getText().toString().trim(), password.getText().toString());
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("registering");
        progressDialog.show();


        user.save().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                try {
                    FirebaseUser userObj = task.getResult().getUser();
                    final DatabaseReference userData = FirebaseDatabase.getInstance().getReference("users").child(userObj.getUid());
                    final Image image = new Image(user, getResources(), R.drawable.user);

                    DatabaseReference dr = FirebaseDatabase.getInstance().getReference()
                            .child("allUsers").push();
                    user.setSearhId(dr.getKey());
                    dr.child(user.getName()).setValue(user.getId());

                    final Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(image.isImageUrlDownloaded()) {
                                user.setImage(image);
                                userData.child("user").setValue(user);
                                progressDialog.dismiss();
                                Toast.makeText(singupActivity.this, "register success", Toast.LENGTH_LONG).show();

                            }else {
                                handler.postDelayed(this, 1000);
                            }
                        }
                    });

                }catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.layout), "this email already exists try another one", 5000).show();
                }
            }
        });

    }

}

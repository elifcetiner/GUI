package com.example.smartbuy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mRegisterEmail;
    private EditText mRegisterPassword;
    private EditText mRepeatPassword;
    private EditText mName;
    private Button mRegBtn;
    private ProgressBar mProgressBar;
    private TextView shortPassword, matchPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.register_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register on SB");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mRegisterEmail = (EditText) findViewById(R.id.reg_email);
        mRegisterPassword = (EditText) findViewById(R.id.reg_password);
        mRepeatPassword = (EditText) findViewById(R.id.reg_repeat_password);
        mName = (EditText) findViewById(R.id.reg_name);
        mRegBtn = (Button) findViewById(R.id.reg_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.reg_progress);

        shortPassword = (TextView) findViewById(R.id.password_short);
        matchPassword = (TextView) findViewById(R.id.password_match);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                String name = mName.getText().toString();
                String email = mRegisterEmail.getText().toString();
                String password = mRegisterPassword.getText().toString();
                String repeatPassword = mRepeatPassword.getText().toString();
                if (password.equals(repeatPassword)){
                    registerUser(name, email, password);
                }else {
                    matchPassword.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

    }

    private void registerUser(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String uid = currentUser.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("image", "default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mProgressBar.setVisibility(View.GONE);
                                        Intent homeIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        startActivity(homeIntent);
                                        finish();
                                    }else {
                                        Toast.makeText(RegisterActivity.this, "Registration Unsuccessful \n" +
                                                "Please try again later", Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Unsuccessful \n" +
                                    "Please try again later", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }
}

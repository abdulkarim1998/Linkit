package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    private FirebaseAuth firebaseAuth;

    private View progressBar;

    private boolean doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.emailText);
        etPassword = findViewById(R.id.passwordText);

        progressBar = findViewById(R.id.progressBar);

    }

    public void login(View view)
    {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.equals(""))
        {
            etEmail.setError(getString(R.string.empty));
        }
        else if (password.equals(""))
        {
            etPassword.setError(getString(R.string.empty));
        }
        else
        {
            firebaseAuth = FirebaseAuth.getInstance();

            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful())
                    {
                        Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity2.class));
                        finish();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Logging in failed, try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void goToSignin(View view)
    {
        startActivity(new Intent(LoginActivity.this, Sign_in_Activity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth f = FirebaseAuth.getInstance();
        FirebaseUser u = f.getCurrentUser();

        if(u != null)
        {
            startActivity(new Intent(LoginActivity.this, MainActivity2.class));
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if(doubleBackPressed)
        {
            finishAffinity();
        }
        else
        {
            doubleBackPressed = true;
            Toast.makeText(this, "press back again to quit", Toast.LENGTH_SHORT).show();

            android.os.Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackPressed = false;
                }
            }, 2000);
        }
    }
}
package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Sign_in_Activity extends AppCompatActivity {

    EditText etNickName;
    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPassword;

    String nickName, userName, email, password, confirmPassword;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_);

        etNickName = findViewById(R.id.nicknameText);
        etEmail = findViewById(R.id.emailText);
        etPassword = findViewById(R.id.passwordText);
        etConfirmPassword = findViewById(R.id.confirmPasswordText);

        firebaseAuth = FirebaseAuth.getInstance();


    }

    public void signIn(View view)
    {
        nickName = etNickName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if(nickName.equals(""))
        {
            etNickName.setError(getString(R.string.empty));
        }
        else if(email.equals(""))
        {
            etEmail.setError(getString(R.string.empty));
        }
        else if(password.equals(""))
        {
            etPassword.setError(getString(R.string.empty));
        }
        else if(confirmPassword.equals(""))
        {
            etConfirmPassword.setError(getString(R.string.empty));
        }
        else if(!password.equals(confirmPassword))
        {
            etConfirmPassword.setError("Confirm password must be the same as password");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmail.setError(getString(R.string.email_wrong_format));
        }
        else
        {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(Sign_in_Activity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(Sign_in_Activity.this, "Something went wrong" + task.getException()  , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }
}
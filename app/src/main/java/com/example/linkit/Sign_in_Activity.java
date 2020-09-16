package com.example.linkit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

public class Sign_in_Activity extends AppCompatActivity {

    EditText etNickName;
    EditText etUserName;
    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPassword;

    String nickName, userName, email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_);

        etNickName = findViewById(R.id.nickName);
        etUserName = findViewById(R.id.username);
        etEmail = findViewById(R.id.emailText);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirmPassword);


    }

    public void signIn(View view)
    {
        nickName = etNickName.getText().toString().trim();
        userName = etUserName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if(nickName.equals(""))
        {
            etNickName.setError(getString(R.string.empty));
        }
        else if(userName.equals(""))
        {
            etUserName.setError(getString(R.string.empty));
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
        else if(Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmail.setError(getString(R.string.email_wrong_format));
        }
        else
        {

        }


    }
}
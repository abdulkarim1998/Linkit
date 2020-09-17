package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etPassword = findViewById(R.id.passwordText);
        etConfirmPassword = findViewById(R.id.confirmPasswordText);
    }

    public void changePassword(View view)
    {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if(password.equals(""))
        {
            etPassword.setError(getString(R.string.empty));
        }
        else if(confirmPassword.equals(""))
        {
            etConfirmPassword.setError(getString(R.string.empty));
        }
        else if(!password.equals(confirmPassword))
        {
            etConfirmPassword.setError("Password must match");
        }
        else
        {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if(firebaseUser != null)
            {
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(ChangePasswordActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else
                        {
                            Toast.makeText(ChangePasswordActivity.this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
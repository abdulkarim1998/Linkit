package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailText;
    private TextView textMessage;
    private LinearLayout llResetPassword, llMessage;
    private Button btnRetry;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailText = findViewById(R.id.emailText);
        textMessage = findViewById(R.id.textMessage);
        llResetPassword = findViewById(R.id.llResetPassword);
        llMessage = findViewById((R.id.llMessage));

    }
    //btn Listener to reset password
    public void btnResetPassword(View view){
      final String email =  emailText.getText().toString().trim();

      if(email.equals(""))
      {
          emailText.setError(getString(R.string.email));
      }
      else
      {
          FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

          firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
              llResetPassword.setVisibility(View.GONE);
              llMessage.setVisibility(View.VISIBLE);

              if(task.isSuccessful())
              {
                  textMessage.setText(getString(R.string.reset_password_instruction, email));
                  new CountDownTimer(60000, 1000){

                      @Override
                      public void onTick(long l) {
                          btnRetry.setText(getString(R.string.resend_timer, String.valueOf(l/1000 )));
                          btnRetry.setOnClickListener(null);
                      }

                      @Override
                      public void onFinish() {
                            btnRetry.setText(R.string.retry);
                            btnRetry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                   llResetPassword.setVisibility(View.VISIBLE);
                                   llMessage.setVisibility(View.GONE);
                                }
                            });
                      }
                  };
              }
              else
              {
                  textMessage.setText(getString(R.string.email_sent_faild, task.getException()));
                  btnRetry.setText(R.string.retry);

                  btnRetry.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          llResetPassword.setVisibility(View.VISIBLE);
                          llMessage.setVisibility(View.GONE);
                      }
                  });
              }
              }
          });
      }
    }
    public void btnCloseClick(View view){
        finish();
    }
}
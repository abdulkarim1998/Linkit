package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.linkit.Extras.Node;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Random;

public class Sign_in_Activity extends AppCompatActivity {

    final int photoRequestCode = 22;
    final int permissionToReadExternalMemory = 69;

    EditText etNickName;
    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPassword;

    private ImageView profile;

    String nickName, userName, email, password, confirmPassword;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri localUri, serverUri;

    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_);

        etNickName = findViewById(R.id.nicknameText);
        etEmail = findViewById(R.id.emailText);
        etPassword = findViewById(R.id.passwordText);
        etConfirmPassword = findViewById(R.id.confirmPasswordText);

        progressBar = findViewById(R.id.progressBar);

        profile = findViewById(R.id.profile);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


    }
    // if user want to choose a photo for the profile
    public void choosePhoto(View view)
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, photoRequestCode);
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, permissionToReadExternalMemory);

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == photoRequestCode)
        {
            if(resultCode == RESULT_OK)
            {
                localUri = data.getData();
                profile.setImageURI(localUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == permissionToReadExternalMemory)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, photoRequestCode);
            }
            else
            {
                Toast.makeText(this, "Access permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // the user will sign in if the user chose a photo
    private void updateNameAndPhoto()
    {
        String filename = firebaseUser.getUid()+ ".jpg";

        final StorageReference fileReference = storageReference.child("images/"+ filename);

        fileReference.putFile(localUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverUri = uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(etNickName.getText().toString().trim())
                                    .setPhotoUri(serverUri)
                                    .build();

                            progressBar.setVisibility(View.VISIBLE);
                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if(task.isSuccessful())
                                    {
                                        String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.USERS);

                                        HashMap<String,String> hashMap = new HashMap<>();

                                        hashMap.put(Node.NICKNAME, etNickName.getText().toString().trim());
                                        hashMap.put(Node.EMAIL, etEmail.getText().toString().trim());
                                        hashMap.put(Node.CODE, generateCode());
                                        hashMap.put(Node.STATUS,"Online");
                                        hashMap.put(Node.PHOTO, serverUri.getPath());

                                        databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(Sign_in_Activity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Sign_in_Activity.this, MainActivity2.class));
                                                finish();
                                            }
                                        });

                                    }
                                    else
                                    {
                                        Toast.makeText(Sign_in_Activity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    //the user will sign in if the user did not chose a photo
    private void updateName()
    {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etNickName.getText().toString().trim())
                .build();

        progressBar.setVisibility(View.VISIBLE);
        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful())
                {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.USERS);

                    HashMap<String,String> hashMap = new HashMap<>();

                    hashMap.put(Node.NICKNAME, etNickName.getText().toString().trim());
                    hashMap.put(Node.EMAIL, etEmail.getText().toString().trim());
                    hashMap.put(Node.CODE, generateCode());
                    hashMap.put(Node.STATUS,"Online");
                    hashMap.put(Node.PHOTO, "");

                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Sign_in_Activity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Sign_in_Activity.this, MainActivity2.class));
                            finish();
                        }
                    });

                }
                else
                {
                    Toast.makeText(Sign_in_Activity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    //check the user information requirements, then sign in new account
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
                        firebaseUser = firebaseAuth.getCurrentUser();

                        if(localUri != null)
                        {
                            updateNameAndPhoto();
                        }
                        else {
                            updateName();
                        }
                    }
                    else
                    {
                        Toast.makeText(Sign_in_Activity.this, "Something went wrong" + task.getException()  , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    // generating user codes
    private String generateCode()
    {
        Random random = new Random();
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        String code = "";
        code+= chars[random.nextInt(25)];
        code+= chars[random.nextInt(25)];
        code+= chars[random.nextInt(25)];
        code+= random.nextInt(9);
        code+= random.nextInt(9);
        code+= random.nextInt(9);

        return code;


    }
}
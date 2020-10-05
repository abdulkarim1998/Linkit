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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.linkit.Extras.Node;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    final int photoRequestCode = 22;
    final int permissionToReadExternalMemory = 69;

    EditText etNickName;
    EditText etEmail;

    private View progressBar;

    private ImageView profile;

    String nickName, userName, email;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri localUri, serverUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etNickName = findViewById(R.id.nicknameText);
        etEmail = findViewById(R.id.emailText);

        profile = findViewById(R.id.profile);

        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        if(firebaseUser != null)
        {
            etNickName.setText(firebaseUser.getDisplayName());
            etEmail.setText(firebaseUser.getEmail());
            serverUri = firebaseUser.getPhotoUrl();

            if(serverUri != null)
            {
                Glide.with(this)
                        .load(serverUri)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(profile);

            }
        }
    }
    //save updates that is done by the user
    public void saveChanges(View view)
    {
        if(etNickName.getText().toString().equals(""))
        {
            etNickName.setError(getString(R.string.empty));
        }
        else
        {
            if(localUri !=null)
            {
                updateNameAndPhoto();
            }
            else
            {
                updateName();
            }
        }
    }
    //logout Clicklistner btn
    public void logout(View view)
    {
        FirebaseAuth f = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }
    // clickListner if the user wants to either remove photo or change photo
    public void changeProfilePhoto(View view)
    {
        if(serverUri == null)
        {
            choosePhoto();
        }
        else
        {
            PopupMenu popupMenu = new PopupMenu(this,view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    int choice = item.getItemId();
                    if(choice == R.id.menu_changePhoto)
                    {
                        choosePhoto();
                    }
                    else if(choice == R.id.menu_deletePhot)
                    {
                        removeProfilePhoto();
                    }

                    return false;
                }
            });

            popupMenu.show();
        }
    }
    //helper function for the above function
    private void removeProfilePhoto()
    {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etNickName.getText().toString().trim())
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    final String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.USERS);

                    databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String previousEmail = dataSnapshot.child(Node.EMAIL).getValue().toString();
                            String previousCode = dataSnapshot.child(Node.CODE).getValue().toString();
                            String previousStatus = dataSnapshot.child(Node.STATUS).getValue().toString();

                            HashMap<String,String> hashMap = new HashMap<>();

                            hashMap.put(Node.NICKNAME, etNickName.getText().toString().trim());
                            hashMap.put(Node.EMAIL, previousEmail);
                            hashMap.put(Node.CODE, previousCode);
                            hashMap.put(Node.STATUS,previousStatus);
                            hashMap.put(Node.PHOTO, "");


                            databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    finish();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    // helper function
    private void choosePhoto()
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
    // if the user request to choose a photo
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
    // to update name and photo
    private void updateNameAndPhoto()
    {
        String filename = firebaseUser.getUid()+ ".jpg";

        final StorageReference fileReference = storageReference.child("images/"+ filename);

        progressBar.setVisibility(View.VISIBLE);
        fileReference.putFile(localUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressBar.setVisibility(View.GONE);
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

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        final String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.USERS);

                                        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                String previousEmail = dataSnapshot.child(Node.EMAIL).getValue().toString();
                                                String previousCode = dataSnapshot.child(Node.CODE).getValue().toString();
                                                String previousStatus = dataSnapshot.child(Node.STATUS).getValue().toString();

                                                HashMap<String,String> hashMap = new HashMap<>();

                                                hashMap.put(Node.NICKNAME, etNickName.getText().toString().trim());
                                                hashMap.put(Node.EMAIL, previousEmail);
                                                hashMap.put(Node.CODE, previousCode);
                                                hashMap.put(Node.STATUS,previousStatus);
                                                hashMap.put(Node.PHOTO, serverUri.getPath());

                                                databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        finish();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                    else
                                    {
                                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    // helper function for the function "saveChanges"
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
                    final String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.USERS);

                    databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String previousEmail = dataSnapshot.child(Node.EMAIL).getValue().toString();
                            String previousCode = dataSnapshot.child(Node.CODE).getValue().toString();
                            String previousStatus = dataSnapshot.child(Node.STATUS).getValue().toString();
                            String previousPhoto = dataSnapshot.child(Node.PHOTO).getValue().toString();

                            HashMap<String,String> hashMap = new HashMap<>();

                            hashMap.put(Node.NICKNAME, etNickName.getText().toString().trim());
                            hashMap.put(Node.EMAIL, previousEmail);
                            hashMap.put(Node.CODE, previousCode);
                            hashMap.put(Node.STATUS,previousStatus);
                            hashMap.put(Node.PHOTO, previousPhoto);


                            databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    finish();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
                else
                {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    // it will take the user to changePasswordActivity
    public void changePasswordbtn(View view)
    {
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }
}
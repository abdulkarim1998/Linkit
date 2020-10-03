package com.example.linkit.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.linkit.Node;
import com.example.linkit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_READ_EXTERNAL_MEMORY = 69;
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_PICK_VIDEO = 2;
    private static final int REQUEST_CAPTURE_IMAGE = 3;

    private ImageView sentBtn, profilePhoto;
    private TextView tvUserName;
    private ImageView attachBtn;
    private EditText typingSpace;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private String currentUserID, chatUserID;

    private int page = 1;
    private static final int MESSAGED_PER_PAGE = 30;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MessagedAdapter adapter;
    private List<MessageModel> messageModels;

    private DatabaseReference databaseReferenceMessaged;
    private ChildEventListener childEventListener;

    private String user_name, user_photo;

    private BottomSheetDialog bottomSheetDialog;
    private LinearLayout fileUploadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        typingSpace = findViewById(R.id.typingSpace);
        sentBtn = findViewById(R.id.sendingBtn);
        attachBtn = findViewById(R.id.attachBtn);
        fileUploadProgress = (LinearLayout) findViewById(R.id.fileUploadProgress);

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        if(getIntent().hasExtra(Extras.USER_KEY))
        {
            chatUserID = getIntent().getStringExtra(Extras.USER_KEY);
        }
        if(getIntent().hasExtra(Extras.USER_NAME)) {
            user_name = getIntent().getStringExtra(Extras.USER_NAME);
        }
        if(getIntent().hasExtra(Extras.USER_PHOTO)) {
            user_photo = getIntent().getStringExtra(Extras.USER_PHOTO);
        }


        recyclerView = findViewById(R.id.rvMessages);
        swipeRefreshLayout = findViewById(R.id.rLayout);

        messageModels = new ArrayList<>();
        adapter = new MessagedAdapter(this, messageModels);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        attachBtn.setOnClickListener(this);

        loadMessages();
        recyclerView.scrollToPosition(messageModels.size()-1);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page++;
                loadMessages();
            }
        });

        ActionBar actionbar = getSupportActionBar();
        if(actionbar!= null){
            actionbar.setTitle("");
            ViewGroup actionbarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.actionbar, null);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
            actionbar.setElevation(0);
            actionbar.setCustomView(actionbarLayout);
            actionbar.setDisplayOptions(actionbar.getDisplayOptions()|actionbar.DISPLAY_SHOW_CUSTOM);

        }

        profilePhoto = findViewById(R.id.profilePhoto);
        tvUserName = (TextView) findViewById(R.id.tvUserName);

        tvUserName.setText(user_name);
        if(!TextUtils.isEmpty(user_photo)) {
            StorageReference photoReference = FirebaseStorage.getInstance().getReference().child("images").child(user_photo);
            photoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ChatActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profilePhoto);

                }
            });
        }


        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.send_file_options, null);
        view.findViewById(R.id.llCamera).setOnClickListener(this);
        view.findViewById(R.id.llGallery).setOnClickListener(this);
        view.findViewById(R.id.llVideo).setOnClickListener(this);
        view.findViewById(R.id.close).setOnClickListener(this);
        bottomSheetDialog.setContentView(view);
    }

    public void sendMessage(View view)
    {
        if(haveNetworkConnection()) {
            DatabaseReference userMessage = rootRef.child(Node.MESSAGE).child(currentUserID).child(chatUserID).push();
            String msgID = userMessage.getKey();
            createMessage(typingSpace.getText().toString().trim(), "text", msgID);
        }
        else
        {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }


    private void createMessage(String msg, String msgType, String msgID) {
        try {
            if (!msg.equals("")) {
                HashMap hashMap = new HashMap<>();
                hashMap.put(Node.MESSAGE_ID, msgID);
                hashMap.put(Node.MESSAGE, msg);
                hashMap.put(Node.MESSAGE_TYPE, msgType);
                hashMap.put(Node.MESSAGE_FROM, currentUserID);
                hashMap.put(Node.MESSAGE_TIME, ServerValue.TIMESTAMP);

                String currentUserRef = Node.MESSAGES + "/" + currentUserID + "/" + chatUserID;
                String chatUserRef = Node.MESSAGES + "/" + chatUserID + "/" + currentUserID;

                HashMap msgMap = new HashMap();

                msgMap.put(currentUserRef + "/" + msgID, hashMap);
                msgMap.put(chatUserRef + "/" + msgID, hashMap);

                typingSpace.setText("");

                rootRef.updateChildren(msgMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(ChatActivity.this, "failed to send a message", Toast.LENGTH_SHORT).show();
                        } else {

                        }
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "failed to send a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages()
    {

        messageModels.clear();
        databaseReferenceMessaged = rootRef.child(Node.MESSAGES).child(currentUserID).child(chatUserID);

        Query mQuery = databaseReferenceMessaged.limitToLast(page * MESSAGED_PER_PAGE);

        if(childEventListener != null)
        {

            mQuery.removeEventListener(childEventListener);

            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    Log.i("chat", messageModel.getMessage());

                    messageModels.add(messageModel);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageModels.size()-1);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    loadMessages();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    swipeRefreshLayout.setRefreshing(false);
                }
            };

            mQuery.addChildEventListener(childEventListener);
        }
        else
        {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                    messageModels.add(messageModel);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageModels.size()-1);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    swipeRefreshLayout.setRefreshing(false);
                }
            };
            mQuery.addChildEventListener(childEventListener);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item) ;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.attachBtn:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    if(bottomSheetDialog != null)
                    {
                        bottomSheetDialog.show();
                    }
                }
                else
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_EXTERNAL_MEMORY);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(inputMethodManager != null)
                {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
                break;

            case R.id.llCamera:
                bottomSheetDialog.dismiss();
                Intent intentForCamera = new Intent(ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentForCamera, REQUEST_CAPTURE_IMAGE);
                break;
            case R.id.llGallery:
                bottomSheetDialog.dismiss();
                Intent intentForGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentForGallery, REQUEST_PICK_IMAGE);
                break;
            case R.id.llVideo:
                bottomSheetDialog.dismiss();
                Intent intentForVideo = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentForVideo, REQUEST_PICK_VIDEO);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_READ_EXTERNAL_MEMORY)
        {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if(bottomSheetDialog != null)
                {
                    bottomSheetDialog.show();
                }
            }
            else
            {
                Toast.makeText(this, "Permission required to access the files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if(requestCode == REQUEST_CAPTURE_IMAGE)
            {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,bytes);

                uploadBytes(bytes, Constants.MESSAGE_TYPE_IMAGE);
            }
            else if (requestCode == REQUEST_PICK_IMAGE)
            {
                Uri uri = data.getData();
                uploadFile(uri, Constants.MESSAGE_TYPE_IMAGE);
            }
            else if (requestCode == REQUEST_PICK_VIDEO)
            {
                Uri uri = data.getData();
                uploadFile(uri, Constants.MESSAGE_TYPE_VIDEO);
            }
        }
    }

    private void uploadFile(Uri uri, String messageType)
    {

        DatabaseReference databaseReference = rootRef.child(Node.MESSAGES).child(currentUserID).child(chatUserID).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?Constants.MESSAGE_VIDEO:Constants.MESSAGE_IMAGES;
        String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)? pushId+ ".mb4" : pushId + ".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);
        UploadTask task = fileRef.putFile(uri);
        uploadProgress(task, fileRef, pushId, messageType);
    }


    private void uploadBytes(ByteArrayOutputStream bytes, String messageType)
    {
        DatabaseReference databaseReference = rootRef.child(Node.MESSAGES).child(currentUserID).child(chatUserID).push();
        String pushId = databaseReference.getKey();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?Constants.MESSAGE_VIDEO:Constants.MESSAGE_IMAGES;
        String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)? pushId+ ".mb4" : pushId + ".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(fileName);
        UploadTask task = fileRef.putBytes(bytes.toByteArray());
        uploadProgress(task, fileRef, pushId, messageType);
    }



    public void uploadProgress(final UploadTask task, final StorageReference ref, final String pushId, final String messageType){
        final View view = getLayoutInflater().inflate(R.layout.file_progress, null);
        final TextView tvFileProgress = view.findViewById(R.id.tvfileProgress);
        final ProgressBar pbFile = view.findViewById(R.id.pbFile);
        final ImageView ivPause = view.findViewById(R.id.ivpause);
        final ImageView ivPlay = view.findViewById(R.id.play);
        ImageView ivCancel = view.findViewById(R.id.cancel);


        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.pause();
                ivPlay.setVisibility(View.VISIBLE);
                ivPause.setVisibility(View.GONE);
            }
        });

        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.resume();
                ivPause.setVisibility(View.VISIBLE);
                ivPlay.setVisibility(View.GONE);
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.cancel();

            }
        });

        fileUploadProgress.addView(view);
        tvFileProgress.setText(getString(R.string.uploading_file, messageType, "0"));
        task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = 100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();

                pbFile.setProgress((int) progress);
                tvFileProgress.setText(getString(R.string.uploading_file, messageType, String.valueOf(pbFile.getProgress())));

            }
        });

        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                fileUploadProgress.removeView(view);

                if(task.isSuccessful()){
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            createMessage(downloadUrl, messageType, pushId);
                        }
                    });
                }
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fileUploadProgress.removeView(view);
                Toast.makeText(ChatActivity.this,"fail to upload", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteMessage(final String messageId, final String messageType)
    {
        Log.i("messageType", messageType);
        Log.i("messageID", messageId);

        DatabaseReference databaseReference = rootRef.child(Node.MESSAGES)
                .child(currentUserID).child(chatUserID).child(messageId);

        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            if(task.isSuccessful())
            {
                DatabaseReference databaseReferenceChatUser = rootRef.child(Node.MESSAGES)
                        .child(chatUserID).child(currentUserID).child(messageId);

                databaseReferenceChatUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, R.string.message_deleted_successfully, Toast.LENGTH_SHORT).show();

                            if(!messageType.equals(Constants.MESSAGE_TYPE_TEXT))
                            {
                                StorageReference fileRef = FirebaseStorage.getInstance().getReference();
                                String folder = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?Constants.MESSAGE_VIDEO:Constants.MESSAGE_IMAGES;
                                String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?messageId + ".mp4" : messageId + "jpg";
                                StorageReference f = fileRef.child(folder).child(fileName);

                                f.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful())
                                        {
                                            Toast.makeText(ChatActivity.this,
                                                    getString( R.string.failed_to_delete_file, task.getException()), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                            loadMessages();
                        }
                        else
                        {
                            Log.i("Here", "inner one");
                            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_delete_message,task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else
            {
                Log.i("Here", "outer one");
                Toast.makeText(ChatActivity.this, getString(R.string.failed_to_delete_message,task.getException()),
                        Toast.LENGTH_SHORT).show();
            }
            }
        });


    }
    public void downloadFile (String messageId, final String messageType, final boolean isForShare){

    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

        ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
    }

    else{

        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?Constants.MESSAGE_VIDEO:Constants.MESSAGE_IMAGES;
        String fileName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?messageId + ".mb4" : messageId + ".jpg";

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(folderName).child(fileName);
        final String localPath = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;


        final File localFile = new File(localPath);

        try {

            if (localFile.exists() || localFile.createNewFile()) {

                final FileDownloadTask downloadTask = fileRef.getFile(localFile);

                final View view = getLayoutInflater().inflate(R.layout.file_progress, null);
                final TextView tvFileProgress = view.findViewById(R.id.tvfileProgress);
                final ProgressBar pbFile = view.findViewById(R.id.pbFile);
                final ImageView ivPause = view.findViewById(R.id.ivpause);
                final ImageView ivPlay = view.findViewById(R.id.play);
                ImageView ivCancel = view.findViewById(R.id.cancel);


                ivPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadTask.pause();
                        ivPlay.setVisibility(View.VISIBLE);
                        ivPause.setVisibility(View.GONE);
                    }
                });

                ivPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadTask.resume();
                        ivPause.setVisibility(View.VISIBLE);
                        ivPlay.setVisibility(View.GONE);
                    }
                });

                ivCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadTask.cancel();

                    }
                });

                fileUploadProgress.addView(view);
                tvFileProgress.setText(getString(R.string.downloading_file, messageType, "0"));

                downloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = 100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();

                        pbFile.setProgress((int) progress);
                        tvFileProgress.setText(getString(R.string.downloading_file, messageType, String.valueOf(pbFile.getProgress())));

                    }
                });

                downloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        fileUploadProgress.removeView(view);

                        if(task.isSuccessful()) {

                            if(isForShare)
                            {
                                Intent intentForShare = new Intent();
                                intentForShare.setAction(Intent.ACTION_SEND);
                                intentForShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(localPath));

                                if(messageType.equals(Constants.MESSAGE_TYPE_VIDEO))
                                {
                                    intentForShare.setType("video/mp4");
                                }
                                if(messageType.equals(Constants.MESSAGE_TYPE_IMAGE))
                                {
                                    intentForShare.setType("image/jpg");
                                }
                                startActivity(Intent.createChooser(intentForShare, "share it to ..."));
                            }
                            else {

                                final Snackbar snackbar = Snackbar.make(fileUploadProgress, "file downloaded successfully", Snackbar.LENGTH_INDEFINITE);

                                snackbar.setAction(R.string.view, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Uri uri = Uri.parse(localPath);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                                        if (messageType.equals(Constants.MESSAGE_TYPE_VIDEO)) {
                                            intent.setDataAndType(uri, "video/mp4");
                                        } else if (messageType.equals(Constants.MESSAGE_TYPE_IMAGE)) {
                                            intent.setDataAndType(uri, "image/jpg");
                                        }

                                        startActivity(intent);
                                    }
                                });
                                snackbar.show();
                            }
                        }
                    }
                });
                downloadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, getString(R.string.fail_to_download, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
            } else {

                Toast.makeText(this, "fail to store the file", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex){
            Toast.makeText(ChatActivity.this, getString(R.string.fail_to_download, ex.getMessage()), Toast.LENGTH_LONG).show();

        }
    }
    }
}
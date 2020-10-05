package com.example.linkit.Extras;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Utility {


    public static boolean connectionAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null &&  connectivityManager.getActiveNetworkInfo() != null)
        {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else
        {
            return false;
        }
    }

    public static void updateDeviceToken(final Context context, String token)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
            DatabaseReference databaseReference = root.child(Node.TOKENS).child(user.getUid());

            HashMap<String, String> hashMap = new HashMap<>();

            hashMap.put(Node.DEVICE_TOKEN, token);

            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful())
                    {
                        Toast.makeText(context, "failed to save token", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

   /* public static void sendingNotification(final Context context, final String title, final String msg, String uid)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.TOKENS)
                .child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token ="";
                if(snapshot.child(Node.DEVICE_TOKEN).getValue() != null)
                {
                    token = snapshot.child(Node.DEVICE_TOKEN).getValue().toString();
                }
                JSONObject forNotification = new JSONObject();
                JSONObject forNotificationData = new JSONObject();

                try {

                    forNotificationData.put(Constants.NOTIFICATION_TITLE, title);
                    forNotificationData.put(Constants.NOTIFICATION_MESSAGE, msg);


                    forNotification.put(Constants.NOTIFICATION_TO, token);
                    forNotification.put(Constants.NOTIFICATION_DATA, forNotificationData);

                    String fcmUrl = "https://fcm.googleapis.com/fcm/send";
                    final String type = "application/json";


                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(fcmUrl, forNotification, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {

                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put("Authorization", "key="+ Constants.KEY);
                            hashMap.put("Sender", "id="+ Constants.SENDER_ID);
                            hashMap.put("Content-Type",type);

                            return hashMap;

                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(jsonObjectRequest);





                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/
}

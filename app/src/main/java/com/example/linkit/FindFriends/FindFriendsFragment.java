package com.example.linkit.FindFriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.linkit.Node;
import com.example.linkit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FindFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindFriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FindFriendAdapter adapter;
    private List<FindFriendModel> findFriendModels;
    private TextView textView;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceFriendRequest;
    private FirebaseUser user;

    private View progressBar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FindFriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FindFriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FindFriendsFragment newInstance(String param1, String param2) {
        FindFriendsFragment fragment = new FindFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvMessages);
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.emptyChatList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendModels = new ArrayList<>();
        adapter = new FindFriendAdapter(getActivity(), findFriendModels);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.USERS);
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceFriendRequest = FirebaseDatabase.getInstance().getReference().child(Node.FRIEND_REQUEST).child(user.getUid());

        textView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = databaseReference.orderByChild(Node.NICKNAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                findFriendModels.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren())
                {
                    final String userID = ds.getKey();
                    if(userID.equals(user.getUid()))
                    {
                        continue;
                    }
                    if (ds.child(Node.NICKNAME).getValue() != null)
                    {
                        final String name = ds.child(Node.NICKNAME).getValue().toString();
                        String photo = "";
                        if(ds.child(Node.PHOTO).getValue() != null) {
                            photo = ds.child(Node.PHOTO).getValue().toString();
                        }

                        final String finalPhoto = photo;
                        databaseReferenceFriendRequest.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    String requestType = dataSnapshot.child(Node.REQUEST_TYPE).getValue().toString();
                                    if(requestType.equals("sent"))
                                    {
                                        findFriendModels.add(new FindFriendModel(name, finalPhoto, userID, true));
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                else
                                {
                                    findFriendModels.add(new FindFriendModel(name, finalPhoto, userID, false));
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                        textView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to fetch friends", Toast.LENGTH_SHORT).show();
            }
        });
        Log.i("Size", Integer.toString(findFriendModels.size()));
    }
}
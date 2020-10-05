package com.example.linkit.Chat;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.linkit.Extras.Node;
import com.example.linkit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//fragment for chatList
public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private View progressBar;
    private TextView textView;
    private ChatListAdapter adapter;
    private List<ChatListModel> chatListModels;

    private DatabaseReference  databaseReferenceChats, databaseReferenceUsers;
    private FirebaseUser user;

    private ChildEventListener childEventListener;
    private Query query;

    SearchView searchView;
    ListView  listView;
    ArrayList<String> list;
    ArrayList<String> list_2;
    ArrayAdapter <String> adapter_2;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
    //will fetch all the friends and make a chatActivity for each one and put them in a chatList
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvMessages);
        textView = view.findViewById(R.id.emptyChatList);
        progressBar = view.findViewById(R.id.progressBar);
        searchView = (SearchView) view.findViewById(R.id.searchView);
        listView = (ListView) view.findViewById(R.id.lv1);

        chatListModels = new ArrayList<>();
        adapter = new ChatListAdapter(getActivity(), chatListModels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(Node.USERS);
        databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(Node.CHATS).child(user.getUid());

        query = databaseReferenceChats.orderByChild(Node.TIME_STAMP);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                updateList(dataSnapshot, true, dataSnapshot.getKey());
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

            }
        };

        query.addChildEventListener(childEventListener);
        textView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        list = new ArrayList<String>();
        //list_2 = ChatListAdapter.searchUsername(ChatListAdapter.searchUsername(list));
        adapter_2 = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter_2);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String qquery) {

                if(ChatListAdapter.searchUsername(ChatListAdapter.searchUsername(list)).contains(qquery)){
                    adapter_2.getFilter().filter(qquery);
                }
                else{
                    Toast.makeText(getActivity(), "No Match found",Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter_2.getFilter().filter(newText);
                return false;
            }
        });

    }
    // if the user add new contacts it will added in the chatList
    private void updateList(DataSnapshot dataSnapshot, boolean ifNew, final String userID)
    {
        textView.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        final String lastMessage, lastMessageTime, unreadMessageCount;

        lastMessage = "";
        lastMessageTime = "";
        unreadMessageCount = "";

        databaseReferenceUsers.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!userID.equals(user.getUid())) {
                    String username = dataSnapshot.child(Node.NICKNAME).getValue() != null ?
                            dataSnapshot.child(Node.NICKNAME).getValue().toString() : "";

                    String photoName = dataSnapshot.child(Node.PHOTO).getValue() != null ?
                            dataSnapshot.child(Node.PHOTO).getValue().toString() : "";

                    ChatListModel chatListModel = new ChatListModel(userID, username, photoName, unreadMessageCount, lastMessage, lastMessageTime);

                    chatListModels.add(chatListModel);
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Failed to fetch chat list", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(childEventListener);
    }
}
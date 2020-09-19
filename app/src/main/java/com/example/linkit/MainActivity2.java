package com.example.linkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.linkit.FindFriends.FindFriendsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity2 extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private boolean doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tabLayout = findViewById(R.id.tabMain);
        viewPager = findViewById(R.id.viewPager);

        makeTabs();
    }

    class Adapter extends FragmentStateAdapter {
        public Adapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position)
            {
                case 0:
                    return new ChatFragment();
                case 1:
                    return new RequestFragment();
                default:
                    return new FindFriendsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private void makeTabs()
    {
        //tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_chat));
        //tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_requests));
        //tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_find_friends));

        //tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        Adapter adapter = new Adapter(this);
        viewPager.setAdapter(adapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                switch (position)
                {
                    case 0:
                        tab.setCustomView(R.layout.tab_chat);
                        break;
                    case 1:
                        tab.setCustomView(R.layout.tab_requests);
                        break;
                    case 2:
                        tab.setCustomView(R.layout.tab_find_friends);
                        break;
                }
            }
        });

        tabLayoutMediator.attach();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int choice = item.getItemId();

        if(choice == R.id.menu_goToProfile)
        {
            startActivity(new Intent(MainActivity2.this, ProfileActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if(tabLayout.getSelectedTabPosition() > 0)
        {
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }
        else
        {
            if(doubleBackPressed)
            {
                finishAffinity();
            }
            else
            {
                doubleBackPressed = true;
                Toast.makeText(this, "press back again to quit", Toast.LENGTH_SHORT).show();

                android.os.Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackPressed = false;
                    }
                }, 2000);
            }
        }
    }
}
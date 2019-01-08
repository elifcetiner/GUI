package com.example.smartbuy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.flags.impl.DataUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView mSearchList;
    private final List<Item> searchList = new ArrayList<>();
    private ItemAdapter mAdapter;
    private DatabaseReference mDatabase;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mToolbar = (Toolbar) findViewById(R.id.search_app_bar);
        setSupportActionBar(mToolbar);
        Intent intent = getIntent();
        String search = intent.getStringExtra("search");
        getSupportActionBar().setTitle("Search results for " + search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("items");

        mSearchList = (RecyclerView) findViewById(R.id.search_list);
        mAdapter = new ItemAdapter(searchList, getApplicationContext());
        mSearchList.setHasFixedSize(true);
        mSearchList.setLayoutManager(new GridLayoutManager(this, 2));
        mSearchList.setAdapter(mAdapter);
        loadItems(search);

    }

    private void loadItems(final String search) {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Item item = dataSnapshot.getValue(Item.class);
                String desc = item.getDesc();
                String title = item.getTitle();
                if (desc.toLowerCase().contains(search.toLowerCase()) ||
                        search.toLowerCase().contains(desc.toLowerCase()) ||
                        title.toLowerCase().contains(search.toLowerCase()) ||
                        search.toLowerCase().contains(title.toLowerCase()))
                    searchList.add(item);
                mAdapter.notifyDataSetChanged();
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
        });
    }
}

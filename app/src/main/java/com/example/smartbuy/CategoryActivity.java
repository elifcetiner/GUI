package com.example.smartbuy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView mItemsList;
    private final List<Item> itemsList = new ArrayList<>();
    private ItemAdapter mAdapter;

    private DatabaseReference mDatabase;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mToolbar = (Toolbar) findViewById(R.id.category_app_bar);
        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        String category = intent.getStringExtra("category");

        getSupportActionBar().setTitle(category);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("items");
        mDatabase.keepSynced(true);
        mItemsList = (RecyclerView) findViewById(R.id.items_list);
        mAdapter = new ItemAdapter(itemsList, getApplicationContext());
        mItemsList.setHasFixedSize(true);
        mItemsList.setLayoutManager(new GridLayoutManager(this, 2));
        mItemsList.setAdapter(mAdapter);
        loadItems(category);

    }

    private void loadItems(final String category) {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Item item = dataSnapshot.getValue(Item.class);
                String cat = item.getCategory();
                if (cat.equals(category))
                    itemsList.add(item);
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

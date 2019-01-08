package com.example.smartbuy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView mWishlist;
    private final List<Item> wishList = new ArrayList<>();
    private ItemAdapter mAdapter;

    private DatabaseReference mWishlistDatabase;
    private DatabaseReference mItemsDatabase;
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        mToolbar = (Toolbar) findViewById(R.id.wishlist_app_bar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Wishlist");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mWishlistDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("wishlist");
        mItemsDatabase = FirebaseDatabase.getInstance().getReference().child("items");

        mWishlist = (RecyclerView) findViewById(R.id.wishlist);
        mAdapter = new ItemAdapter(wishList, getApplicationContext());
        mWishlist.setHasFixedSize(true);
        mWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        mWishlist.setAdapter(mAdapter);
        loadItems();
    }

    private void loadItems() {
        mWishlistDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.i("key", dataSnapshot.getKey());
                String itemId = dataSnapshot.getKey();
                mItemsDatabase.child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Item item = dataSnapshot.getValue(Item.class);
                        wishList.add(item);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

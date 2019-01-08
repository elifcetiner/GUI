package com.example.smartbuy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static android.app.PendingIntent.getActivity;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> mItemsList;
    private Context context;
    private DatabaseReference mUserDatabase, mItemDatabase;
    private FirebaseAuth mAuth;

    public ItemAdapter(List<Item> mItemsList, Context context) {
        this.mItemsList = mItemsList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        Item item= mItemsList.get(position);
        holder.titleText.setText(item.getTitle());
        holder.priceText.setText(item.getCurr_bid());

        final String uid = mAuth.getCurrentUser().getUid();
        final String key = item.getKey();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("wishlist");
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(key))
                    holder.wishlistImage.setImageResource(R.drawable.wished);
                else
                    holder.wishlistImage.setImageResource(R.drawable.wishlist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mItemDatabase = FirebaseDatabase.getInstance().getReference().child("items");
        mItemDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String sid = dataSnapshot.child("sellerid").getValue().toString();
                if (sid.equalsIgnoreCase(uid))
                    holder.wishlistImage.setVisibility(View.GONE);
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

        StorageReference db = FirebaseStorage.getInstance().getReference().child("items").child(key).child("0.jpg");
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(db)
                .into(holder.itemImage);

        holder.wishlistImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserDatabase.child(key)
                        .child("added_on")
                        .setValue(ServerValue.TIMESTAMP)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Item added to wishlist", Toast.LENGTH_LONG).show();
                                    holder.wishlistImage.setImageResource(R.drawable.wished);
                                }
                            }
                        });
            }
        });
        holder.wishlistImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mUserDatabase.child(key)
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Item removed from wishlist", Toast.LENGTH_LONG).show();
                                    holder.wishlistImage.setImageResource(R.drawable.wishlist);
                                }
                            }
                        });
                return true;
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itemIntent = new Intent(context, ItemActivity.class);
                itemIntent.putExtra("key", key);
                context.startActivity(itemIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItemsList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        public TextView titleText, priceText;
        public ImageView itemImage, wishlistImage;
        public View mView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            titleText = (TextView) itemView.findViewById(R.id.item_title);
            priceText = (TextView) itemView.findViewById(R.id.item_price);
            itemImage = (ImageView) itemView.findViewById(R.id.item_image);
            wishlistImage = (ImageView) itemView.findViewById(R.id.item_wishlist);
        }
    }
}

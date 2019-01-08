package com.example.smartbuy;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tmall.ultraviewpager.UltraViewPager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemActivity extends AppCompatActivity {

    private DatabaseReference mItemDatabase, mSellerDatabase;
    private StorageReference mItemStorage;
    private FirebaseAuth mAuth;
    private UltraViewPager mImagesPager;

    private int mImageCount;
    private String count;
    private String pagerState;
    private String itemId;
    private ArrayList<StorageReference> mImagesList;

    private ImageView itemImage;
    private CircleImageView mMapView;
    private TextView sellerName, postTime, basePrice, itemTitle, itemDesc, itemAddress, itemCityPin, itemBids;
    private CardView mItemCard;
    private Button mPlaceBidBtn;
    private EditText mBidText;
    private String currBid;
    private BottomNavigationView bottomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent intent = getIntent();
        final String iid = intent.getStringExtra("key");
        itemId = iid;

        pagerState = "close";

        mAuth = FirebaseAuth.getInstance();
        final String uid = mAuth.getCurrentUser().getUid();


        itemImage = (ImageView) findViewById(R.id.item_main_image);
        mMapView = (CircleImageView) findViewById(R.id.map);
        sellerName = (TextView) findViewById(R.id.seller_name);
        postTime = (TextView) findViewById(R.id.post_time);
        basePrice = (TextView) findViewById(R.id.current_bid);
        itemTitle = (TextView) findViewById(R.id.item_title);
        itemDesc = (TextView) findViewById(R.id.item_desc);
        itemAddress = (TextView) findViewById(R.id.sell_address);
        itemCityPin = (TextView) findViewById(R.id.sell_city);
        itemBids = (TextView) findViewById(R.id.more_bids);
        bottomView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        mItemCard = (CardView) findViewById(R.id.item_details_card);
        mImagesPager = (UltraViewPager) findViewById(R.id.images_pager);

        mBidText = (EditText) findViewById(R.id.bid_text);
        mPlaceBidBtn = (Button) findViewById(R.id.bid_btn);

        mSellerDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mItemStorage = FirebaseStorage.getInstance().getReference().child("items").child(iid);
        mItemDatabase = FirebaseDatabase.getInstance().getReference().child("items").child(iid);
        mItemDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Glide.with(getApplicationContext())
                        .using(new FirebaseImageLoader())
                        .load(mItemStorage.child("0.jpg"))
                        .into(itemImage);
                final Item item = dataSnapshot.getValue(Item.class);
                String title = item.getTitle();
                currBid = item.getCurr_bid();
                mSellerDatabase.child(item.getSellerid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        sellerName.setText(dataSnapshot.child("name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                long time = item.getAdded();
                postTime.setText("Added " + GetTimeAgo.getTimeAgo(time, getApplicationContext()));
                basePrice.setText("₺" + item.getCurr_bid());
                itemTitle.setText(title);
                itemDesc.setText(item.getDesc());
                itemAddress.setText(item.getAddress());
                itemCityPin.setText(item.getCity() + " - " + item.getPincode());
                mMapView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent searchAddress = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + item.getAddress() + "," + item.getCity()));
                        startActivity(searchAddress);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImages();
            }
        });

        mPlaceBidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String bid = mBidText.getText().toString();
                if (TextUtils.isEmpty(bid))
                    mBidText.setError("Please place a valid bid");
                else{
                    int ubid = Integer.parseInt(bid);
                    int cbid = Integer.parseInt(currBid);
                    if (ubid <= cbid)
                        mBidText.setError("Please place a valid bid");
                    else if ((ubid - cbid) < 50)
                        mBidText.setError("Your bid must be at least ₹50 higher than the existing bid");
                    else{
                        mItemDatabase.child("bids").child(uid).setValue(bid).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ItemActivity.this, "Bid placed successfully", Toast.LENGTH_LONG).show();
                                    mBidText.setText("");
                                    mItemDatabase.child("curr_bid").setValue(bid);
                                }
                                else
                                    Toast.makeText(ItemActivity.this, "Oops! Some error occured\nPlease try again later", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });

    }

    private void showImages() {
        pagerState = "open";
        itemImage.setVisibility(View.GONE);
        mImagesList = new ArrayList<>();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mItemCard.getLayoutParams();
        layoutParams.addRule(RelativeLayout.BELOW, R.id.images_pager);
        layoutParams.setMargins(16, 16, 16, 16);

        mImagesPager.setVisibility(View.VISIBLE);
        mImagesPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        mImagesPager.initIndicator();

        mImagesPager.getIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setFocusColor(Color.GREEN)
                .setNormalColor(Color.WHITE)
                .setMargin(16, 16, 16, 16)
                .setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        mImagesPager.getIndicator().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        mImagesPager.getIndicator().build();

        final ImagesAdapter adapter = new ImagesAdapter(ItemActivity.this, mImagesList);
        mImagesPager.setAdapter(adapter);

        mItemDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = dataSnapshot.child("count").getValue().toString();
                mImageCount = Integer.valueOf(count);
//                Log.i("count", mImageCount + " ");
                for (int i = 0; i < mImageCount;i++) {
                    mImagesList.add(mItemStorage.child(i + ".jpg"));
                    adapter.notifyDataSetChanged();
                    mImagesPager.refresh();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        if (pagerState.equalsIgnoreCase("open")){
            mImagesPager.setVisibility(View.GONE);
            itemImage.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mItemCard.getLayoutParams();
            layoutParams.setMargins(16, 450, 16, 16);
            pagerState = "close";
        } else
            super.onBackPressed();
    }
}

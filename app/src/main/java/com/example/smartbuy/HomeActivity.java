package com.example.smartbuy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private CoordinatorLayout layout;

    private EditText searchText;
    private String searchState = "";

    private FirebaseAuth mAuth;
    private DatabaseReference mItemsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private ImageView newCategory, womenCategory, menCategory, saleCategory;

    private RecyclerView mItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.home_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        newCategory = (ImageView) findViewById(R.id.category_New);
        womenCategory = (ImageView) findViewById(R.id.category_Women);
        menCategory = (ImageView) findViewById(R.id.category_Men);
        saleCategory = (ImageView) findViewById(R.id.category_Sale);

        newCategory.setOnClickListener(this);
        womenCategory.setOnClickListener(this);
        menCategory.setOnClickListener(this);
        saleCategory.setOnClickListener(this);

        layout = (CoordinatorLayout) findViewById(R.id.root_layout);

        isConnect(getApplicationContext());

        mItemsDatabase = FirebaseDatabase.getInstance().getReference().child("items");
        mItemsDatabase.keepSynced(true);
        mItemList = (RecyclerView) findViewById(R.id.item_list);
        mItemList.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplication(), 2);
        mItemList.setLayoutManager(mLayoutManager);

        searchText = (EditText) findViewById(R.id.search_edit_text);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String text = searchText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    Intent searchIntent = new Intent(HomeActivity.this, SearchActivity.class);
                    searchIntent.putExtra("search", text);
                    startActivity(searchIntent);
                }
                return true;
            }
        });
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        if (mAuth.getCurrentUser()==null){
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        String uid = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                View header = mNavigationView.getHeaderView(0);
                TextView headerName = (TextView) header.findViewById(R.id.header_user_name);
                CircleImageView headerImage = (CircleImageView) findViewById(R.id.header_user_image);
                headerName.setText(name);
                if (!image.equals("default"))
                    Picasso.get().load(image).placeholder(R.drawable.default_profile).into(headerImage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(false);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.nav_bid:
                                Toast.makeText(HomeActivity.this, getText(R.string.bid), Toast.LENGTH_LONG).show();
                                return true;
                            case R.id.nav_profile:
                                Toast.makeText(HomeActivity.this, getText(R.string.profile), Toast.LENGTH_LONG).show();
                                return true;
                            case R.id.nav_wishlist:
                                startActivity(new Intent(HomeActivity.this, WishlistActivity.class));
                                return true;
                            case R.id.nav_home:
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                return true;
                            case R.id.nav_sell:
                                startActivity(new Intent(HomeActivity.this, SellActivity.class));
                                return true;
                            case R.id.log_out:
                                AuthUI.getInstance().signOut(getApplicationContext());
                                Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                                finish();
                                return true;
                            default:
                                Toast.makeText(HomeActivity.this, "abc", Toast.LENGTH_LONG).show();
                                return true;
                        }
                    }
                });
    }

    private void isConnect(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected)
            layout.setVisibility(View.GONE);
        else
            layout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.search:
                layout.setVisibility(View.GONE);
                searchText.setVisibility(View.VISIBLE);
                searchState = "open";
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (searchState.equalsIgnoreCase("open")) {
            layout.setVisibility(View.VISIBLE);
            searchText.setVisibility(View.INVISIBLE);
            searchState = "close";
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(mItemsDatabase, Item.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull Item model) {
                holder.setTitle(model.getTitle());
                holder.setPrice(model.getCurr_bid());
                final String key = getRef(position).getKey();
                holder.setImage(key, getApplicationContext());
                final ImageView wish = (ImageView) holder.mView.findViewById(R.id.item_wishlist);
                mUserDatabase.child("wishlist").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(key))
                            wish.setImageResource(R.drawable.wished);
                        else
                            wish.setImageResource(R.drawable.wishlist);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mItemsDatabase.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String sid = dataSnapshot.child("sellerid").getValue().toString();
                        if (sid.equalsIgnoreCase(uid))
                            wish.setVisibility(View.GONE);
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

                wish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            mUserDatabase.child("wishlist")
                                    .child(key)
                                    .child("added_on")
                                    .setValue(ServerValue.TIMESTAMP)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(HomeActivity.this, "Item added to wishlist", Toast.LENGTH_LONG).show();
                                                wish.setImageResource(R.drawable.wished);
                                            }
                                        }
                                    });
                    }
                });
                wish.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mUserDatabase.child("wishlist")
                                .child(key)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(HomeActivity.this, "Item removed from wishlist", Toast.LENGTH_LONG).show();
                                            wish.setImageResource(R.drawable.wishlist);
                                        }
                                    }
                                });
                        return true;
                    }
                });
            }
            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_card, parent, false);

                return new ItemViewHolder(view);
            }
        };
        mItemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onClick(View view) {
        Intent categoryIntent = new Intent(HomeActivity.this, CategoryActivity.class);
        switch (view.getId()){
            case R.id.category_New:
                categoryIntent.putExtra("category", getString(R.string.New));
                break;
            case R.id.category_Women:
                categoryIntent.putExtra("category", getString(R.string.Women));
                break;
            case R.id.category_Men:
                categoryIntent.putExtra("category", getString(R.string.Men));
                break;
            case R.id.category_Sale:
                categoryIntent.putExtra("category", getString(R.string.Sale));
                break;
        }
        startActivity(categoryIntent);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView titleText = (TextView) mView.findViewById(R.id.item_title);
            titleText.setText(title);
        }

        public void setPrice(String price) {
            TextView priceText = (TextView) mView.findViewById(R.id.item_price);
            priceText.setText(price);
        }

        public void setImage(String key, Context context) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.item_image);
            StorageReference mainImage = FirebaseStorage.getInstance().getReference().child("items").child(key).child("0.jpg");
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(mainImage)
                    .into(imageView);
        }
    }
}

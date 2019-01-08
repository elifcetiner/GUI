package com.example.smartbuy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SellActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private Spinner spinner;

    private ImageButton mainPicture, sellPicture1, sellPicture2, sellPicture3, sellPicture4;
    private EditText sellTitle, sellPrice, sellDesc, sellAddress, sellPincode, sellNumber;
    private Spinner sellCity;
    private ProgressBar progressBar;
    private ConstraintLayout content;
    String sellCategory, title, price, desc, address, city, pincode, number, uid;
    private static boolean mainPictureCheck = false;

    private Uri mainPictureUri = null, picture1Uri = null, picture2Uri = null, picture3Uri = null, picture4Uri = null;
    private Uri[] picturesUri;
    int pictureCount = 0;

    private static final int MAIN_PICTURE_REQ = 0, PICTURE1_REQ = 1, PICTURE2_REQ = 2, PICTURE3_REQ = 3, PICTURE4_REQ = 4;

    private Button sell;

    private StorageReference filepath;
    private DatabaseReference mItemsDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        mToolbar = (Toolbar) findViewById(R.id.sell_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sell");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        picturesUri = new Uri[5];
        progressBar = (ProgressBar) findViewById(R.id.bar);
        content = (ConstraintLayout) findViewById(R.id.content);

        //Firebase Objects
        mAuth = FirebaseAuth.getInstance();
        filepath = FirebaseStorage.getInstance().getReference().child("items");
        mItemsDatabase = FirebaseDatabase.getInstance().getReference().child("items");

        //EditText fields
        sellTitle = (EditText) findViewById(R.id.sell_title);
        sellPrice = (EditText) findViewById(R.id.sell_price);
        sellDesc = (EditText) findViewById(R.id.sell_desc);
        sellAddress = (EditText) findViewById(R.id.sell_address);
        sellPincode = (EditText) findViewById(R.id.sell_pincode);
        sellNumber = (EditText) findViewById(R.id.sell_number);

        //ImageButtons
        mainPicture = (ImageButton) findViewById(R.id.sell_main_picture);
        sellPicture1 = (ImageButton) findViewById(R.id.sell_picture1);
        sellPicture2 = (ImageButton) findViewById(R.id.sell_picture2);
        sellPicture3 = (ImageButton) findViewById(R.id.sell_picture3);
        sellPicture4 = (ImageButton) findViewById(R.id.sell_picture4);

        //Sell Button
        sell = (Button) findViewById(R.id.sell_btn);

        sellCity = (Spinner) findViewById(R.id.sell_city);
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.turkey_top_places, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sellCity.setAdapter(cityAdapter);
        sellCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Start Category Spinner
        spinner = (Spinner) findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_spinner, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(categoryAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                sellCategory = parent.getItemAtPosition(pos).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //End Category Spinner

        mainPicture.setOnClickListener(this);
        sellPicture1.setOnClickListener(this);
        sellPicture2.setOnClickListener(this);
        sellPicture3.setOnClickListener(this);
        sellPicture4.setOnClickListener(this);

        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellItem();
                progressBar.setVisibility(View.VISIBLE);
                sell.setVisibility(View.GONE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    private void sellItem() {

        title = sellTitle.getText().toString().trim();
        price = sellPrice.getText().toString().trim();
        uid = mAuth.getCurrentUser().getUid();
        desc = sellDesc.getText().toString().trim();
        address = sellAddress.getText().toString().trim();
        pincode = sellPincode.getText().toString().trim();
        number = sellNumber.getText().toString().trim();

        if (checkItem()) {
            String itemId = mItemsDatabase.push().getKey();
            Map itemMap = new HashMap();
            mItemsDatabase = mItemsDatabase.child(itemId);
            filepath = filepath.child(itemId);
            itemMap.put("title", title);
            itemMap.put("curr_bid", price);
//            itemMap.put("bids", "0");
            itemMap.put("sellerid", uid);
            itemMap.put("added", ServerValue.TIMESTAMP);
            itemMap.put("address", address);
            itemMap.put("city", city);
            itemMap.put("pincode", pincode);
            itemMap.put("number", number);
            itemMap.put("desc", desc);
            itemMap.put("category", sellCategory);
            itemMap.put("key", itemId);
            itemMap.put("count", String.valueOf(pictureCount));
            mItemsDatabase.setValue(itemMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    uploadImages();
                }
            });
        }

    }

    private void uploadImages() {
        filepath.child("0.jpg").putFile(picturesUri[0]).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                for (int i = 1; i < pictureCount; i++) {
                    filepath.child(i + ".jpg").putFile(picturesUri[i]).addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            failure();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            failure();
                        }
                    });
                }
                Toast.makeText(SellActivity.this, "Item added Successfully", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(SellActivity.this, HomeActivity.class));
                finish();
            }
        });

    }

    private void failure() {
        mItemsDatabase.removeValue();
        filepath.delete();
        Toast.makeText(SellActivity.this, "Your item could not be added \nPlease try again later", Toast.LENGTH_LONG).show();
    }

    private boolean checkItem() {

        boolean titleCheck = false;
        boolean priceCheck = false;
        boolean descCheck = false;
        boolean categoryCheck = false;
        boolean addressCheck = false;
        boolean cityCheck = false;
        boolean pincodeCheck = false;
        boolean numberCheck = false;

        //Main Picture Check
        if (!mainPictureCheck)
            Toast.makeText(SellActivity.this, "Main picture is required", Toast.LENGTH_LONG).show();

        //Title Check
        if (TextUtils.isEmpty(title))
            sellTitle.setError("Title is required");
        else if (title.length() > 255)
            sellTitle.setError("Too long title");
        else
            titleCheck = true;

        //Price Check
        if (TextUtils.isEmpty(price))
            sellPrice.setError("Price is rewuired");
        else if (Double.parseDouble(price) < 0)
            sellPrice.setError("Invalid Price");
        else
            priceCheck = true;

        //Desc Check
        if (TextUtils.isEmpty(desc))
            sellDesc.setError("Description is required");
        else if (desc.length() > 2000)
            sellDesc.setError("Too long description");
        else descCheck = true;

        //Category Check
        if (sellCategory.equalsIgnoreCase("Choose a Category"))
            Toast.makeText(SellActivity.this, "Please select a category", Toast.LENGTH_LONG).show();
        else
            categoryCheck = true;

        //Address Check
        if (TextUtils.isEmpty(address))
            sellAddress.setError("Pick up address is required");
        else
            addressCheck = true;

        //City Check
        if (city.equalsIgnoreCase("Choose a City"))
            Toast.makeText(SellActivity.this, "Please select city", Toast.LENGTH_LONG).show();
        else
            cityCheck = true;

        //Pincode Check
        if (TextUtils.isEmpty(pincode))
            sellPincode.setError("Pincode is required");
        else if (pincode.length() != 6)
            sellPincode.setError("Invalid Pincode");
        else
            pincodeCheck = true;

        //Number Check
        if (TextUtils.isEmpty(number))
            sellNumber.setError("Contact Number is required");
        else if (number.length() != 10)
            sellNumber.setError("Please provide a valid 10 digit mobile number");
        else
            numberCheck = true;

        if (mainPictureCheck && titleCheck && categoryCheck && priceCheck && descCheck && addressCheck && cityCheck && pincodeCheck && numberCheck)
            return true;
        else
            return false;

    }

    @Override
    public void onClick(View view) {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        switch (view.getId()) {
            case R.id.sell_main_picture:
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), MAIN_PICTURE_REQ);
                break;
            case R.id.sell_picture1:
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), PICTURE1_REQ);
                break;
            case R.id.sell_picture2:
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), PICTURE2_REQ);
                break;
            case R.id.sell_picture3:
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), PICTURE3_REQ);
                break;
            case R.id.sell_picture4:
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), PICTURE4_REQ);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MAIN_PICTURE_REQ:
                    mainPictureUri = data.getData();
                    if (mainPictureUri != null)
                        picturesUri[pictureCount++] = mainPictureUri;
                    mainPictureCheck = true;
                    mainPicture.setPadding(0, 0, 0, 0);
                    Picasso.get().load(mainPictureUri).placeholder(R.drawable.ic_add).fit().into(mainPicture);
                    break;
                case PICTURE1_REQ:
                    picture1Uri = data.getData();
                    if (picture1Uri != null)
                        picturesUri[pictureCount++] = picture1Uri;
                    sellPicture1.setPadding(0, 0, 0, 0);
                    Picasso.get().load(picture1Uri).placeholder(R.drawable.ic_add).fit().into(sellPicture1);
                    break;
                case PICTURE2_REQ:
                    picture2Uri = data.getData();
                    if (picture2Uri != null)
                        picturesUri[pictureCount++] = picture2Uri;
                    sellPicture2.setPadding(0, 0, 0, 0);
                    Picasso.get().load(picture2Uri).placeholder(R.drawable.ic_add).fit().into(sellPicture2);
                    break;
                case PICTURE3_REQ:
                    picture3Uri = data.getData();
                    if (picture3Uri != null)
                        picturesUri[pictureCount++] = picture3Uri;
                    sellPicture3.setPadding(0, 0, 0, 0);
                    Picasso.get().load(picture3Uri).placeholder(R.drawable.ic_add).fit().into(sellPicture3);
                    break;
                case PICTURE4_REQ:
                    picture4Uri = data.getData();
                    if (picture4Uri != null)
                        picturesUri[pictureCount++] = picture4Uri;
                    sellPicture4.setPadding(0, 0, 0, 0);
                    Picasso.get().load(picture4Uri).placeholder(R.drawable.ic_add).fit().into(sellPicture4);
                    break;
                default:
                    Toast.makeText(SellActivity.this, "Could not load image", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}

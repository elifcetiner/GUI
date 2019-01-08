package com.example.smartbuy;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BidAlertDialog extends DialogFragment {

    private DatabaseReference mItemBidDatabase;
    private Bundle bundle;
    private AlertDialog.Builder builder;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        bundle = getArguments();

        builder = new AlertDialog.Builder(getActivity());
        showDialog();

        return builder.create();
    }

    private void showDialog() {
        String iid = bundle.getString("key");
        final String uid = bundle.getString("uid");
        mItemBidDatabase = FirebaseDatabase.getInstance().getReference().child("items").child(iid);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_layout, null, false))
                .setPositiveButton("Bid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final EditText bidText = (EditText) ((AlertDialog) dialog).findViewById(R.id.bid_edit_text);
                        final String bid = bidText.getText().toString();
                        mItemBidDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String currBid = dataSnapshot.child("curr_bid").getValue().toString();
                                if (Integer.valueOf(currBid) > Integer.valueOf(bid)){
                                    bidText.setError("Please enter a valid bid");
                                    dialog.cancel();
                                    showDialog();
                                } else {
                                    mItemBidDatabase.child("bids").child(uid).setValue(bid);
                                    mItemBidDatabase.child("curr_bid").setValue(bid);
                                    dialog.cancel();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
    }
}

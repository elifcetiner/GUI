package com.example.smartbuy;

public class Item {
    public String title;
    public String curr_bid;
    public String address;
//    public String bids;
    public String category;
    public String city;
    public String desc;
    public String number;
    public String pincode;
    public String sellerid;
    public String key;
    public String count;
    public long added;

    public Item(){}

    public Item(String title, String curr_bid, long added, String address, /*String bids,*/ String category, String city, String desc, String number, String pincode, String sellerid, String count) {
        this.title = title;
        this.curr_bid = curr_bid;
        this.added = added;
        this.address = address;
//        this.bids = bids;
        this.category = category;
        this.city = city;
        this.desc = desc;
        this.number = number;
        this.pincode = pincode;
        this.sellerid = sellerid;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurr_bid() {
        return curr_bid;
    }

    public void setCurr_bid(String curr_bid) {
        this.curr_bid = curr_bid;
    }

    public long getAdded() {
        return added;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

//    public String getBids() {
//        return bids;
//    }
//
//    public void setBids(String bids) {
//        this.bids = bids;
//    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getSellerid() {
        return sellerid;
    }

    public void setSellerid(String sellerid) {
        this.sellerid = sellerid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}

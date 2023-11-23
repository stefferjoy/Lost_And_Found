package com.ls.lostfound.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class LostAndFoundItem {


    private String lostId;
    private String foundId;
    private String documentId;
    private String userId; // User who created the post
    private String itemName;
    private String userName;
    private String description;
    private String location;
    private String date;
    private String localImagePath;
    private String firebaseImageUrl;
    private String address;
    private double latitude;
    private double longitude;

    public LostAndFoundItem() {
        // Default no-argument constructor required by Firebase Firestore
    }



    public LostAndFoundItem(String userId, String userName, String itemName, String description, String location, String date, String localImagePath, String firebaseImageUrl,double latitude, double longitude) {

        this.documentId = documentId;
        this.userId = userId;
        this.userName = userName;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.date = date;
        this.localImagePath = localImagePath;
        this.firebaseImageUrl = firebaseImageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters for the model properties


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getLostId() {
        return lostId;
    }

    public void setLostId(String lostId) {
        this.lostId = lostId;
    }

    public String getFoundId() {
        return foundId;
    }

    public void setFoundId(String foundId) {
        this.foundId = foundId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public String getFirebaseImageUrl() {
        return firebaseImageUrl;
    }

    public void setFirebaseImageUrl(String firebaseImageUrl) {
        this.firebaseImageUrl = firebaseImageUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // New method to set the correct ID based on item status
    public void generateIdForStatus(boolean isLostItem) {
        if (isLostItem) {
            this.lostId = UUID.randomUUID().toString();
        } else {
            this.foundId = UUID.randomUUID().toString();
        }
    }


}

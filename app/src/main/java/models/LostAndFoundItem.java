package models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class LostAndFoundItem {
    private String postId;
    private String userId; // User who created the post
    private String itemName;
    private String description;
    private String location;
    private String date;
    private String localImagePath;
    private String firebaseImageUrl;

    public LostAndFoundItem() {
        // Default no-argument constructor required by Firebase Firestore
    }

    public LostAndFoundItem(String userId, String itemName, String description, String location, String date, String localImagePath, String firebaseImageUrl) {
        this.postId = UUID.randomUUID().toString(); // Generate a unique post ID
        this.userId = userId;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.date = date;
        this.localImagePath = localImagePath;
        this.firebaseImageUrl = firebaseImageUrl;
    }

    // Getters and setters for the model properties

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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


    /*public static List<LostAndFoundItem> getSampleData() {
        List<LostAndFoundItem> sampleData = new ArrayList<>();

        // Create sample LostAndFoundItem objects and add them to the list
        sampleData.add(new LostAndFoundItem("User1", "Item 1", "Description 1", "Location 1", "Date 1", "imagePath1"));
        //sampleData.add(new LostAndFoundItem("User2", "Item 2", "Description 2", "Location 2", "Date 2", "imagePath2"));

        return sampleData;
    }*/

    public void saveItem() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("lost_and_found_items");

        // Push the item to generate a unique key
        DatabaseReference newItemRef = databaseReference.push();
        setPostId(newItemRef.getKey()); // Set the post ID generated by Firebase

        // Save the item to Firebase
        newItemRef.setValue(this)
                .addOnSuccessListener(aVoid -> {
                    // Item saved successfully
                    // You can show a success message to the user or navigate to a different screen.
                })
                .addOnFailureListener(e -> {
                    // Item save failed
                    // Handle the error or show an error message to the user.
                });
    }
}

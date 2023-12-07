package com.ls.lostfound.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ls.lostfound.R;
import com.ls.lostfound.models.LostAndFoundItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String chatId;
    private String receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        adapter = new MessageAdapter(messages);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Receive data from Intent
        receiverUserId = getIntent().getStringExtra("RECEIVER_USER_ID");
        chatId = getIntent().getStringExtra("CHAT_ID");

        // Now fetch chat history
        if (chatId != null && !chatId.isEmpty()) {
            fetchChatHistory(chatId);
        } else {
            Log.e(TAG, "Chat ID is null or empty");
            prepareAndSendMessage();
        }

        // Log received data for debugging
        Log.d(TAG, "Received Receiver User ID: " + receiverUserId);
        Log.d(TAG, "Received Chat ID: " + chatId);

        Log.d(TAG, "Received Receiver User ID: " + receiverUserId);

        sendButton.setOnClickListener(v -> prepareAndSendMessage());
    }


    private void prepareAndSendMessage() {
        String text = messageEditText.getText().toString();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check if the text is empty or receiverUserId is null or empty
        if (text.isEmpty() || receiverUserId == null || receiverUserId.isEmpty()) {
            Log.e(TAG, "Text is empty or Receiver User ID is null or empty");
            Log.e(TAG, "Receiver User ID : "+receiverUserId);
            // Handle the error, maybe show a Toast to the user
            return;
        } else {
            // Generate chat room ID if it's not already set
            if (chatId == null) {
                chatId = generateChatRoomId(currentUserId, receiverUserId);
                setupChat(); // Setup the chat after determining the chat ID
            }
            fetchSenderNameAndSendMessage(currentUserId, text, chatId);
        }

    }

    private String generateChatRoomId(String userId1, String userId2) {
        List<String> userIds = Arrays.asList(userId1, userId2);
        Collections.sort(userIds);
        return userIds.get(0) + "_" + userIds.get(1);
    }


    private void fetchSenderNameAndSendMessage(String currentUserId, String text, String chatRoomId) {
        db.collection("userProfiles").document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String senderName = document.getString("name"); // Assuming 'name' is the field
                            sendMessage(senderName, text, chatRoomId);
                        } else {
                            Log.d(TAG, "No user profile found");
                            // Optionally send the message with a default name or handle it accordingly
                        }
                    } else {
                        Log.e(TAG, "Error fetching user profile", task.getException());
                        // Handle the error appropriately
                    }
                });
    }

    private void sendMessage(String senderName, String text, String chatRoomId) {
        Date currentTime = new Date();
        Message newMessage = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), senderName, text, currentTime);

        db.collection("chats").document(chatRoomId).collection("messages").add(newMessage)
                .addOnSuccessListener(documentReference -> {
                    messages.add(newMessage); // Add new message to list
                    adapter.notifyDataSetChanged(); // Notify adapter about data change
                    messageEditText.setText(""); // Clear the input field
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending message", e));
    }


    private void setupChat() {
        DocumentReference chatRef = FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId);
        listenForMessages(chatRef);
        setupChat(); // Now we are sure chatId is not null

    }

    private void listenForMessages(DocumentReference chatRef) {
        chatRef.collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            Message newMessage = documentChange.getDocument().toObject(Message.class);
                            messages.add(newMessage); // Add new message to list
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter about data change
                });
    }

    private void fetchChatHistory(String chatId) {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Message message = document.toObject(Message.class);
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

}

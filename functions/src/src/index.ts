import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

exports.createNotificationOnNewItem = functions.firestore
    .document('lostAndFoundItems/{itemId}')
    .onCreate(async (snap: functions.firestore.DocumentSnapshot, context: functions.EventContext) => {
        const newPost = snap.data();

        if (!newPost) {
            console.log('No data found in the snapshot!');
            return;
        }

        const itemStatus = newPost.lostId ? 'Lost' : 'Found';
        const notificationTitle = itemStatus === 'Lost' ? 'Lost item added' : 'Found item added';
        const notificationMessage = `${newPost.userName} ${itemStatus.toLowerCase()} an item: ${newPost.itemName}`;

        // Notification for Firestore
        const firestoreNotification = {
            title: notificationTitle,
            message: notificationMessage,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            userId: newPost.userId
        };

        // Add the notification to the Firestore 'notifications' collection
        await admin.firestore().collection('notifications').add(firestoreNotification);

        
        // Create the FCM payload
        const payload = {
            notification: {
                title: notificationTitle,
                body: notificationMessage
            },
            topic: "allUsers" // Sends the notification to all users subscribed to this topic
        };

        // Send a message to devices subscribed to the provided topic
        try {
            await admin.messaging().send(payload);
            console.log("Notification sent successfully");
        } catch (error) {
            console.error("Error sending notification", error);
        }
    });

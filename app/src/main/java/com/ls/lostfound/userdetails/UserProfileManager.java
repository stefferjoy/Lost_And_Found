package com.ls.lostfound.userdetails;

public class UserProfileManager {
    private static UserProfile userProfile;

    public static UserProfile getUserProfile() {
        if (userProfile == null) {
            userProfile = new UserProfile();
        }
        return userProfile;
    }

    public static void setUserProfile(UserProfile profile) {
        userProfile = profile;
    }
}


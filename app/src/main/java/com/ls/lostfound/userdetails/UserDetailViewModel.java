package com.ls.lostfound.userdetails;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserDetailViewModel extends ViewModel {
    // LiveData or fields to hold user details
    private MutableLiveData<String> name = new MutableLiveData<>();
    private MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    // Other details...

    // Getters and Setters for each detail...
}


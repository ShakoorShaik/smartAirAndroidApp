package com.example.smartair.provider.utils;

import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProviderDataReading {



    /*
    query for user
    query user subcollection linkedParents
    read the parentUid

    query parentUid search for array linkedChildren
    creates widget in scroll view containing children name and next to them a view child button
     */

    /*
    upon clicking view child button
    logs child uid and settings value to the ProviderCurrentChildData
     */

    /*
    read ProviderCurrentChildData load viewable base on child setting values
    change textfields to display currently viewing child's name
     */

    /*
    query subcollection with ProviderCurrentChildData load the document that contains dates
    write dates as a field and loads the remaining data as a field

    on the xml it will create a widget inside the scroll that displays the date
    upon clicking the date inside the scroll wheel it will give a popout for the user
    that shows data fields
     */

    /*
    query subcollection for documents that contains dates
    organize them in a date format

    on the xml it will create a widget inside the scroll that displays the date
    upon clicking the date inside the scroll wheel it will give a popout for the user
    that shows data fields
     */

    private ProviderCurrentParentData parentData;

    private ProviderCurrentChildData childData;


    public interface ParentUidCallback {
        void onSuccess(Object parentData);
        void onFailure(String message);
    }

    public interface ChildUidCallback {
        void onSuccess(Object childData);
        void onFailure(String message);
    }

    public void getParentUid(ParentUidCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            callback.onFailure("User is null");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("linkedParents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot parentDoc = task.getResult().getDocuments().get(0);

                            String parentUid = parentDoc.getId();

                            String parentEmail = parentDoc.getString("email");

                            if (parentEmail == null) {
                                callback.onFailure("Email field not found in parent document");
                                return;
                            }

                            if (parentData == null) {
                                parentData = new ProviderCurrentParentData();
                            }

                            parentData.parentUid = parentUid;
                            parentData.parentEmail = parentEmail;

                            callback.onSuccess(parentData);
                        } else {
                            callback.onFailure("No parent linked");
                        }
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }


}

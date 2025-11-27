package utils;

import android.content.Context;
import android.content.SharedPreferences;

/*
----------------------------------------------------------------
Whenever you want to store data on a Child's document/
collection, please use this function to save the child's
uid when accessing the child's dashboard through the parent's
account. This is to prevent any data from being stored in
the parent's document/collection instead of the child's.

The implementation should look like this:

Inside the onClick function before you store data, do this:

    ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        if (!curr_child_id.equals("NA")) {
            // code to store data in database goes here,
            // make sure to use curr_child_id :)
        } else {
            // code to store data in database the normal way
            // (usually using getCurrentUser() and getUid())
            // goes here :)
        }

If you're still confused about how to implement this,
check class LogUsageActivity, lines 251-255 (pls dont change)

----------------------------------------------------------------


*/


public class ChildIdManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String NAME = "SmartAirPref";
    private static final String CHILD_ID = "curr_child_id";

    /**
     * Constructor for ChildIdManager.
     * @param context - the current context/activity
     */
    public ChildIdManager(Context context) {
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Saves the child ID
     * @param childId - the child ID you want to save.
     */
    public void SaveChildId(String childId) {
        editor.putString(CHILD_ID, childId);
        editor.apply();
    }

    /**
     * clears the saved child ID.
     */
    public void clearChildId() {
        editor.remove(CHILD_ID);
        editor.apply();
    }

    /**
     * Returns the saved child ID
     * @return the saved child ID if it exists, NA otherwise.
     */
    public String getChildId() {
        return sharedPreferences.getString(CHILD_ID, "NA");
    }
}

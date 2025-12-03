package utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SnippetManager {

    public interface SnippetCallback {
        void onSuccess(LineData lineData);
        void onFailure(Exception e);
    }
    public static void getPastScaleDaysData(int scale, SnippetCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection("users")
                .whereEqualTo("parentUid", user.getUid())
                .get()
                .addOnSuccessListener(task -> {
                    List<DocumentReference> userRefs = new ArrayList<>();

                    for (DocumentSnapshot userDoc : task.getDocuments()) {
                        userRefs.add(userDoc.getReference());
                    }

                    List<String> days = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar cal = Calendar.getInstance();

                    for (int i = 0; i < scale; i++) {
                        days.add(sdf.format(cal.getTime()));
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                    }

                    if (userRefs.isEmpty()) {
                        LineData emptyLineData = new LineData(new LineDataSet(new ArrayList<>(), "Total symptoms/days ago"));
                        callback.onSuccess(emptyLineData);
                        return;
                    }

                    int[] dayTotals = new int[days.size()];

                    int totalRequests = days.size() * userRefs.size();  //since we are getting data for every child
                    AtomicInteger requestsCompletedCount = new AtomicInteger(0);

                    for (int dayIndex = 0; dayIndex < days.size(); dayIndex++) {
                        String dayId = days.get(dayIndex);
                        int finalDayIndex = dayIndex;

                        for (DocumentReference userRef : userRefs) {
                            userRef.collection("symptomLogs")
                                    .document(dayId)
                                    .get()
                                    .addOnSuccessListener(ds -> {
                                        if (ds.exists()) {
                                            Map<String, Object> data = ds.getData();
                                            if (data != null) {
                                                dayTotals[finalDayIndex] += data.size();
                                            }
                                        }

                                        if (requestsCompletedCount.incrementAndGet() == totalRequests) {
                                            finishAndCallback(scale, days, dayTotals, callback);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (requestsCompletedCount.incrementAndGet() == totalRequests) {
                                            finishAndCallback(scale, days, dayTotals, callback);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    private static void finishAndCallback(int scale, List<String> days,
                                          int[] dayTotals,
                                          SnippetCallback callback) {
        List<Entry> lineEntries = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            lineEntries.add(new Entry(i, dayTotals[i]));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Total symptoms/days ago");
        lineDataSet.setDrawValues(false);
        LineData lineData = new LineData(lineDataSet);

        callback.onSuccess(lineData);
    }
}

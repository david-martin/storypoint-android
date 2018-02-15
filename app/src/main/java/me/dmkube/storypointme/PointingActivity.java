package me.dmkube.storypointme;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

public class PointingActivity extends AppCompatActivity {

    private PointingApplication pointingApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointing);

        SharedPreferences settings = getSharedPreferences("appPrefs", 0);
        String sessionId = settings.getString("sessionID", null);
        String name = settings.getString("name", null);
        if (sessionId == null || sessionId.isEmpty()) {
            Log.d("app", String.format("finishing activity sessionId=%s", sessionId));
            finish();
        }
        if (name == null || name.isEmpty()) {
            Log.d("app", String.format("finishing activity name=%s", name));
            finish();
        }

        Pointer me = new Pointer(name);
        pointingApplication = new PointingApplication(sessionId, me);

        final ListView pointersListView = findViewById(R.id.pointersListView);
        ArrayAdapter<Pointer> pointersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pointingApplication.getPointers());

        pointingApplication.setEventListener(new PointingEventListener(){
            @Override
            public void onPointers(JSONObject event) {
                runOnUiThread(() -> {
                    pointersListAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onScore(JSONObject event) {
                runOnUiThread(() -> {
                    pointersListAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onShow(JSONObject event) {
                pointingApplication.showPointerScores();
                runOnUiThread(() -> {
                    pointersListAdapter.notifyDataSetChanged();
                });
                updateStoryDescription();
            }
            @Override
            public void onClear(JSONObject event) {
                pointingApplication.clearPointerScores();
                runOnUiThread(() -> {
                    pointersListAdapter.notifyDataSetChanged();
                });
                updateStoryDescription();
            }

            @Override
            public void onExit(String reason) {
                Log.d("app", String.format("PointingApplication exited with reason (%s)", reason));
                finish();
            }
        });

        pointersListView.setAdapter(pointersListAdapter);
        pointingApplication.start();
        updateStoryDescription();
    }

    @Override
    public void finish() {
        super.finish();
        if (pointingApplication != null) {
            pointingApplication.finish();
        }
    }

    private void updateStoryDescription() {
        // TODO: This doesn't actually have the story description, as it doesn't seem to be implemented/working on server
        TextView storyDescriptionView = findViewById(R.id.storyDescriptionView);
        runOnUiThread(() -> storyDescriptionView.setText(String.format("Name: %s\nSession ID: %s\nAverage : %s", pointingApplication.getPointer().getName(), pointingApplication.getSessionId(), pointingApplication.calculateAverageScores())));

    }
}

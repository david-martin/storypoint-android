package me.dmkube.storypointme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class PointingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointing);

        // TODO: read story description from server and set it here
        TextView storyDescriptionView = findViewById(R.id.storyDescriptionView);
        storyDescriptionView.setText("some description");

        final ListView pointersListView = findViewById(R.id.pointersListView);
        final List<String> pointers = new ArrayList<>();
        ArrayAdapter<String> pointersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pointers);

        // TODO: allow setting of session in different activity before showing this activity
        String sessionId = "android_test_session";
        // TODO: allow setting of name in different activity before showing this activity
        String name = "android_user_" + (new Date()).getTime();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(String.format("wss://api.storypoint.me/session/%s?name=%s", sessionId, name)).build();
        WebSocket ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("app", "WebSocket open");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("app", "Receiving text: " + text);
                try {
                    JSONObject event = new JSONObject(text);
                    String eventType = event.getString("event");
                    if ("pointers".equals(eventType)) {
                        pointers.clear();
                        JSONArray points = event.getJSONArray("points");
                        for (int i = 0, l = points.length(); i < l; i++) {
                            JSONObject pointer = points.getJSONObject(i);
                            String name = pointer.getString("name");
                            String score = pointer.optString("score", "-");
                            // TODO: abstract this formatting of a row into a method
                            pointers.add(String.format("%s : %s", name, score));
                        }
                        runOnUiThread(() -> {
                            pointersListAdapter.notifyDataSetChanged();
                        });
                    } else if ("score".equals(eventType)) {
                        String name = event.getString("name");
                        String score = event.getString("score");
                        ListIterator<String> it = pointers.listIterator();
                        boolean found = false;
                        while (it.hasNext() && !found) {
                            String pointerText = it.next();
                            Log.d("app", String.format("pointerText=%s", pointerText));

                            // TODO: abstract a user/score pairing into an object that can be compared for equality more easily than a formatted string
                            String startOfPointerText = String.format("%s : ", name);
                            if (pointerText.startsWith(startOfPointerText)) {
                                Log.d("app", String.format("found string for startOfPointerText=%s", startOfPointerText));
                                found = true;
                                // TODO: abstract this formatting of a row into a method
                                // TODO: mask the score until the 'show' event happens
                                it.set(String.format("%s : %s", name, score));
                                runOnUiThread(() -> {
                                    pointersListAdapter.notifyDataSetChanged();
                                });
                            }
                        }
                    } else if ("show".equals(eventType)) {
                        // TODO: calculate average & make scores visible
                    } else if ("clear".equals(eventType)) {
                        // TODO: clear all user scores
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d("app", "Receiving bytes: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("app", "Closing : " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("app", "Error : " + t.getMessage());
            }
        });
        client.dispatcher().executorService().shutdown();

        pointersListView.setAdapter(pointersListAdapter);
    }
}

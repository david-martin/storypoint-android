package me.dmkube.storypointme;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by dmartin on 14/02/18.
 */

public class PointingApplication {

    public static final String STORYPOINT_WS_ENDPOINT = "wss://api.storypoint.me/session/%s?name=%s";
    private String sessionId;
    private Pointer pointer;
    private List<Pointer> pointers;
    private boolean showingScores = false;
    private PointingEventListener listener;

    /**
     *
     * @param sessionId the storypoint.me session id
     * @param pointer the current user/pointer of this app. Automatically added ot the pointers Set
     */
    public PointingApplication(String sessionId, Pointer pointer) {
        this.sessionId = sessionId;
        this.pointer = pointer;
        this.pointers = new ArrayList<>();
        pointers.add(pointer);
    }

    public void start() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(String.format(STORYPOINT_WS_ENDPOINT, sessionId, pointer.getName())).build();
        WebSocket ws = client.newWebSocket(request, new WebSocketListener() {
            // TODO: is it necessary to override any other methods?
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
                            String score = pointer.optString("score", null);
                            pointers.add(new Pointer(name, score));
                        }
                        listener.onPointers(event);
                    } else if ("score".equals(eventType)) {
                        String name = event.getString("name");
                        String score = event.getString("score");

                        for (Pointer eventPointer : pointers) {
                            Log.d("app", String.format("pointer=%s", eventPointer.toString()));

                            if (eventPointer.getName().equals(name)) {
                                eventPointer.setScore(score);
                                break;
                            }
                        }

                        listener.onScore(event);
                    } else if ("show".equals(eventType)) {
                        listener.onShow(event);
                    } else if ("clear".equals(eventType)) {
                        listener.onClear(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("app", String.format("WebSocket onFailure %s", t.getMessage()));
            }
        });
        client.dispatcher().executorService().shutdown();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isShowingScores() {
        return showingScores;
    }

    public void setShowingScores(boolean showingScores) {
        this.showingScores = showingScores;
    }

    public List<Pointer> getPointers() {
        return pointers;
    }

    public void setPointers(List<Pointer> pointers) {
        this.pointers = pointers;
    }

    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
    }

    /**
     *
     * @return the average score from all Pointers, as a String.
     *         Only calculated and returned if scores should be shown, otherwise is an empty string/placeholder
     */
    public String calculateAverageScores() {
        if (!showingScores) {
            return "-";
        }

        int average = 0;
        // TODO: calculate the avg

        return String.valueOf(average);
    }

    public void setEventListener(PointingEventListener listener) {
        this.listener = listener;
    }
}

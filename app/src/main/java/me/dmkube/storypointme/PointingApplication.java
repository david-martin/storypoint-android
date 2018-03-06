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
    private PointingEventListener listener;
    private WebSocket ws;
    private boolean showingScores = false;

    /**
     *
     * @param sessionId the storypoint.me session id
     * @param pointer the current user/pointer of this app. Automatically added ot the pointers Set
     */
    public PointingApplication(String sessionId, Pointer pointer) {
        this.sessionId = sessionId;
        this.pointer = pointer;
        this.pointers = new ArrayList<>();
        pointer.setIsMe(true);
        pointers.add(pointer);
    }

    public void start() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(String.format(STORYPOINT_WS_ENDPOINT, sessionId, pointer.getName())).build();
        ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("app", "Receiving text: " + text);
                try {
                    JSONObject event = new JSONObject(text);
                    // TODO: is switch statement OK/better here instead?
                    String eventType = event.getString("event");
                    if ("pointers".equals(eventType)) {
                        pointers.clear();
                        JSONArray points = event.getJSONArray("points");
                        for (int i = 0, l = points.length(); i < l; i++) {
                            JSONObject jPointer = points.getJSONObject(i);
                            String name = jPointer.getString("name");
                            String score = jPointer.optString("score", null);
                            Pointer tmpPointer = new Pointer(name, score);

                            // need to set if it's us, so our score won't be obfuscated
                            if (tmpPointer.getName().equals(pointer.getName())) {
                                tmpPointer.setIsMe(true);
                            }
                            pointers.add(tmpPointer);
                        }
                        listener.onPointers(event);
                    } else if ("score".equals(eventType)) {
                        String name = event.getString("name");
                        String score = event.getString("score");

                        for (Pointer eventPointer : pointers) {
                            Log.d("app", String.format("pointer=%s", eventPointer.getName()));

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
                    } else {
                        Log.d("app", String.format("Unknown eventType %s event=%s", eventType, event));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                String error = String.format("WebSocket onFailure %s", t.getMessage());
                Log.d("app", error);
                listener.onExit(error);
            }
        });
        client.dispatcher().executorService().shutdown();
    }

    public void finish() {
        if (ws != null) {
            ws.close(1000, "Finishing Application");
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

        int totalOfScores = 0;
        int totalNumberOfValidScorers = 0;
        for (Pointer tmpPointer: pointers) {
            try {
                int score = Integer.valueOf(tmpPointer.getScore()).intValue();
                totalOfScores += score;
                totalNumberOfValidScorers++;
            } catch (NumberFormatException e) {
                // Ignore
                Log.d("app", String.format("Ignoring NumberFormatException for score=%s", tmpPointer.getScore()));
            }
        }

        int average = Math.round((float) totalOfScores / (float) totalNumberOfValidScorers);

        return String.valueOf(average);
    }

    public void setEventListener(PointingEventListener listener) {
        this.listener = listener;
    }

    public void showPointerScores() {
        showingScores = true;
        for (Pointer tmpPointer: pointers) {
            tmpPointer.setObfuscateScore(false);
        }
    }

    public void clearPointerScores() {
        showingScores = false;
        for (Pointer tmpPointer: pointers) {
            tmpPointer.reset();
        }
    }

    public void setPointerScore(String pointerScore) {
        this.pointer.setScore(pointerScore);

        // {"event":"score","score":"34"}
        JSONObject scoreObject = new JSONObject();
        try {
            scoreObject.put("event", "score");
            scoreObject.put("score", pointerScore);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        ws.send(scoreObject.toString());
    }
}

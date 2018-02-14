package me.dmkube.storypointme;

import org.json.JSONObject;

import java.util.EventListener;

/**
 * Created by dmartin on 14/02/18.
 */

public interface PointingEventListener extends EventListener {
    void onPointers(JSONObject event);
    void onScore(JSONObject event);
    void onShow(JSONObject event);
    void onClear(JSONObject event);
}

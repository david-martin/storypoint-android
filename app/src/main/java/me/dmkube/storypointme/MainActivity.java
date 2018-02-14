package me.dmkube.storypointme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements TextWatcher {

    private Button joinSessionButton;
    private EditText sessionIDText;
    private EditText nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("appPrefs", 0);
        String sessionId = settings.getString("sessionID", null);
        sessionIDText = findViewById(R.id.sessionIDText);
        if (sessionId != null && !sessionId.isEmpty()) {
            sessionIDText.setText(sessionId);
        }
        sessionIDText.addTextChangedListener(this);

        nameText = findViewById(R.id.nameText);
        String name = settings.getString("name", null);
        if (name != null && !name.isEmpty()) {
            nameText.setText(name);
        }
        nameText.addTextChangedListener(this);

        joinSessionButton = findViewById(R.id.joinSessionButton);
        joinSessionButton.setOnClickListener((view) -> {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("sessionID", sessionIDText.getText().toString());
            editor.putString("name", nameText.getText().toString());
            editor.commit();

            Intent intent = new Intent(this, PointingActivity.class);
            startActivity(intent);
        });

        checkEnableJoinSessionButton();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //
    }

    @Override
    public void afterTextChanged(Editable editable) {
        checkEnableJoinSessionButton();
    }

    private void checkEnableJoinSessionButton() {
        boolean inputsHaveText = (sessionIDText.getText().toString().length() > 0 && nameText.getText().toString().length() > 0);
        joinSessionButton.setEnabled(inputsHaveText);
    }
}

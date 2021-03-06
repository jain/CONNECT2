package vikram.connect.com.connect;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Activity which allows user to customize the phrase tree to suit his/her needs better
 * Similar to MainActivity but focuses more on customization
 */
public class EditActivity extends AppCompatActivity {
    private HashMap<String, HashSet<String>> wordMap; // reference to phrase mappings to generate phrase tree
    private HashMap<String, JSONObject> jsonMap; // reference to map of strings following word
    private LinearLayout layout1; // reference to layout to modify dynamically to generate phrase tree
    private EditText command; // reference to input text
    private static String stringSoFar = ""; // reference to input so far to decide state of phrase tree
    private EditText phraseText; // reference
    private Dialog dialog; // reference to dialog which can be created

    /**
     * Creates the the layout for the activity
     * Instantiates the elements of the layouts
     * Instantiates key variables
     *
     * @param savedInstanceState data stored in application
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        layout1 = (LinearLayout) findViewById(R.id.list2);
        command = (EditText) findViewById(R.id.command2);
        command.addTextChangedListener(new TextWatcher() {

            /**
             * Called just before text written into EditText
             * Default required method by Interface, but is unused in this application
             *
             * @param charSequence entered text
             * @param i
             * @param i1
             * @param i2
             */
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            /**
             * Called as text is being changed
             * Default required method by Interface, but is unused in this application
             *
             * @param charSequence
             * @param i
             * @param i1
             * @param i2
             */
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            /**
             * Called after text in editText is modified
             *
             * @param editable content which is in the EditText after modification
             */
            @Override
            public void afterTextChanged(Editable editable) {
                EditActivity.stringSoFar = editable.toString();
                remake(editable.toString());
            }
        });
    }

    /**
     * If user chooses to add a phrase this method is called
     * Checks if the phrase is valid and/or exists already and then decides to add it
     *
     * @param view view which was clicked
     */
    public void addPhrase(View view) {
        // check validity
        String phr = phraseText.getText().toString().trim().toLowerCase();
        if (phr.isEmpty()) {
            Toast.makeText(this, "Please enter a valid phrase", Toast.LENGTH_LONG).show();
            dialog.cancel();
            return;
        }
        try {
            // check duplication
            JSONObject phrases = Data.module.getJSONObject("phrases");
            if (phrases.has(phr)) {
                Toast.makeText(this, "Entered Phrase Already Exists", Toast.LENGTH_LONG).show();
            } else {
                // if successful regenerate view to take into account latest happenings
                phrases.put(phr, ".asd");
                Data.save(this);
                onResume();
            }
        } catch (JSONException e) { // catch errs and show toasts for them
            e.printStackTrace();
            Toast.makeText(this, "JSON Err", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Saving Err", Toast.LENGTH_LONG).show();
        }
        dialog.cancel();
    }

    /**
     * Called from XML when user wants to add a new phrase
     * Generates a new dialog allowing the user to do so
     *
     * @param view view which was clicked
     */
    public void newPhrase(View view) {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.phrase);
        dialog.setTitle("Addition");
        // set the custom dialog components - text, image and button
        phraseText = (EditText) dialog.findViewById(R.id.phrase);
        dialog.show();
    }

    /**
     * Used for recreating the activity when screen changes or some data got modified
     */
    @Override
    public void onResume() {
        super.onResume();
        stringSoFar = "";
        wordMap = new HashMap<String, HashSet<String>>();
        jsonMap = new HashMap<String, JSONObject>();
        try {
            fillMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        remake("");
    }

    /**
     * Generate remainder of the map for the phrase tree based on the json data loaded in the app
     *
     * @param soFar String which is so far in the map, and for what children phrases can be added
     * @param next  Next set of phrases to be parsed for generating child phrases
     * @throws JSONException
     */
    public void fillMapRecursion(String soFar, JSONObject next) throws JSONException {
        Iterator<String> iter = next.keys();
        while (iter.hasNext()) {
            if (!wordMap.containsKey(soFar)) {
                wordMap.put(soFar, new HashSet<String>());
                jsonMap.put(soFar, next);
            }
            String word = iter.next();
            wordMap.get(soFar).add(word.trim().toLowerCase());
            if (next.get(word) instanceof JSONObject) {
                fillMapRecursion((soFar + " " + word.trim().toLowerCase()).trim().toLowerCase(), next.getJSONObject(word));
            }
        }
    }

    /**
     * Get the EditText
     *
     * @return EditText for input text
     */
    public EditText getCommand() {
        return command;
    }

    /**
     * Generate the map for the phrase tree based on the json data loaded in the app
     *
     * @throws JSONException
     */
    public void fillMap() throws JSONException {
        JSONObject phrases = Data.module.getJSONObject("phrases");
        Iterator<String> iter = phrases.keys();
        while (iter.hasNext()) {
            String soFar = "";
            if (!wordMap.containsKey(soFar)) {
                wordMap.put(soFar, new HashSet<String>());
                jsonMap.put(soFar, phrases);
            }
            String word = iter.next();
            wordMap.get(soFar).add(word.trim().toLowerCase());
            if (phrases.get(word) instanceof JSONObject) {
                fillMapRecursion(word.trim().toLowerCase(), phrases.getJSONObject(word));
            }
        }
    }

    /**
     * Method for creating the phrase tree on the screen
     *
     * @param soFar String which is currently in EditText
     */
    public void remake(String soFar) {
        // invalidate the view
        layout1.removeAllViews();
        layout1.invalidate();
        soFar = stringSoFar.toLowerCase().trim();
        // check if the string exists in the HashMap, if so we can generate the phrase tree from this point
        if (!wordMap.containsKey(soFar)) {
            command.setSelection(command.getText().length());
            return;
        }
        // generate the first linear layout and populate it
        for (String word : wordMap.get(soFar)) {
            LinearLayout layout2 = new LinearLayout(this);
            layout2.setOrientation(LinearLayout.HORIZONTAL);
            layout2.setLayoutParams(new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 10.0f));
            layout2.setGravity(Gravity.CENTER);
            Button first = new Button(this);
            first.setText(word);
            final String w2 = word;
            // if a phrase is selected add it to the speech which will be said
            first.setOnClickListener(new Button.OnClickListener() {
                /**
                 * method which is called when the button is clicked, it will append phrase to the edittext
                 * @param view view which was clicked
                 */
                @Override
                public void onClick(View view) {
                    command.setText(command.getText().toString() + " " + w2);
                }
            });
            // set a listener for long clicking the phrase
            first.setOnLongClickListener(new EditLongClickListener(this, soFar, jsonMap, word));
            // generate the second layer of the phrase tree
            layout2.addView(first);
            layout2.addView(new TextView(this));
            if (wordMap.containsKey((soFar + " " + word).trim().toLowerCase())) {
                LinearLayout layout3 = new LinearLayout(this);
                layout3.setOrientation(LinearLayout.VERTICAL);
                layout3.setGravity(Gravity.CENTER);
                // add each phrase to second layer
                for (String word2 : wordMap.get((soFar + " " + word).trim().toLowerCase())) {
                    TextView sec = new TextView(this);
                    sec.setTextSize(20);
                    sec.setText(word2);
                    layout3.addView(sec);
                }

                layout2.addView(layout3);
            }
            // add view and validate the layout
            layout1.addView(layout2);
            layout1.postInvalidate();
        }
        command.setSelection(command.getText().length());
    }
}

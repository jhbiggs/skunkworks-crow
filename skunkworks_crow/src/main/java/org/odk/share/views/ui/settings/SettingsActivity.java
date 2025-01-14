package org.odk.share.views.ui.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import org.odk.share.R;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;


/**
 * Created by laksh on 5/27/2018.
 */

public class SettingsActivity extends PreferenceActivity {

    EditTextPreference hotspotNamePreference;
    Preference hotspotPasswordPreference;
    CheckBoxPreference passwordRequirePreference;
    EditTextPreference odkDestinationDirPreference;
    private SharedPreferences prefs;
    //set the minimum password length
    static final int MIN_PASSWORD_LENGTH = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup root = getRootView();
        Toolbar toolbar = (Toolbar) View.inflate(this, R.layout.toolbar, null);
        toolbar.setTitle(getString(R.string.settings));
        root.addView(toolbar, 0);

        addPreferencesFromResource(R.xml.preferences_menu);
        addPreferences();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPreferences() {
        hotspotNamePreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_NAME);
        hotspotPasswordPreference = findPreference(PreferenceKeys.KEY_HOTSPOT_PASSWORD);
        passwordRequirePreference = (CheckBoxPreference) findPreference(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE);
        odkDestinationDirPreference = (EditTextPreference) findPreference(PreferenceKeys.KEY_ODK_DESTINATION_DIR);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        hotspotNamePreference.setSummary(prefs.getString(PreferenceKeys.KEY_HOTSPOT_NAME,
                getString(R.string.default_hotspot_ssid)));
        boolean isPasswordSet = prefs.getBoolean(PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE, false);
        odkDestinationDirPreference.setSummary(prefs.getString(PreferenceKeys.KEY_ODK_DESTINATION_DIR,
                getString(R.string.default_odk_destination_dir)));

        hotspotPasswordPreference.setEnabled(isPasswordSet);
        passwordRequirePreference.setChecked(isPasswordSet);

        hotspotNamePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        hotspotPasswordPreference.setOnPreferenceChangeListener(preferenceChangeListener());
        passwordRequirePreference.setOnPreferenceChangeListener(preferenceChangeListener());
        odkDestinationDirPreference.setOnPreferenceChangeListener(preferenceChangeListener());

        hotspotPasswordPreference.setOnPreferenceClickListener(preferenceClickListener());
    }

    private Preference.OnPreferenceClickListener preferenceClickListener() {
        return preference -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    showPasswordDialog();
                    break;
            }
            return false;
        };
    }

    private Preference.OnPreferenceChangeListener preferenceChangeListener() {
        return (preference, newValue) -> {
            switch (preference.getKey()) {
                case PreferenceKeys.KEY_HOTSPOT_NAME:
                    String name = newValue.toString();
                    if (name.length() == 0) {
                        Toast.makeText(getBaseContext(), getString(R.string.hotspot_name_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        hotspotNamePreference.setSummary(name);
                    }
                    break;
                case PreferenceKeys.KEY_HOTSPOT_PASSWORD:
                    String password = newValue.toString();
                    if (password.length() < 8) {
                        Toast.makeText(getBaseContext(), getString(R.string.hotspot_password_error), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    break;
                case PreferenceKeys.KEY_HOTSPOT_PWD_REQUIRE:
                    boolean isRequire = (Boolean) newValue;
                    if (isRequire) {
                        hotspotPasswordPreference.setEnabled(true);
                    } else {
                        hotspotPasswordPreference.setEnabled(false);
                    }
                    break;
                case PreferenceKeys.KEY_ODK_DESTINATION_DIR:
                    String dir = newValue.toString();
                    if (dir.length() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.odk_destination_dir_error), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        odkDestinationDirPreference.setSummary(dir);
                    }
                    break;
            }
            return true;
        };
    }

    private ViewGroup getRootView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
        } else {
            return (ViewGroup) findViewById(android.R.id.list).getParent();
        }
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //use layout inflater for our own password layout and editText
        LayoutInflater factory = LayoutInflater.from(this);
        View dialogView = factory.inflate(R.layout.dialog_password_til, null);
        TextInputLayout tlPassword = dialogView.findViewById(R.id.et_password_layout);

        //set the default password text
        tlPassword.getEditText().setText(prefs.getString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, getString(R.string.default_hotspot_password)));

        builder.setTitle(getString(R.string.title_hotspot_password));
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        String password = tlPassword.getEditText().getText().toString();
                        prefs.edit().putString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, password).apply();
                    });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password,
        // and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        AlertDialog alertDialog = builder.create();

        //thanks to StackOverflow Aaron for the solution below
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //required function for interface
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= MIN_PASSWORD_LENGTH) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    String password = tlPassword.getEditText().getText().toString();

                    prefs.edit().putString(PreferenceKeys.KEY_HOTSPOT_PASSWORD, password).apply();

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //required function for interface
            }
        });



        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                //set positive OK button to be disabled by default
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        alertDialog.show();
        alertDialog.setCancelable(true);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}

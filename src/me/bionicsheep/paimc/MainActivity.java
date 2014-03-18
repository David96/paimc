package me.bionicsheep.paimc;
import me.bionicsheep.paimc.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity {

	SharedPreferences sp;
	SharedPreferences.Editor editor;
	CheckBoxPreference service_checkbox;
	Activity currentActivity = this;
	Toast toast;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.main_preference);

		loadValues();
		initializeObjects();
	}

	@SuppressWarnings("deprecation")
	public void initializeObjects(){
		service_checkbox = (CheckBoxPreference)getPreferenceManager().findPreference("service_checkbox");
		service_checkbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				startPie((Boolean) newValue);
				return true;
			}
		});
		startPie(sp.getBoolean("service_status", false));
	}

	public void startPie(boolean state){
		editor = sp.edit();

		if (state == true) {
			startService(new Intent(currentActivity, TriggerService.class));
			editor.putBoolean("service_status", true);
			toast = Toast.makeText(currentActivity, "Service Running", Toast.LENGTH_SHORT);
			toast.show();
		} else {
			stopService(new Intent(currentActivity, TriggerService.class));
			editor.putBoolean("service_status", false);
			toast = Toast.makeText(currentActivity, "Service Not Running", Toast.LENGTH_SHORT);
			toast.show();
		}

		editor.commit();
	}

	public void loadValues(){
		sp = getSharedPreferences("app_settings", MODE_PRIVATE);
		
		boolean firstRun = sp.getBoolean("firstrun", true);
		if(firstRun){
			toast = Toast.makeText(this, "First Run", Toast.LENGTH_LONG);
			toast.show();
			enterValues();
		}
	}

	public void enterValues(){
		editor = sp.edit();
		editor.putBoolean("firstrun", false);
		editor.putBoolean("service_status", false);
		editor.commit();
	}
}

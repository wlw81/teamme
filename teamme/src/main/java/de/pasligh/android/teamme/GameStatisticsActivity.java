package de.pasligh.android.teamme;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import de.pasligh.android.teamme.backend.BackendFacade;

/**
 * An activity representing a single game detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link GameCreatorActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link GameStatisticsFragment}.
 */
public class GameStatisticsActivity extends AppCompatActivity {

	private BackendFacade facade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_statistics);

		// Show the Up button in the action bar.
		// getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(GameStatisticsFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(GameStatisticsFragment.ARG_ITEM_ID));
			GameStatisticsFragment fragment = new GameStatisticsFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.game_statistics_container, fragment).commit();
		}

	}

	/**
	 * @return the facade
	 */
	public BackendFacade getFacade() {
		if (null == facade) {
			facade = new BackendFacade(getApplicationContext());
		}
		return facade;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		getFacade().getObjDB_API().close();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					GameCreatorActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

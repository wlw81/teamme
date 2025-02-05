package de.pasligh.android.teamme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ShareActionProvider;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.TeamReactor;
import de.pasligh.android.teamme.tools.TextHelper;

/**
 * A fragment representing a single GameRecord detail screen.
 * This fragment is either contained in a {@link GameRecordListActivity}
 * in two-pane mode (on tablets) or a {@link GameRecordDetailActivity}
 * on handsets.
 */
public class GameRecordDetailFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_GAME_ID = "item_id";
    private static Context applicationContext;

    private BackendFacade facade;

    public void setApplicationContext(Context p_context) {
        if (p_context != null) {
            this.applicationContext = p_context;
        }
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }


    /**
     * The record content this fragment is presenting.
     */
    private GameRecord gameRecord;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GameRecordDetailFragment() {
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.team_overview, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.TeamOverviewShareContext);

        // Fetch and store ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            mShareActionProvider.setShareIntent(createShareIntent());
    }

    private Intent createShareIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        StringBuilder shareText = new StringBuilder();
        String title = gameRecord.getSport() + " " + dateFormat.format(gameRecord.getStartedAt());

        shareText.append("[").append(title).append("] ");
        shareText.append(TextHelper.createTeamDecided_ShareText(getString(R.string.shareIntent), getString(R.string.team)));
        try {
            PackageManager pm= getActivity().getPackageManager();
            PackageInfo info= pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            shareIntent.setPackage("com.whatsapp");
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
        //Check if package exists or not. If not then code
        //in catch block will be called

        // create app footer
        TextHelper.appendFooter_Signature(shareText, getString(R.string.shareFooter));

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.decisiontext);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString().trim());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        return shareIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments().containsKey(ARG_GAME_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            gameRecord = getFacade().getGame(Integer.parseInt(getArguments().getString(ARG_GAME_ID)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gamerecord_detail, container, false);

        if (gameRecord != null) {
            try {

                for (PlayerAssignment p : gameRecord.getAssignments()) {
                    p.setRevealed(true);
                }

                TeamReactor.overwriteAssignments(new HashSet<PlayerAssignment>(gameRecord.getAssignments()));

                // Get the ViewPager and set it's PagerAdapter so that it can display items
                final ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.gamerecordDetailVP);
                viewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
                // Give the TabLayout the ViewPager
                final TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.sliding_tabs);
                tabLayout.setupWithViewPager(viewPager);

                rootView.findViewById(R.id.gamerecordDetailFAB).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent reportScores = new Intent(getApplicationContext(),
                                ReportScoresActivity.class);
                        reportScores.putExtra(Flags.GAME_ID, gameRecord.getId());
                        startActivity(reportScores);
                    }
                });

                rootView.findViewById(R.id.gamerecordDetailAddPlayerFAB).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Set up the input
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),  R.style.AlertDialogCustom);
                        builder.setTitle(getString(R.string.addPlayer));

                        // Set up the input
                        final AutoCompleteTextView input = new AutoCompleteTextView(getApplicationContext());
                        ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(getContext(),
                                android.R.layout.simple_dropdown_item_1line, getFacade()
                                .getPlayersAsStringArray());
                        input.setAdapter(playerAdapter);
                        input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                        input.setHint(R.string.playername);
                        input.setSingleLine();
                        input.setBackgroundTintList(ColorStateList.valueOf(getContext().getColor(R.color.accent)));
                        input.setTextColor(getContext().getColor(R.color.primary_light));
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newPlayerName = input.getText().toString().trim();
                                if (!newPlayerName.isEmpty()) {
                                    PlayerAssignment assignmentNew = new PlayerAssignment();
                                    assignmentNew.setGame(gameRecord.getId());
                                    assignmentNew.setTeam(tabLayout.getSelectedTabPosition() + 1);
                                    assignmentNew.setPlayer(new Player(newPlayerName));
                                    assignmentNew.setRevealed(true);
                                    try {
                                        getFacade().persistPlayer(assignmentNew.getPlayer());
                                    } catch (Exception e) {
                                        Log.d(Flags.LOGTAG, assignmentNew.getPlayer() + " already known.");
                                    }

                                    // we make sure that the new player hasn't been assigned to another team, yet
                                    if (getFacade().getAssignments(gameRecord.getId(), newPlayerName).isEmpty()) {
                                        getFacade().addPlayerAssignment(assignmentNew);
                                        TeamReactor.overwriteAssignments(new HashSet<PlayerAssignment>(getFacade().getAssignments(assignmentNew.getGame())));
                                    } else {
                                        Toast.makeText(getContext(), newPlayerName + ": " + getString(R.string.playerAlreadyAssigned), Toast.LENGTH_LONG).show();
                                    }
                                    viewPager.getAdapter().notifyDataSetChanged();

                                }
                            }
                        });

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        final AlertDialog alertToShow = builder.create();
                        alertToShow.getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        alertToShow.show();
                        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE
                                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                                    alertToShow.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                                    return true;
                                }
                                return false;
                            }
                        });


                    }
                });

            } catch (Exception e) {
                Log.e(Flags.LOGTAG, "Gamerecord manipulated: " + gameRecord);
                getFacade().deleteGame(gameRecord.getId());
                Toast.makeText(getApplicationContext(), "" + gameRecord + " " + getString(R.string.deleted), Toast.LENGTH_LONG).show();
            }
        }
        return rootView;
    }

}
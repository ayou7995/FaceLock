package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Calendar;

public class LobbyFragment extends Fragment {

    private static final String TAG = "Jonathan";
    private static final String tag = "[LobbyFragment] : ";
    LinearLayout ll;

    private Handler backgroundHandler;
    Calendar c;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lobby_fragment, container, false);
        ll = (LinearLayout) view.findViewById(R.id.lobby_ll);
        Button register_profile_B = (Button) view.findViewById(R.id.register_profile_button);
        Button update_profile_B = (Button) view.findViewById(R.id.update_profile_button);

        backgroundHandler = new Handler();
        backgroundHandler.removeCallbacks(runnable);
        backgroundHandler.post(runnable);

        ((MainActivity) getActivity()).setActionState(MainActivity.IDLESTATE);
        if (((MainActivity)getActivity()).getUser().equals("")) {
            update_profile_B.setVisibility(View.GONE);
        }
        // Todo
        // Update button onClick;
        register_profile_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setActionState(MainActivity.REGISTERSTATE);
                ((MainActivity) getActivity()).setCurrentFragment(MainActivity.PHOTOFRAG);
                ((MainActivity) getActivity()).replaceFragments(PhotoFragment.class);
            }
        });
        update_profile_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setActionState(MainActivity.UPDATESTATE);
                ((MainActivity) getActivity()).setCurrentFragment(MainActivity.REGISTERFRAG);
                ((MainActivity) getActivity()).replaceFragments(RegisterFragment.class);
            }
        });

        return view;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ((MainActivity)getActivity()).setBackgroundTheme(ll);
            c = Calendar.getInstance();
            int seconds = c.get(Calendar.SECOND);
            ((MainActivity) getActivity()).setCurrentColor(seconds%3);
            backgroundHandler.postDelayed(this,1000);
        }
    };



    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onPause() {
        super.onPause();
        backgroundHandler.removeCallbacksAndMessages(null);
    }
}

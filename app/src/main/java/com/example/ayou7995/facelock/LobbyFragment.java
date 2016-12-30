package com.example.ayou7995.facelock;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LobbyFragment extends Fragment {

    private static final String TAG = "LobbyFragment";
    // private static final String TAG = "Jonathan";

    private Button update_profile_B;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lobby_fragment, container, false);
        Button register_profile_B = (Button) view.findViewById(R.id.register_profile_button);
        update_profile_B = (Button) view.findViewById(R.id.update_profile_button);

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

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (getFragmentManager().findFragmentById(this.getId()) != null) {
//            Log.i(TAG, TAG + " destroy.");
//            getFragmentManager().beginTransaction().remove(this)
//                    .commit();
//        }
//    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

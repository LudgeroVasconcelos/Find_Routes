package find.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import find.MainActivity;
import find.map.R;
import find.routes.Occurrence;
import find.routes.Section;

public class InfoFragment extends Fragment implements TextWatcher {

    OnSectionUpdateListener listener;
    View layout;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        try {
            listener = (OnSectionUpdateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSeekBarReleasedListener");
        }

        listener.onInfoAttached();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.info_window, container, false);
        return layout;
    }

    public void updateView(final Section section) {
        View layout = getView();
        if (layout == null) {
            layout = this.layout;
        }

        if (layout == null) {
            Log.d(MainActivity.TAG, "layout é null");
        } else {
            Log.d(MainActivity.TAG, "layout não é null");
        }
        final Button saveButton = (Button) layout
                .findViewById(R.id.info_save_button);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                View layout = getView();

                EditText ed = (EditText) layout
                        .findViewById(R.id.table_tsunami_risk);
                int riskTsunami = Integer.parseInt(ed.getText().toString());
                ed = (EditText) layout.findViewById(R.id.table_earthquake_risk);
                int riskEarthquake = Integer.parseInt(ed.getText().toString());
                ed = (EditText) layout.findViewById(R.id.table_fire_risk);
                int riskFire = Integer.parseInt(ed.getText().toString());
                ed = (EditText) layout.findViewById(R.id.table_landslip_risk);
                int riskLandslip = Integer.parseInt(ed.getText().toString());

                LatLng[] points = section.getPoints();
                Map<Occurrence, Integer> risks = new HashMap<>();

                risks.put(Occurrence.EARTHQUAKE, riskEarthquake);
                risks.put(Occurrence.TSUNAMI, riskTsunami);
                risks.put(Occurrence.LANDSLIP, riskLandslip);
                risks.put(Occurrence.FIRE, riskFire);

                Section updatedSection = new Section(section.getId(),
                        points[0], points[1], risks, section.isOpen(), System
                        .currentTimeMillis());

                listener.updateSection(updatedSection);
            }
        });

        ToggleButton toggle = (ToggleButton) layout
                .findViewById(R.id.state_togglebutton);
        toggle.setChecked(section.isOpen());
        toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                section.setState(isChecked);
                saveButton.setEnabled(true);
            }
        });

        EditText ed = (EditText) layout.findViewById(R.id.table_tsunami_risk);
        ed.setText(String.valueOf(section.getRisk(Occurrence.TSUNAMI)));
        ed.addTextChangedListener(this);
        ed = (EditText) layout.findViewById(R.id.table_earthquake_risk);
        ed.setText(String.valueOf(section.getRisk(Occurrence.EARTHQUAKE)));
        ed.addTextChangedListener(this);
        ed = (EditText) layout.findViewById(R.id.table_fire_risk);
        ed.setText(String.valueOf(section.getRisk(Occurrence.FIRE)));
        ed.addTextChangedListener(this);
        ed = (EditText) layout.findViewById(R.id.table_landslip_risk);
        ed.setText(String.valueOf(section.getRisk(Occurrence.LANDSLIP)));
        ed.addTextChangedListener(this);

        TextView tv = (TextView) layout.findViewById(R.id.info_timestamp);
        long date = section.getTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                Locale.getDefault());
        String dateString = sdf.format(date);
        tv.setText(dateString);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        Button saveButton = (Button) getView().findViewById(
                R.id.info_save_button);
        saveButton.setEnabled(true);

        String riskString = s.toString();
        Log.d(MainActivity.TAG, riskString);
        if (!riskString.equals("")) {
            int risk = Integer.valueOf(riskString);
            if (risk > 100) {
                s.replace(0, s.length(), "100");
            } else if (!riskString.endsWith("0") && riskString.startsWith("0")) {
                s.replace(0, s.length(), String.valueOf(risk));
            } else if (riskString.equals("00")) {
                s.replace(0, s.length(), "0");
            }
        }
    }

    @Override
    public void onDetach() {
        if (listener != null) {
            listener.onInfoClosed();
        }

        super.onDetach();
    }

    public interface OnSectionUpdateListener {
        void updateSection(Section section);

        void onInfoAttached();

        void onInfoClosed();
    }
}

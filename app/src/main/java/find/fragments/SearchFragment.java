package find.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import find.map.R;

public class SearchFragment extends Fragment implements
        OnSeekBarChangeListener, OnCheckedChangeListener,
        android.widget.CompoundButton.OnCheckedChangeListener {

    private OnSeekBarReleasedListener listener;
    private boolean mode = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnSeekBarReleasedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSeekBarReleasedListener");
        }

        listener.onSearchAttached();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater
                .inflate(R.layout.search_window, container, false);

        // checkboxes
        CheckBox cb = (CheckBox) layout.findViewById(R.id.check_tsunami);
        cb.setOnCheckedChangeListener(this);
        cb = (CheckBox) layout.findViewById(R.id.check_earthquake);
        cb.setOnCheckedChangeListener(this);
        cb = (CheckBox) layout.findViewById(R.id.check_fire);
        cb.setOnCheckedChangeListener(this);
        cb = (CheckBox) layout.findViewById(R.id.check_landslip);
        cb.setOnCheckedChangeListener(this);

        // radio group
        // RadioGroup rg = (RadioGroup) layout.findViewById(R.id.radio_group);
        // rg.setOnCheckedChangeListener(this);

        // tsunami seekbar
        SeekBar sb1 = (SeekBar) layout.findViewById(R.id.seekBar1);
        sb1.setOnSeekBarChangeListener(this);
        sb1.setEnabled(false);

        // earthquake seekbar
        SeekBar sb2 = (SeekBar) layout.findViewById(R.id.seekBar2);
        sb2.setOnSeekBarChangeListener(this);
        sb2.setEnabled(false);

        // fire seekbar
        SeekBar sb3 = (SeekBar) layout.findViewById(R.id.seekBar3);
        sb3.setOnSeekBarChangeListener(this);
        sb3.setEnabled(false);

        // landslip seekbar
        SeekBar sb4 = (SeekBar) layout.findViewById(R.id.seekBar4);
        sb4.setOnSeekBarChangeListener(this);
        sb4.setEnabled(false);

        // set description text
        String what = mode ? "sections" : "routes";
        String desc = "Showing all " + what + " available";

        TextView tv = (TextView) layout
                .findViewById(R.id.search_description);
        tv.setText(desc);

        return layout;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        TextView tv;

        if (seekBar.getId() == R.id.seekBar1) {
            tv = (TextView) getView().findViewById(R.id.textViewProgress1);
        } else if (seekBar.getId() == R.id.seekBar2) {
            tv = (TextView) getView().findViewById(R.id.textViewProgress2);
        } else if (seekBar.getId() == R.id.seekBar3) {
            tv = (TextView) getView().findViewById(R.id.textViewProgress3);
        } else {
            tv = (TextView) getView().findViewById(R.id.textViewProgress4);
        }

        tv.setText(String.valueOf(progress));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        search();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        search();
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean checked) {

        SeekBar s;
        TextView occurrence;
        TextView risk;

        // Check which checkbox was clicked
        switch (view.getId()) {

            case R.id.check_tsunami:
                s = (SeekBar) getView().findViewById(R.id.seekBar1);
                s.setProgress(100);
                s.setEnabled(checked);

                occurrence = (TextView) getView().findViewById(R.id.textView1);
                occurrence.setTextColor(checked ? Color.BLACK : getResources()
                        .getColor(R.color.searchTextDimmedOut));

                risk = (TextView) getView().findViewById(R.id.textViewProgress1);
                risk.setTextColor(checked ? Color.BLACK : getResources().getColor(
                        R.color.searchTextDimmedOut));
                break;

            case R.id.check_earthquake:
                s = (SeekBar) getView().findViewById(R.id.seekBar2);
                s.setProgress(100);
                s.setEnabled(checked);

                occurrence = (TextView) getView().findViewById(R.id.textView2);
                occurrence.setTextColor(checked ? Color.BLACK : getResources()
                        .getColor(R.color.searchTextDimmedOut));

                risk = (TextView) getView().findViewById(R.id.textViewProgress2);
                risk.setTextColor(checked ? Color.BLACK : getResources().getColor(
                        R.color.searchTextDimmedOut));

                break;

            case R.id.check_fire:
                s = (SeekBar) getView().findViewById(R.id.seekBar3);
                s.setProgress(100);
                s.setEnabled(checked);

                occurrence = (TextView) getView().findViewById(R.id.textView3);
                occurrence.setTextColor(checked ? Color.BLACK : getResources()
                        .getColor(R.color.searchTextDimmedOut));

                risk = (TextView) getView().findViewById(R.id.textViewProgress3);
                risk.setTextColor(checked ? Color.BLACK : getResources().getColor(
                        R.color.searchTextDimmedOut));
                break;

            case R.id.check_landslip:
                s = (SeekBar) getView().findViewById(R.id.seekBar4);
                s.setProgress(100);
                s.setEnabled(checked);

                occurrence = (TextView) getView().findViewById(R.id.textView4);
                occurrence.setTextColor(checked ? Color.BLACK : getResources()
                        .getColor(R.color.searchTextDimmedOut));

                risk = (TextView) getView().findViewById(R.id.textViewProgress4);
                risk.setTextColor(checked ? Color.BLACK : getResources().getColor(
                        R.color.searchTextDimmedOut));
                break;
        }

        search();
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            search();
        }

        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDetach() {
        listener.onSearchClosed();
        super.onDetach();
    }

    private void search() {

        SeekBar sb1 = (SeekBar) getView().findViewById(R.id.seekBar1);
        int progress1 = sb1.getProgress();
        SeekBar sb2 = (SeekBar) getView().findViewById(R.id.seekBar2);
        int progress2 = sb2.getProgress();
        SeekBar sb3 = (SeekBar) getView().findViewById(R.id.seekBar3);
        int progress3 = sb3.getProgress();
        SeekBar sb4 = (SeekBar) getView().findViewById(R.id.seekBar4);
        int progress4 = sb4.getProgress();

//		RadioGroup rg = (RadioGroup) getView().findViewById(R.id.radio_group);
//		int checkedRadio = rg.getCheckedRadioButtonId();

        changeDescription(progress1, progress2, progress3, progress4);

        // if (checkedRadio == R.id.radio_sections) {
        // listener.searchSections(progress1, progress2, progress3, progress4);
        // } else {
        // listener.searchRoutes(progress1, progress2, progress3, progress4);
        // }

        // earthquake, tsunami, fire and landslip
        listener.search(new int[]{progress2, progress1, progress3, progress4});
    }

    private void changeDescription(int risk1, int risk2, int risk3, int risk4) {

        String what = mode ? "sections" : "routes";
        String desc = "Showing all " + what + " available";
        boolean all100 = true;

        int risk[] = {risk1, risk2, risk3, risk4};
        String riskName[] = {"Tsunami", "Earthquake", "Fire", "Landslip"};

        StringBuilder message = new StringBuilder();
        message.append("Showing ");
        message.append(what);

        boolean firstRiskToWrite = true;
        for (int i = 0; i < risk.length; i++) {
            if (risk[i] < 100) {
                all100 = false;
                if (firstRiskToWrite) {
                    message.append(" with ");
                    firstRiskToWrite = false;
                } else {
                    boolean hasNextRiskToWrite = false;
                    int j = i + 1;
                    while (j < risk.length && !hasNextRiskToWrite) {
                        hasNextRiskToWrite = risk[j] < 100;
                        j++;
                    }

                    if (hasNextRiskToWrite)
                        message.append(", ");
                    else
                        message.append(" and ");
                }

                message.append(riskName[i]);
                message.append(" risk level below ");
                message.append(risk[i]);
            }
        }

        TextView tv = (TextView) getView()
                .findViewById(R.id.search_description);
        tv.setText(all100 ? desc : message.toString());
    }

    public interface OnSeekBarReleasedListener {
        void search(int[] risks);

        void onSearchAttached();

        void onSearchClosed();
    }
}

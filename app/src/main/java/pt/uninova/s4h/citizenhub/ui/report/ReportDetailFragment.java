package pt.uninova.s4h.citizenhub.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.report.Group;
import pt.uninova.s4h.citizenhub.report.Item;
import pt.uninova.s4h.citizenhub.report.MeasurementTypeLocalizedResource;
import pt.uninova.s4h.citizenhub.report.Report;
import pt.uninova.s4h.citizenhub.ui.accounts.AccountsViewModel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Class responsible for generating and displaying the daily report / activities. */
public class ReportDetailFragment extends Fragment {

    private ReportViewModel model;

    private MeasurementKindLocalization measurementKindLocalization;

    private String monthToString(int month) {
        switch (month) {
            case 1:
                return getString(R.string.date_month_01);
            case 2:
                return getString(R.string.date_month_02);
            case 3:
                return getString(R.string.date_month_03);
            case 4:
                return getString(R.string.date_month_04);
            case 5:
                return getString(R.string.date_month_05);
            case 6:
                return getString(R.string.date_month_06);
            case 7:
                return getString(R.string.date_month_07);
            case 8:
                return getString(R.string.date_month_08);
            case 9:
                return getString(R.string.date_month_09);
            case 10:
                return getString(R.string.date_month_10);
            case 11:
                return getString(R.string.date_month_11);
            case 12:
                return getString(R.string.date_month_12);
        }

        return "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

        measurementKindLocalization = new MeasurementKindLocalization(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_report_detail, container, false);

        AccountsViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountsViewModel.class);

        if (viewModel.hasSmart4HealthAccount()) {
            setHasOptionsMenu(true);
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocalDate currentDate = model.getCurrentDate();

        String year = String.valueOf(currentDate.getYear());
        String month = monthToString(currentDate.getMonthValue());
        String day = String.valueOf(currentDate.getDayOfMonth());

        final TextView infoTextView_day = view.findViewById(R.id.fragment_report_detail_text_view_day);
        final TextView infoTextView_year = view.findViewById(R.id.fragment_report_detail_text_view_year);

        infoTextView_day.setText(String.format("%s %s", day, month));
        infoTextView_year.setText(year);

        System.out.println("Dewde");
        Observer<Report> observerWorkTimeReport = workTimeData -> {

            Observer<Report> observerNotWorkTimeReport = notWorkTimeData -> {

                List<Group> groupsWorkTimeData = workTimeData.getGroups();
                List<Group> groupsNotWorkTimeData = notWorkTimeData.getGroups();

                TableLayout tableLayout = view.findViewById(R.id.reportTableLayout);

                requireActivity().runOnUiThread(() -> {

                    for (Group groupNotWorkTime : groupsNotWorkTimeData) {

                        int labelNotWorkTime = ((MeasurementTypeLocalizedResource) groupNotWorkTime.getLabel()).getMeasurementType();

                        displayTitle(tableLayout, measurementKindLocalization.localize(labelNotWorkTime));

                        if (labelNotWorkTime == Measurement.TYPE_BLOOD_PRESSURE || labelNotWorkTime == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING) {
                            boolean addPadding = false;
                            for (Group group : groupNotWorkTime.getGroupList()) {
                                String timestamp = group.getLabel().getLocalizedString();
                                displayTimestamp(tableLayout, timestamp.substring(timestamp.indexOf("T") + 1, timestamp.indexOf("Z")), addPadding);
                                addPadding = true;
                                for (Item item : group.getItemList()) {
                                    addNewRow(tableLayout,
                                            item.getLabel().getLocalizedString(),
                                            item.getValue().getLocalizedString(),
                                            "-",
                                            item.getUnits().getLocalizedString());
                                }
                            }
                            for (Group groupWorkTime : groupsWorkTimeData) {
                                int labelWorkTime = ((MeasurementTypeLocalizedResource) groupWorkTime.getLabel()).getMeasurementType();
                                if (labelWorkTime == labelNotWorkTime) {
                                    for (Group group : groupWorkTime.getGroupList()) {
                                        String timestamp = group.getLabel().getLocalizedString();
                                        displayTimestamp(tableLayout, timestamp.substring(timestamp.indexOf("T") + 1, timestamp.indexOf("Z")), addPadding);
                                        for (Item item : group.getItemList()) {
                                            addNewRow(tableLayout,
                                                    item.getLabel().getLocalizedString(),
                                                    "-",
                                                    item.getValue().getLocalizedString(),
                                                    item.getUnits().getLocalizedString());
                                        }
                                    }
                                }
                            }
                        } else {
                            boolean hasGroup = false;
                            for (Group groupWorkTime : groupsWorkTimeData) {
                                int labelWorkTime = ((MeasurementTypeLocalizedResource) groupWorkTime.getLabel()).getMeasurementType();
                                if (labelNotWorkTime == labelWorkTime) {
                                    hasGroup = true;
                                    for (Item itemNotWorkTime : groupNotWorkTime.getItemList()) {
                                        String itemLabel = itemNotWorkTime.getLabel().getLocalizedString();
                                        for (Item itemWorkTime : groupWorkTime.getItemList()) {
                                            if (itemLabel.equals(itemWorkTime.getLabel().getLocalizedString())) {
                                                addNewRow(tableLayout,
                                                        itemNotWorkTime.getLabel().getLocalizedString(),
                                                        itemNotWorkTime.getValue().getLocalizedString(),
                                                        itemWorkTime.getValue().getLocalizedString(),
                                                        itemNotWorkTime.getUnits().getLocalizedString());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!hasGroup) {
                                for (Item item : groupNotWorkTime.getItemList()) {
                                    addNewRow(tableLayout,
                                            item.getLabel().getLocalizedString(),
                                            item.getValue().getLocalizedString(),
                                            "-",
                                            item.getUnits().getLocalizedString());
                                }
                            }
                        }
                    }

                    for (Group groupWorkTime : groupsWorkTimeData) {
                        int labelWorkTime = ((MeasurementTypeLocalizedResource) groupWorkTime.getLabel()).getMeasurementType();
                        boolean hasGroup = false;
                        for (Group groupNotWorkTime : groupsNotWorkTimeData) {
                            if (labelWorkTime == ((MeasurementTypeLocalizedResource) groupNotWorkTime.getLabel()).getMeasurementType()) {
                                hasGroup = true;
                                break;
                            }
                        }
                        if (!hasGroup) {
                            displayTitle(tableLayout, measurementKindLocalization.localize(labelWorkTime));
                            if (labelWorkTime == Measurement.TYPE_BLOOD_PRESSURE || labelWorkTime == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING) {
                                boolean addPadding = false;
                                for (Group group : groupWorkTime.getGroupList()) {
                                    String timestamp = group.getLabel().getLocalizedString();
                                    displayTimestamp(tableLayout, timestamp.substring(timestamp.indexOf("T") + 1, timestamp.indexOf("Z")), addPadding);
                                    addPadding = true;
                                    for (Item item : group.getItemList()) {
                                        addNewRow(tableLayout,
                                                item.getLabel().getLocalizedString(),
                                                "-",
                                                item.getValue().getLocalizedString(),
                                                item.getUnits().getLocalizedString());
                                    }
                                }
                            } else {
                                for (Item item : groupWorkTime.getItemList()) {
                                    addNewRow(tableLayout,
                                            item.getLabel().getLocalizedString(),
                                            "-",
                                            item.getValue().getLocalizedString(),
                                            item.getUnits().getLocalizedString());
                                }
                            }
                        }
                    }
                });
            };

            model.getNotWorkTimeReport(requireActivity().getApplication(), false, observerNotWorkTimeReport);
        };

        model.getWorkTimeReport(requireActivity().getApplication(), false, observerWorkTimeReport);
    }

    /** Displays the title of a specific activity.
     * @param tableLayout The table in the layout.
     * @param title Title to be added to the table.
     * @return
     * */
    private void displayTitle(TableLayout tableLayout, String title) {
        View vTitle = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_title, null);
        TextView tvTitle = vTitle.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        tableLayout.addView(vTitle);
    }

    /** Adds a row into the table.
     * @param tableLayout The table in the layout.
     * @param label Label of the information to be added to the row.
     * @param valueWorkTime Value read during working hours.
     * @param valueMyTime Value read outside working hours.
     * @param units Units.
     * @return
     * */
    private void addNewRow(TableLayout tableLayout, String label, String valueMyTime, String valueWorkTime, String units) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_rows, null);
        TextView tvLabel = v.findViewById(R.id.tvLabel);
        TextView tvValueMyTime = v.findViewById(R.id.tvValueMyTime);
        TextView tvUnitsMyTime = v.findViewById(R.id.tvUnitsMyTime);
        TextView tvValueWorkTime = v.findViewById(R.id.tvValueWorkTime);
        TextView tvUnitsWorkTime = v.findViewById(R.id.tvUnitsWorkTime);

        tvLabel.setText(label);
        tvValueMyTime.setText(valueMyTime);
        if (valueMyTime.equals("-") || units.equals("-")) {
            tvUnitsMyTime.setVisibility(View.INVISIBLE);
        } else {
            tvUnitsMyTime.setText(units);
        }
        tvValueWorkTime.setText(valueWorkTime);
        if (valueWorkTime.equals("-") || units.equals("-")) {
            tvUnitsWorkTime.setVisibility(View.INVISIBLE);
        } else {
            tvUnitsWorkTime.setText(units);
        }

        tableLayout.addView(v);
    }

    /** Adds a row into the table.
     * @param tableLayout The table in the layout.
     * @param timestamp Timestamp value.
     * @param addPadding When true, creates a space between the timestamp text view and the information above it.
     * @return
     * */
    private void displayTimestamp(TableLayout tableLayout, String timestamp, boolean addPadding) {
        View vTimestamp = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_timestamp, null);
        TextView tvTimestamp = vTimestamp.findViewById(R.id.tvTimestamp);
        if (addPadding)
            tvTimestamp.setPadding(0, 15, 0, 0);
        tvTimestamp.setText(timestamp);
        tableLayout.addView(vTimestamp);
    }

}

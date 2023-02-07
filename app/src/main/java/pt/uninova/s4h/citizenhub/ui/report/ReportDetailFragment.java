package pt.uninova.s4h.citizenhub.ui.report;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.persistence.repository.ReportRepository;

import pt.uninova.s4h.citizenhub.report.Group;
import pt.uninova.s4h.citizenhub.report.Item;
import pt.uninova.s4h.citizenhub.report.MeasurementTypeLocalizedResource;
import pt.uninova.s4h.citizenhub.report.PDFDailyReport;
import pt.uninova.s4h.citizenhub.report.PDFWeeklyAndMonthlyReport;
import pt.uninova.s4h.citizenhub.report.Report;
import pt.uninova.s4h.citizenhub.report.ReportGenerator;
import pt.uninova.s4h.citizenhub.ui.accounts.AccountsViewModel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

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

    private void onUploadPdfClick() {
        Observer<byte[]> observer = pdfData -> {
            try {
                System.out.println("Aqui");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path.toString(), "my_file" + ".pdf");
                OutputStream os = new FileOutputStream(file);
                os.write(pdfData);
                os.close();
                System.out.println("Escreveu");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ReportRepository reportRepository = new ReportRepository(requireContext());
        ReportGenerator dailyReportGenerator = new ReportGenerator(requireContext());

        Observer<Report> observerWorkTimeReport = workTimeReport -> {
            Observer<Report> observerNotWorkTimeReport = notWorkTimeReport -> {
                if (workTimeReport.getGroups().size() > 0 || notWorkTimeReport.getGroups().size() > 0) {
                    PDFDailyReport pdfDailyReport = new PDFDailyReport(getContext());
                    pdfDailyReport.generateCompleteReport(workTimeReport, notWorkTimeReport, getResources(), model.getCurrentDate(), measurementKindLocalization, observer);
                }
            };
            dailyReportGenerator.generateNotWorkTimeReport(reportRepository, model.getCurrentDate(), true, observerNotWorkTimeReport);
        };

        dailyReportGenerator.generateWorkTimeReport(reportRepository, model.getCurrentDate(), true, observerWorkTimeReport);
    }

    public void onUploadWeeklyPdf() {
        Observer<byte[]> observer = pdfData -> {
            try {
                System.out.println("Aqui");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path.toString(), "my_weekly_file" + ".pdf");
                OutputStream os = new FileOutputStream(file);
                os.write(pdfData);
                os.close();
                System.out.println("Escreveu");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ReportRepository reportRepository = new ReportRepository(requireContext());
        ReportGenerator dailyReportGenerator = new ReportGenerator(requireContext());

        Observer<Report> observerWorkTime = workTime -> {
            Observer<Report> observerNotWorkTime = notWorkTime -> {
                System.out.println(model.getCurrentDate().minusDays(6));
                System.out.println(model.getCurrentDate());
                if (workTime.getGroups().size() > 0 || notWorkTime.getGroups().size() > 0) {
                    PDFWeeklyAndMonthlyReport pdfWeeklyAndMonthlyReport = new PDFWeeklyAndMonthlyReport(getContext(), model.getCurrentDate());
                    pdfWeeklyAndMonthlyReport.generateCompleteReport(workTime, notWorkTime, getResources(), model.getCurrentDate(), 7, measurementKindLocalization, observer);
                }
            };
            dailyReportGenerator.generateWeeklyOrMonthlyNotWorkTimeReport(reportRepository, model.getCurrentDate(), 7, true, observerNotWorkTime);
        };

        dailyReportGenerator.generateWeeklyOrMonthlyWorkTimeReport(reportRepository, model.getCurrentDate(), 7, true, observerWorkTime);
    }

    public void onUploadMonthlyPdf() {
        Observer<byte[]> observer = pdfData -> {
            try {
                System.out.println("Aqui");
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path.toString(), "my_monthly_file" + ".pdf");
                OutputStream os = new FileOutputStream(file);
                os.write(pdfData);
                os.close();
                System.out.println("Escreveu");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ReportRepository reportRepository = new ReportRepository(requireContext());
        ReportGenerator dailyReportGenerator = new ReportGenerator(requireContext());

        int days = model.getCurrentDate().lengthOfMonth();

        Observer<Report> observerWorkTime = workTime -> {
            Observer<Report> observerNotWorkTime = notWorkTime -> {
                if (workTime.getGroups().size() > 0 || notWorkTime.getGroups().size() > 0) {
                    PDFWeeklyAndMonthlyReport pdfWeeklyAndMonthlyReport = new PDFWeeklyAndMonthlyReport(getContext(), model.getCurrentDate());
                    pdfWeeklyAndMonthlyReport.generateCompleteReport(workTime, notWorkTime, getResources(), model.getCurrentDate(), days, measurementKindLocalization, observer);
                }
            };
            dailyReportGenerator.generateWeeklyOrMonthlyNotWorkTimeReport(reportRepository, model.getCurrentDate(), days, true, observerNotWorkTime);
        };

        dailyReportGenerator.generateWeeklyOrMonthlyWorkTimeReport(reportRepository, model.getCurrentDate(), days, true, observerWorkTime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_report_detail, container, false);

        AccountsViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountsViewModel.class);

        if (viewModel.hasSmart4HealthAccount()) {
            MenuHost menuHost = requireActivity();
            menuHost.addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.report_upload_pdf_fragment, menu);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.upload_pdf) {
                        onUploadPdfClick();
                    } else if (menuItem.getItemId() == R.id.upload_weekly_pdf) {
                        onUploadWeeklyPdf();
                    } else if (menuItem.getItemId() == R.id.upload_monthly_pdf) {
                        onUploadMonthlyPdf();
                    }

                    return true;
                }
            });

        }

        /*Button uploadPdfButton = view.findViewById(R.id.uploadButton);
        Button viewPdfButton = view.findViewById(R.id.viewPdfButton);
        AccountsViewModel viewModel = new AccountsViewModel(requireActivity().getApplication());

        if (viewModel.hasSmart4HealthAccount()) {

            viewPdfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = BuildConfig.SMART4HEALTH_APP_URL;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        } else {
            viewPdfButton.setVisibility(View.GONE);
            uploadPdfButton.setVisibility(View.GONE);
        }*/

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

        // Primeira verão da página dos relatórios
        /*Observer<Report> observerWorkTimeReport = workTimeData -> {

            Observer<Report> observerNotWorkTimeReport = notWorkTimeData -> {

                List<Group> groupsWorkTimeData = workTimeData.getGroups();
                List<Group> groupsNotWorkTimeData = notWorkTimeData.getGroups();

                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                TableLayout tableLayout = view.findViewById(R.id.reportTableLayout);

                requireActivity().runOnUiThread(() -> {

                    for (Group groupNotWorkTime : groupsNotWorkTimeData) {

                        int labelNotWorkTime = ((StringMeasurementId)groupNotWorkTime.getLabel()).getMeasurementId();
                        //String labelNotWorkTime = groupNotWorkTime.getLabel().getLocalizedString();

                        displayTitle(tableLayout, measurementKindLocalization.localize(labelNotWorkTime));

                        if (labelNotWorkTime == Measurement.TYPE_BLOOD_PRESSURE || labelNotWorkTime == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING) {
                            for (Group group : groupNotWorkTime.getGroupList()) {
                                String timestamp = group.getLabel().getLocalizedString();
                                displayTimestamp(tableLayout, timestamp.substring(timestamp.indexOf("T") + 1, timestamp.indexOf("Z")) + " - MyTime");
                                for (Item item : group.getItemList()) {
                                    addNewRow(tableLayout,
                                            item.getLabel().getLocalizedString(),
                                            "-",
                                            item.getValue().getLocalizedString(),
                                            item.getUnits().getLocalizedString());
                                }
                            }
                            for(Group groupWorkTime : groupsWorkTimeData){
                                int labelWorkTime = ((StringMeasurementId)groupWorkTime.getLabel()).getMeasurementId();
                                if(labelWorkTime == labelNotWorkTime){
                                    for(Group group : groupsWorkTimeData){
                                        String timestamp = group.getLabel().getLocalizedString();
                                        displayTimestamp(tableLayout, timestamp.substring(timestamp.indexOf("T") + 1, timestamp.indexOf("Z")) + " - MyWork");
                                        for(Item item : group.getItemList()){
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
                            for (Group groupWorkTime : groupsWorkTimeData){
                                int labelWorkTime = ((StringMeasurementId)groupWorkTime.getLabel()).getMeasurementId();
                                if(labelNotWorkTime == labelWorkTime){
                                    hasGroup = true;
                                    for (Item itemNotWorkTime : groupNotWorkTime.getItemList()){
                                        String itemLabel = itemNotWorkTime.getLabel().getLocalizedString();
                                        for(Item itemWorkTime : groupWorkTime.getItemList()){
                                            if(itemLabel.equals(itemWorkTime.getLabel().getLocalizedString())){
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
                            if(!hasGroup){
                                for (Item item : groupNotWorkTime.getItemList()) {
                                    addNewRow(tableLayout,
                                            item.getLabel().getLocalizedString(),
                                            item.getValue().getLocalizedString(),
                                            "0",
                                            item.getUnits().getLocalizedString());
                                }
                            }
                        }
                    }

                    for (Group groupWorkTime : groupsWorkTimeData){
                        int labelWorkTime = ((StringMeasurementId)groupWorkTime.getLabel()).getMeasurementId();
                        boolean hasGroup = false;
                        for (Group groupNotWorkTime : groupsNotWorkTimeData) {
                            if (labelWorkTime == ((StringMeasurementId) groupNotWorkTime.getLabel()).getMeasurementId()) {
                                hasGroup = true;
                                break;
                            }
                        }
                        if(!hasGroup){
                            displayTitle(tableLayout, measurementKindLocalization.localize(labelWorkTime));
                            if(labelWorkTime == Measurement.TYPE_BLOOD_PRESSURE || labelWorkTime == Measurement.TYPE_LUMBAR_EXTENSION_TRAINING){
                                for (Group group : groupWorkTime.getGroupList()) {
                                    String timestamp = group.getLabel().getLocalizedString();
                                    displayTimestamp(tableLayout, timestamp.substring(timestamp.indexOf("T") + 1, timestamp.indexOf("Z")) + " - MyWork");
                                    for (Item item : group.getItemList()) {
                                        addNewRow(tableLayout,
                                                item.getLabel().getLocalizedString(),
                                                "-",
                                                item.getValue().getLocalizedString(),
                                                item.getUnits().getLocalizedString());
                                    }
                                }
                            }
                            else{
                                for (Item item : groupWorkTime.getItemList()) {
                                    addNewRow(tableLayout,
                                            item.getLabel().getLocalizedString(),
                                            "0",
                                            item.getValue().getLocalizedString(),
                                            item.getUnits().getLocalizedString());
                                }
                            }
                        }
                    }
                });
            };

            model.getNotWorkTimeReport(getActivity().getApplication(), observerNotWorkTimeReport);
        };

        model.getWorkTimeReport(getActivity().getApplication(), observerWorkTimeReport);*/

        Observer<Report> observerWorkTimeReport = workTimeData -> {

            Observer<Report> observerNotWorkTimeReport = notWorkTimeData -> {

                List<Group> groupsWorkTimeData = workTimeData.getGroups();
                List<Group> groupsNotWorkTimeData = notWorkTimeData.getGroups();

                DecimalFormat decimalFormat = new DecimalFormat("#.##");
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

    private void displayTitle(TableLayout tableLayout, String title) {
        View vTitle = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_title, null);
        TextView tvTitle = vTitle.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        tableLayout.addView(vTitle);
    }

    /*private void addNewRow(TableLayout tableLayout, String label, String valueMyTime, String valueWorkTime, String units){
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_rows, null);
        TextView tvLabel = v.findViewById(R.id.tvLabel);
        TextView tvValueMyTime = v.findViewById(R.id.tvValueMyTime);
        TextView tvValueWorkTime = v.findViewById(R.id.tvValueWorkTime);
        TextView tvUnits = v.findViewById(R.id.tvUnits);

        tvLabel.setText(label);

        if(valueMyTime.equals("-")) {
            tvValueMyTime.setVisibility(View.INVISIBLE);
        }
        else {
            tvValueMyTime.setText(valueMyTime);
        }

        tvValueWorkTime.setText(valueWorkTime);

        if(units.equals("-")) {
            tvUnits.setVisibility(View.INVISIBLE);
        }
        else {
            tvUnits.setText(units);
        }

        tableLayout.addView(v);
    }*/

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

    private void displayTimestamp(TableLayout tableLayout, String timestamp, boolean addPadding) {
        View vTimestamp = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_timestamp, null);
        TextView tvTimestamp = vTimestamp.findViewById(R.id.tvTimestamp);
        if (addPadding)
            tvTimestamp.setPadding(0, 15, 0, 0);
        tvTimestamp.setText(timestamp);
        tableLayout.addView(vTimestamp);
    }

    private String secondsToString(int value) {
        int seconds = value;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        if (minutes > 0)
            seconds = seconds % 60;

        if (hours > 0) {
            minutes = minutes % 60;
        }

        String result = ((hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + (seconds > 0 ? seconds + "s" : "")).trim();

        return result.equals("") ? "0s" : result;
    }
}

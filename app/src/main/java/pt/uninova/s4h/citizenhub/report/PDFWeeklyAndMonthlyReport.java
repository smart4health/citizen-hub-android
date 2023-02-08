package pt.uninova.s4h.citizenhub.report;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailBloodPressureUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailHeartRateUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.repository.BloodPressureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.ui.summary.ChartFunctions;
import pt.uninova.s4h.citizenhub.ui.summary.VerticalTextView;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class PDFWeeklyAndMonthlyReport {
    private final Context context;

    private final Paint logoBackgroundPaint;
    private final TextPaint footerPaint;
    private final Paint titlePaint;
    private final Paint darkTextPaintAlignLeft;
    private final Paint darkTextPaintAlignRight;
    private final Paint darkItalicTextPaint;
    private final Paint whiteTextPaint;
    private final Paint whiteItalicTextPaint;
    private final Paint backgroundPaint;
    private final Paint rectPaint;
    private final Paint rectFillPaint;
    private final float[] corners;
    private final ChartFunctions chartFunctions;
    private final List<SummaryDetailUtil> steps = new ArrayList<>();
    private List<SummaryDetailUtil> bloodPressure = new ArrayList<>();
    private List<SummaryDetailUtil> heartRate = new ArrayList<>();
    private final List<SummaryDetailUtil> correctPosture = new ArrayList<>();
    private final List<SummaryDetailUtil> incorrectPosture = new ArrayList<>();

    public PDFWeeklyAndMonthlyReport(Context context, LocalDate localDate) {
        this.context = context;

        this.logoBackgroundPaint = new Paint();
        logoBackgroundPaint.setStyle(Paint.Style.FILL);
        logoBackgroundPaint.setColor(Color.parseColor("#f0f0f0"));
        logoBackgroundPaint.setAntiAlias(true);

        this.footerPaint = new TextPaint();
        footerPaint.setStyle(Paint.Style.FILL);
        footerPaint.setTextSize(9);
        footerPaint.setColor(Color.parseColor("#000000"));
        footerPaint.setAntiAlias(true);

        this.titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#FFFFFF"));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        this.darkTextPaintAlignLeft = new Paint();
        darkTextPaintAlignLeft.setColor(Color.parseColor("#000000"));
        darkTextPaintAlignLeft.setTextAlign(Paint.Align.LEFT);
        darkTextPaintAlignLeft.setTypeface(Typeface.DEFAULT);
        darkTextPaintAlignLeft.setTextSize(12);

        this.darkTextPaintAlignRight = new Paint();
        darkTextPaintAlignRight.setColor(Color.parseColor("#000000"));
        darkTextPaintAlignRight.setTextAlign(Paint.Align.RIGHT);
        darkTextPaintAlignRight.setTypeface(Typeface.DEFAULT);
        darkTextPaintAlignRight.setTextSize(12);

        this.darkItalicTextPaint = new Paint();
        darkItalicTextPaint.setColor(Color.parseColor("#000000"));
        darkItalicTextPaint.setTextAlign(Paint.Align.LEFT);
        darkItalicTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        darkItalicTextPaint.setTextSize(12);

        this.whiteTextPaint = new Paint();
        whiteTextPaint.setColor(Color.parseColor("#ffffff"));
        whiteTextPaint.setTextAlign(Paint.Align.LEFT);
        whiteTextPaint.setTypeface(Typeface.DEFAULT);
        whiteTextPaint.setTextSize(12);

        this.whiteItalicTextPaint = new Paint();
        whiteItalicTextPaint.setColor(Color.parseColor("#ffffff"));
        whiteItalicTextPaint.setTextAlign(Paint.Align.RIGHT);
        whiteItalicTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        whiteItalicTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(Color.parseColor("#000000"));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);

        this.backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.parseColor("#2789C2"));
        backgroundPaint.setAntiAlias(true);

        this.rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(Color.parseColor("#06344F"));

        this.rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(Color.parseColor("#06344F"));

        this.corners = new float[]{
                12, 12,        // Top left radius in px
                12, 12,        // Top right radius in px
                0, 0,          // Bottom right radius in px
                0, 0           // Bottom left radius in px
        };

        chartFunctions = new ChartFunctions(context, localDate);

    }

    private void fetchChartsInfo(LocalDate localDate, int days) {

        Observer<List<SummaryDetailUtil>> observerSteps = steps::addAll;
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(context);
        stepsSnapshotMeasurementRepository.readSeveralDays(localDate, days, observerSteps);

        Observer<List<SummaryDetailBloodPressureUtil>> observerBloodPressure = data -> bloodPressure = chartFunctions.parseBloodPressureUtil(data);
        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(context);
        bloodPressureMeasurementRepository.selectSeveralDays(localDate, days, observerBloodPressure);

        Observer<List<SummaryDetailHeartRateUtil>> observerHeartRate = data -> heartRate = chartFunctions.parseHeartRateUtil(data);
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(context);
        heartRateMeasurementRepository.selectSeveralDays(localDate, days, observerHeartRate);

        Observer<List<SummaryDetailUtil>> observerCorrectPosture = correctPosture::addAll;
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(context);
        postureMeasurementRepository.readSeveralDaysCorrectPosture(localDate, days, observerCorrectPosture);

        Observer<List<SummaryDetailUtil>> observerIncorrectPosture = incorrectPosture::addAll;
        postureMeasurementRepository.readSeveralDaysIncorrectPosture(localDate, days, observerIncorrectPosture);
    }

    public void generateCompleteReport(Report workTime, Report notWorkTime, Resources res, LocalDate date, int days, MeasurementKindLocalization measurementKindLocalization, Observer<byte[]> observerReportPDF) {
        if (Looper.myLooper() == null)
            Looper.prepare();
        fetchChartsInfo(date, days);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        //canvas.setDensity(72);

        CanvasWriter canvasWriter = new CanvasWriter(canvas);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        List<Group> groupsWorkTime = workTime.getGroups();
        List<Group> groupsNotWorkTime = notWorkTime.getGroups();

        int y = drawHeaderAndFooter(canvas, canvasWriter, res, workTime.getTitle().getLocalizedString(), date);

        for (Group groupNotWorkTime : groupsNotWorkTime)
        {
            if (verifyGroupSize(groupNotWorkTime, y, false))
            {
                writePage(document, page, canvasWriter);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvasWriter = new CanvasWriter(canvas);
                y = drawHeaderAndFooter(canvas, canvasWriter, res, workTime.getTitle().getLocalizedString(), date);
            }
            int rectHeight = y - 20;
            int notWorkTimeLabel = ((MeasurementTypeLocalizedResource) groupNotWorkTime.getLabel()).getMeasurementType();
            y = drawGroupHeader(canvas, canvasWriter, measurementKindLocalization, notWorkTimeLabel, y, rectHeight);
            boolean hasItem = false;
            for (Group groupWorkTime : groupsWorkTime)
            {
                if (notWorkTimeLabel == ((MeasurementTypeLocalizedResource) groupWorkTime.getLabel()).getMeasurementType())
                {
                    hasItem = true;
                    y = drawCharts(canvas, notWorkTimeLabel, days, y);
                    y = drawSimpleGroups(canvasWriter, groupNotWorkTime, groupWorkTime, y);
                }
            }
            if (!hasItem)
            {
                y = drawCharts(canvas, notWorkTimeLabel, days, y);
                y = drawSimpleGroups(canvasWriter, groupNotWorkTime, null, y);
            }
            drawRect(canvas, y, rectHeight);
        }
        for (Group groupWorkTime : groupsWorkTime)
        {
            boolean hasGroup = false;
            int label = ((MeasurementTypeLocalizedResource) groupWorkTime.getLabel()).getMeasurementType();
            for (Group groupNotWorkTime : groupsNotWorkTime)
            {
                if (label == ((MeasurementTypeLocalizedResource) groupNotWorkTime.getLabel()).getMeasurementType())
                {
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup)
            {
                if (verifyGroupSize(groupWorkTime, y, false))
                {
                    writePage(document, page, canvasWriter);
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvasWriter = new CanvasWriter(canvas);
                    y = drawHeaderAndFooter(canvas, canvasWriter, res, workTime.getTitle().getLocalizedString(), date);
                }
                int rectHeight = y - 20;
                y = drawGroupHeader(canvas, canvasWriter, measurementKindLocalization, label, y, rectHeight);
                y = drawCharts(canvas, label, days, y);
                y = drawSimpleGroups(canvasWriter, null, groupWorkTime, y);
                drawRect(canvas, y, rectHeight);
            }
        }

        if (bloodPressure.size() > 0)
        {
            if (y + 195 > 842)
            {
                writePage(document, page, canvasWriter);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvasWriter = new CanvasWriter(canvas);
                y = drawHeaderAndFooter(canvas, canvasWriter, res, workTime.getTitle().getLocalizedString(), date);
            }
            int rectHeight = y - 20;
            y = drawGroupHeader(canvas, canvasWriter, measurementKindLocalization, Measurement.TYPE_BLOOD_PRESSURE, y, rectHeight);
            y = drawCharts(canvas, Measurement.TYPE_BLOOD_PRESSURE, days, y);
            y += 43;
            drawRect(canvas, y, rectHeight);
        }

        if (heartRate.size() > 0)
        {
            if (y + 195 > 842)
            {
                writePage(document, page, canvasWriter);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvasWriter = new CanvasWriter(canvas);
                y = drawHeaderAndFooter(canvas, canvasWriter, res, workTime.getTitle().getLocalizedString(), date);
            }
            int rectHeight = y - 20;
            y = drawGroupHeader(canvas, canvasWriter, measurementKindLocalization, Measurement.TYPE_HEART_RATE, y, rectHeight);
            y = drawCharts(canvas, Measurement.TYPE_HEART_RATE, days, y);
            y += 43;
            drawRect(canvas, y, rectHeight);
        }

        writePage(document, page, canvasWriter);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            document.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] outByteArray = out.toByteArray();
        document.close();
        observerReportPDF.observe(outByteArray);

        if (Looper.myLooper() != null)
            Looper.myLooper().quitSafely();
    }

    private int drawHeaderAndFooter(Canvas canvas, CanvasWriter canvasWriter, Resources res, String title, LocalDate date) {
        /* CitizenHub Logo */
        final Drawable citizenHubLogo = ResourcesCompat.getDrawable(res, R.drawable.ic_citizen_hub_logo, null);

        citizenHubLogo.setBounds(0, 0, citizenHubLogo.getIntrinsicWidth(), citizenHubLogo.getIntrinsicHeight());
        canvas.save();
        canvas.translate(60, 40);
        canvas.scale(1.0f, 1.0f);
        citizenHubLogo.draw(canvas);
        canvas.restore();

        final Drawable citizenHub = ResourcesCompat.getDrawable(res, R.drawable.logo_citizen_hub_text_only, null);

        citizenHub.setBounds(0, 0, citizenHub.getIntrinsicWidth(), citizenHub.getIntrinsicHeight());
        canvas.save();
        canvas.translate(100, 50);
        canvas.scale(2f, 2f);
        citizenHub.draw(canvas);
        canvas.restore();

        /* Header */
        canvas.drawRoundRect(50, 110, 550, 155, 10, 10, backgroundPaint); // 80
        canvasWriter.addText(title, 60, 138, titlePaint);
        canvasWriter.addText(date.toString(), 445, 138, titlePaint);

        /* Footer */
        StaticLayout textLayout = new StaticLayout(res.getString(R.string.report_footer), footerPaint, 480, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.translate(60, 805);
        textLayout.draw(canvas);
        canvas.translate(-60, -805);

        return 200;
    }

    private int drawGroupHeader(Canvas canvas, CanvasWriter canvasWriter, MeasurementKindLocalization measurementKindLocalization, int label, int y, int rectHeight) {
        Path path = new Path();
        path.addRoundRect(new RectF(50, rectHeight, 550, rectHeight + 25), corners, Path.Direction.CW);
        canvas.drawPath(path, rectFillPaint);
        canvasWriter.addText(measurementKindLocalization.localize(label), 70, y - 4, whiteTextPaint);
        canvasWriter.addText("MyTime", 380, y - 4, whiteItalicTextPaint);
        canvasWriter.addText("MyWork", 500, y - 4, whiteItalicTextPaint);
        return y + 40;
    }

    private void drawRect(Canvas canvas, int y, int rectHeight) {
        RectF rectAround = new RectF(50, rectHeight, 550, y - 50);
        canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
    }

    private boolean verifyGroupSize(Group group, int y, boolean complex) {
        y += 25;
        if (group.getGroupList().size() == 0) {
            if (complex) {
                y += 20 + 20 * group.getItemList().size() + 5 + 38;
                return y >= 842;
            }
            y += 20 * group.getItemList().size() + 5 + 170;
            return y >= 842;
        }
        y += 20 + 20 * group.getItemList().size() + 5 + 38;
        return y >= 842;
    }

    private int drawSimpleGroups(CanvasWriter canvasWriter, Group firstGroup, Group secondGroup, int y) {
        if (firstGroup != null & secondGroup != null) {
            for (Item itemNotWorkTime : firstGroup.getItemList()) {
                for (Item itemWorkTime : secondGroup.getItemList()) {
                    if (itemNotWorkTime.getLabel().getLocalizedString().equals(itemWorkTime.getLabel().getLocalizedString())) {
                        canvasWriter.addText(itemNotWorkTime.getLabel().getLocalizedString(), 90, y, darkTextPaintAlignLeft);
                        canvasWriter.addText(itemNotWorkTime.getValue().getLocalizedString(), 350, y, darkTextPaintAlignRight);
                        if (!itemNotWorkTime.getUnits().getLocalizedString().equals("-")) {
                            canvasWriter.addText(itemNotWorkTime.getUnits().getLocalizedString(), 360, y, darkItalicTextPaint);
                        }
                        canvasWriter.addText(itemWorkTime.getValue().getLocalizedString(), 470, y, darkTextPaintAlignRight);
                        if (!itemWorkTime.getUnits().getLocalizedString().equals("-")) {
                            canvasWriter.addText(itemNotWorkTime.getUnits().getLocalizedString(), 480, y, darkItalicTextPaint);
                        }
                        y += 20;
                        break;
                    }
                }
            }
        } else {
            if (firstGroup != null) {
                for (Item item : firstGroup.getItemList()) {
                    canvasWriter.addText(item.getLabel().getLocalizedString(), 90, y, darkTextPaintAlignLeft);
                    canvasWriter.addText(item.getValue().getLocalizedString(), 350, y, darkTextPaintAlignRight);
                    if (!item.getUnits().getLocalizedString().equals("-")) {
                        canvasWriter.addText(" " + item.getUnits().getLocalizedString(), 360, y, darkItalicTextPaint);
                    }
                    canvasWriter.addText("-", 470, y, darkTextPaintAlignRight);
                    y += 20;
                }
            } else {
                if (secondGroup != null) {
                    for (Item item : secondGroup.getItemList()) {
                        canvasWriter.addText(item.getLabel().getLocalizedString(), 90, y, darkTextPaintAlignLeft);
                        canvasWriter.addText("-", 350, y, darkTextPaintAlignRight);
                        canvasWriter.addText(item.getValue().getLocalizedString(), 470, y, darkTextPaintAlignRight);
                        if (!item.getUnits().getLocalizedString().equals("-")) {
                            canvasWriter.addText(" " + item.getUnits().getLocalizedString(), 480, y, darkItalicTextPaint);
                        }
                        y += 20;
                    }
                }
            }
        }
        return y + 43;
    }

    private int drawCharts(Canvas canvas, int label, int days, int y) {
        View chart;
        switch (label) {
            case Measurement.TYPE_ACTIVITY:
            case Measurement.TYPE_DISTANCE_SNAPSHOT:
                System.out.println("Activity");
                chart = LayoutInflater.from(context).inflate(R.layout.fragment_report_bar_chart, null);
                drawBarChart(chart, steps, days);
                break; //Ver com o carlos
            case Measurement.TYPE_BLOOD_PRESSURE:
                System.out.println("Blood Pressure");
                chart = LayoutInflater.from(context).inflate(R.layout.fragment_report_line_chart, null);
                drawLineChart(chart, bloodPressure, new String[]{context.getString(R.string.summary_detail_blood_pressure_systolic), context.getString(R.string.summary_detail_blood_pressure_diastolic), context.getString(R.string.summary_detail_blood_pressure_mean)}, context.getString(R.string.summary_detail_blood_pressure_with_units), days);
                break;
            case Measurement.TYPE_HEART_RATE:
                System.out.println("Heart Rate");
                chart = LayoutInflater.from(context).inflate(R.layout.fragment_report_line_chart, null);
                drawLineChart(chart, heartRate, new String[]{context.getString(R.string.summary_detail_heart_rate_average), context.getString(R.string.summary_detail_heart_rate_maximum), context.getString(R.string.summary_detail_heart_rate_minimum)}, context.getString(R.string.summary_detail_heart_rate_with_units), days);
                break;
            case Measurement.TYPE_POSTURE:
                System.out.println("Posture");
                chart = LayoutInflater.from(context).inflate(R.layout.fragment_report_line_chart, null);
                drawAreaChart(chart, correctPosture, incorrectPosture, new String[]{context.getString(R.string.summary_detail_posture_correct), context.getString(R.string.summary_detail_posture_incorrect)}, context.getString(R.string.summary_detail_posture), days);
                break;
            default:
                chart = null;
        }
        canvas.translate(65, y - 40);
        assert chart != null;
        chart.draw(canvas);
        canvas.translate(-65, 40 - y);
        return y + 170;
    }

    private void drawBarChart(View chart, List<SummaryDetailUtil> data, int days) {
        BarChart barChart = chart.findViewById(R.id.bar_chart);
        barChart.getXAxis().setTextSize(6f);
        barChart.getAxisLeft().setTextSize(6f);
        chartFunctions.setupBarChart(barChart, null);
        chartFunctions.setBarChartData(barChart, data, context.getString(R.string.summary_detail_activity_steps), days);
        chart.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        chart.layout(chart.getLeft(), chart.getTop(), chart.getRight(), chart.getBottom());
    }

    private void drawLineChart(View chart, List<SummaryDetailUtil> data, String[] labels, String leftAxisLabel, int days) {
        LineChart lineChart = chart.findViewById(R.id.line_chart);
        chartFunctions.setupLineChart(lineChart, null);
        lineChart.getXAxis().setAxisMaximum(days - 1);
        lineChart.getXAxis().setTextSize(6f);
        lineChart.getAxisLeft().setTextSize(6f);
        VerticalTextView verticalTextView = chart.findViewById(R.id.text_view_y_axis_label);
        verticalTextView.setText(leftAxisLabel);
        chartFunctions.setLineChartData(lineChart, data, labels, days);
        chart.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        chart.layout(chart.getLeft(), chart.getTop(), chart.getRight(), chart.getBottom());
    }

    private void drawAreaChart(View chart, List<SummaryDetailUtil> data1, List<SummaryDetailUtil> data2, String[] labels, String leftAxisLabel, int days) {
        LineChart lineChart = chart.findViewById(R.id.line_chart);
        chartFunctions.setupLineChart(lineChart, null);
        lineChart.getAxisLeft().setAxisMaximum(100);
        lineChart.getXAxis().setAxisMaximum(days - 1);
        lineChart.getAxisLeft().setTextSize(6f);
        lineChart.getXAxis().setTextSize(6f);
        VerticalTextView verticalTextView = chart.findViewById(R.id.text_view_y_axis_label);
        verticalTextView.setText(leftAxisLabel);
        chartFunctions.setAreaChart(lineChart, data1, data2, labels, days);
        chart.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        chart.layout(chart.getLeft(), chart.getTop(), chart.getRight(), chart.getBottom());
    }

    private int createNewPage(Canvas canvas, CanvasWriter canvasWriter, Resources res, String title, LocalDate date) {
        drawHeaderAndFooter(canvas, canvasWriter, res, title, date);
        return 200;
    }

    private void writePage(PdfDocument document, PdfDocument.Page page, CanvasWriter canvasWriter) {
        canvasWriter.draw();
        document.finishPage(page);
    }
}

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
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.repository.BloodPressureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsMeasurementRepository;
import pt.uninova.s4h.citizenhub.ui.summary.ChartFunctions;
import pt.uninova.s4h.citizenhub.ui.summary.TwoDimensionalChartData;
import pt.uninova.s4h.citizenhub.ui.summary.VerticalTextView;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Class responsible for drawing weekly and monthly PDFs. */
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
    private TwoDimensionalChartData steps = null;
    private TwoDimensionalChartData bloodPressure = null;
    private TwoDimensionalChartData heartRate = null;

    private TwoDimensionalChartData posture = null;

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

    /** Calls multiple queries to have the information to draw into the chart to add to the PDF report.
     * @param localDate Date of the report..
     * @param days The number of days that are going to be displayed in the report. It depends on the current month and if it is a weekly report.
     * @return
     * */
    private void fetchChartsInfo(LocalDate localDate, int days) {

        Observer<List<DailyStepsPanel>> observerSteps = data -> steps = chartFunctions.parseStepsUtil(data, days);
        StepsMeasurementRepository stepsMeasurementRepository = new StepsMeasurementRepository(context);
        stepsMeasurementRepository.readSeveralDays(localDate, days, observerSteps);

        Observer<List<DailyBloodPressurePanel>> observerBloodPressure = data -> {
            if(data.size() > 0)
                bloodPressure = chartFunctions.parseBloodPressureUtil(data, days);
        };
        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(context);
        bloodPressureMeasurementRepository.selectSeveralDays(localDate, days, observerBloodPressure);

        Observer<List<DailyHeartRatePanel>> observerHeartRate = data -> {
            if(data.size() > 0)
                heartRate = chartFunctions.parseHeartRateUtil(data, days);
        };
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(context);
        heartRateMeasurementRepository.selectSeveralDays(localDate, days, observerHeartRate);

        Observer<List<DailyPosturePanel>> observerPosture = data -> posture = chartFunctions.parsePostureUtil(data, days);
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(context);
        postureMeasurementRepository.readSeveralDaysPosture(localDate, days, observerPosture);

    }

    /** Generates a complete report containing the complete weekly or monthly information.
     * @param workTime Report with the information during working hours.
     * @param notWorkTime Report with the information outside working hours.
     * @param res Android resources.
     * @param date Date of the report.
     * @param days The number of days that are going to be displayed in the report. It depends on the current month and if it is a weekly report.
     * @param measurementKindLocalization Decodes the type of information to process.
     * @param observerReportPDF Observes the PDF generation.
     * @return
     * */
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

        if (bloodPressure != null)
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

        if (heartRate != null)
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

    /** Draws the header and the footer of the report.
     * @param canvas The canvas where the report is drawn.
     * @param canvasWriter A canvas writer.
     * @param res Android resources.
     * @param title It can be "Weekly Report" or "Monthly Report".
     * @param date Report date.
     * @return Position in the PDF page (height) after drawing the header.
     * */
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

    /** Draws the header of a group.
     * @param canvas The canvas where the report is drawn.
     * @param canvasWriter A canvas writer.
     * @param measurementKindLocalization Decodes the type of information to process.
     * @param label Group label.
     * @param y The position where the group header will be drawn (height).
     * @param rectHeight The starting position of the rectangle that surrounds a group.
     * @return
     * */
    private int drawGroupHeader(Canvas canvas, CanvasWriter canvasWriter, MeasurementKindLocalization measurementKindLocalization, int label, int y, int rectHeight) {
        Path path = new Path();
        path.addRoundRect(new RectF(50, rectHeight, 550, rectHeight + 25), corners, Path.Direction.CW);
        canvas.drawPath(path, rectFillPaint);
        canvasWriter.addText(measurementKindLocalization.localize(label), 70, y - 4, whiteTextPaint);
        canvasWriter.addText("MyTime", 380, y - 4, whiteItalicTextPaint);
        canvasWriter.addText("MyWork", 500, y - 4, whiteItalicTextPaint);
        return y + 40;
    }

    /** Draws a group surrounding rectangle.
     * @param canvas The canvas where the report is drawn.
     * @param y The position where the group header will be drawn (height).
     * @param rectHeight The starting position of the rectangle.
     * */
    private void drawRect(Canvas canvas, int y, int rectHeight) {
        RectF rectAround = new RectF(50, rectHeight, 550, y - 50);
        canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
    }

    /** Verifies if the next group to be drawn into the report still fits the PDF page.
     * @param group The group to be drawn.
     * @param y The current page position (height) where the PDF is being drawn.
     * @param complex If a group is composed by other groups.
     * @return True if the group still fits the page and false if not.
     * */
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

    /** Draws groups composed only by items into the PDF.
     * @param canvasWriter A writer for the canvas.
     * @param notWorkTime Group with the information regarding the time outside the working hours.
     * @param workTime Group with the information regarding the time during the working hours.
     * @param y Page current position (height).
     * @return The PDF position after the group was drawn.
     * */
    private int drawSimpleGroups(CanvasWriter canvasWriter, Group notWorkTime, Group workTime, int y) {
        if (notWorkTime != null & workTime != null) {
            for (Item itemNotWorkTime : notWorkTime.getItemList()) {
                for (Item itemWorkTime : workTime.getItemList()) {
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
            if (notWorkTime != null) {
                for (Item item : notWorkTime.getItemList()) {
                    canvasWriter.addText(item.getLabel().getLocalizedString(), 90, y, darkTextPaintAlignLeft);
                    canvasWriter.addText(item.getValue().getLocalizedString(), 350, y, darkTextPaintAlignRight);
                    if (!item.getUnits().getLocalizedString().equals("-")) {
                        canvasWriter.addText(" " + item.getUnits().getLocalizedString(), 360, y, darkItalicTextPaint);
                    }
                    canvasWriter.addText("-", 470, y, darkTextPaintAlignRight);
                    y += 20;
                }
            } else {
                if (workTime != null) {
                    for (Item item : workTime.getItemList()) {
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

    /** Draws the charts and their respective information into the PDF page.
     * @param canvas The canvas where the report is drawn.
     * @param label Label of the information to be drawn. Used to know which chart it is going to be drawn.
     * @param days Number of days to be displayed in the charts.
     * @param y Page current position (height).
     * @return The PDF position (height) after the group was drawn.
     * */
    private int drawCharts(Canvas canvas, int label, int days, int y) {
        View chart;
        switch (label) {
            case Measurement.TYPE_ACTIVITY:
            case Measurement.TYPE_DISTANCE_SNAPSHOT:
                System.out.println("Activity");
                chart = LayoutInflater.from(context).inflate(R.layout.fragment_report_bar_chart, null);
                drawBarChart(chart, steps, days);
                break;
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
                drawAreaChart(chart, posture, new String[]{context.getString(R.string.summary_detail_posture_correct), context.getString(R.string.summary_detail_posture_incorrect)}, context.getString(R.string.summary_detail_posture), days);
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

    /** Draws a bar chart into the PDF page.
     * @param chart View for the respective chart.
     * @param twoDimensionalChartData Generic class containing the information to display in the chart.
     * @param days Number of days to be displayed in the chart.
     * @return
     * */
    private void drawBarChart(View chart, TwoDimensionalChartData twoDimensionalChartData, int days) {
        BarChart barChart = chart.findViewById(R.id.bar_chart);
        barChart.getXAxis().setTextSize(6f);
        barChart.getAxisLeft().setTextSize(6f);
        chartFunctions.setupBarChart(barChart, null);
        chartFunctions.setBarChartData(barChart, twoDimensionalChartData, context.getString(R.string.summary_detail_activity_steps), days);
        chart.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        chart.layout(chart.getLeft(), chart.getTop(), chart.getRight(), chart.getBottom());
    }

    /** Draws a line chart into the PDF page.
     * @param chart View for the respective chart.
     * @param twoDimensionalChartData Generic class containing the information to display in the chart.
     * @param labels Chart's labels.
     * @param leftAxisLabel Y axis label.
     * @param days Number of days to be displayed in the chart.
     * */
    private void drawLineChart(View chart, TwoDimensionalChartData twoDimensionalChartData, String[] labels, String leftAxisLabel, int days) {
        LineChart lineChart = chart.findViewById(R.id.line_chart);
        chartFunctions.setupLineChart(lineChart, null);
        lineChart.getXAxis().setAxisMaximum(days - 1);
        lineChart.getXAxis().setTextSize(6f);
        lineChart.getAxisLeft().setTextSize(6f);
        VerticalTextView verticalTextView = chart.findViewById(R.id.text_view_y_axis_label);
        verticalTextView.setText(leftAxisLabel);
        chartFunctions.setLineChartData(lineChart, twoDimensionalChartData, labels, days);
        chart.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        chart.layout(chart.getLeft(), chart.getTop(), chart.getRight(), chart.getBottom());
    }

    /** Draws a area chart into the PDF page.
     * @param chart View for the respective chart.
     * @param twoDimensionalChartData Generic class containing the information to display in the chart.
     * @param labels Chart's labels.
     * @param leftAxisLabel Y axis label.
     * @param days Number of days to be displayed in the chart.
     * */
    private void drawAreaChart(View chart, TwoDimensionalChartData twoDimensionalChartData, String[] labels, String leftAxisLabel, int days) {
        LineChart lineChart = chart.findViewById(R.id.line_chart);
        chartFunctions.setupLineChart(lineChart, null);
        lineChart.getAxisLeft().setAxisMaximum(100);
        lineChart.getXAxis().setAxisMaximum(days - 1);
        lineChart.getAxisLeft().setTextSize(6f);
        lineChart.getXAxis().setTextSize(6f);
        VerticalTextView verticalTextView = chart.findViewById(R.id.text_view_y_axis_label);
        verticalTextView.setText(leftAxisLabel);
        chartFunctions.setAreaChart(lineChart, twoDimensionalChartData, labels, days);
        chart.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        chart.layout(chart.getLeft(), chart.getTop(), chart.getRight(), chart.getBottom());
    }

    /** Writes the canvas page into the PDF itself.
     * @param document Document containing the PDF.
     * @param page PDF page.
     * @param canvasWriter Canvas containing the PDF page.
     * */
    private void writePage(PdfDocument document, PdfDocument.Page page, CanvasWriter canvasWriter) {
        canvasWriter.draw();
        document.finishPage(page);
    }
}

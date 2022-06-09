package pt.uninova.s4h.citizenhub.report;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.persistence.repository.ReportRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class DailyReportGeneratorPDF {

    public static void generateWorkTimeReportPDF(Observer observerReportPDF, Resources res, ReportRepository reportRepository, LocalDate date, MeasurementKindLocalization measurementKindLocalization) {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 1100, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.setDensity(72);

        CanvasWriter canvasWriter = new CanvasWriter(canvas);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        Paint titlePaint = new Paint();

        titlePaint.setColor(Color.parseColor("#FFFFFF"));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        Paint darkTextPaint = new Paint();
        darkTextPaint.setColor(Color.parseColor("#000000"));
        darkTextPaint.setTextAlign(Paint.Align.LEFT);
        darkTextPaint.setTypeface(Typeface.DEFAULT);
        darkTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(Color.parseColor("#000000"));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.parseColor("#2789C2"));
        backgroundPaint.setAntiAlias(true);

        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(Color.parseColor("#34A1AD"));

        Paint rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(Color.parseColor("#34A1AD"));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        Observer<Report> observerReport = reportData -> {

            int x = 30;
            int y = 10;
            //String content = "MyTime Daily Report";
            canvas.drawRoundRect(x, y, 570, y + 50, 10, 10, backgroundPaint);
            canvasWriter.addText("MyTime Daily Report", 200, 38, titlePaint);

            canvas.drawRect(x + 20, y + 70, 550, y + 100, rectFillPaint);
            canvasWriter.addText("Results of: " + date.toString(), 150, 100, titlePaint);

            x += 20;
            y += 130;

            List<Group> groupsReportData = reportData.getGroups();
            for (Group group : groupsReportData) {
                int rectHeight = y - 10;
                System.out.println(group.getLabel().getLocalizedString());
                //canvas.drawRect(x-10, rectHeight - 10, 540, rectHeight + 15, rectFillPaint);
                canvas.drawRoundRect(x, rectHeight - 10, 550, rectHeight + 15, 12, 12, rectFillPaint);
                canvasWriter.addText(group.getLabel().getLocalizedString(), x + 20, y, darkTextPaint);
                y += 20;
                if (group.getLabel().getLocalizedString().equals(measurementKindLocalization.localize(Measurement.TYPE_BLOOD_PRESSURE)) ||
                        group.getLabel().getLocalizedString().equals(measurementKindLocalization.localize(Measurement.TYPE_LUMBAR_EXTENSION_TRAINING))) {
                    for (Group groupDailyMeasurements : group.getGroupList()) {
                        System.out.println(groupDailyMeasurements.getLabel().getLocalizedString());
                        canvasWriter.addText(groupDailyMeasurements.getLabel().getLocalizedString(), x + 50, y, darkTextPaint);
                        y += 20;
                        for (Item item : groupDailyMeasurements.getItemList()) {
                            System.out.println(item.getValue().getLocalizedString());
                            System.out.println(item.getLabel().getLocalizedString());
                            canvasWriter.addText(item.getLabel().getLocalizedString() + ":", x + 80, y, darkTextPaint);
                            canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 320, y, darkTextPaint);
                            //canvasWriter.addTextInFront(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), darkTextPaint);
                            y = y + 20;
                        }
                        y = y + 40;
                    }
                } else {
                    for (Item item : group.getItemList()) {
                        System.out.println(item.getValue().getLocalizedString());
                        System.out.println(item.getLabel().getLocalizedString());
                        canvasWriter.addText(item.getLabel().getLocalizedString(), x + 80, y, darkTextPaint);
                        canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 320, y, darkTextPaint);
                        //canvasWriter.addTextInFront(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), darkTextPaint);
                        y = y + 20;
                    }
                    y = y + 40;
                }
                RectF rectAround = new RectF(x, rectHeight - 10, 550, y - 50);
                canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
            }

            canvasWriter.draw();

            document.finishPage(page);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                document.writeTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] outByteArray = out.toByteArray();

            document.close();

            observerReportPDF.observe(outByteArray);

        };

        DailyReportGenerator.generateWorkTimeReport(reportRepository, date, measurementKindLocalization, observerReport);
    }

    public static void generateNotWorkTimeReportPDF(Observer observerReportPDF, Resources res, ReportRepository reportRepository, LocalDate date, MeasurementKindLocalization measurementKindLocalization) {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 1100, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.setDensity(72);

        CanvasWriter canvasWriter = new CanvasWriter(canvas);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        Paint titlePaint = new Paint();

        titlePaint.setColor(Color.parseColor("#FFFFFF"));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        Paint darkTextPaint = new Paint();
        darkTextPaint.setColor(Color.parseColor("#000000"));
        darkTextPaint.setTextAlign(Paint.Align.LEFT);
        darkTextPaint.setTypeface(Typeface.DEFAULT);
        darkTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(Color.parseColor("#000000"));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.parseColor("#2789C2"));
        backgroundPaint.setAntiAlias(true);

        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(Color.parseColor("#34A1AD"));

        Paint rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(Color.parseColor("#34A1AD"));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        Observer<Report> observerReport = reportData -> {

            int x = 30;
            int y = 10;
            //String content = "MyTime Daily Report";
            canvas.drawRoundRect(x, y, 570, y + 50, 10, 10, backgroundPaint);
            canvasWriter.addText("MyTime Daily Report", 200, 38, titlePaint);

            canvas.drawRect(x + 20, y + 70, 550, y + 100, rectFillPaint);
            canvasWriter.addText("Results of: " + date.toString(), 150, 100, titlePaint);

            x += 20;
            y += 130;

            List<Group> groupsReportData = reportData.getGroups();
            for (Group group : groupsReportData) {
                int rectHeight = y - 10;
                System.out.println(group.getLabel().getLocalizedString());
                //canvas.drawRect(x-10, rectHeight - 10, 540, rectHeight + 15, rectFillPaint);
                canvas.drawRoundRect(x, rectHeight - 10, 550, rectHeight + 15, 12, 12, rectFillPaint);
                canvasWriter.addText(group.getLabel().getLocalizedString(), x + 20, y, darkTextPaint);
                y += 20;
                if (group.getLabel().getLocalizedString().equals(measurementKindLocalization.localize(Measurement.TYPE_BLOOD_PRESSURE)) ||
                        group.getLabel().getLocalizedString().equals(measurementKindLocalization.localize(Measurement.TYPE_LUMBAR_EXTENSION_TRAINING))) {
                    for (Group groupDailyMeasurements : group.getGroupList()) {
                        System.out.println(groupDailyMeasurements.getLabel().getLocalizedString());
                        canvasWriter.addText(groupDailyMeasurements.getLabel().getLocalizedString(), x + 50, y, darkTextPaint);
                        y += 20;
                        for (Item item : groupDailyMeasurements.getItemList()) {
                            System.out.println(item.getValue().getLocalizedString());
                            System.out.println(item.getLabel().getLocalizedString());
                            canvasWriter.addText(item.getLabel().getLocalizedString() + ":", x + 80, y, darkTextPaint);
                            canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 320, y, darkTextPaint);
                            //canvasWriter.addTextInFront(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), darkTextPaint);
                            y = y + 20;
                        }
                        y = y + 40;
                    }
                } else {
                    for (Item item : group.getItemList()) {
                        System.out.println(item.getValue().getLocalizedString());
                        System.out.println(item.getLabel().getLocalizedString());
                        canvasWriter.addText(item.getLabel().getLocalizedString(), x + 80, y, darkTextPaint);
                        canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 320, y, darkTextPaint);
                        //canvasWriter.addTextInFront(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), darkTextPaint);
                        y = y + 20;
                    }
                    y = y + 40;
                }
                RectF rectAround = new RectF(x, rectHeight - 10, 550, y - 50);
                canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
            }

            canvasWriter.draw();

            document.finishPage(page);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                document.writeTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] outByteArray = out.toByteArray();

            document.close();

            observerReportPDF.observe(outByteArray);

        };

        DailyReportGenerator.generateNotWorkTimeReport(reportRepository, date, measurementKindLocalization, observerReport);
    }

    public static void getDrawableIcon() {

    }

    public static void generateCompleteReport(Observer observerReportPDF, Resources res, ReportRepository reportRepository, LocalDate date, MeasurementKindLocalization measurementKindLocalization) {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 1100, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.setDensity(72);

        CanvasWriter canvasWriter = new CanvasWriter(canvas);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        Paint titlePaint = new Paint();

        titlePaint.setColor(Color.parseColor("#FFFFFF"));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        Paint darkTextPaint = new Paint();
        darkTextPaint.setColor(Color.parseColor("#000000"));
        darkTextPaint.setTextAlign(Paint.Align.LEFT);
        darkTextPaint.setTypeface(Typeface.DEFAULT);
        darkTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(Color.parseColor("#000000"));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.parseColor("#2789C2"));
        backgroundPaint.setAntiAlias(true);

        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(Color.parseColor("#34A1AD"));

        Paint rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(Color.parseColor("#34A1AD"));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        Observer<Report> observerWorkTimeReport = workTimeData -> {

            Observer<Report> observerNotWorkTimeReport = notWorkTimeData -> {

                int x = 30;
                int y = 10;
                canvas.drawRoundRect(x, y, 570, y + 50, 10, 10, backgroundPaint);
                canvasWriter.addText("Complete Daily Report", 200, 38, titlePaint);

                x += 20;

                canvas.drawRect(x, y + 70, 550, y + 100, rectFillPaint);
                canvasWriter.addText("Results of: " + date.toString(), 150, 100, titlePaint);

                /*canvas.drawRect(x + 20, y + 120, 250, y + 150, rectFillPaint);
                canvasWriter.addText("Not Work Time Results ", 150, 100, titlePaint);

                canvas.drawRect(x + 230, y + 120, 550, y + 150, rectFillPaint);
                canvasWriter.addText("Work Time Results ", 150, 100, titlePaint);*/

                y += 170;

                List<Group> groupsWorkTimeData = workTimeData.getGroups();
                List<Group> groupsNotWorkTimeData = notWorkTimeData.getGroups();

                for (Group groupNotWorkTime : groupsNotWorkTimeData) {

                    int rectHeight = y - 10;
                    String notWorkTimeLabel = groupNotWorkTime.getLabel().getLocalizedString();

                    canvas.drawRoundRect(x, rectHeight - 10, 550, rectHeight + 15, 12, 12, rectFillPaint);
                    canvasWriter.addText(notWorkTimeLabel, x + 20, y, darkTextPaint);

                    y += 20;

                    if (notWorkTimeLabel.equals(measurementKindLocalization.localize(Measurement.TYPE_BLOOD_PRESSURE)) ||
                            notWorkTimeLabel.equals(measurementKindLocalization.localize(Measurement.TYPE_LUMBAR_EXTENSION_TRAINING))) {
                        for (Group group : groupNotWorkTime.getGroupList()) {
                            canvasWriter.addText(group.getLabel().getLocalizedString(), x + 25, y, darkTextPaint);
                            //O icon é só para diferenciar, não é para ficar
                            Drawable stepsTaken = res.getDrawable(R.drawable.ic_heartbeat_item, null);
                            stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
                            canvas.save();
                            canvas.translate(x + 300, y + 15);
                            canvas.scale(0.35f, 0.35f);
                            stepsTaken.draw(canvas);
                            canvas.restore();
                            y += 20;
                            for (Item item : group.getItemList()) {
                                canvasWriter.addText(item.getLabel().getLocalizedString(), x + 40, y, darkTextPaint);
                                canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 200, y, darkTextPaint);
                                y += 20;
                            }
                            y += 40;
                        }
                        for (Group groupWorkTime : groupsWorkTimeData) {
                            String workTimeLabel = groupWorkTime.getLabel().getLocalizedString();
                            if (workTimeLabel.equals(measurementKindLocalization.localize(Measurement.TYPE_BLOOD_PRESSURE)) ||
                                    workTimeLabel.equals(measurementKindLocalization.localize(Measurement.TYPE_LUMBAR_EXTENSION_TRAINING))) {
                                for (Group group : groupWorkTime.getGroupList()) {
                                    canvasWriter.addText(group.getLabel().getLocalizedString(), x + 25, y, darkTextPaint);
                                    //O icon é só para diferenciar, não é para ficar
                                    Drawable stepsTaken = res.getDrawable(R.drawable.card_activity, null);
                                    stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
                                    canvas.save();
                                    canvas.translate(x + 300, y + 15);
                                    canvas.scale(0.35f, 0.35f);
                                    stepsTaken.draw(canvas);
                                    canvas.restore();
                                    y += 20;
                                    for (Item item : group.getItemList()) {
                                        canvasWriter.addText(item.getLabel().getLocalizedString(), x + 40, y, darkTextPaint);
                                        canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 200, y, darkTextPaint);
                                        y = y + 20;
                                    }
                                    y = y + 40;
                                }
                            }
                        }
                    } else {
                        Boolean hasItem = false;
                        Boolean itemFound;
                        for (Group groupWorkTime : groupsWorkTimeData) {
                            itemFound = false;
                            if (groupNotWorkTime.getLabel().getLocalizedString().equals(groupWorkTime.getLabel().getLocalizedString())) {
                                for (Item itemNotWorkTime : groupNotWorkTime.getItemList()) {
                                    for (Item itemWorkTime : groupWorkTime.getItemList()) {
                                        if (itemNotWorkTime.getLabel().getLocalizedString().equals(itemWorkTime.getLabel().getLocalizedString())) {
                                            canvasWriter.addText(itemNotWorkTime.getLabel().getLocalizedString(), x + 40, y, darkTextPaint);
                                            canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(itemNotWorkTime.getValue().getLocalizedString())), x + 160, y, darkTextPaint);
                                            canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(itemWorkTime.getValue().getLocalizedString())), x + 240, y, darkTextPaint);
                                            y += 20;
                                            itemFound = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (itemFound) {
                                hasItem = true;
                                break;
                            }
                        }
                        if (!hasItem) {
                            for (Item item : groupNotWorkTime.getItemList()) {
                                canvasWriter.addText(item.getLabel().getLocalizedString(), x + 40, y, darkTextPaint);
                                canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 160, y, darkTextPaint);
                                canvasWriter.addText(" 0", x + 240, y, darkTextPaint);
                                y = y + 20;
                            }

                        }
                        y = y + 40;
                    }
                    RectF rectAround = new RectF(x, rectHeight - 10, 550, y - 50);
                    canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
                }

                for (Group groupWorkTime : groupsWorkTimeData) {

                    int rectHeight = y - 10;
                    Boolean hasGroup = false;
                    String workTimeLabel = groupWorkTime.getLabel().getLocalizedString();

                    for (Group groupNotWorkTime : groupsNotWorkTimeData) {
                        if (workTimeLabel.equals(groupNotWorkTime.getLabel().getLocalizedString())) {
                            hasGroup = true;
                        }
                    }
                    if (!hasGroup) {
                        if (workTimeLabel.equals(measurementKindLocalization.localize(Measurement.TYPE_BLOOD_PRESSURE)) ||
                                workTimeLabel.equals(measurementKindLocalization.localize(Measurement.TYPE_LUMBAR_EXTENSION_TRAINING))) {
                            canvas.drawRoundRect(x, rectHeight - 10, 550, rectHeight + 15, 12, 12, rectFillPaint);
                            canvasWriter.addText(workTimeLabel, x + 20, y, darkTextPaint);
                            y += 20;
                            for (Group group : groupWorkTime.getGroupList()) {
                                canvasWriter.addText(group.getLabel().getLocalizedString(), x + 25, y, darkTextPaint);
                                //O icon é só para diferenciar, não é para ficar
                                Drawable stepsTaken = res.getDrawable(R.drawable.card_activity, null);
                                stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
                                canvas.save();
                                canvas.translate(x + 300, y + 15);
                                canvas.scale(0.35f, 0.35f);
                                stepsTaken.draw(canvas);
                                canvas.restore();
                                y += 20;
                                for (Item item : group.getItemList()) {
                                    canvasWriter.addText(item.getLabel().getLocalizedString() + ":", x + 40, y, darkTextPaint);
                                    canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 200, y, darkTextPaint);
                                    y = y + 20;
                                }
                                y = y + 40;
                            }
                            RectF rectAround = new RectF(x, rectHeight - 10, 550, y - 50);
                            canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
                        } else {
                            canvas.drawRoundRect(x, rectHeight - 10, 550, rectHeight + 15, 12, 12, rectFillPaint);
                            canvasWriter.addText(workTimeLabel, x + 20, y, darkTextPaint);
                            y += 20;
                            for (Item item : groupWorkTime.getItemList()) {
                                System.out.println(item.getValue().getLocalizedString());
                                System.out.println(item.getLabel().getLocalizedString());
                                canvasWriter.addText(item.getLabel().getLocalizedString(), x + 80, y, darkTextPaint);
                                canvasWriter.addText(" " + decimalFormat.format(Double.valueOf(item.getValue().getLocalizedString())), x + 240, y, darkTextPaint);
                                canvasWriter.addText(" 0", x + 160, y, darkTextPaint);
                                y = y + 20;
                            }
                            y = y + 40;
                            RectF rectAround = new RectF(x, rectHeight - 10, 550, y - 50);
                            canvas.drawRoundRect(rectAround, 12, 12, rectPaint);
                        }
                    }
                }

                canvasWriter.draw();

                document.finishPage(page);

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                try {
                    document.writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] outByteArray = out.toByteArray();

                document.close();

                observerReportPDF.observe(outByteArray);

            };

            DailyReportGenerator.generateNotWorkTimeReport(reportRepository, date, measurementKindLocalization, observerNotWorkTimeReport);
        };

        DailyReportGenerator.generateWorkTimeReport(reportRepository, date, measurementKindLocalization, observerWorkTimeReport);

    }

}
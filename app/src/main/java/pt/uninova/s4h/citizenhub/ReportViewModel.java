package pt.uninova.s4h.citizenhub;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import care.data4life.fhir.r4.model.Attachment;
import care.data4life.fhir.r4.model.CodeSystemDocumentReferenceStatus;
import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.Coding;
import care.data4life.fhir.r4.model.DocumentReference;
import care.data4life.fhir.r4.model.FhirDate;
import care.data4life.fhir.r4.model.FhirDateTime;
import care.data4life.fhir.r4.model.FhirInstant;
import care.data4life.fhir.r4.model.FhirTime;
import care.data4life.fhir.r4.model.Organization;
import care.data4life.fhir.r4.model.Practitioner;
import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.SdkContract.Fhir4RecordClient;
import care.data4life.sdk.call.Callback;
import care.data4life.sdk.call.Fhir4Record;
import care.data4life.sdk.helpers.r4.AttachmentBuilder;
import care.data4life.sdk.helpers.r4.DocumentReferenceBuilder;
import care.data4life.sdk.helpers.r4.OrganizationBuilder;
import care.data4life.sdk.helpers.r4.PractitionerBuilder;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.persistence.entity.LumbarExtensionTrainingMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.repository.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.persistence.entity.util.MeasurementAggregate;
import pt.uninova.s4h.citizenhub.report.CanvasWriter;
import pt.uninova.s4h.citizenhub.util.Pair;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;


public class ReportViewModel extends AndroidViewModel {

    final private LumbarExtensionTrainingRepository lumbarTrainingRepository;

    final private MutableLiveData<Set<LocalDate>> availableReportsLive;
    final private Set<Pair<Integer, Integer>> peekedMonths;

    private LocalDate detailDate;
    private Map<Integer, MeasurementAggregate> detailAggregates;
    private Map<Integer, MeasurementAggregate> detailAggregatesWorkTime;
    private LumbarExtensionTrainingMeasurementRecord lumbarTraining;

    public ReportViewModel(Application application) {
        super(application);

        lumbarTrainingRepository = new LumbarExtensionTrainingRepository(application);
        availableReportsLive = new MutableLiveData<>(new HashSet<>());
        peekedMonths = new HashSet<>();

        detailDate = LocalDate.now();

        peek();
    }

    static Practitioner getFakePractitioner() {
        return PractitionerBuilder.buildWith(
                "Bruce",
                "Banner",
                "Dr.",
                "MD",
                "Walvisbaai 3",
                "2333ZA",
                "Den helder",
                "+31715269111",
                "www.webpage.com");
    }

    static CodeableConcept getFakePracticeSpeciality() {
        Coding coding = new Coding();
        coding.code = "General Medicine";
        coding.display = "General Medicine";
        coding.system = "http://www.ihe.net/xds/connectathon/practiceSettingCodes";

        CodeableConcept concept = new CodeableConcept();
        concept.coding = Arrays.asList(coding);
        return concept;
    }

    static CodeableConcept getFakeDocumentReferenceType() {
        Coding coding = new Coding();
        coding.code = "34108-1";
        coding.display = "Outpatient Note";
        coding.system = "http://loinc.org";

        CodeableConcept concept = new CodeableConcept();
        concept.coding = Arrays.asList(coding);
        return concept;
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

    public byte[] createWorkTimePdf() throws IOException {
        PdfDocument document = new PdfDocument();
        Resources res = getApplication().getResources();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.setDensity(72);

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        Paint titlePaint = new Paint();

        titlePaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HWhite));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        Paint textPaint = new Paint();

        textPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HGreyLight));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(12);

        Paint darkTextPaint = new Paint();
        darkTextPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        darkTextPaint.setTextAlign(Paint.Align.LEFT);
        darkTextPaint.setTypeface(Typeface.DEFAULT);
        darkTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);


        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HDarkBlue));
        backgroundPaint.setAntiAlias(true);

        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HTurquoise));

        Paint rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HTurquoise));

        Paint ecInfoPaint = new Paint();
        ecInfoPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        ecInfoPaint.setTextAlign(Paint.Align.LEFT);
        ecInfoPaint.setTypeface(Typeface.DEFAULT);
        ecInfoPaint.setTextSize(8);

        Paint poweredByPaint = new Paint();

        poweredByPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        poweredByPaint.setTextAlign(Paint.Align.LEFT);
        poweredByPaint.setTypeface(Typeface.DEFAULT_BOLD);
        poweredByPaint.setTextSize(10);

        int y = 50;
        int x = 50;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        Bitmap logo = BitmapFactory.decodeResource(res, R.drawable.img_citizen_hub_logo_text, options);


        canvas.save();
        canvas.translate(x + 175, 2);
        canvas.scale(0.85f, 0.85f);
        canvas.drawBitmap(logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        Bitmap ec_logo = BitmapFactory.decodeResource(res, R.drawable.img_ec_logo_png, options);
        canvas.save();
        canvas.translate(10, 790);
        canvas.scale(0.04f, 0.04f);
        canvas.drawBitmap(ec_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        CanvasWriter canvasWriter = new CanvasWriter(canvas);
        canvasWriter.addText("This work has received funding from the European Union's Horizon 2020 research and", x + 25, 808, ecInfoPaint);
        canvasWriter.addNewLine("innovation programme under Grant agreement No 826117", 13);
        canvasWriter.addText("powered by", x + 365, 813, poweredByPaint);

        Bitmap smart4Health_logo = BitmapFactory.decodeResource(res, R.drawable.img_s4h_logo_transparent_png, options);
        canvas.save();
        canvas.translate(475, 801);
        canvas.scale(0.35f, 0.35f);
        canvas.drawBitmap(smart4Health_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        x += 60;
        y += 120;

        String content = "MyWork Daily Report";
        RectF rect = new RectF(x - 30, y - 50, x + 430, y + 15);
        canvas.drawRoundRect(rect, 10, 10, backgroundPaint);
        canvasWriter.addText("Results of: " + detailDate.toString(), x + 120, y + 30, titlePaint);
        canvasWriter.addText(content, x + 140, y - 15, titlePaint);
        canvas.drawRect(80, 175, 540, 210, rectFillPaint);


        Drawable icon = res.getDrawable(R.drawable.ic_daily);
        icon.setBounds(x - 10, y - 40, x + 40, y);
        icon.draw(canvas);

        y += 50;
        x += 20;


        MeasurementAggregate measurementAggregate = detailAggregatesWorkTime.get(Measurement.TYPE_POSTURE_CORRECT);
        MeasurementAggregate measurementAggregate1 = detailAggregatesWorkTime.get(Measurement.TYPE_POSTURE_INCORRECT);
        if (measurementAggregate != null) {
            Drawable timeSitting = res.getDrawable(R.drawable.ic_time_sitting, null);
            timeSitting.setBounds(0, 0, timeSitting.getIntrinsicWidth(), timeSitting.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            timeSitting.draw(canvas);
            canvas.restore();

            y += 40;

            canvasWriter.addText("Good Posture: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + secondsToString(measurementAggregate.getSum().intValue()), boldTextPaint);

            y += 20;

            if (measurementAggregate1 != null) {
                canvasWriter.addText("Bad Posture: ", x + 70, y, darkTextPaint);
                canvasWriter.addTextInFront(" " + secondsToString(measurementAggregate1.getSum().intValue()), boldTextPaint);
                y += 20;
            }
            y += 40;
        }

        measurementAggregate = detailAggregatesWorkTime.get(Measurement.TYPE_DISTANCE_SNAPSHOT);
        MeasurementAggregate measurementAggregate2 = detailAggregatesWorkTime.get(Measurement.TYPE_STEPS_SNAPSHOT);
        MeasurementAggregate measurementAggregate3 = detailAggregatesWorkTime.get(Measurement.TYPE_CALORIES_SNAPSHOT);

        if (measurementAggregate != null && measurementAggregate2 != null && measurementAggregate3 != null) {
            Drawable stepsTaken = res.getDrawable(R.drawable.card_activity, null);
            stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            stepsTaken.draw(canvas);
            canvas.restore();

            y += 20;
            canvasWriter.addText("Steps: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);

            y += 20;
            canvasWriter.addText("Distance: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);
            canvasWriter.addTextInFront(" m", darkTextPaint);

            y += 20;
            canvasWriter.addText("Calories:", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);
            canvasWriter.addTextInFront(" kcal", darkTextPaint);
            y += 20;

            y += 40;
        }

        measurementAggregate = detailAggregatesWorkTime.get(Measurement.TYPE_HEART_RATE);

        if (measurementAggregate != null) {

            y -= 10;

            Drawable heartBeat = res.getDrawable(R.drawable.ic_heartbeat_item, null);
            heartBeat.setBounds(0, 0, heartBeat.getIntrinsicWidth(), heartBeat.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            heartBeat.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Average heart rate", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;
            canvasWriter.addText("Minimum heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate.getMin()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;
            canvasWriter.addText("Maximum heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate.getMax()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;

            y += 20;
        }

        measurementAggregate = detailAggregatesWorkTime.get(Measurement.TYPE_CALORIES_SNAPSHOT);

        if (lumbarTraining != null) {

            Drawable lumbar = res.getDrawable(R.drawable.ic_heartbeat_item, null);
            lumbar.setBounds(0, 0, lumbar.getIntrinsicWidth(), lumbar.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            lumbar.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Training duration:", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(lumbarTraining.getDuration()), boldTextPaint);
            canvasWriter.addTextInFront("s", darkTextPaint);

            y += 20;
            canvasWriter.addText("Training score: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getScore()), boldTextPaint);
            canvasWriter.addTextInFront("%", darkTextPaint);

            y += 20;
            canvasWriter.addText("Number of Repetitions: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getRepetitions()), boldTextPaint);
            canvasWriter.addTextInFront(" reps", darkTextPaint);

            y += 20;
            canvasWriter.addText("Training weight: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getWeight()), boldTextPaint);
            canvasWriter.addTextInFront(" kg", darkTextPaint);

            y += 20;

            y += 40;

        }

        RectF rectAround = new RectF(81, 205, 539, y);
        canvas.drawRoundRect(rectAround, 12, 12, rectPaint);


        canvasWriter.draw();

        document.finishPage(page);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            document.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        document.close();

        return out.toByteArray();
    }

    public byte[] createPdf() throws IOException {
        PdfDocument document = new PdfDocument();
        Resources res = getApplication().getResources();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 1100, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.setDensity(72);

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        Paint titlePaint = new Paint();

        titlePaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HWhite));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        Paint textPaint = new Paint();

        textPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HGreyLight));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(12);

        Paint darkTextPaint = new Paint();
        darkTextPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        darkTextPaint.setTextAlign(Paint.Align.LEFT);
        darkTextPaint.setTypeface(Typeface.DEFAULT);
        darkTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);


        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HDarkBlue));
        backgroundPaint.setAntiAlias(true);

        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HTurquoise));

        Paint rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HTurquoise));

        Paint ecInfoPaint = new Paint();
        ecInfoPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        ecInfoPaint.setTextAlign(Paint.Align.LEFT);
        ecInfoPaint.setTypeface(Typeface.DEFAULT);
        ecInfoPaint.setTextSize(8);

        Paint poweredByPaint = new Paint();

        poweredByPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        poweredByPaint.setTextAlign(Paint.Align.LEFT);
        poweredByPaint.setTypeface(Typeface.DEFAULT_BOLD);
        poweredByPaint.setTextSize(10);

        int y = 50;
        int x = 50;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        Bitmap logo = BitmapFactory.decodeResource(res, R.drawable.img_citizen_hub_logo_text, options);


        canvas.save();
        canvas.translate(x + 175, 2);
        canvas.scale(0.85f, 0.85f);
        canvas.drawBitmap(logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        Bitmap ec_logo = BitmapFactory.decodeResource(res, R.drawable.img_ec_logo_png, options);
        canvas.save();
        canvas.translate(10, 1048);
        canvas.scale(0.04f, 0.04f);
        canvas.drawBitmap(ec_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        CanvasWriter canvasWriter = new CanvasWriter(canvas);
        canvasWriter.addText("This work has received funding from the European Union's Horizon 2020 research and", x + 25, 1066, ecInfoPaint);
        canvasWriter.addNewLine("innovation programme under Grant agreement No 826117", 13);
        canvasWriter.addText("powered by", x + 365, 1071, poweredByPaint);

        Bitmap smart4Health_logo = BitmapFactory.decodeResource(res, R.drawable.img_s4h_logo_transparent_png, options);
        canvas.save();
        canvas.translate(475, 1059);
        canvas.scale(0.35f, 0.35f);
        canvas.drawBitmap(smart4Health_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        x += 60;
        y += 120;

        String content = "MyTime Daily Report";
        RectF rect = new RectF(x - 30, y - 50, x + 430, y + 15);
        canvas.drawRoundRect(rect, 10, 10, backgroundPaint);
        canvasWriter.addText("Results of: " + detailDate.toString(), x + 120, y + 30, titlePaint);
        canvasWriter.addText(content, x + 140, y - 15, titlePaint);
        canvas.drawRect(80, 175, 540, 210, rectFillPaint);


        Drawable icon = res.getDrawable(R.drawable.ic_daily);
        icon.setBounds(x - 10, y - 40, x + 40, y);
        icon.draw(canvas);

        y += 50;
        x += 20;


        MeasurementAggregate measurementAggregate = detailAggregates.get(Measurement.TYPE_POSTURE_CORRECT);
        MeasurementAggregate measurementAggregate1 = detailAggregates.get(Measurement.TYPE_POSTURE_INCORRECT);
        if (measurementAggregate != null) {
            Drawable timeSitting = res.getDrawable(R.drawable.ic_time_sitting, null);
            timeSitting.setBounds(0, 0, timeSitting.getIntrinsicWidth(), timeSitting.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            timeSitting.draw(canvas);
            canvas.restore();

            y += 40;

            canvasWriter.addText("Good Posture: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + secondsToString(measurementAggregate.getSum().intValue()), boldTextPaint);

            y += 20;

            if (measurementAggregate1 != null) {
                canvasWriter.addText("Bad Posture: ", x + 70, y, darkTextPaint);
                canvasWriter.addTextInFront(" " + secondsToString(measurementAggregate1.getSum().intValue()), boldTextPaint);
                y += 20;
            }
            y += 40;
        }

        measurementAggregate = detailAggregates.get(Measurement.TYPE_DISTANCE_SNAPSHOT);
        MeasurementAggregate measurementAggregate2 = detailAggregates.get(Measurement.TYPE_STEPS_SNAPSHOT);
        MeasurementAggregate measurementAggregate3 = detailAggregates.get(Measurement.TYPE_CALORIES_SNAPSHOT);

        if (measurementAggregate != null && measurementAggregate2 != null && measurementAggregate3 != null) {
            Drawable stepsTaken = res.getDrawable(R.drawable.card_activity, null);
            stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            stepsTaken.draw(canvas);
            canvas.restore();

            y += 20;
            canvasWriter.addText("Steps: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);

            y += 20;
            canvasWriter.addText("Distance: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);
            canvasWriter.addTextInFront(" m", darkTextPaint);

            y += 20;
            canvasWriter.addText("Calories:", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);
            canvasWriter.addTextInFront(" kcal", darkTextPaint);
            y += 20;

            y += 40;
        }

        measurementAggregate = detailAggregates.get(Measurement.TYPE_HEART_RATE);

        if (measurementAggregate != null) {

            y -= 10;

            Drawable heartBeat = res.getDrawable(R.drawable.ic_heartbeat_item, null);
            heartBeat.setBounds(0, 0, heartBeat.getIntrinsicWidth(), heartBeat.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            heartBeat.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Average heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;
            canvasWriter.addText("Minimum heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate.getMin()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;
            canvasWriter.addText("Maximum heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate.getMax()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;

            y += 20;
        }

        measurementAggregate = detailAggregates.get(Measurement.TYPE_RESPIRATION_RATE);

        if (measurementAggregate != null) {
            Drawable respiration_rate = res.getDrawable(R.drawable.ic_lungs, null);
            respiration_rate.setBounds(0, 0, respiration_rate.getIntrinsicWidth(), respiration_rate.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            respiration_rate.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Breathing rate: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" rpm", darkTextPaint);

            y += 20;

            y += 40;
        }


        if (lumbarTraining != null) {

            Drawable lumbar = res.getDrawable(R.drawable.ic_medx_program, null);
            lumbar.setBounds(0, 0, lumbar.getIntrinsicWidth(), lumbar.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            lumbar.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Training duration:", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(lumbarTraining.getDuration()), boldTextPaint);
            canvasWriter.addTextInFront("s", darkTextPaint);

            y += 20;
            canvasWriter.addText("Training score: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getScore()), boldTextPaint);
            canvasWriter.addTextInFront("%", darkTextPaint);

            y += 20;
            canvasWriter.addText("Number of Repetitions: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getRepetitions()), boldTextPaint);
            canvasWriter.addTextInFront(" reps", darkTextPaint);

            y += 20;
            canvasWriter.addText("Training weight: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getWeight()), boldTextPaint);
            canvasWriter.addTextInFront(" kg", darkTextPaint);

            y += 20;

            y += 40;

        }

        RectF rectAround = new RectF(81, 205, 539, y);
        canvas.drawRoundRect(rectAround, 12, 12, rectPaint);


        canvasWriter.draw();

        document.finishPage(page);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            document.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        document.close();

        return out.toByteArray();
    }

    public byte[] doPdf(String[] info) throws IOException {
        PdfDocument document = new PdfDocument();
        Resources res = getApplication().getResources();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 1100, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.setDensity(72);

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        Paint titlePaint = new Paint();

        titlePaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HWhite));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(18);

        Paint textPaint = new Paint();

        textPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HGreyLight));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(12);

        Paint darkTextPaint = new Paint();
        darkTextPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        darkTextPaint.setTextAlign(Paint.Align.LEFT);
        darkTextPaint.setTypeface(Typeface.DEFAULT);
        darkTextPaint.setTextSize(12);

        Paint boldTextPaint = new Paint();
        boldTextPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boldTextPaint.setTextSize(12);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HDarkBlue));
        backgroundPaint.setAntiAlias(true);

        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
        rectPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HTurquoise));

        Paint rectFillPaint = new Paint();
        rectFillPaint.setStyle(Paint.Style.FILL);
        rectFillPaint.setStrokeWidth(2);
        rectFillPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HTurquoise));

        Paint ecInfoPaint = new Paint();
        ecInfoPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        ecInfoPaint.setTextAlign(Paint.Align.LEFT);
        ecInfoPaint.setTypeface(Typeface.DEFAULT);
        ecInfoPaint.setTextSize(8);

        Paint poweredByPaint = new Paint();

        poweredByPaint.setColor(ContextCompat.getColor(getApplication(), R.color.colorS4HBlack));
        poweredByPaint.setTextAlign(Paint.Align.LEFT);
        poweredByPaint.setTypeface(Typeface.DEFAULT_BOLD);
        poweredByPaint.setTextSize(10);

        int y = 50;
        int x = 50;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDensity = 72;

        Bitmap logo = BitmapFactory.decodeResource(res, R.drawable.img_citizen_hub_logo_text, options);


        canvas.save();
        canvas.translate(x + 175, 2);
        canvas.scale(0.85f, 0.85f);
        canvas.drawBitmap(logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        Bitmap ec_logo = BitmapFactory.decodeResource(res, R.drawable.img_ec_logo_png, options);
        canvas.save();
        canvas.translate(10, 1048);
        canvas.scale(0.04f, 0.04f);
        canvas.drawBitmap(ec_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        CanvasWriter canvasWriter = new CanvasWriter(canvas);
        canvasWriter.addText("This work has received funding from the European Union's Horizon 2020 research and", x + 25, 1066, ecInfoPaint);
        canvasWriter.addNewLine("innovation programme under Grant agreement No 826117", 13);
        canvasWriter.addText("powered by", x + 365, 1071, poweredByPaint);

        Bitmap smart4Health_logo = BitmapFactory.decodeResource(res, R.drawable.img_s4h_logo_transparent_png, options);
        canvas.save();
        canvas.translate(475, 1059);
        canvas.scale(0.35f, 0.35f);
        canvas.drawBitmap(smart4Health_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        x += 60;
        y += 120;

        String content = "MyTime Daily Report";
        RectF rect = new RectF(x - 30, y - 50, x + 430, y + 15);
        canvas.drawRoundRect(rect, 10, 10, backgroundPaint);
        canvasWriter.addText("Results of: " + detailDate.toString(), x + 120, y + 30, titlePaint);
        canvasWriter.addText(content, x + 140, y - 15, titlePaint);
        canvas.drawRect(80, 175, 540, 210, rectFillPaint);


        Drawable icon = res.getDrawable(R.drawable.ic_daily);
        icon.setBounds(x - 10, y - 40, x + 40, y);
        icon.draw(canvas);

        y += 50;
        x += 20;

        for(int i = 0; i < info.length; i++){

        }

        MeasurementAggregate measurementAggregate = detailAggregates.get(MeasurementKind.POSTURE_CORRECT);
        MeasurementAggregate measurementAggregate1 = detailAggregates.get(MeasurementKind.POSTURE_INCORRECT);
        if (measurementAggregate != null) {
            Drawable timeSitting = res.getDrawable(R.drawable.ic_time_sitting, null);
            timeSitting.setBounds(0, 0, timeSitting.getIntrinsicWidth(), timeSitting.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            timeSitting.draw(canvas);
            canvas.restore();

            y += 40;

            canvasWriter.addText("Good Posture: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + secondsToString(measurementAggregate.getSum().intValue()), boldTextPaint);

            y += 20;

            if (measurementAggregate1 != null) {
                canvasWriter.addText("Bad Posture: ", x + 70, y, darkTextPaint);
                canvasWriter.addTextInFront(" " + secondsToString(measurementAggregate1.getSum().intValue()), boldTextPaint);
                y += 20;
            }
            y += 40;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.DISTANCE);
        MeasurementAggregate measurementAggregate2 = detailAggregates.get(MeasurementKind.STEPS);
        MeasurementAggregate measurementAggregate3 = detailAggregates.get(MeasurementKind.CALORIES);

        if (measurementAggregate != null && measurementAggregate2 != null && measurementAggregate3 != null) {
            Drawable stepsTaken = res.getDrawable(R.drawable.card_activity, null);
            stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            stepsTaken.draw(canvas);
            canvas.restore();

            y += 20;
            canvasWriter.addText("Steps: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);

            y += 20;
            canvasWriter.addText("Distance: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);
            canvasWriter.addTextInFront(" m", darkTextPaint);

            y += 20;
            canvasWriter.addText("Calories:", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getSum()), boldTextPaint);
            canvasWriter.addTextInFront(" kcal", darkTextPaint);
            y += 20;

            y += 40;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.HEART_RATE);

        if (measurementAggregate != null) {

            y -= 10;

            Drawable heartBeat = res.getDrawable(R.drawable.ic_heartbeat_item, null);
            heartBeat.setBounds(0, 0, heartBeat.getIntrinsicWidth(), heartBeat.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            heartBeat.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Average heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;
            canvasWriter.addText("Minimum heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate.getMin()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;
            canvasWriter.addText("Maximum heart rate: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate.getMax()), boldTextPaint);
            canvasWriter.addTextInFront(" bpm", darkTextPaint);

            y += 20;

            y += 20;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.RESPIRATION_RATE);

        if (measurementAggregate != null) {
            Drawable respiration_rate = res.getDrawable(R.drawable.ic_lungs, null);
            respiration_rate.setBounds(0, 0, respiration_rate.getIntrinsicWidth(), respiration_rate.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            respiration_rate.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Breathing rate: ", x + 70, y + 10, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" rpm", darkTextPaint);

            y += 20;

            y += 40;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.BLOOD_PRESSURE_SYSTOLIC);
        MeasurementAggregate measurementAggregate2work = detailAggregates.get(MeasurementKind.BLOOD_PRESSURE_DIASTOLIC);
        MeasurementAggregate measurementAggregate3work = detailAggregates.get(MeasurementKind.BLOOD_PRESSURE_MEAN_ARTERIAL_PRESSURE);
        if (measurementAggregate != null && measurementAggregate2work != null && measurementAggregate3work != null) {

            y -= 10;

            Drawable bloodPressure = res.getDrawable(R.drawable.ic_blood_pressure, null);
            bloodPressure.setBounds(0, 0, bloodPressure.getIntrinsicWidth(), bloodPressure.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            bloodPressure.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Average Systolic Blood Pressure:", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(measurementAggregate.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" mmHg", darkTextPaint);

            y += 20;
            canvasWriter.addText("Average Diastolic Blood Pressure: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate2work.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" mmHg", darkTextPaint);

            y += 20;
            canvasWriter.addText("Mean Arterial Pressure: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(measurementAggregate3work.getAverage()), boldTextPaint);
            canvasWriter.addTextInFront(" mmHg", darkTextPaint);

            y += 20;

            y += 40;
        }

        if (lumbarTraining != null) {

            Drawable lumbar = res.getDrawable(R.drawable.ic_medx_program, null);
            lumbar.setBounds(0, 0, lumbar.getIntrinsicWidth(), lumbar.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            lumbar.draw(canvas);
            canvas.restore();

            y += 40;
            canvasWriter.addText("Training duration:", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(" " + decimalFormat.format(lumbarTraining.getDuration()), boldTextPaint);
            canvasWriter.addTextInFront("s", darkTextPaint);

            y += 20;
            canvasWriter.addText("Training score: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getScore()), boldTextPaint);
            canvasWriter.addTextInFront("%", darkTextPaint);

            y += 20;
            canvasWriter.addText("Number of Repetitions: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getRepetitions()), boldTextPaint);
            canvasWriter.addTextInFront(" reps", darkTextPaint);

            y += 20;
            canvasWriter.addText("Training weight: ", x + 70, y, darkTextPaint);
            canvasWriter.addTextInFront(String.valueOf(lumbarTraining.getWeight()), boldTextPaint);
            canvasWriter.addTextInFront(" kg", darkTextPaint);

            y += 20;

            y += 40;

        }

        RectF rectAround = new RectF(81, 205, 539, y);
        canvas.drawRoundRect(rectAround, 12, 12, rectPaint);


        canvasWriter.draw();

        document.finishPage(page);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            document.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        document.close();

        return out.toByteArray();
    }

    public LiveData<Set<LocalDate>> getAvailableReportDates() {
        return availableReportsLive;
    }

    public LocalDate getDetailDate() {
        return detailDate;
    }

    public void setDetailDate(LocalDate detailDate) {
        this.detailDate = detailDate;
    }

    public void obtainSummary(Observer<Map<Integer, MeasurementAggregate>> observer) {
    }

    public void obtainWorkTimeSummary(Observer<Map<Integer, MeasurementAggregate>> observer) {
    }

    private void onDatesChanged(List<LocalDate> dates) {
        if (dates.size() > 0) {
            Set<LocalDate> localDates = availableReportsLive.getValue();

            localDates.addAll(dates);

            availableReportsLive.postValue(localDates);
        }
    }

    public void peek() {
        final LocalDate now = LocalDate.now();

        peek(now.getYear(), now.getMonthValue());
    }

    public void peek(int year, int month) {
        Pair<Integer, Integer> peek = new Pair<>(year, month);

        if (!peekedMonths.contains(peek)) {
            peekedMonths.add(peek);
            //repository.obtainDates(peek, this::onDatesChanged);
//            lumbarTrainingRepository.obtainDates(peek,this::onDatesChanged);
        }
    }

    public void sendDetailWorkTime(Callback<Fhir4Record<DocumentReference>> callback) throws IOException {
        Fhir4RecordClient client = Data4LifeClient.getInstance().getFhir4();
        final LocalDateTime now = LocalDateTime.now();
        List<Attachment> attachments = new ArrayList<>(1);

        byte[] data = createWorkTimePdf();
        Attachment attach = null;

        try {
            attach = AttachmentBuilder.buildWith(now.toString(),
                    new FhirDateTime(new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth()),
                            new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), null, null),
                            TimeZone.getDefault()),
                    "application/pdf",
                    data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        attachments.add(attach);

        //TODO this comment is temporary, do not apply this change
        /**/
        DocumentReference documentReference = DocumentReferenceBuilder.buildWith(
                "Citizen Hub Daily during Work Report " + detailDate.toString(),
                CodeSystemDocumentReferenceStatus.CURRENT,
                attachments,
                getFakeDocumentReferenceType(),
                getOrganization(),
                getFakePracticeSpeciality()
        );

        documentReference.date = new FhirInstant(new FhirDateTime(new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth()), new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), 0, 0), TimeZone.getDefault()));

        client.create(documentReference, new ArrayList<>(), callback);
    }


    public void sendDetail(Callback<Fhir4Record<DocumentReference>> callback) throws IOException {
        Fhir4RecordClient client = Data4LifeClient.getInstance().getFhir4();
        final LocalDateTime now = LocalDateTime.now();
        List<Attachment> attachments = new ArrayList<>(1);

        byte[] data = createPdf();
        Attachment attach = null;

        try {
            attach = AttachmentBuilder.buildWith(now.toString(),
                    new FhirDateTime(new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth()),
                            new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), null, null),
                            TimeZone.getDefault()),
                    "application/pdf",
                    data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        attachments.add(attach);

        //TODO this comment is temporary, do not apply this change
        /**/
        DocumentReference documentReference = DocumentReferenceBuilder.buildWith(
                "Citizen Hub Daily Report " + detailDate.toString(),
                CodeSystemDocumentReferenceStatus.CURRENT,
                attachments,
                getFakeDocumentReferenceType(),
                getOrganization(),
                getFakePracticeSpeciality()
        );

        documentReference.date = new FhirInstant(new FhirDateTime(new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth()), new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), 0, 0), TimeZone.getDefault()));

        client.create(documentReference, new ArrayList<>(), callback);
    }

    private CodeableConcept getOrganizationType() {
        final Coding organizationType = new Coding();

        organizationType.code = "edu";

        final CodeableConcept codeableConcept = new CodeableConcept();

        codeableConcept.coding = Collections.singletonList(organizationType);

        return codeableConcept;
    }

    private Organization getOrganization() {
        final Organization organization = OrganizationBuilder.buildWith(
                getOrganizationType(),
                "UNINOVA - Instituto de Desenvolvimento de Novas Tecnologias",
                "Faculdade de Ciências e Tecnologia",
                "2829-516",
                "Caparica",
                "(+351) 21 29 48 527",
                "https://www.uninova.pt/");

        return organization;
    }

}

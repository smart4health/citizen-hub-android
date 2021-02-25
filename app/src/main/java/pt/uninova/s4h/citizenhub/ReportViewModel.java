package pt.uninova.s4h.citizenhub;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import care.data4life.fhir.r4.model.CodeableConcept;
import care.data4life.fhir.r4.model.Coding;
import care.data4life.fhir.r4.model.Practitioner;
import care.data4life.sdk.helpers.r4.PractitionerBuilder;
import pt.uninova.s4h.citizenhub.persistence.MeasurementAggregate;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;
import pt.uninova.s4h.citizenhub.persistence.MeasurementRepository;
import pt.uninova.util.Pair;
import pt.uninova.util.messaging.Observer;
import pt.uninova.util.time.LocalDateInterval;

public class ReportViewModel extends AndroidViewModel {

    final private MeasurementRepository repository;


    final private boolean testHeartBeat = true;
    final private boolean testDistanceWalked = true;
    final private boolean testStepsTaken = true;
    final private boolean testCalories = true;
    final private boolean testTimeSitting = true;


    final private MutableLiveData<Set<LocalDate>> availableReportsLive;
    final private MediatorLiveData<LocalDateInterval> dateBoundsLive;

    final private Set<Pair<Integer, Integer>> peekedMonths;

    private LocalDate detailDate;
    private Map<MeasurementKind, MeasurementAggregate> detailAggregates;

    public ReportViewModel(Application application) {
        super(application);

        repository = new MeasurementRepository(application);

        availableReportsLive = new MutableLiveData<>(new HashSet<>());
        dateBoundsLive = new MediatorLiveData<>();

        dateBoundsLive.addSource(repository.getDateBounds(), this::onDateBoundsChanged);

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

    public Bitmap resize(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public byte[] createPdf() throws IOException {
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

        Bitmap logo = BitmapFactory.decodeResource(res, R.drawable.citizen_hub_logo, options);


        canvas.save();
        canvas.translate(x + 80, 2);
        canvas.scale(0.25f, 0.25f);
        canvas.drawBitmap(logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();


        Bitmap ec_logo = BitmapFactory.decodeResource(res, R.drawable.ec_logo, options);
        canvas.save();
        canvas.translate(10, 790);
        canvas.scale(0.04f, 0.04f);
        canvas.drawBitmap(ec_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        canvas.drawText("This work has received funding from the European Union's Horizon 2020 research and", x + 25, 808, ecInfoPaint);
        canvas.drawText("innovation programme under Grant agreement No 826117", x + 25, 821, ecInfoPaint);

        Bitmap smart4Health_logo = BitmapFactory.decodeResource(res, R.drawable.img_s4h_logo_report, options);
        canvas.save();
        canvas.translate(475, 801);
        canvas.scale(0.35f, 0.35f);
        canvas.drawBitmap(smart4Health_logo, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas.restore();

        canvas.drawText("powered by", x + 365, 813, poweredByPaint);


        x += 60;
        y += 120;

        String content = "Your Daily Report";
        RectF rect = new RectF(x - 30, y - 50, x + 430, y + 15);
        canvas.drawRoundRect(rect, 10, 10, backgroundPaint);

        canvas.drawText(content, x + 140, y - 15, titlePaint);
        canvas.drawRect(80, 175, 540, 210, rectFillPaint);
        canvas.drawText("Results of: " + detailDate.toString(), x + 120, y + 30, titlePaint);

////2º titulo detailDate.toString()

        Drawable icon = res.getDrawable(R.drawable.ic_daily);
        icon.setBounds(x - 10, y - 40, x + 40, y);
        icon.draw(canvas);
//        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_daily_item);
//        canvas.drawBitmap(bitmap, x-15, y, new Paint(Paint.FILTER_BITMAP_FLAG));

        y += 50;
        x += 20;

        MeasurementAggregate measurementAggregate = detailAggregates.get(MeasurementKind.HEART_RATE);


        if (measurementAggregate != null) {
            canvas.drawText("Average heart rate (bpm): " + decimalFormat.format(measurementAggregate.getAverage()), x, y, textPaint);
            y += 20;
            canvas.drawText("Minimum heart rate (bpm): " + measurementAggregate.getMin(), x, y, textPaint);
            y += 20;
            canvas.drawText("Maximum heart rate (bpm): " + measurementAggregate.getMax(), x, y, textPaint);
            y += 20;
        }


        if (testTimeSitting) {
            Drawable timeSitting = res.getDrawable(R.drawable.ic_time_sitting, null);
            timeSitting.setBounds(0, 0, timeSitting.getIntrinsicWidth(), timeSitting.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            timeSitting.draw(canvas);
            canvas.restore();

            y += 40;


            canvas.drawText("Spent: " + " sitting", x + 70, y, darkTextPaint);
            y += 20;
            canvas.drawText("Spent " + " seated with good posture", x + 70, y, darkTextPaint);
            y += 20;

            y += 40;
        }

        if (testDistanceWalked) {

            Drawable distanceWalked = res.getDrawable(R.drawable.ic_distance, null);
            distanceWalked.setBounds(0, 0, distanceWalked.getIntrinsicWidth(), distanceWalked.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            distanceWalked.draw(canvas);
            canvas.restore();

            y += 40;
            canvas.drawText("Total distance walked: " + " km", x + 70, y + 10, darkTextPaint);
            y += 20;

            y += 40;
        }

        if (testHeartBeat) {

            y -= 10;

            Drawable heartBeat = res.getDrawable(R.drawable.ic_heartbeat_item, null);
            heartBeat.setBounds(0, 0, heartBeat.getIntrinsicWidth(), heartBeat.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x - 15, y + 15);
            canvas.scale(0.35f, 0.35f);
            heartBeat.draw(canvas);
            canvas.restore();

            y += 40;
            canvas.drawText(res.getString(R.string.pdf_report_average_HR_text), x + 70, y, darkTextPaint);
            double example = 2.0;
            canvas.drawText(String.valueOf(example), x + 70 + darkTextPaint.measureText(res.getString(R.string.pdf_report_average_HR_text)), y, boldTextPaint);
            canvas.drawText(" bpm", x + 70 + darkTextPaint.measureText(res.getString(R.string.pdf_report_average_HR_text)) + boldTextPaint.measureText(String.valueOf(example)), y, darkTextPaint);

            y += 20;
            canvas.drawText("Minimum heart rate (bpm): ", x + 70, y, darkTextPaint);
            boldInFront(canvas, "Minimum heart rate (bpm):", "2.0", x + 70, y, darkTextPaint, boldTextPaint);
            y += 20;
            canvas.drawText("Maximum heart rate (bpm): ", x + 70, y, darkTextPaint);
            y += 20;

            y += 20;
        }

        if (testStepsTaken) {
            Drawable stepsTaken = res.getDrawable(R.drawable.ic_steps, null);
            stepsTaken.setBounds(0, 0, stepsTaken.getIntrinsicWidth(), stepsTaken.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            stepsTaken.draw(canvas);
            canvas.restore();

            y += 40;
            canvas.drawText("Steps taken: ", x + 70, y + 10, darkTextPaint);
            y += 20;

            y += 40;
        }

        if (testCalories) {
            Drawable calories = res.getDrawable(R.drawable.ic_calories, null);
            calories.setBounds(0, 0, calories.getIntrinsicWidth(), calories.getIntrinsicHeight());
            canvas.save();
            canvas.translate(x, y + 15);
            canvas.scale(0.35f, 0.35f);
            calories.draw(canvas);
            canvas.restore();

            y += 40;
            canvas.drawText("Estimated Calories burned:" + " calories", x + 70, y + 10, darkTextPaint);
            y += 20;

            y += 40;
        }

        RectF rectAround = new RectF(81, 205, 539, y);
        canvas.drawRoundRect(rectAround, 12, 12, rectPaint);

        measurementAggregate = detailAggregates.get(MeasurementKind.STEPS);

        if (measurementAggregate != null) {
            canvas.drawText("Steps taken (number): " + measurementAggregate.getSum(), x, y, textPaint);
            y += 20;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.DISTANCE);

        if (measurementAggregate != null) {
            canvas.drawText("Travelled distance (m): " + measurementAggregate.getSum(), x, y, textPaint);
            y += 20;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.CALORIES);

        if (measurementAggregate != null) {
            canvas.drawText("Calories consumed (kcal): " + measurementAggregate.getSum(), x, y, textPaint);
            y += 20;
        }

        measurementAggregate = detailAggregates.get(MeasurementKind.GOOD_POSTURE);

        if (measurementAggregate != null) {
            canvas.drawText("Total time spent with good posture (min): " + measurementAggregate.getSum(), x, y, textPaint);
        }

        document.finishPage(page);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            document.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        document.close();
        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/citizen.pdf";
        FileOutputStream fos = new FileOutputStream(myFilePath);
        fos.write(out.toByteArray());
        fos.flush();
        fos.close();

        return out.toByteArray();
    }

    private void onDateBoundsChanged(LocalDateInterval dateBounds) {
        if (dateBoundsLive.getValue() == null || !dateBoundsLive.getValue().equals(dateBounds)) {
            dateBoundsLive.postValue(dateBounds);
        }
    }

    public LiveData<LocalDateInterval> getAvailableReportDateBoundaries() {
        return dateBoundsLive;
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

    public void obtainSummary(Observer<Map<MeasurementKind, MeasurementAggregate>> observer) {
        repository.obtainDailyAggregate(detailDate, value -> {
            detailAggregates = value;

            observer.onChanged(value);
        });
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
            repository.obtainDates(peek, this::onDatesChanged);
        }
    }


    public void boldInFront(Canvas canvas, String normal, String bold, float x, float y, Paint normalPaint, Paint boldPaint) {
        canvas.drawText(normal, x, y, normalPaint);
        canvas.drawText(" " + bold, x + normalPaint.measureText(normal), y, boldPaint);
    }

    public void boldBetween(Canvas canvas, String normal, String normal2, String bold, float x, float y, Paint normalPaint, Paint boldPaint) {
        boldInFront(canvas, normal, bold, x, y, normalPaint, boldPaint);
        canvas.drawText(normal2, x + normalPaint.measureText(normal + " ") + boldPaint.measureText(bold), y, normalPaint);
    }


//    public void sendDetail(Callback<Fhir4Record<DocumentReference>> callback) throws IOException {
//        Fhir4RecordClient client = Data4LifeClient.getInstance().getFhir4();
//        final LocalDateTime now = LocalDateTime.now();
//        List<Attachment> attachments = new ArrayList<>(1);
//
//        byte[] data = createPdf();
//        Attachment attach = null;
//
//        try {
//            attach = AttachmentBuilder.buildWith(now.toString(),
//                    new FhirDateTime(new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth()),
//                            new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), null, null),
//                            TimeZone.getDefault()),
//                    "application/pdf",
//                    data);
//        } catch (DataRestrictionException.UnsupportedFileType | DataRestrictionException.MaxDataSizeViolation unsupportedFileType) {
//            unsupportedFileType.printStackTrace();
//        }
//
//        attachments.add(attach);
//
//        DocumentReference documentReference = DocumentReferenceBuilder.buildWith(
//                "Citizen Hub Daily Report " + detailDate.toString(),
//                CodeSystemDocumentReferenceStatus.CURRENT,
//                attachments,
//                getFakeDocumentReferenceType(),
//                getFakePractitioner(),
//                getFakePracticeSpeciality()
//        );
//
//        documentReference.date = new FhirInstant(new FhirDateTime(new FhirDate(now.getYear(), now.getMonthValue(), now.getDayOfMonth()), new FhirTime(now.getHour(), now.getMinute(), now.getSecond(), 0, 0), TimeZone.getDefault()));
//
//        client.<DocumentReference>create(documentReference, new ArrayList<>(), callback);
//    }


}

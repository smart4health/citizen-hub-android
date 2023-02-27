package pt.uninova.s4h.citizenhub.report;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class CanvasWriter {
    Canvas canvas;
    List<DrawObject> drawingList;
    float x = 0;
    float y = 0;


    public CanvasWriter(Canvas canvas, float x, float y) {
        this.canvas = canvas;
        drawingList = new ArrayList<DrawObject>();
        this.x = x;
        this.y = y;
    }

    public CanvasWriter(Canvas canvas) {
        this(canvas, 0, 0);
    }

    /** Draws text in a given position.
     * @param text Text to write.
     * @param paint Paint. */
    public void addText(String text, float x, float y, Paint paint) {
        DrawObject drawObject = new DrawObject(text, x, y, paint);
        drawingList.add(drawObject);
    }

    /** Writes text in front of the last text writen.
     * @param text Text to write.
     * @param paint Text paint. */
    public void addTextInFront(String text, Paint paint) {

        DrawObject lastDrawObject = drawingList.get(drawingList.size() - 1);

        drawingList.add(new DrawObject(text, lastDrawObject.getXCoordinate() + lastDrawObject.getPaint().measureText(lastDrawObject.getText()), lastDrawObject.getYCoordinate(), paint));

    }

    public void addNewLine(String text, float x, float verticalSpacing, Paint paint) {
        DrawObject lastDrawObject = drawingList.get(drawingList.size() - 1);
        DrawObject drawObject = new DrawObject(text, x, verticalSpacing + lastDrawObject.getYCoordinate(), paint);
        drawingList.add(drawObject);
    }


    public void addNewLine(String text, float verticalSpacing) {
        DrawObject lastDrawObject = drawingList.get(drawingList.size() - 1);
        drawingList.add(new DrawObject(text, lastDrawObject.getXCoordinate(), verticalSpacing + lastDrawObject.getYCoordinate(), lastDrawObject.getPaint()));
    }

    public void draw() {
        DrawObject drawObject = new DrawObject();
        if (drawingList != null) {
            for (int i = 0; i < drawingList.size(); i++) {
                drawObject = drawingList.get(i);
                //fazer gets
                canvas.drawText(drawObject.getText(), drawObject.getXCoordinate(), drawObject.getYCoordinate(), drawObject.getPaint());
            }
        }
    }

    public void clear() {
        drawingList.clear();
    }


}

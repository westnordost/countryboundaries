package de.westnordost.countryboundaries.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class GridOverlay extends Overlay {

    public int width = 360;
    public int height = 180;

    private final Paint mLinePaint = new Paint();

    public GridOverlay() {
        mLinePaint.setAntiAlias(false);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1f);
        mLinePaint.setColor(0x99000000);
    }

    @Override
    public void draw(Canvas c, Projection pProjection) {
        if (!isEnabled()) return;

        Point p = new Point();
        GeoPoint pos = new GeoPoint(0.0, 0.0);

        for (int x = 0; x < width; x++) {
            pos.setLongitude(-180.0 + x * 360.0 / width);
            pProjection.toPixels(pos, p);
            c.drawLine(p.x, 0, p.x, c.getHeight(), mLinePaint);
        }
        pos.setLongitude(0);
        pos.setLatitude(0);
        for (int y = 0; y < height; y++) {
            pos.setLatitude(90.0 - 180.0 * y / height);
            pProjection.toPixels(pos, p);
            c.drawLine(0, p.y, c.getWidth(), p.y, mLinePaint);
        }
    }
}

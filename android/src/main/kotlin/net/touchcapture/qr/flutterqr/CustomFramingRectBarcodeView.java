package net.touchcapture.qr.flutterqr;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.Size;

public class CustomFramingRectBarcodeView extends BarcodeView {

    private static final int BOTTOM_OFFSET_NOT_SET_VALUE = -1;

    private int bottomOffset = BOTTOM_OFFSET_NOT_SET_VALUE;

    private BarcodeCallback barcodeCallback = null;
    private Rect container = null;
    private Rect surfaceRect = null;

    public CustomFramingRectBarcodeView(Context context) {
        super(context);
    }

    public CustomFramingRectBarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFramingRectBarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected Rect calculateFramingRect(Rect container, Rect surface) {
        this.container = container;
        this.surfaceRect = surface;
        Rect containerArea = new Rect(container);
        boolean intersects = containerArea.intersect(surface);//adjusts the containerArea (code from super.calculateFramingRect)
        Rect scanAreaRect = super.calculateFramingRect(container, surface);
        if (bottomOffset != BOTTOM_OFFSET_NOT_SET_VALUE) {//if the setFramingRect function was called, then we shift the scan area by Y
            Rect scanAreaRectWithOffset = new Rect(scanAreaRect);
            scanAreaRectWithOffset.bottom -= bottomOffset;
            scanAreaRectWithOffset.top -= bottomOffset;

            boolean belongsToContainer = scanAreaRectWithOffset.intersect(containerArea);
            if(belongsToContainer){
                return scanAreaRectWithOffset;
            }
        }
        return scanAreaRect;
    }

    @Override
    public void decodeContinuous(BarcodeCallback callback) {
        barcodeCallback = callback;
        super.decodeContinuous(callback);
    }

    @Override
    public Rect getPreviewFramingRect(){
        if(container != null && surfaceRect != null){
            return calculateFramingRect(container, surfaceRect);
        }
        return  super.getPreviewFramingRect();
//        final Size previewSize = getPreviewSize();
//
//        int previewWidth = previewSize.width;
//        int previewHeight = previewSize.height;
//
////        int width = containerSize.width;
////        int height = containerSize.height;
////        Rect container = new Rect(0, 0, width, height);
//
//        final Rect framingRect = getFramingRect();
//        if(framingRect == null || surfaceRect == null) return super.getPreviewFramingRect();
//
//        Rect frameInPreview = new Rect(framingRect);
//        frameInPreview.offset(-surfaceRect.left, -surfaceRect.top);
//
//        return new Rect(frameInPreview.left * previewWidth / surfaceRect.width(),
//                frameInPreview.top * previewHeight / surfaceRect.height(),
//                frameInPreview.right * previewWidth / surfaceRect.width(),
//                frameInPreview.bottom * previewHeight / surfaceRect.height());
    }

    public void setFramingRect(int rectWidth, int rectHeight, int bottomOffset) {
        this.bottomOffset = bottomOffset;
        this.setFramingRectSize(new Size(rectWidth, rectHeight));
        if(barcodeCallback != null){
            super.decodeContinuous(barcodeCallback);
        }

    }
}

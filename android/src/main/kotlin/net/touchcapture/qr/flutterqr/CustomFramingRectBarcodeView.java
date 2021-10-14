package net.touchcapture.qr.flutterqr;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.Size;
import com.journeyapps.barcodescanner.camera.DisplayConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class CustomFramingRectBarcodeView extends BarcodeView {

    private static final int BOTTOM_OFFSET_NOT_SET_VALUE = -1;

    private int bottomOffset = BOTTOM_OFFSET_NOT_SET_VALUE;

    private BarcodeCallback barcodeCallback = null;

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
        Size containerSize = null;
        Size previewSize = null;
        DisplayConfiguration displayConfiguration = null;
        try{
           final Class cameraPreviewClass =  this.getClass().getSuperclass().getSuperclass();
           final Field containerSizeField = cameraPreviewClass.getDeclaredField("containerSize");
           final Field previewSizeField = cameraPreviewClass.getDeclaredField("previewSize");
           final Field displayConfigurationField = cameraPreviewClass.getDeclaredField("displayConfiguration");

           containerSizeField.setAccessible(true);
           previewSizeField.setAccessible(true);
           displayConfigurationField.setAccessible(true);

            containerSize = (Size) containerSizeField.get(this);
            previewSize = (Size) previewSizeField.get(this);
            displayConfiguration = (DisplayConfiguration) displayConfigurationField.get(this);
        } catch (Exception e){
            final String d = "";
        }


        if (containerSize == null || previewSize == null || displayConfiguration == null) {

            throw new IllegalStateException("containerSize or previewSize is not set yet");
            //return super.getPreviewFramingRect();
        }

        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        int width = containerSize.width;
        int height = containerSize.height;

        Rect scaledPreview = displayConfiguration.scalePreview(previewSize);
        if (scaledPreview.width() <= 0 || scaledPreview.height() <= 0) {
            // Something is not ready yet - we can't start the preview.
            throw new IllegalStateException("Something is not ready yet - we can't start the preview.");
        }

        final Rect surfaceRect = scaledPreview;

        final Rect container = new Rect(0, 0, width, height);
        final Rect framingRect = calculateFramingRect(container, surfaceRect);
        final Rect frameInPreview = new Rect(framingRect);
        frameInPreview.offset(-surfaceRect.left, -surfaceRect.top);

        return new Rect(frameInPreview.left * previewWidth / surfaceRect.width(),
                frameInPreview.top * previewHeight / surfaceRect.height(),
                frameInPreview.right * previewWidth / surfaceRect.width(),
                frameInPreview.bottom * previewHeight / surfaceRect.height());

    }

    public void setFramingRect(int rectWidth, int rectHeight, int bottomOffset) {
        final Size newSize = new Size(rectWidth, rectHeight);
        if(bottomOffset == this.bottomOffset && newSize.equals(this.getFramingRectSize())) return;
        this.bottomOffset = bottomOffset;
        this.setFramingRectSize(newSize);
        if(barcodeCallback != null){
            synchronized (this){
                super.stopDecoding();

                try{
                    Thread.sleep(100);
                } catch (Exception e){}


                super.decodeContinuous(barcodeCallback);
            }
        }
//        this.pause();
//        this.resume();

    }

//    private void calculateFrames(){
//        try {
//            Method method = this.getClass().getDeclaredMethod("calculateFrames");
//            method.setAccessible(true);
//            method.invoke(this);
//        } catch (Exception e){
//
//        }
//
//    }
}

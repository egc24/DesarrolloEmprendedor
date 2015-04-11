package egcws.bawd.zxingadapt;

/**
 * This LuminanceSource implementation is meant for J2SE clients and our blackbox unit tests.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 * @author code@elektrowolle.de (Wolfgang Jung)
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.google.zxing.LuminanceSource;

/**
 * This LuminanceSource implementation is meant for J2SE clients and our blackbox unit tests.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 * @author code@elektrowolle.de (Wolfgang Jung)
 */
public final class BufferedImageLuminanceSource extends LuminanceSource {

  private static final double MINUS_45_IN_RADIANS = -0.7853981633974483; // Math.toRadians(-45.0)

  private final Bitmap image;
  private final int left;
  private final int top;

  public BufferedImageLuminanceSource(Bitmap image) {
    this(image, 0, 0, image.getWidth(), image.getHeight());
  }

  public BufferedImageLuminanceSource(Bitmap image, int left, int top, int width, int height) {
    super(width, height);

    this.image = image;
    this.left = left;
    this.top = top;
  }

  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
    	row = new byte[width];
    }
    int[] array = new int[width];
    // The underlying raster of image consists of bytes with the luminance values
    image.getPixels(array, 0, width, left, top + y, width, 1);
    row = this.intArrayToByteArrayHC(array);
    return row;
  }

  @Override
  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();
    int area = width * height;
    int[] array = new int[area];
    // The underlying raster of image consists of area bytes with the luminance values
    image.getPixels(array, 0, width, left, top, width, height);
    byte[] matrix = this.intArrayToByteArrayHC(array);
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  @Override
  public LuminanceSource crop(int left, int top, int width, int height) {
    return new BufferedImageLuminanceSource(image, this.left + left, this.top + top, width, height);
  }

  /**
   * This is always true, since the image is a gray-scale image.
   *
   * @return true
   */
  @Override
  public boolean isRotateSupported() {
    return true;
  }

  @Override
  public LuminanceSource rotateCounterClockwise() {
    int sourceWidth = image.getWidth();
    int sourceHeight = image.getHeight();

    // Note width/height are flipped since we are rotating 90 degrees.
    Matrix matrix = new Matrix();
    matrix.preRotate(90);
    Bitmap rotatedImage = Bitmap.createBitmap(image, 0, 0, 
    								image.getWidth(), image.getHeight(), 
                                  matrix, true);

    // Maintain the cropped region, but rotate it too.
    int width = getWidth();
    return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth - (left + width), getHeight(), width);
  }

  @Override
  public LuminanceSource rotateCounterClockwise45() {
    int width = getWidth();
    int height = getHeight();

    int oldCenterX = left + width / 2;
    int oldCenterY = top + height / 2;
        
    // Rotate 45 degrees counterclockwise.
    int sourceDimension = Math.max(image.getWidth(), image.getHeight());
    Matrix matrix = new Matrix();
    matrix.postRotate(45);
    Bitmap rotatedImage = Bitmap.createBitmap(image, 0, 0, 
    								image.getWidth(), image.getHeight(), 
                                  matrix, true);


    int halfDimension = Math.max(width, height) / 2;
    int newLeft = Math.max(0, oldCenterX - halfDimension);
    int newTop = Math.max(0, oldCenterY - halfDimension);
    int newRight = Math.min(sourceDimension - 1, oldCenterX + halfDimension);
    int newBottom = Math.min(sourceDimension - 1, oldCenterY + halfDimension);

    return new BufferedImageLuminanceSource(rotatedImage, newLeft, newTop, newRight - newLeft, newBottom - newTop);
  }
  
  private byte[] intArrayToByteArrayHC(int [] intArray){
	  byte[] byteArray = new byte[intArray.length];
	  if (intArray.length > 0) {
		  for (int i = 0; i < intArray.length; i++){
			  byteArray[i] = (byte) intArray[i];
		  }
		  return byteArray;
	  } else {
		  return null;
	  }
  }
}

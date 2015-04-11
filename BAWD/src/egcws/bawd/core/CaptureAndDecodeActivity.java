package egcws.bawd.core;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.EAN13Reader;
import com.google.zxing.qrcode.QRCodeReader;

import egcws.bawd.example.YinYanDetector;
import egcws.bawd.generated.R;
import egcws.bawd.zxingadapt.BufferedImageLuminanceSource;

// se implementa la interfaz CvCameraViewListener2 para poder ser cliente
// de la interfaz que provee OpenCV para poder usar la camara
public class CaptureAndDecodeActivity extends Activity implements CvCameraViewListener2, OnTouchListener{
	
	public enum CodeType {
		QRCODE,	EAN13CODE
	}
	
	// clase que provee una interfaz para interactuar con OpenCV, permite controlar:
	// habilitacion de camara
	// procesesar el frame correspondiente
	// llamar a un controlador externo para hacer ajustes al frame y dibujarlo como corresponde en pantalla
	private CameraBridgeViewBase cameraBridgeViewBase;

	// clase que provee una interfaz para interactuar con ZXing, permite decodificar:
	//	1D producto	1D industrial	2D
	//	UPC-A		Code 39			QR Code
	//	UPC-E		Code 93			Data Matrix
	//	EAN-8		Code 128		Aztec (beta)
	//	EAN-13		Codabar			PDF 417 (beta)
	//	ITF	
	//	RSS-14	
	//	RSS-Expanded 
	private Reader codeReader;
	
	// media player para reproducir el sonido cuando al final se logra decodificar
	// la imagen
	private MediaPlayer mediaPlayer;
	
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                	// si todo salio bien habilito la interfaz
                    cameraBridgeViewBase.enableView();
                    cameraBridgeViewBase.setOnTouchListener(CaptureAndDecodeActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture_and_decode);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// creo la interfaz correspondiente para laburar con OpenCV
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.native_camera_view);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        
        // creo el mediaplayer para ejecutar el efecto
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound_effect);
        
        // muestro un mensaje con indicaciones
        Toast.makeText(this, "The capture must be done with the same orientation than this text. Touch the screen to exit.", 
        		Toast.LENGTH_LONG).show();
        
		switch (getIntent().getIntExtra("captureTargetCode", 0)){
			case 1:
				codeReader = new QRCodeReader();
			break;
			
			case 2:
				codeReader = new EAN13Reader();
			break;
		}
	}

    @Override
    public void onPause()
    {
        // en el momento en que se pausa o minimiza la aplicacion si la interfaz para laburar con OpenCV no se dispuso,
        // entonces es eliminada
        super.onPause();
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        // en el momento que se resume la aplicacion se lanza el callback de OpenCV que permite habilitar la camara
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, baseLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        // en el momento en que se finaliza la aplicacion si la interfaz para laburar con OpenCV no se dispuso,
        // entonces es eliminada
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

	@Override
	public void onCameraViewStarted(int width, int height) {
		// codigo para cuando se inicializa la camara 
	}

	@Override
	public void onCameraViewStopped() {
		// codigo para cuando se finializa la camara
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// metodo para procesar cada frame que devuelve la camara
		try {
			Bitmap frame = Bitmap.createBitmap(inputFrame.gray().cols(), inputFrame.gray().rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(inputFrame.gray(), frame);
			Result resultado = this.codeReader.decode(this.getBinaryBitmapImageFrom(frame));
			
			mediaPlayer.start();
			this.returnWithResult(resultado.toString());
		} catch (NotFoundException e) {
		} catch (ChecksumException e) {
		} catch (FormatException e) {
		}
		Mat frame = inputFrame.rgba();
		this.paintHorizontalLine(frame, new Scalar(255, 0, 0));
		return frame;
	}
	
	private BinaryBitmap getBinaryBitmapImageFrom(Bitmap image) {
    	return new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
	}
	
	private void paintHorizontalLine(Mat source, Scalar color){
		Point start = new Point(0.0, source.rows() / 2.0);
		Point end = new Point(source.cols(), source.rows() / 2.0);

		Core.line(source, start, end, color);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Are you sure you want to exit?");
    	builder.setIcon(android.R.drawable.ic_dialog_alert);
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {			      	
    	    	CaptureAndDecodeActivity.this.returnWithResult(null);
    	    	CaptureAndDecodeActivity.this.finish();   
    	    }
    	});
    	builder.setNegativeButton("No", null);
    	builder.show();
		return false;
	}
	
	private void returnWithResult(String captureResult){
		Intent returnIntent = new Intent(this, YinYanDetector.class);
		returnIntent.putExtra("captureResult", captureResult);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
}

package egcws.bawd.example;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import egcws.bawd.core.CaptureAndDecodeActivity;
import egcws.bawd.generated.R;


public class YinYanDetector extends Activity implements OnClickListener, OnItemSelectedListener {

	private String selectedItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button button = (Button) findViewById(R.id.scan);
        button.setOnClickListener(this);
        Spinner spinner = (Spinner) findViewById(R.id.barCodeTypeSpinner);
        spinner.setOnItemSelectedListener(this);
        this.fillCodeBarTypeSpinner();
	}

	private void fillCodeBarTypeSpinner(){
		List<String> spinnerArray =  new ArrayList<String>();
		spinnerArray.add("QR");
		spinnerArray.add("EAN13");
	
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
		    this, android.R.layout.simple_spinner_item, spinnerArray);
	
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner sItems = (Spinner) findViewById(R.id.barCodeTypeSpinner);
		sItems.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if ("QR" == this.selectedItem){
			this.startCaptureAndDecodeForCodeType(1);
		} else if ("EAN13" == this.selectedItem) {
			this.startCaptureAndDecodeForCodeType(2);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		this.selectedItem = parent.getItemAtPosition(position).toString();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == 1) {
	        if(resultCode == RESULT_OK){
	        	EditText decodingResult = (EditText) findViewById(R.id.decodingResult);
	        	decodingResult.setText(data.getStringExtra("captureResult"));
	        } else if (resultCode == RESULT_CANCELED) {
	            
	        }
	    }
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}
	
	private void startCaptureAndDecodeForCodeType(int codeType){
		Intent intent = new Intent(this, CaptureAndDecodeActivity.class);
		intent.putExtra("captureTargetCode", codeType);
		this.startActivityForResult(intent, 1);
	}
}

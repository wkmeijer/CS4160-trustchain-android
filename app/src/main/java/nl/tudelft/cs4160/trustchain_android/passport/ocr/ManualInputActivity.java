package nl.tudelft.cs4160.trustchain_android.passport.ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.passport.DocumentData;

public class ManualInputActivity extends AppCompatActivity {
    private EditText docNumber;

    Spinner dobDaySpinner;
    Spinner dobMonthSpinner;
    Spinner dobYearSpinner;

    Spinner expiryDaySpinner;
    Spinner expiryMonthSpinner;
    Spinner expiryYearSpinner;

    // Define the length of document details here, because getting maxLength from EditText is complex
    private final int DOC_NUM_LENGTH = 9;
    private final int DOB_YEAR_STARTING_INDEX = 60; // Start at 1960

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);

        docNumber = (EditText) findViewById(R.id.doc_num);

        Button submitBut = (Button) findViewById(R.id.submit_button);
        submitBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verifyInput()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(DocumentData.identifier, getData());
                    setResult(Activity.RESULT_OK, returnIntent);
                    startActivity(returnIntent);
                }
            }
        });
        setupDOBSpinners();
        setupExpirySpinners();

        // When docData was previously filled in, update text fields
        if(getIntent().hasExtra(DocumentData.identifier)) {
            putData(getIntent().getExtras());
        }
    }

    private void setupExpirySpinners() {
        expiryDaySpinner = (Spinner) findViewById(R.id.expiry_day_spinner);
        expiryMonthSpinner = (Spinner) findViewById(R.id.expiry_month_spinner);
        expiryYearSpinner = (Spinner) findViewById(R.id.expiry_year_spinner);

        List<String> days = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            days.add("" + (i + 1));
        }

        // Leave the default view (android.R.layout.simple_spinner_item) but set custom view for dropdown to add extra padding
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        expiryDaySpinner.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expiryMonthSpinner.setAdapter(monthAdapter);

        Date dt = new Date();
        List<String> years = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            years.add("" + (dt.getYear() + 1900 + i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expiryYearSpinner.setAdapter(yearAdapter);
    }

    private void setupDOBSpinners () {
        dobDaySpinner = (Spinner) findViewById(R.id.dob_day_spinner);
        dobMonthSpinner = (Spinner) findViewById(R.id.dob_month_spinner);
        dobYearSpinner = (Spinner) findViewById(R.id.dob_year_spinner);

        List<String> days = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            days.add("" + (i + 1));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        dobDaySpinner.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dobMonthSpinner.setAdapter(monthAdapter);

        Date dt = new Date();
        List<String> years = new ArrayList<>();
        int maxYear = dt.getYear() + 1900 - 18;
        for (int i = 1900; i <= maxYear; i++) {
            years.add("" + i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dobYearSpinner.setAdapter(yearAdapter);
        dobYearSpinner.setSelection(DOB_YEAR_STARTING_INDEX);
    }

    /**
     * Update textFields with docData if it was previously filled in, else leave as is.
     */
    public void putData(Bundle extras) {
        DocumentData docData = (DocumentData) extras.get(DocumentData.identifier);
        if(docData != null && docData.isValid()) {
            docNumber.setText(docData.getDocumentNumber());
            dobYearSpinner.setSelection(Integer.parseInt(docData.getDateOfBirth().substring(0,2)));
            dobMonthSpinner.setSelection(Integer.parseInt(docData.getDateOfBirth().substring(2,4))-1);
            dobDaySpinner.setSelection(Integer.parseInt(docData.getDateOfBirth().substring(4,6))-1);

            Date dt = new Date();
            int currYear = dt.getYear() - 100;
            expiryYearSpinner.setSelection(Integer.parseInt(docData.getExpiryDate().substring(0,2)) - currYear);
            expiryMonthSpinner.setSelection(Integer.parseInt(docData.getExpiryDate().substring(2,4))-1);
            expiryDaySpinner.setSelection(Integer.parseInt(docData.getExpiryDate().substring(4,6))-1);
        }
    }

    /**
     * Create a @link{DocumentData} object of the input date in the same way as the OCR scanner does.
     * @return data - A DocumentData object of the required document data for BAC.
     */
    public DocumentData getData() {
        DocumentData data = new DocumentData();
        DecimalFormat formatter = new DecimalFormat("00");
        data.setDocumentNumber(docNumber.getText().toString().toUpperCase());
        data.setDateOfBirth(dobYearSpinner.getSelectedItem().toString().substring(2) +
                formatter.format(dobMonthSpinner.getSelectedItemId()+1) +
                formatter.format(Integer.parseInt(dobDaySpinner.getSelectedItem().toString())));
        data.setExpiryDate(expiryYearSpinner.getSelectedItem().toString().substring(2) +
                formatter.format(expiryMonthSpinner.getSelectedItemId()+1) +
                formatter.format(Integer.parseInt(expiryDaySpinner.getSelectedItem().toString())));
        return data;
    }

    /**
     * Check if all the fields have been filled in and check for wrong length input.
     * TODO this does not check if dates exist, e.g no error is given when feb 31st is entered.
     * @return valid - boolean which indicates whether the input is valid.
     */
    public boolean verifyInput() {
        boolean valid = true;
        int docNumLength = docNumber.getText().toString().length();
        if(docNumLength != DOC_NUM_LENGTH ) {
            valid = false;
            if(docNumLength == 0) {
                docNumber.setError(getResources().getString(R.string.errInputDocNum));
            }
            else {
                docNumber.setError(getResources().getString(R.string.errFormatDocNum));
            }
        }
        return valid;
    }

}

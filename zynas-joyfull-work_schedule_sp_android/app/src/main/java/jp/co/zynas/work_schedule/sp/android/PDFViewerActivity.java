package jp.co.zynas.work_schedule.sp.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import java.io.File;

public class PDFViewerActivity extends AppCompatActivity implements OnLoadCompleteListener {

    private static final String FILEPATH = "FILEPATH";
    private static final String TITLE = "TITLE";

    public static Intent newIntent(Context context, String path, String title) {
        Intent intent = new Intent(context, PDFViewerActivity.class);
        intent.putExtra(FILEPATH, path);
        intent.putExtra(TITLE, title);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        setTitle(getIntent().getStringExtra(TITLE));
        PDFView view = findViewById(R.id.pdfView);
        File pdfFile = new File(getIntent().getStringExtra(FILEPATH));
        view.fromFile(pdfFile).onLoad(this).load();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadComplete(int nbPages) {

    }
}

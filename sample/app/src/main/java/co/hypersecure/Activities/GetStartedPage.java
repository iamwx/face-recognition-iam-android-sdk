package co.hypersecure.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import co.hypersecure.R;

public class GetStartedPage extends FDActivity {

    public static void start(Context context){
        Intent i = new Intent(context, GetStartedPage.class);
        context.startActivity(i);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started_page);
        Button btNext = (Button) findViewById(R.id.tvGetStarted);
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.start(GetStartedPage.this);
                finish();
            }
        });
    }
}

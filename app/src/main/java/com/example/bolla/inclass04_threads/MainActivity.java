package com.example.bolla.inclass04_threads;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    SeekBar s1,s2;
    TextView pwd_count, pwd_length, pwd_view;
    private Handler h;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(Boolean.TRUE);
        getSupportActionBar().setTitle("InClass4");

        s1 = (SeekBar) findViewById(R.id.sb1);
        s2 = (SeekBar) findViewById(R.id.sb2);
        pwd_count = (TextView) findViewById(R.id.countView);
        pwd_length = (TextView) findViewById(R.id.lengthView);
        pwd_view = (TextView) findViewById(R.id.passwordView);

        s1.setProgress(1);
        s1.setMax(10);
        s2.setProgress(8);
        s2.setMax(23);

        pwd_count.setText(Integer.toString(s1.getProgress()));
        pwd_length.setText(Integer.toString(s2.getProgress()));

        h = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                final CharSequence[] allPasswords;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);

                switch (message.what){
                    case DoWork.STATUS_START:
                        pd = new ProgressDialog(MainActivity.this);
                        pd.setMessage("Generating Passwords...");
                        pd.setCancelable(false);
                        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pd.setMax(s1.getProgress());
                        pd.show();
                        break;
                    case DoWork.STATUS_COMPUTING:
                        pd.setProgress((Integer)message.obj);
                        pd.show();

                        break;

                    case DoWork.STATUS_END:
                        pd.dismiss();
                        allPasswords = message.getData().getCharSequenceArray("RESULT_STRINGS");
                        Log.d("demo", (String) allPasswords[0]);

                        alertBuilder.setTitle("Passwords")
                                .setItems(allPasswords, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        pwd_view.setText(allPasswords[i]);
                                    }
                                }).create().show();
                        break;
                }
                return false;
            }
        });

        findViewById(R.id.buttonThread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorService taskPool = Executors.newFixedThreadPool(2);
                taskPool.execute(new DoWork());
            }
        });

        findViewById(R.id.buttonAsync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               new AsyncWork().execute();

            }
        });

        s1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i >=1){
                    pwd_count.setText(Integer.toString(i));

                }
                else{
                    s1.setProgress(1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        s2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i >=8){
                    pwd_length.setText(Integer.toString(i));
                }
                else{
                    s2.setProgress(8);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public class DoWork implements Runnable {

        static final int STATUS_START = 100;
        static  final int STATUS_COMPUTING = 101;
        static  final int STATUS_END = 102;

        @Override
        public void run() {
            int count = s1.getProgress();
            int length = s2.getProgress();
            CharSequence[] pwd_strings = new CharSequence[count];

            Message m = new Message();
            m.what = STATUS_START;
            h.sendMessage(m);

            for(int i =0; i< count;i++){
                String s = Util.getPassword(   length  );
                pwd_strings[i] = s;
                Log.d("demo",s);

                m = new Message();
                m.obj = i+1;
                m.what = STATUS_COMPUTING;
                h.sendMessage(m);
            }

            m = new Message();
            m.what = STATUS_END;
            Bundle b = new Bundle();
            b.putCharSequenceArray("RESULT_STRINGS",pwd_strings);
            m.setData(b);
            h.sendMessage(m);
        }
    }

    private class AsyncWork extends AsyncTask<Void,Integer,CharSequence[]>{
        int count = s1.getProgress();
        int length = s2.getProgress();

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Generating Passwords...");
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(s1.getProgress());
            pd.setProgress(0);
            pd.show();
        }

        @Override
        protected CharSequence[] doInBackground(Void... voids) {
           CharSequence[] pwd_strings = new CharSequence[count];
            for(int i =0; i< count;i++){
                String s = Util.getPassword(  length  );
                pwd_strings[i] = s;
                Log.d("demo",s);
                publishProgress(i+1);
            }
            return pwd_strings;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
         pd.setProgress(values[0]);
            pd.show();
        }

        @Override
        protected void onPostExecute(final CharSequence[] strings) {
            pd.dismiss();
            AlertDialog.Builder alertBuilder1 = new AlertDialog.Builder(MainActivity.this);
            alertBuilder1.setTitle("Passwords")
                    .setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pwd_view.setText(strings[i]);
                        }
                    }).create().show();
        }


    }
}

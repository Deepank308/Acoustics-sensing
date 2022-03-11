package com.example.acoustics;

//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    // Initializing all variables..
    private TextView startTV, stopTV, playTV, stopPlayTV, statusTV;
    private EditText editTextPlayTime, editTextRecordTime;

    // creating a variable for media recorder object class.
    private MediaRecorder mRecorder;

    // creating a variable for mediaPlayer class
    private MediaPlayer mPlayer;

    // string variable is created for storing a file name
    private static String mFileName = null;
    private static String[] playFileName = new String[3];
    private int playFileNameIdx = 0;
    private static final String LOG_TAG = "Acoustics";
//    private ExtAudioRecorder extAudioRecorder = ExtAudioRecorder.getInstance(false);

    // in milli secs
    private static long ts = System.currentTimeMillis();
    private static Long playTime = 500L;
    private static Long recordTime = 1000L;
    private boolean stopPressed = false;
    private int recordNo = 0;
    private CountDownTimer playTimer = null;
    private CountDownTimer recordTimer = null;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private FileChooserFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize all variables with their layout items.
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fragment_fileChooser);

        statusTV = findViewById(R.id.idTVstatus);
        stopTV = findViewById(R.id.btnStop);
        playTV = findViewById(R.id.btnPlay);
        editTextPlayTime = findViewById(R.id.editText_audioPlayTime);
        editTextRecordTime = findViewById(R.id.editText_responseRecordTime);

//        startTV = findViewById(R.id.btnRecord);
//        stopPlayTV = findViewById(R.id.btnStopPlay);

        stopTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
//        stopPlayTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));


        stopTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFileNameIdx = 0;
                recordNo = 0;
                // pause Recording method will
                // pause the recording of audio.
                stopPressed = true;

                if(playTimer != null) playTimer.cancel();
                if(recordTimer != null) recordTimer.cancel();

                stopRecording();
                stopPlaying();
            }
        });
        playTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFileNameIdx = 0;
                if(!editTextPlayTime.getText().toString().equals("")) {
                    long t = Long.parseLong(editTextPlayTime.getText().toString());
                    if(t > 0) playTime = t;
                    else editTextPlayTime.setError("0 not Allowed.");
                }
                if(!editTextRecordTime.getText().toString().equals("")) {
                    long t = Long.parseLong(editTextRecordTime.getText().toString());
                    if(t > 0) recordTime = t;
                    else editTextRecordTime.setError("0 not Allowed");
                }

                playFileName[0] = fragment.getPath();
                String pathToChirps = Paths.get(playFileName[0]).getParent().toString();
                playFileName[1] = Paths.get(pathToChirps, "14000.wav").toString();
                playFileName[2] = Paths.get(pathToChirps, "16000.wav").toString();
                if(playFileName[0].equals("")) {
                    fragment.setEditTextError("Audio File Not specified.");
                }
                else {
                    stopPressed = false;
                    recordNo = 0;
//                    extAudioRecorder.reset();
                    startExperiment();
                }
            }
        });
    }

    private void prepareRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record nd store the audio.
        if (CheckPermissions()) {

            // setBackGroundColor method will change
            // the background color of text view.
//            startTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
//            stopPlayTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
            stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
            playTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));

            mFileName = String.format(getString(R.string.recording_name), ts, playFileNameIdx, recordNo/playFileName.length);
            recordNo += 1;

            // we are here initializing our filename variable
            // with the path of the recorded audio file.
            ContentValues values = new ContentValues(4);
            values.put(MediaStore.Audio.Media.TITLE, mFileName);
            values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (ts / 1000));
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac");
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, mFileName);
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Acoustics Recordings/");

            Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            ParcelFileDescriptor file = null;
//            File file = null;
            try {
//                file = new File(mFileName);
                file = getContentResolver().openFileDescriptor(audioUri, "rw");
            } catch (Exception e) {
                Log.e(LOG_TAG, "File not found exception. Error:" + e);
            }
//            Log.i(MainActivity.class.getName(), audioUri.getPath());
//            extAudioRecorder.setOutputFile(mFileName, file);
//            extAudioRecorder.prepare();
//            extAudioRecorder.start();
//
//            // below method is used to initialize
//            // the media recorder class
            mRecorder = new MediaRecorder();
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            // below method is used to set the audio
            // source which we are using a mic.
            if(audio.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == null) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            } else {
                Log.i(LOG_TAG, "Using Unprocessed source.");
//                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
            }

            // below method is used to set
            // the output format of the audio.
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // below method is used to set the
            // audio encoder for our recorded audio.
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(320000);
            mRecorder.setAudioSamplingRate(96000);

            // below method is used to set the
            // output file location for our recorded audio
            if(file != null) mRecorder.setOutputFile(file.getFileDescriptor());
            try {
                // below method will prepare
                // our audio recorder class
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            // start method will start the audio recording
//            mRecorder.start();

            statusTV.setText("Prepared Recorder");
        } else {
            // if audio recording permissions are
            // not granted by user below method will
            // ask for runtime permission for mic and storage.
            RequestPermissions();
            prepareRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private void startPlaying() {
        preparePlayAudio();
        playFileNameIdx = (playFileNameIdx + 1) % playFileName.length;
        mPlayer.start();

        playTimer = new CountDownTimer(playTime, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                String dis = "Playing: " + (playTime - millisUntilFinished) + " ms";
                statusTV.setText(dis);
            }

            @Override
            public void onFinish() {
                if (stopPressed) {
                    return;
                }

                mPlayer.stop();
                mPlayer.release();
                startRecording();
            }
        };
        playTimer.start();
    }

    private void startRecording() {
//        try {
//            mRecorder.resume();
//        } catch (IllegalStateException e) {
//            if (stopPressed) {
//                return;
//            } else mRecorder.start();
//        }
        prepareRecording();
        mRecorder.start();

        recordTimer = new CountDownTimer(recordTime, 1) {
            int idx;
            @Override
            public void onTick(long millisUntilFinished) {
                String dis = "Recording: " + (recordTime - millisUntilFinished) + " ms";
                statusTV.setText(dis);
            }
            @Override
            public void onFinish() {
                if(stopPressed) {
                    return;
                }

                stopRecording();
                startPlaying();
            }
        };
        recordTimer.start();
    }

    private void startExperiment() {
//        preparePlayAudio();
//        prepareRecording();

        startPlaying();
//        startRecording();
    }

    public void preparePlayAudio() {
//        stopPlayTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
//        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
        playTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
        stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));

        // for playing our recorded audio
        // we are using media player class.
        mPlayer = new MediaPlayer();
        try {
            // below method is used to set the
            // data source which will be our file name
            mPlayer.setDataSource(playFileName[playFileNameIdx]);

            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
//            mPlayer.start();
            statusTV.setText("Prepared Audio");
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }

    public void stopRecording() {
        try{
            // below method will stop
            // the audio recording.
            Log.i(LOG_TAG, "Recorded: " + ts + "-" + recordNo);
            mRecorder.stop();
//            extAudioRecorder.stop();
//            extAudioRecorder.release();

            // below method will release
            // the media recorder class.
            mRecorder.release();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e);
        }
        mRecorder = null;

        stopTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
//        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
//        stopPlayTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
        statusTV.setText("Recording Stopped");
    }

    public void stopPlaying() {
        // this method will release the media player
        // class and pause the playing of our recorded audio.
        try {
            mPlayer.stop();
            mPlayer.release();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e);
        }
        mPlayer = null;

        stopTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
//        startTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
        playTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getApplicationContext().getTheme()));
//        stopPlayTV.setBackgroundColor(getResources().getColor(R.color.gray, this.getApplicationContext().getTheme()));
        statusTV.setText("Audio Play Stopped");
    }
}

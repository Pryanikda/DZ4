package com.example.dz4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity  implements Runnable{


    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private boolean wasPlaying = false;
    private FloatingActionButton fabPlayPause;
    private TextView seekBarHint;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fabPlayPause = findViewById(R.id.fabPlayPause);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);


        fabPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);


                int timeTrack = (int) Math.ceil(progress/1000f);


                if (timeTrack < 10) {
                    seekBarHint.setText("00:0" + timeTrack);
                } else if (timeTrack < 60){
                    seekBarHint.setText("00:" + timeTrack);
                } else if (timeTrack >= 60) {
                    seekBarHint.setText("01:" + (timeTrack - 60));
                }


                double percentTrack = progress / (double) seekBar.getMax();
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.92));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    MainActivity.this.seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }


    public void playSong() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {


                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                mediaPlayer.pause();

                wasPlaying = true;

                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                clearMediaPlayer();


            }


            if (!wasPlaying) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }


                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));

                AssetFileDescriptor descriptor = getAssets().openFd("Н.А.Римский-Корсаков - Полёт шмеля.mp3");

                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start(); // старт mediaPlayer
                new Thread(this).start(); // запуск дополнительного потока
            }

            wasPlaying = false; // возврат отсутствия проигрывания mediaPlayer

        } catch (Exception e) { // обработка исключения на случай отстутствия файла
            e.printStackTrace(); // вывод в консоль сообщения отсутствия файла
        }
    }

    // при уничтожении активити вызов метода остановки и очиски MediaPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    // метод остановки и очиски MediaPlayer
    private void clearMediaPlayer() {
        mediaPlayer.stop(); // остановка медиа
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление mediaPlayer
    }

    // метод дополнительного потока для обновления SeekBar
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); // считывание текущей позиции трека
        int total = mediaPlayer.getDuration(); // считывание длины трека

        // бесконечный цикл при условии не нулевого mediaPlayer, проигрывания трека и текущей позиции трека меньше длины трека
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {

                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mediaPlayer.getCurrentPosition(); // обновление текущей позиции трека

            } catch (InterruptedException e) { // вызывается в случае блокировки данного потока
                e.printStackTrace();
                return; // выброс из цикла
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition); // обновление seekBar текущей позицией трека

        }
    }
}
package com.fcxy.the.metronome;

import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends Activity {

    public static AudioTrack AudioTrackr1=null;
    public static PlayThread plthread=null;
    
    public static byte [] raw_c1=null;
    public static byte [] raw_c2=null;
    
    public static byte [] raw_d1=null;
    public static byte [] raw_d2=null;
    
    public static byte [] raw_w1=null;
    public static byte [] raw_w2=null;
    
    public static byte [] raw_1=null;
    public static byte [] raw_2=null;
    
    public static byte [] white=null;
    
    int bpm=60;
    int currtick=0;
    int beatperbar = 4;
    int beatbase = 4;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        int minSize =AudioTrack.getMinBufferSize( 48000, 
        		AudioFormat.CHANNEL_OUT_MONO, 
        		AudioFormat.ENCODING_PCM_16BIT );
        
        AudioTrackr1 = new AudioTrack(AudioManager.STREAM_MUSIC, 
        		48000, AudioFormat.CHANNEL_OUT_MONO, 
        		AudioFormat.ENCODING_PCM_16BIT, minSize*4, 
        		AudioTrack.MODE_STREAM);
        
        white=new byte[500000];
        
        AssetManager mgr = getAssets();
    	try {
			InputStream c1=mgr.open("crisp_1.raw");
			raw_c1=new byte[28800];
			c1.read(raw_c1, 0, c1.available());
			
			c1=mgr.open("crisp_2.raw");
			raw_c2=new byte[28800];
			c1.read(raw_c2, 0, c1.available());
			
			c1=mgr.open("drum_1.raw");
			raw_d1=new byte[28800];
			c1.read(raw_d1, 0, c1.available());
			
			c1=mgr.open("drum_2.raw");
			raw_d2=new byte[28800];
			c1.read(raw_d2, 0, c1.available());
			
			c1=mgr.open("wave_1.raw");
			raw_w1=new byte[28800];
			c1.read(raw_w1, 0, c1.available());
			
			c1=mgr.open("wave_2.raw");
			raw_w2=new byte[28800];
			c1.read(raw_w2, 0, c1.available());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		raw_1=raw_c1;
		raw_2=raw_c2;
		
		Button buttonkill = (Button) findViewById(R.id.button1);
		final Button buttontoggle = (Button) findViewById(R.id.Button01);
		
		final EditText ebpm = (EditText) findViewById(R.id.editText1);
		final EditText ebeatperbar = (EditText) findViewById(R.id.editText2);
		final EditText ebeatbase = (EditText) findViewById(R.id.EditText01);
		
		RadioButton buttonm1= (RadioButton) findViewById(R.id.radio0);
		RadioButton buttonm2= (RadioButton) findViewById(R.id.radio1);
		RadioButton buttonm3= (RadioButton) findViewById(R.id.radio2);
		
		buttonm1.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked){
					raw_1=raw_c1;
					raw_2=raw_c2;
				}
			}
		});
		
		buttonm2.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked){
					raw_1=raw_d1;
					raw_2=raw_d2;
				}
			}
		});
		
		buttonm3.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked){
					raw_1=raw_w1;
					raw_2=raw_w2;
				}
			}
		});
		
		buttontoggle.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				if (plthread!=null){
					plthread.cancel();
					plthread=null;
					currtick=0;
					synchronized(this){
						try {
							this.wait(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					AudioTrackr1.stop();
					buttontoggle.setText("Start!");
				}else{
					AudioTrackr1.play();
					plthread=new PlayThread();
					plthread.start();
					buttontoggle.setText("Stop!");
				}
			}
		});
		
        buttonkill.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		bpm=Integer.parseInt(ebpm.getText().toString());
        		int newbeat=Integer.parseInt(ebeatperbar.getText().toString());
        		int newbase=Integer.parseInt(ebeatbase.getText().toString());
        		
        		if (bpm<=15){
        			bpm=15;
        			ebpm.setText("15");
        		}
        		if (bpm>=240){
        			bpm=240;
        			ebpm.setText("240");
        		}
        		
        		if ((newbeat!=beatperbar) || (newbase!=beatbase)){
        			beatperbar=newbeat;
        			beatbase=newbase;
        			currtick=0;
        		}
        		
        	}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    class PlayThread extends Thread {
        private boolean running=true;

		public PlayThread() {
        }
     
        public void run() {
        	
        	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        	
        	while (running){
        		int samplength=28800;
        		if (bpm>200){
        			samplength=24000;
        		}else{
        			samplength=28800;
        		}
        		
        		long start=System.nanoTime();
        		if (currtick%beatperbar==0){
        			AudioTrackr1.write(raw_1, 0, samplength);
        		}else{
        			AudioTrackr1.write(raw_2, 0, samplength);
        		}
        		start=System.nanoTime()-start;
        		if (((double)samplength/2.0/48000.0)<((double)start)/1000000000.0){
        			samplength+=(int)(((double)start)/1000000000.0*48000.0*2.0);
        			samplength/=2;
        			Log.d("metro", "Samplength:"+samplength);
        		}
        		
        		//Log.d("metro", "Elapsed:"+start);
        		
    			AudioTrackr1.write(white, 0, (int)((double)48000*2.0*60.0/(double)bpm)-samplength);
    			currtick++;
        	}
        }
     
        public void cancel() {
        	running=false;
        }
    }

}

package com.temp.tempreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnItemClickListener {
	
	//edit
    TextView timeView;
    TextView temperatureView;
    String mainText,tempText,timeText;
    String prevText = "0000000000";
    
    ArrayAdapter<String> listAdapter;
    //Button connectNew;
    ListView listView;
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	IntentFilter filter;
	BroadcastReceiver receiver;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ  = 1;
	String tag = "debugging";
	Handler mHandler = new Handler(){
		
    	@Override
    	public void handleMessage(Message msg) {

    		super.handleMessage(msg);
    		switch(msg.what){
    		case SUCCESS_CONNECT:
    			//Do something
    			ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
    			Toast.makeText(getApplicationContext(), "CONNECTED", 0).show();
    			String s = "s";
    			connectedThread.write(s.getBytes());
    			Log.i(tag, "connected"); 
    			break;
    		case MESSAGE_READ:
    			//byte[] readBuf = (byte[])msg.obj;
    			//String string = new String (readBuf);
    	        String readMessage = (String) msg.obj;
    	        
    			mainText = readMessage;
    			if (mainText.length()>5){
        			displayRes(mainText);
        			prevText =mainText;

    			}
    			else{
        			displayRes(prevText);

    			}
    			
    			break;
    		}
    	}


    };
    
	private void displayRes(String mainText) {
		// TODO Auto-generated method stub
		//Toast.makeText(getApplicationContext(), string , 0).show();
		tempText = mainText.substring(0,mainText.indexOf(".")+2);
		timeText = mainText.substring(mainText.indexOf(".")+3);
		
		temperatureView = (TextView) findViewById(R.id.temperatureView);
		temperatureView.setText(tempText+" 'C");
		timeView = (TextView)findViewById(R.id.timeView);
		timeView.setText(timeText);

	}

	private ConnectedThread mConnectedThread;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if (btAdapter==null){
        	Toast.makeText(getApplicationContext(), "No bluetooth adapter detected", 0).show();
        	finish();
        }
        else{
        	 if (!btAdapter.isEnabled()){
        		 turnOnBT();	  
        	 }
        	 getPairedDevices();
             startDiscovery();
        }
        
        
    }


    private void startDiscovery() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}


	private void turnOnBT() {
		// TODO Auto-generated method stub
    	Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent,1);
	}


	private void getPairedDevices() {
		// TODO Auto-generated method stub
		devicesArray = btAdapter.getBondedDevices();
		if (devicesArray.size()>0){
			for(BluetoothDevice device:devicesArray){
				pairedDevices.add(device.getName());
			}	
		}
	}


	private void init() {
		// TODO Auto-generated method stub
		//connectNew = (Button)findViewById(R.id.bConnectNew);
		listView = (ListView)findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,0);
		listView.setAdapter(listAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = new ArrayList<String>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		devices = new ArrayList<BluetoothDevice>();
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action)){
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
						//for(int i = 0;i<listAdapter.getCount();i++){
							for (int a = 0;a<pairedDevices.size();a++){
								if(device.getName().equals(pairedDevices.get(a))){
									//append
									s = "(Paired)";
									break;
								}
							}
						//}
						//ex:matt_hp (paired)
					listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());

					
				}
				else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
					//run some code
				}
				else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

				}
				else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
					if (btAdapter.getState() == btAdapter.STATE_OFF){
						turnOnBT();
					}
				}
			}
		};
		
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);


	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==RESULT_CANCELED){
			Toast.makeText(getApplicationContext(),"Bluetooth must be enabled to continue",Toast.LENGTH_SHORT).show();
			finish();
		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if(btAdapter.isDiscovering()){
			btAdapter.cancelDiscovery();
		}
		if(listAdapter.getItem(arg2).contains("Paired")){
			BluetoothDevice selectedDevice = devices.get(arg2);
			//Toast.makeText(getApplicationContext(), "device is paired",0).show();	
			ConnectThread connect = new ConnectThread(selectedDevice);
			connect.start();
			Log.i(tag,"in click listner");
			
			//edit
			//ConnectedThread connected = new ConnectedThread(selectedDevice);
			
		}
		else {
			Toast.makeText(getApplicationContext(), "device is not paired",0).show();
		}
		
	}
	
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery beca use it will slow down the connection
	        btAdapter.cancelDiscovery();
	        Log.i(tag,"connect-run");
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	            Log.i(tag, "connect - succeeded");
	        } catch (IOException connectException) { Log.i(tag, "connect - failed");
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        mHandler.obtainMessage(SUCCESS_CONNECT,mmSocket).sendToTarget();
	        connected(mmSocket);//edit
	    }
	 


		/** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer;  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	            	buffer  = new byte[1024];
	                bytes = mmInStream.read(buffer);
	                String readMessage = new String(buffer, 0, bytes);

	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, readMessage) //readMessage replaces buffer
	                        .sendToTarget();
	                
	            } catch (IOException e) {
	                break;
	            }
	           SystemClock.sleep(300);

	        }

	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	} 
	//edit
	public synchronized void connected(BluetoothSocket socket) {

	    mConnectedThread = new ConnectedThread(socket);
	    mConnectedThread.start();


	}
	

    
}

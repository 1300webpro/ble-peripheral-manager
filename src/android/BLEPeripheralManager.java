package com.nge.BLEPeripheralManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import java.util.UUID; 
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import android.os.ParcelUuid;
import android.content.Context;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;


/**
* This class echoes a string called from JavaScript.
*/
public class BLEPeripheralManager extends CordovaPlugin {

    private static final String TAG = "BLEPeripheralManager";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer mGattServer;
    private AdvertiseData mAdvData;
    private AdvertiseSettings mAdvSettings;
    private BluetoothLeAdvertiser mAdvertiser;
    
    private ArrayList<BluetoothGattService> bleServices;
    private Map<BluetoothGattCharacteristic, CallbackContext> monitorCharacteristics;
    
    private Context context;
    
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
          /*super.onConnectionStateChange(device, status, newState);
          if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
              mBluetoothDevices.add(device);
              updateConnectedDevicesStatus();
              Log.v(TAG, "Connected to device: " + device.getAddress());
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
              mBluetoothDevices.remove(device);
              updateConnectedDevicesStatus();
              Log.v(TAG, "Disconnected from device");
            }
          } else {
            mBluetoothDevices.remove(device);
            updateConnectedDevicesStatus();
            // There are too many gatt errors (some of them not even in the documentation) so we just
            // show the error to the user.
            final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(Peripheral.this, errorMessage, Toast.LENGTH_LONG).show();
              }
            });
            Log.e(TAG, "Error when connecting: " + status);
          }*/
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
            BluetoothGattCharacteristic characteristic) {
          super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
          Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
          if (offset != 0) {
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                /* value (optional) */ null);
            return;
          }
          mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
              offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
          super.onNotificationSent(device, status);
          Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
            BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
            int offset, byte[] value) {
          super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
              responseNeeded, offset, value);
          if (offset != 0) {
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                /* value (optional) */ null);
            return;
          }
          
            String valueToWrite = new String(value);
            int status = characteristic.setValue(valueToWrite)?1:0;

            if(monitorCharacteristics != null){
                CallbackContext callback = monitorCharacteristics.get(characteristic);
                if(callback != null){
                    callback.success("{\""+characteristic.getUuid().toString()+"\":\""+valueToWrite+"\"}");
                }
            }
            
          if (responseNeeded) {
            mGattServer.sendResponse(device, requestId, status,
                /* No need to respond with an offset */ 0,
                /* No need to respond with a value */ null);
          }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
            BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
            int offset,
            byte[] value) {
          Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid());
          super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
              offset, value);
          if(responseNeeded) {
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                /* No need to respond with offset */ 0,
                /* No need to respond with a value */ null);
          }
        }
    };
    
    
    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Not broadcasting: " + errorCode);
            switch (errorCode) {
              case ADVERTISE_FAILED_ALREADY_STARTED:
                Log.w(TAG, "App was already advertising");
                break;
              case ADVERTISE_FAILED_DATA_TOO_LARGE:
                Log.w(TAG, "ADVERTISE_FAILED_DATA_TOO_LARGE");
                break;
              case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                Log.w(TAG, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
                break;
              case ADVERTISE_FAILED_INTERNAL_ERROR:
                Log.w(TAG, "ADVERTISE_FAILED_INTERNAL_ERROR");
                break;
              case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                Log.w(TAG, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
                break;
              default:
                Log.wtf(TAG, "Unhandled error: " + errorCode);
            }
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v(TAG, "Broadcasting");
        }
    };

    
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // your init code here
        
        context = cordova.getActivity().getApplicationContext();
        //Intent intent=new Intent(context,Next_Activity.class);
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        
        bleServices = new ArrayList<BluetoothGattService>();
        
        monitorCharacteristics = new HashMap<BluetoothGattCharacteristic, CallbackContext>();

        
    }

    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("addService")) {
            Log.v(TAG, "addService: " + args.getString(0));
            this.addService(new JSONObject(args.getString(0)), callbackContext);
            return true;
        } else if (action.equals("startAdvertising")) {
            this.startAdvertising(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("stopAdvertising")) {
            this.stopAdvertising(callbackContext);
            return true;
        } else if (action.equals("removeAllServices")) {
            this.removeAllServices(callbackContext);
            return true;
        } else if (action.equals("changeCharacteristic")) {
            String uuid = args.getString(0);
            String value = args.getString(1);
            this.changeCharacteristic(uuid,value, callbackContext);
            return true;
        } else if (action.equals("monitorCharacteristic")) {
            String uuid = args.getString(0);
            this.monitorCharacteristic(uuid,callbackContext);
            return true;
        }

        return false;
    }

    private void addService(JSONObject service, CallbackContext callbackContext) {
        if (service != null && service.length() > 0) {
            Log.v(TAG, "addService: " + service.toString());
            try {
                
                if(characteristic.getBool('primary')){
                    int service_type = BluetoothGattService.SERVICE_TYPE_PRIMARY;
                } else {
                    int service_type = BluetoothGattService.SERVICE_TYPE_SECONDARY;
                }
                
                BluetoothGattService bleService =
                    new BluetoothGattService(UUID.fromString(service.getString("UUID")), service_type);
                
                JSONArray characteristics = service.getJSONArray("characteristics");
                for(int i =0; i < characteristics.length(); i++){
                    JSONObject characteristic = new JSONObject(characteristics.getString(i));
                    
                    if(characteristic.getBool('write')){
                        int prop = BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE;
                        int perm = BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE;
                    } else {
                        int prop = BluetoothGattCharacteristic.PROPERTY_READ;
                        int perm = BluetoothGattCharacteristic.PERMISSION_READ;
                    }
                    
                    BluetoothGattCharacteristic bleCharacteristic =
                        new BluetoothGattCharacteristic(UUID.fromString(characteristic.getString("UUID")),prop,perm);

                    bleCharacteristic.setValue(characteristic.getString("value"));
                    
                    bleService.addCharacteristic(bleCharacteristic);
                }
                
                
                bleServices.add(bleService);
                
            } catch (JSONException e) {
                callbackContext.error("Missing elements: "+e.getMessage());
            }

            callbackContext.success("Service added");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
  
    private void startAdvertising(String device_name, CallbackContext callbackContext) {

        mGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);
        if (mGattServer == null) {
            callbackContext.error("BLE Server not started");
            return;
        }

        mAdvSettings = new AdvertiseSettings.Builder()
          .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
          .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
          .setConnectable(true)
          .build();
        
        
        AdvertiseData.Builder mAdvDataBuilder  = new AdvertiseData.Builder()
          .setIncludeDeviceName(false)
          .setIncludeTxPowerLevel(true);
        
        // Add a service for a total of three services (Generic Attribute and Generic Access
        // are present by default).
        for(int i =0; i < bleServices.size(); i++){
            BluetoothGattService bleService = bleServices.get(i);
            mGattServer.addService(bleService);
            
            ParcelUuid uuid = new ParcelUuid(bleService.getUuid());
            
            mAdvDataBuilder.addServiceUuid(uuid);
        }
        
        mAdvData = mAdvDataBuilder.build();
        
        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);
        
        callbackContext.success("BLE Started");
        
    }
  
    private void stopAdvertising(CallbackContext callbackContext) {
        if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
            // If stopAdvertising() gets called before close() a null
            // pointer exception is raised.
            mAdvertiser.stopAdvertising(mAdvCallback);
        }
        
        callbackContext.success("BLE Stopped");
    }
  
    private void removeAllServices(CallbackContext callbackContext) {
        bleServices = new ArrayList<BluetoothGattService>();
        callbackContext.success("Services Removed");
    }
  
    private void changeCharacteristic(String uuid, String value, CallbackContext callbackContext) {
        for(int i =0; i < bleServices.size(); i++){
            BluetoothGattService bleService = bleServices.get(i);
            
            BluetoothGattCharacteristic characteristic = bleService.getCharacteristic(UUID.fromString(uuid));
            if(characteristic != null){
                characteristic.setValue(value);
                callbackContext.success("Characteristic changed");
                return;
            }
        }
        
        callbackContext.error("Not Found");
    }
  
    private void monitorCharacteristic(String uuid, CallbackContext callbackContext) {

        for(int i =0; i < bleServices.size(); i++){
            BluetoothGattService bleService = bleServices.get(i);
            
            BluetoothGattCharacteristic characteristic = bleService.getCharacteristic(UUID.fromString(uuid));
            if(characteristic != null){
                monitorCharacteristics.put(characteristic, callbackContext);
                return;
            }
        }
        
        callbackContext.error("Not Found");
        
    }

}
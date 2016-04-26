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
import java.util.Iterator;


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
    
    private ArrayList bleServices;
    
    /*private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
          super.onConnectionStateChange(device, status, newState);
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
          }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
            BluetoothGattCharacteristic characteristic) {
          super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
          Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
          Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
          if (offset != 0) {
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                /* value (optional) *//* null);
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
          Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
          int status = mCurrentServiceFragment.writeCharacteristic(characteristic, offset, value);
          if (responseNeeded) {
            mGattServer.sendResponse(device, requestId, status,
                /* No need to respond with an offset */// 0,
                /* No need to respond with a value */ /*null);
          }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
            BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
            int offset,
            byte[] value) {
          Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
          super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
              offset, value);
          if(responseNeeded) {
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                /* No need to respond with offset */// 0,
                /* No need to respond with a value */ /*null);
          }
        }
    };*/

    
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // your init code here
        
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        
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
                
                BluetoothGattService bleService =
                    new BluetoothGattService(UUID.fromString(service.getString("UUID")), BluetoothGattService.SERVICE_TYPE_PRIMARY);
                
                JSONArray characteristics = service.getJSONArray("characteristics");
                for(int i =0; i < characteristics.length(); i++){
                    JSONObject characteristic = new JSONObject(characteristics.getString(i));

                    BluetoothGattCharacteristic bleCharacteristic =
                        new BluetoothGattCharacteristic(
                            UUID.fromString(service.getString("UUID")),
                            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                            BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

                    bleService.addCharacteristic(bleCharacteristic);
                }
                
                
                bleServices.add(bleService);
                
            } catch (JSONException e) {
                callbackContext.error("Missing elements: "+e.getMessage());
            }

            callbackContext.success("Yay!");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
        
        
        
        
/*

    mCharacteristic.addDescriptor(
        Peripheral.getClientCharacteristicConfigurationDescriptor());
        */
  
    

    }
  
    private void startAdvertising(String device_name, CallbackContext callbackContext) {

        mGattServer = mBluetoothManager.openGattServer(this/*, mGattServerCallback*/);
        if (mGattServer == null) {
          callbackContext.error("BLE Server not started");
        }
        
        
        mAdvSettings = new AdvertiseSettings.Builder()
          .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
          .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
          .setConnectable(true)
          .build();
        
        // Add a service for a total of three services (Generic Attribute and Generic Access
        // are present by default).
        Iterator loop = bleServices.iterator();
        while( loop.hasNext() ){
            mGattServer.addService(loop.next());
        }

        
        mAdvData = new AdvertiseData.Builder()
          .setIncludeDeviceName(true)
          .setIncludeTxPowerLevel(true)
          .addServiceUuid(bleServices.get(0).getServiceUUID())
          .build();
        
        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertiser.startAdvertising(mAdvSettings, mAdvData);
    }
  
    private void stopAdvertising(CallbackContext callbackContext) {
        /*if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
            // If stopAdvertising() gets called before close() a null
            // pointer exception is raised.
            mAdvertiser.stopAdvertising(mAdvCallback);
        }*/
    }
  
    private void removeAllServices(CallbackContext callbackContext) {

    }
  
    private void changeCharacteristic(String uuid, String value, CallbackContext callbackContext) {

    }
  
    private void monitorCharacteristic(String uuid, CallbackContext callbackContext) {

    }

}
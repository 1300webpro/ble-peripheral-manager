package com.nge.BLEPeripheralManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

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


  private BluetoothGattService[] mServices;
  private BluetoothGattCharacteristic[] mCharacteristics;

  private BluetoothGattServer mGattServer;
  private AdvertiseData mAdvData;
  private AdvertiseSettings mAdvSettings;
  private BluetoothLeAdvertiser mAdvertiser;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  
      if (action.equals("addService")) {
          Log.v(TAG, "addService");
          JSONObject services = args.getJSONObject(0);
          this.addService(services, callbackContext);
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

  private void addService(JSONObject services, CallbackContext callbackContext) {
      if (services != null && services.length() > 0) {
          callbackContext.success("Yay!");
      } else {
          callbackContext.error("Expected one non-empty string argument.");
      }
      
      
      /*for( service in services){
          BluetoothGattService service =
            new BluetoothGattService(SERVICE_UUID, SERVICE_TYPE_PRIMARY);
            
          for ( characteristic in service.characteristics){
              BluetoothGattCharacteristic dimmerCharacteristic =
                new BluetoothGattCharacteristic(
                  DIMMER_UUID,
                  PROPERTY_READ | PROPERTY_WRITE,
                  PERMISSION_READ | PERMISSION_WRITE);
          }
      }*/
      
      


      
      
      /*
      
      for each service:

// GATT
private BluetoothGattService mServices;
private BluetoothGattCharacteristic mCharacteristics;

public ServiceFragment() {

private static final UUID BATTERY_SERVICE_UUID = UUID
      .fromString("0000180F-0000-1000-8000-00805f9b34fb");

  // for each characteristic
    mCharacteristic =
        new BluetoothGattCharacteristic(BATTERY_LEVEL_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ);

    mCharacteristic.addDescriptor(
        Peripheral.getClientCharacteristicConfigurationDescriptor());
        
  //Add the service

  mService = new BluetoothGattService(BATTERY_SERVICE_UUID,
      BluetoothGattService.SERVICE_TYPE_PRIMARY);
      
      
  //Add the characteristics to the service
  mService.addCharacteristic(mCharacteristic);
  
  
  
  
  
  mAdvSettings = new AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        .setConnectable(true)
        .build();
    mAdvData = new AdvertiseData.Builder()
        .setIncludeDeviceName(true)
        .setIncludeTxPowerLevel(true)
        .addServiceUuid(mService.getServiceUUID())
        .build();
  
  
}
      */
      
  }
  
  private void startAdvertising(String device_name, CallbackContext callbackContext) {
      

      /*mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
      if (mGattServer == null) {
        ensureBleFeaturesAvailable();
        return;
      }
      // Add a service for a total of three services (Generic Attribute and Generic Access
      // are present by default).
      mGattServer.addService(mService);

      mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
      mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);*/
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
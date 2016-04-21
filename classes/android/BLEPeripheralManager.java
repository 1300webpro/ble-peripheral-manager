package org.apache.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

  


  private BluetoothGattService[] mServices;
  private BluetoothGattCharacteristic[] mCharacteristics;

  private BluetoothGattServer mGattServer;
  private AdvertiseData mAdvData;
  private AdvertiseSettings mAdvSettings;
  private BluetoothLeAdvertiser mAdvertiser;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  
      if (action.equals("addService")) {
      
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
          this.monitorCharacteristic(callbackContext);
          return true;
      }
      
      return false;
  }

  private void addService(String message, CallbackContext callbackContext) {
      if (message != null && message.length() > 0) {
          callbackContext.success(message);
      } else {
          callbackContext.error("Expected one non-empty string argument.");
      }
  }
  
  private void startAdvertising(String message, CallbackContext callbackContext) {
      

      mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
      if (mGattServer == null) {
        ensureBleFeaturesAvailable();
        return;
      }
      // Add a service for a total of three services (Generic Attribute and Generic Access
      // are present by default).
      mGattServer.addService(mService);

      mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
      mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);
  }
  
  private void stopAdvertising(String message, CallbackContext callbackContext) {
      if (mGattServer != null) {
          mGattServer.close();
      }
      if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
          // If stopAdvertising() gets called before close() a null
          // pointer exception is raised.
          mAdvertiser.stopAdvertising(mAdvCallback);
      }
  }
  
  private void removeAllServices(String message, CallbackContext callbackContext) {
  
  }
  
  private void changeCharacteristic(String message, CallbackContext callbackContext) {
  
  }
  
  private void monitorCharacteristic(String message, CallbackContext callbackContext) {
  
  }

}
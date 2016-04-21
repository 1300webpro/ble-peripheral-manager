# BLE Peripheral Manager Plugin for PhoneGap

This plugin is only for Bluetooth Peripheral Manager functionality. If you're looking for Central Manager functionality check out https://github.com/randdusing/BluetoothLE.

## Methods

### blePM.addService(service, successCallback)

Creates a service from an object you pass. All object parameters are required. You may call this multiple times to add multiple services, but all services must be added before you start advertising. This method is asynchronous, so pass a function to the success callback for any code you may need to run after the service has been added, which includes the startAdvertising method.

#### Parameters:

- UUID: A UUID string. Create by running 'uuidgen' on the command line.
- primary: True or false as to whether this is the primary service for your app. True if you are only running one service.
- characteristics: An array of characteristics for this service. Each characteristic has two parameters, UUID and value, that are both strings. Characteristic values must be a string, not null, an array or an object. See example.

### blePM.startAdvertising('deviceName', successCallback)

Begin advertising. This must be called after at least one service has been created, so make sure to run this in the callback of the addService method. Pass in any string to represent the name of your device. This method is asynchronous, so pass a function to the success callback for any code you may need to run after advertising has begun.

### blePM.stopAdvertising()

Stops all advertising.

### blePM.removeAllServices()

Removes all services from a class. 

### blePM.changeCharacteristic(characteristic_uuid, value, successCallback, errorCallback)

Change the value of the characteristic after it has started.

### blePM.monitorCharacteristic(characteristic_uuid, successCallback, errorCallback)

Attach a function to monitor the characteristic. successCallback accepts 2 arguments which is the characteristic_uuid and the new value

## Example Usage

	// Setup a service object. Generate your own UUIDs by running 'uuidgen' on the command line.
	var service = {
		UUID: 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX',
		primary: true,
		characteristics: [
			{
				UUID: 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX',
				value: 'characteristic1Value'
			},
			{
				UUID: 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX',
				value: 'characteristic2Value'
			}
		]
	};

	// Add your service
	blePM.addService(service, function success() {
		// Start advertising services
		blePM.startAdvertising('aNameForYourDevice', function success() {
			console.log('Advertising!');
		});
	}

	// Stop advertising when finished
	blePM.stopAdvertising();

	// Remove all services
	blePM.removeAllServices();
var blePM = (function() {

    /**
    * Private Variables
    **/

    /*var BLEStates = [
            'CBPeripheralManagerStateUnknown',
            'CBPeripheralManagerStateResetting',
            'CBPeripheralManagerStateUnsupported',
            'CBPeripheralManagerStateUnauthorized',
            'CBPeripheralManagerStatePoweredOff',
            'CBPeripheralManagerStatePoweredOn'
        ],
        state;
*/

    /**
    * Public Methods
    **/

    function addService(service, successCallback, errorCallback) {
        var jsonService = JSON.stringify(service);

        cordova.exec(
            function success(data) {
                if(successCallback) {
                    successCallback(data);
                }
            
                /*function serviceAdded() {
                    console.log('service added');
                    unsubscribe(subscription);
                    if(successCallback) {
                        successCallback();
                    }
                }*/

                //var subscription = subscribe('didAddService', serviceAdded);
            },
            function error(err) {
                if(errorCallback) {
                    errorCallback(err);
                }
            },
            'BLEPeripheralManager',
            'addService',
            [jsonService]
        );
    }
/*
    function getState() {
        return state;
    }

    function logState(BLEstate) {
        state = BLEStates[BLEstate];
        console.log('BLE state: ', state);
    }
*/
    function removeAllServices(successCallback, errorCallback) {
        cordova.exec(
            function success(data) {
                if (successCallback) {
                    successCallback(data);
                }
            },
            function error(err) {
                if(errorCallback) {
                    errorCallback(err);
                }
            },
            'BLEPeripheralManager',
            'removeAllServices',
            []
        );
    }

    function startAdvertising(localNameKey, successCallback, errorCallback) {
        var localNameKey = localNameKey ? localNameKey : 'missing-service-name';

        cordova.exec(
            function success(data) {
                if (successCallback) {
                    successCallback(data);
                }
            },
            function error(err) {
                if(errorCallback) {
                    errorCallback(err);
                }
            },
            'BLEPeripheralManager',
            'startAdvertising',
            [localNameKey]
        );
    }

    function stopAdvertising(successCallback, errorCallback) {
        cordova.exec(
            function success(data) {
                if (successCallback) {
                    successCallback(data);
                }
            },
            function error(err) {
                if(errorCallback) {
                    errorCallback(err);
                }
            },
            'BLEPeripheralManager',
            'stopAdvertising',
            []
        );
    }
    
    function changeCharacteristic(characteristic_uuid, value, successCallback, errorCallback) {
        var characteristic_uuid = characteristic_uuid ? characteristic_uuid : '';
        var value = value ? value : '';
    
        if(characteristic_uuid == ''){
            if(errorCallback) {
                errorCallback(err);
            }
            return;
        }
    
        cordova.exec(
            function success(data) {
                if (successCallback) {
                    successCallback(data);
                }
            },
            function error(err) {
                if(errorCallback) {
                    errorCallback(err);
                }
            },
            'BLEPeripheralManager',
            'changeCharacteristic',
            [characteristic_uuid,value]
        );
    }
    

    function monitorCharacteristic(characteristic_uuid,onChangeCallback, successCallback, errorCallback) {
        var characteristic_uuid = characteristic_uuid ? characteristic_uuid : '';
    
        if(characteristic_uuid == ''){
            if(errorCallback) {
                errorCallback(err);
            }
            return;
        }
    
        cordova.exec(
            function success(data) {
                if (successCallback) {
                    successCallback(data);
                }
            },
            function error(err) {
                if(errorCallback) {
                    errorCallback(err);
                }
            },
            'BLEPeripheralManager',
            'changeCharacteristic',
            [characteristic_uuid]
        );
    }
    

    /**
    * Pub/Sub Implementation
    **/
/*
    var topics = {};
    var subUid = -1;

    function publish(topic, args) {
        if ( !topics[topic] ) {
            return false;
        }

        var subscribers = topics[topic],
            len = subscribers ? subscribers.length : 0;

        while (len--) {
            subscribers[len].func( topic, args );
        }

        return this;
    }

    function subscribe(topic, func) {
        if (!topics[topic]) {
            topics[topic] = [];
        }

        var token = ( ++subUid ).toString();
        topics[topic].push({
            token: token,
            func: func
        });
        return token;
    }

    function unsubscribe(token) {
        for ( var m in topics ) {
            if ( topics[m] ) {
                for ( var i = 0, j = topics[m].length; i < j; i++ ) {
                    if ( topics[m][i].token === token ) {
                        topics[m].splice( i, 1 );
                        return token;
                    }
                }
            }
        }
        return this;
    }
*/
    return {
        addService: addService,
        //getState: getState,
        removeAllServices: removeAllServices,
        startAdvertising: startAdvertising,
        stopAdvertising: stopAdvertising,
        changeCharacteristic: changeCharacteristic,
        monitorCharacteristic: monitorCharacteristic

        // Public for the purposes of calling from Objective-C
        //logState: logState,
        //publish: publish
    };

})();

module.exports = blePM;
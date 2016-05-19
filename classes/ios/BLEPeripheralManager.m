//
//  BLEPeripheralManager.m
//  BLEPeripheralManager
//
//  Created by Nick Stevens on 3/6/14.
//
//

#import "BLEPeripheralManager.h"

@implementation BLEPeripheralManager {
    CBPeripheralManager *_peripheralManager;
    NSMutableDictionary *monitorList;
    NSMutableArray *serviceList;
}

- (void)pluginInitialize {
    [super pluginInitialize];
    
    _peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
    monitorList = [[NSMutableDictionary alloc] init];
    serviceList = [[NSMutableArray alloc] init];
    
}

- (void)addService:(CDVInvokedUrlCommand *)command {
    NSMutableArray *characteristics = [[NSMutableArray alloc] init];
    
    NSError *error = nil;
    NSData *jsonService = [[command.arguments objectAtIndex:0] dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *serviceArgs = (NSDictionary*)[NSJSONSerialization JSONObjectWithData:jsonService options:kNilOptions error:&error];
    
    for (NSDictionary *characteristicArgs in [serviceArgs valueForKey:@"characteristics"]) {
        //NSString *value = [characteristicArgs objectForKey:@"value"];
        //NSData *dataValue = [value dataUsingEncoding:NSUTF8StringEncoding];
        
        int permissions;
        int properties;
        if([characteristicArgs objectForKey:@"write"]){
            permissions = CBAttributePermissionsReadable | CBAttributePermissionsWriteable;
            properties = CBCharacteristicPropertyRead | CBCharacteristicPropertyWrite;
        } else {
            permissions = CBAttributePermissionsReadable;
            properties = CBCharacteristicPropertyRead;
        }
        
        CBMutableCharacteristic *characteristic = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:[characteristicArgs objectForKey:@"UUID"]] properties:properties value:nil permissions:permissions];
        
        
        
        [characteristics addObject:characteristic];
        
        //characteristic.value = dataValue;
    }
    
    CBUUID *uuid = [CBUUID UUIDWithString:[serviceArgs valueForKey:@"UUID"]];
    BOOL primaryBool = [[NSNumber numberWithInt:(NSInteger)[serviceArgs valueForKey:@"primary"]] boolValue];
    
    CBMutableService *service = [[CBMutableService alloc] initWithType:uuid primary:primaryBool];
    
    NSLog(@"characteristics: %@", characteristics);
    
    //NSArray *immutableCharacteristics = [characteristics copy];
    
    service.characteristics = characteristics;
    
    [serviceList addObject:service];
    
    [_peripheralManager addService:service];
    
    
    for (NSDictionary *characteristicArgs in [serviceArgs valueForKey:@"characteristics"]) {
        for (CBMutableCharacteristic *characteristic in characteristics) {
            
            if ([characteristic.UUID isEqual: [CBUUID UUIDWithString:[characteristicArgs objectForKey:@"UUID"]]]) {
                NSString *value = [characteristicArgs objectForKey:@"value"];
                NSData *dataValue = [value dataUsingEncoding:NSUTF8StringEncoding];
                characteristic.value = dataValue;
                break;
            }
        }
    }
    
    
    
    
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)startAdvertising:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult;
    
    if (_peripheralManager.state == CBPeripheralManagerStatePoweredOn) {
        //CBMutableService *service = [serviceList objectAtIndex:0];, CBAdvertisementDataServiceUUIDsKey: service.UUID
        
        NSDictionary *advertisingData = @{CBAdvertisementDataLocalNameKey: [command.arguments objectAtIndex:0]};
        
        [_peripheralManager startAdvertising:advertisingData];
        
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)stopAdvertising:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult;

    [_peripheralManager stopAdvertising];
    
    pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)removeAllServices:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult;
    
    [_peripheralManager removeAllServices];
    
    //Reset the service list
    [serviceList removeAllObjects];
    
    pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)changeCharacteristic:(CDVInvokedUrlCommand *)command {
    
    CDVPluginResult *pluginResult;
    
    pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    //Find characteristic based on the uuid
    CBMutableCharacteristic *characteristic;
    
    
    
    
    NSString *uuidSearch = [command.arguments objectAtIndex:0];
    
    for (CBMutableService *service in serviceList) {
        for (CBMutableCharacteristic *characteristics in service.characteristics) {
            
            if ([characteristics.UUID isEqual: [CBUUID UUIDWithString:uuidSearch]]) {
                
                characteristic = characteristics;
                break;
            }
        }
    }
    
    //If the characteristic isn't null, then proceed with the update
    if(characteristic){
        
        NSString *value = [command.arguments objectAtIndex:1];// fetch the characteristic's new value as the second argument
        NSData *dataValue = [value dataUsingEncoding:NSUTF8StringEncoding];
        characteristic.value = dataValue;
        
        
        //Update the value with the new value
        //BOOL didSendValue = [_peripheralManager updateValue:dataValue forCharacteristic:characteristic onSubscribedCentrals:nil];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)monitorCharacteristic:(CDVInvokedUrlCommand *)command {
    
    NSString *uuidSearch = [command.arguments objectAtIndex:0];
    [monitorList setObject:command.callbackId forKey:[uuidSearch uppercaseString]];
    
}




#pragma mark - Peripheral Methods

- (void) peripheralManager:(CBPeripheralManager *)peripheral didAddService:(CBService *)service error:(NSError *)error {
    NSLog(@"Added service %@", [service UUID]);
    if (error) {
        NSLog(@"There was an error adding service");
        NSLog(@"%@", error);
    }
}

- (void) peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral error:(NSError *)error {
    NSLog(@"Started advertising");
    if (error) {
        NSLog(@"There was an error advertising");
        NSLog(@"%@", error);
    }
    
}




-(void)peripheralManager:(CBPeripheralManager *)peripheral didReceiveReadRequest:(CBATTRequest *)request {
    NSLog(@"Received read request for %@", [request characteristic]);
    
    // TODO real code should check offsets and handle errors
    /*if ([request.characteristic.UUID isEqual:_switchCharacteristic.UUID]) {
     
     request.value = [_switchCharacteristic.value
     subdataWithRange:NSMakeRange(request.offset,
     _switchCharacteristic.value.length - request.offset)];
     
     } else if ([request.characteristic.UUID isEqual:_dimmerCharacteristic.UUID]) {
     request.value = _dimmerCharacteristic.value;
     
     }*/
    for (CBMutableService *service in serviceList) {
        for (CBMutableCharacteristic *characteristics in service.characteristics) {
            
            if ([request.characteristic.UUID isEqual:characteristics.UUID]) {
                
                request.value = characteristics.value;
                break;
            }
        }
    }
    
    
    [_peripheralManager respondToRequest:request withResult:CBATTErrorSuccess];
}

-(void)peripheralManager:(CBPeripheralManager *)peripheral didReceiveWriteRequests:(NSArray *)requests
{
    NSLog(@"Received %lu write request(s)", (unsigned long)[requests count]);
    
    // TODO real code needs to handle multiple requests (but only send one result)
    CBATTRequest *request = [requests firstObject];
    
    NSString *callbackId = monitorList[request.characteristic.UUID.UUIDString];
    
    //NSLog(@"Callback ID %@", callbackId);
    
    //![callbackId isEqual:[NSNull null]] && ![callbackId isEqual:@""]
    if([callbackId length] != 0){
        
        //NSData *value = [request value];

        NSLog(@"Received Data %@", [request value]);
        NSString *stringValue = [[NSString alloc] initWithData:[request value] encoding:NSUTF8StringEncoding];
        NSString *stringUuid = request.characteristic.UUID.UUIDString;
        
        NSString *returnString = [NSString stringWithFormat:@"{\"%@\":\"%@\"}", stringUuid, stringValue];
        
        NSLog(@"Received %@ as a write request", returnString);
        
        CDVPluginResult *pluginResult;
        
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString:returnString];
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
    
    [_peripheralManager respondToRequest:request withResult:CBATTErrorSuccess];
    
}

#pragma mark - CBPeripheralManagerDelegate

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    
    NSLog(@"state: %i", peripheral.state);
}


/*
 peripheral
 
 peripheralManager:didReceiveWriteRequests: :(CBATTRequest *)request {
 
 
 
 if ([request.characteristic.UUID isEqual:myCharacteristic.UUID]) {
 
 }
 
 }
 */


/*
 #pragma mark - CBPeripheralManagerDelegate
 
 - (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
 NSString *js = [NSString stringWithFormat:@"BLEPeripheralManager.logState(%i);", peripheral.state];
 
 [self.commandDelegate evalJs:js];
 
 NSLog(@"state: %i", peripheral.state);
 }
 
 
 
 - (void)peripheralManager:(CBPeripheralManager *)peripheral didAddService:(CBService *)service error:(NSError *)error {
 NSString *js = [NSString stringWithFormat:@"BLEPeripheralManager.publish('didAddService');"];
 [self.commandDelegate evalJs:js];
 
 NSLog(@"did add service: %@", service.characteristics);
 }
 
 
 
 - (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral error:(NSError *)error {
 NSLog(@"peripheral manager did start advertising: %@", peripheral);
 NSString *js = [NSString stringWithFormat:@"BLEPeripheralManager.publish('didStartAdvertising');"];
 [self.commandDelegate evalJs:js];
 }
 */

@end
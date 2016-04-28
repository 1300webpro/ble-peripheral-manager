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
        NSString *value = [characteristicArgs objectForKey:@"value"];
        NSData *dataValue = [value dataUsingEncoding:NSUTF8StringEncoding];

        CBMutableCharacteristic *characteristic = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:[characteristicArgs objectForKey:@"UUID"]] properties:CBCharacteristicPropertyRead value:dataValue permissions:CBAttributePermissionsReadable];

        [characteristics addObject:characteristic];
    }

    CBUUID *uuid = [CBUUID UUIDWithString:[serviceArgs valueForKey:@"UUID"]];
    BOOL primaryBool = [[NSNumber numberWithInt:(NSInteger)[serviceArgs valueForKey:@"primary"]] boolValue];

    CBMutableService *service = [[CBMutableService alloc] initWithType:uuid primary:primaryBool];

    NSLog(@"characteristics: %@", characteristics);

    NSArray *immutableCharacteristics = [characteristics copy];

    service.characteristics = immutableCharacteristics;

    [serviceList addObject:service];

    [_peripheralManager addService:service];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)startAdvertising:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult;
    
    if (_peripheralManager.state == CBPeripheralManagerStatePoweredOn) {
        CBMutableService *service = [serviceList objectAtIndex:0];
        
        NSDictionary *advertisingData = @{CBAdvertisementDataLocalNameKey: [command.arguments objectAtIndex:0], CBAdvertisementDataServiceUUIDsKey: service.UUID};
        
        [_peripheralManager startAdvertising:advertisingData];
        
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)stopAdvertising:(CDVInvokedUrlCommand *)command {
    [_peripheralManager stopAdvertising];
}



- (void)removeAllServices:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult;
    
    pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    [_peripheralManager removeAllServices];
    
    //Reset the service list
    [serviceList removeAllObjects];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)changeCharacteristic:(CDVInvokedUrlCommand *)command {

      CDVPluginResult *pluginResult;
    
      pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
      
      //Find characteristic based on the uuid
      CBMutableCharacteristic *characteristic;
      
      for (CBMutableService *service in serviceList) {
          for (CBMutableCharacteristic *characteristics in service.characteristics) {
            if (characteristics.UUID.UUIDString == [command.arguments objectAtIndex:0]) {
                characteristic = characteristics;
                break;
            }
          }
      }

      //If the characteristic isn't null, then proceed with the update
      if(![characteristic isEqual:[NSNull null]]){
      
        NSString *value = [command.arguments objectAtIndex:1];// fetch the characteristic's new value as the second argument
        NSData *dataValue = [value dataUsingEncoding:NSUTF8StringEncoding];
        /*characteristic.value = dataValue*/
      

        //Update the value with the new value      
        BOOL didSendValue = [_peripheralManager updateValue:dataValue forCharacteristic:characteristic onSubscribedCentrals:nil];
      }
      
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)monitorCharacteristic:(CDVInvokedUrlCommand *)command {
    [monitorList setObject:command.callbackId forKey:[command.arguments objectAtIndex:0]];
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




/*-(void)peripheralManager:(CBPeripheralManager *)peripheral didReceiveReadRequest:(CBATTRequest *)request {
    NSLog(@"Received read request for %@", [request characteristic]);
    
    // TODO real code should check offsets and handle errors
    if ([request.characteristic.UUID isEqual:_switchCharacteristic.UUID]) {
        
        request.value = [_switchCharacteristic.value
                         subdataWithRange:NSMakeRange(request.offset,
                                                      _switchCharacteristic.value.length - request.offset)];
        
    } else if ([request.characteristic.UUID isEqual:_dimmerCharacteristic.UUID]) {
        request.value = _dimmerCharacteristic.value;

    }
    
    [_peripheralManager respondToRequest:request withResult:CBATTErrorSuccess];
}*/

-(void)peripheralManager:(CBPeripheralManager *)peripheral didReceiveWriteRequests:(NSArray *)requests
{
    NSLog(@"Received %lu write request(s)", (unsigned long)[requests count]);
    
    // TODO real code needs to handle multiple requests (but only send one result)
    CBATTRequest *request = [requests firstObject];

    NSString *callbackId = monitorList[request.characteristic.UUID.UUIDString];
    
    if(![callbackId isEqual:[NSNull null]]){
    
        NSData *value = [request value];
        NSString *stringValue = [[NSString alloc] initWithData:value encoding:NSUTF8StringEncoding];
        
        CDVPluginResult *pluginResult;
    
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString:stringValue];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
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
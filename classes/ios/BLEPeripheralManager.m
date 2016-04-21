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
}

- (void)pluginInitialize {
    [super pluginInitialize];
    
    _peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
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

    [_peripheralManager addService:service];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)startAdvertising:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult;
    
    if (_peripheralManager.state == CBPeripheralManagerStatePoweredOn) {
        NSDictionary *advertisingData = @{CBAdvertisementDataLocalNameKey: [command.arguments objectAtIndex:0], CBAdvertisementDataServiceUUIDsKey: @[[CBUUID UUIDWithString:CBUUIDGenericAccessProfileString]]};
        
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
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)changeCharacteristic:(CDVInvokedUrlCommand *)command {
      NSData *updatedValue = [command.arguments objectAtIndex:1];// fetch the characteristic's new value as the second argument
      
      //Find characteristic based on the uuid
      CBMutableCharacteristic *characteristic;
      
      for (NSArray *services in _peripheralManager.service.characteristics) {
          for (NSArray *characteristics in services) {
            if (characteristics.UUID.Uuid == [command.arguments objectAtIndex:0]) {
                characteristic = characteristics;
                break;
            }
          }
      }

      //If the characteristic isn't null, then proceed with the update
      if(characteristic != [NSNull null]){

        //Update the value with the new value
        BOOL didSendValue = [_peripheralManager updateValue:updatedValue

          forCharacteristic:characteristic onSubscribedCentrals:nil];
      }
      
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)monitorCharacteristic:(CDVInvokedUrlCommand *)command {

}


/*
peripheral

    peripheralManager:didReceiveWriteRequests: :(CBATTRequest *)request {

 

    if ([request.characteristic.UUID isEqual:myCharacteristic.UUID]) {
    
    }
    
    }
*/
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

@end
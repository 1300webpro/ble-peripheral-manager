//
//  BLEPeripheralManager.h
//  BLEPeripheralManager
//
//  Created by Nick Stevens on 3/6/14.
//  Modified by Tristan Lostrah 21/4/2016
//

#import <Cordova/CDV.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEPeripheralManager : CDVPlugin <CBPeripheralManagerDelegate>

-(void)startAdvertising:(CDVInvokedUrlCommand *)command;

-(void)stopAdvertising:(CDVInvokedUrlCommand *)command;

-(void)addService:(CDVInvokedUrlCommand *)command;

-(void)removeAllServices:(CDVInvokedUrlCommand *)command;

-(void)changeCharacteristic:(CDVInvokedUrlCommand *)command;

-(void)monitorCharacteristic:(CDVInvokedUrlCommand *)command;

@end

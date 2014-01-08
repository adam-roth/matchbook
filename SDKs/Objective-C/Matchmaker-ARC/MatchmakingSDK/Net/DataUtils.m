//
//  DataUtils.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "DataUtils.h"
#import "NSObject+SimpleJson.h"

#define ONE_BYTE 0x000000FF
#define MAX_PACKET_SIZE 262144

#define _kCFStreamPropertyReadTimeout CFSTR("_kCFStreamPropertyReadTimeout")
#define _kCFStreamPropertyWriteTimeout CFSTR("_kCFStreamPropertyWriteTimeout")

@implementation DataUtils

+ (NSData*) serializeInt32:(int)num {
    unsigned char buffer[4];
    buffer[0] = (num >> 24) & ONE_BYTE;
    buffer[1] = (num >> 16) & ONE_BYTE;
    buffer[2] = (num >> 8) & ONE_BYTE;
    buffer[3] = num & ONE_BYTE;
    
    return [NSMutableData dataWithBytes:buffer length:4];
}

+ (int) reconstructInt32:(NSData*)data {
    unsigned char* buffer = malloc(sizeof(unsigned char) * 4);
    [data getBytes:buffer length:4];
    
    int result = (buffer[0] & ONE_BYTE) << 24 | (buffer[1] & ONE_BYTE) << 16 | (buffer[2] & ONE_BYTE) << 8 | (buffer[3] & ONE_BYTE);
    free(buffer);
    
    return result;
}

+ (NSData*) serializeJson:(NSString*)jsonData {
    return [jsonData dataUsingEncoding:NSUTF8StringEncoding];
}

+ (BOOL) sendData:(unsigned char*)data toStream:(CFWriteStreamRef)stream withSize:(int)length {
    int totalWritten = 0;
    while (totalWritten < length) {
        int status = CFWriteStreamGetStatus(stream);
        if (CFWriteStreamCanAcceptBytes(stream)) {
            int numWritten = CFWriteStreamWrite(stream, &data[totalWritten], length - totalWritten);
            if (numWritten < 0 || status == kCFStreamStatusClosed || status == kCFStreamStatusError || status == kCFStreamStatusAtEnd) {
                //write error or stream closed
                NSLog(@"DataUtils.sendData() was unable to write to the stream; numWritten=%d, status=%d", numWritten, status);
                return NO;
            }
            else if (numWritten == 0) {
                //the stream is still open but can't accept more bytes just yet, so wait a bit
                [NSThread sleepForTimeInterval:0.1];
            }
            totalWritten += numWritten;
        }
        else {
            if (status == kCFStreamStatusClosed || status == kCFStreamStatusError || status == kCFStreamStatusAtEnd) {
                //error or EOF, can't fill buffer
                NSLog(@"DataUtils.sendData() was unable to send all data; status=%d", status);
                break;
            }
            [NSThread sleepForTimeInterval:0.1];
        }
    }
    
    return totalWritten == length;
}

+ (void) writeJson: (NSString*)jsonData toStream: (CFWriteStreamRef)stream {
    NSData* jsonBytes = [self serializeJson:jsonData];
    unsigned char* sizeBuffer = malloc(sizeof(unsigned char) * 4);
    unsigned char* dataBuffer = malloc(sizeof(unsigned char) * [jsonBytes length]);
    
    [jsonBytes getBytes:dataBuffer];
    [[self serializeInt32:[jsonBytes length]] getBytes:sizeBuffer length:4];
    
    if ([self sendData:sizeBuffer toStream:stream withSize:4]) {
        [self sendData:dataBuffer toStream:stream withSize:[jsonBytes length]];
    }
    
    //numWritten += CFWriteStreamWrite(stream, sizeBuffer, 4);
    //numWritten += CFWriteStreamWrite(stream, dataBuffer, [jsonBytes length]);
    
    free(sizeBuffer);
    free(dataBuffer);
}

+ (NSDictionary*) readJsonFromStream: (CFReadStreamRef)stream {
    int jsonSize = [self reconstructInt32FromStream:stream];
    if (jsonSize < 1 || jsonSize > MAX_PACKET_SIZE) {
        //invalid data or EOF
        NSLog(@"Read failed:  eof");
        return nil;
    }
    NSMutableData* buffer = [NSMutableData dataWithLength:jsonSize];
    int numRead = [self fillBuffer:buffer fromStream:stream];
    if (numRead != jsonSize) {
        //invalid data or EOF
        NSLog(@"Read failed:  wrong number of bytes");
        return nil;
    }
    
    //valid read, return json object
    return [[[NSString alloc] initWithData:buffer encoding:NSASCIIStringEncoding] JSONValue];
}

+ (int) reconstructInt32FromStream:(CFReadStreamRef)stream {
    NSMutableData* buffer = [NSMutableData dataWithLength:4];
    if ([self fillBuffer:buffer fromStream:stream] == 4) {
        return [self reconstructInt32:buffer];
    }
    
    //couldn't read an int from the stream
    return -1;
}

+ (int) fillBuffer: (NSMutableData*)buffer fromStream: (CFReadStreamRef)stream {
    CFRetain(stream);       //FIXME:  app will crash without this retain and the corresponding release below
    
    int totalRead = 0;
    int target = [buffer length];
    unsigned char* readBuffer = malloc(sizeof(unsigned char) * [buffer length]);
    
    while (totalRead < target) {
        int status = CFReadStreamGetStatus(stream);
        if (CFReadStreamHasBytesAvailable(stream)) {
            int numRead = CFReadStreamRead(stream, &readBuffer[totalRead], target - totalRead);
            if (numRead < 0 || status == kCFStreamStatusClosed || status == kCFStreamStatusError || status == kCFStreamStatusAtEnd) {
                //error or EOF, can't fill buffer
                NSLog(@"DataUtils.fillBuffer() could not read from stream, numRead=%d, status=%d", numRead, status);
                break;
            }
            if (numRead == 0) {
                //the stream is still open, but does not have any more bytes yet; wait a bit
                [NSThread sleepForTimeInterval:0.1];
            }
            
            totalRead += numRead;
        }
        else {
            if (status == kCFStreamStatusClosed || status == kCFStreamStatusError || status == kCFStreamStatusAtEnd) {
                //error or EOF, can't fill buffer
                NSLog(@"DataUtils.fillBuffer() could not read enough bytes from stream, status=%d", status);
                break;
            }
            [NSThread sleepForTimeInterval:0.1];
        }
    }
    
    [buffer setData:[NSData dataWithBytes:readBuffer length:totalRead]];
    free(readBuffer);
    CFRelease(stream);
    
    return totalRead;
}

+ (void)openSocketToServer:(NSString*)server onPort:(int)port withReadStream:(CFReadStreamRef*)readStream andWriteStream:(CFWriteStreamRef*)writeStream {
    double to = 60 * 60; // 1 hour timeout
    CFNumberRef timeout = CFNumberCreate(kCFAllocatorDefault, kCFNumberDoubleType, &to);
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)server, port, readStream, writeStream);
    
    CFReadStreamSetProperty(*readStream, _kCFStreamPropertyReadTimeout, timeout);
    CFReadStreamSetProperty(*readStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    
    CFWriteStreamSetProperty(*writeStream, _kCFStreamPropertyWriteTimeout, timeout);
    CFWriteStreamSetProperty(*writeStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    
    if (! CFReadStreamOpen(*readStream) || ! CFWriteStreamOpen(*writeStream)) {
        NSLog(@"Matchmaker could not initialize streams!");
    }
}

@end

//
//  DetectionManager.h
//  NewOfficeApp
//
//  Created by GJ on 2018/11/26.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_include(<React/RCTViewManager.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTViewManager.h>
#import <React/RCTView.h>
#import <React/RCTBridge.h>
#import <React/RCTUtils.h>
#import <React/RCTConvert.h>
#import <React/UIView+React.h>
#import <React/RCTUIManager.h>
#else
#import "RCTBridgeModule.h"
#import "RCTViewManager.h"
#import "RCTView.h"
#import "RCTBridge.h"
#import "RCTUtils.h"
#import "RCTConvert.h"
#import "UIView+React.h"
#import "RCTUIManager.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface DetectionViewManager : RCTViewManager <RCTBridgeModule>

@end

NS_ASSUME_NONNULL_END

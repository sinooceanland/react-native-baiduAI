//
//  DetectionView.h
//  MeiTuan
//
//  Created by GJ on 2018/11/27.
//  Copyright © 2018 Facebook. All rights reserved.
//

#if __has_include(<React/RCTView.h>)
#import <React/RCTView.h>
#import <React/RCTBridge.h>
#import <React/RCTUtils.h>
#import <React/RCTConvert.h>

#else
#import "RCTView.h"
#import "RCTBridge.h"
#import "RCTUtils.h"
#import "RCTConvert.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface DetectionView : RCTView

@property (nonatomic, copy) RCTBubblingEventBlock onRecongnizeFinish;
- (void)reload;

@end

NS_ASSUME_NONNULL_END

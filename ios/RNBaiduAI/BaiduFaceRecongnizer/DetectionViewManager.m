//
//  DetectionManager.m
//  NewOfficeApp
//
//  Created by GJ on 2018/11/26.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "DetectionViewManager.h"
#import "DetectionView.h"

@implementation DetectionViewManager

RCT_EXPORT_MODULE()

RCT_EXPORT_VIEW_PROPERTY(onRecongnizeFinish, RCTBubblingEventBlock)

- (UIView *)view
{
  return [[DetectionView alloc] init];
}

RCT_EXPORT_METHOD(reload:(nonnull NSNumber *)reactTag)
{
  NSLog(@"DetectionViewManager reload");
  
  [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RCTView *> *viewRegistry) {
    RCTView *view = viewRegistry[reactTag];
    if (![view isKindOfClass:[DetectionView class]]) {
      RCTLogError(@"Invalid view returned from registry, expecting DetectionView, got: %@", view);
    } else {
      DetectionView *detectionView  = (DetectionView *)view;
      [detectionView reload];
    }
  }];
}

@end

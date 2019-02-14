//
//  DetectionManager.m
//  NewOfficeApp
//
//  Created by GJ on 2018/11/26.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "DetectionViewControllerManager.h"
#import "DetectionViewController.h"

@implementation DetectionViewControllerManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
  return [[DetectionViewController alloc] init].view;
}

@end

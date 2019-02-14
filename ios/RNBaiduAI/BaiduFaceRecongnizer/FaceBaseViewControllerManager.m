//
//  FaceBaseViewControllerManager.m
//  NewOfficeApp
//
//  Created by GJ on 2018/11/26.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "FaceBaseViewControllerManager.h"

#import "FaceBaseViewController.h"
@implementation FaceBaseViewControllerManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
  return [[FaceBaseViewController alloc] init].view;
}


@end

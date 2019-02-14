//
//  DetectionView.m
//  MeiTuan
//
//  Created by GJ on 2018/11/27.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "DetectionView.h"
#import "DetectionViewController.h"

@interface DetectionView ()

@property(strong, nonatomic) DetectionViewController *detectionVC;

@end

@implementation DetectionView

- (instancetype)initWithFrame:(CGRect)frame
{
  self = [super initWithFrame:frame];
  if (self) {
    self.backgroundColor = [UIColor clearColor];
    self.detectionVC.view.frame = self.bounds;
    [self addSubview:self.detectionVC.view];
  }
  return self;
}

#pragma mark - lazy load
- (DetectionViewController *)detectionVC {
  if (!_detectionVC) {
    _detectionVC = [[DetectionViewController alloc] init];
    __weak typeof(self) weakSelf = self;
    self.detectionVC.detectionCallback = ^(NSDictionary *body) {
      if (weakSelf.onRecongnizeFinish) {
        weakSelf.onRecongnizeFinish(body);
      }
    };
  }
  return _detectionVC;
}

-(void)reload {
  [_detectionVC reloadAction];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end

# 表情 FaceDemo
这个Demo是根据Qiujuer老师的《全程MVP手把手打造IM即时通讯Android APP》视频中的表情扩展模块学习编写。
此表情demo，并没有实现表情动态效果。
传参传错导致表情加载时显示有问题，使用时将MainActivity的第50行代码改为
```
Face.decode(txt, spannable, (int) Ui.dipToPx(getResources(), 20));
```
如有不对指出，请直接指出。
![problem.gif](https://upload-images.jianshu.io/upload_images/2194177-5562c4618f353881.gif?imageMogr2/auto-orient/strip)

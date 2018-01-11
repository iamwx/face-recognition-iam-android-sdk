#### Version 1.2.1
##### - Added:
- ProgressListener
- CAPTURE mode
- Generic method for making image upload based operations

#### Version 1.2.2
##### - Added:
- Fix for onFaceRecognitionResult being called twice for VERIFICATION mode

#### Version 1.2.4
##### - Added:
- Method to control screen brightness with HVFrCamera View

#### Version 1.2.5
##### - Bug Fixes:
- Possible crash fix upon call of HVFrCamera's stopCamera method.

##### - Improvements:
- Calling HVFrCameraProgressListener's onFaceProcessingEnd method when pauseFR is called while face image upload is in progress 

#### Version 1.2.6
##### - Bug Fixes:
- Progress Dialog not getting disabled when no internet is present for HVFrCamera operations
- onFaceProcessingEnd getting called before onFaceProcessingStart in case of no internet

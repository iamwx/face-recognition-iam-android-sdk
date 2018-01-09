## Face Recognition SDK Documentation


### Introduction
HyperSecure is an Android SDK of HyperVerge's Face Recognition based Identity and Access Management (IAM) System. This documentation explains how to use the SDK to build your own app.
<br></br>

![](https://media.giphy.com/media/P7hZDbHqQAKxG/giphy.gif)
<br>

#### Overview
In the context of HyperVerge's face recognition based IAM system, there are 3 entities: User, Group and Organization. Users are the people enrolled for Face Recognition having registered with their face image. A Group can represent a team or site or location or building or any other collection of people. An Organization can thus have multiple Groups and each Group can have multiple Users. 

A high-level overview of the Face Recognition workflow is as follows:

- Enroll Users into a Group of the Organization, with their face images captured from Camera in Registration Mode
- To recognize who a person is, capture their face image from the Camera in Recognition Mode
- To verify if a person is actually who they're claiming to be, capture their face from the Camera in Verification Mode along with the userId
- To add more faces to a person to improve recognition accuracy, capture their face images from the Camera in Face Add Mode

#### Prerequisites
- Gradle Version: 3.3 (Recommended)
- Tested with Gradle Plugin for Android Studio - version 2.3.1 
- minSdkVersion 14
- targetSdkVersion 23

---

### Integration Steps

#### 1. Getting SDK credentials

- **Using HyperSecure within your own organization**: Please use the `tenantId`, `tenantKey` and `adminToken` supplied by HyperVerge. If you don't have them already, then drop a mail to contact@hyperverge.co
- **Using HyperSecure for other organizations**: Channel Partners using HyperSecure as part of solutions for other organizations shall use a dashboard or an API provided by HyperVerge to create a client organization. Upon creation, they will receive a `tenantId`, `tenantKey` and `adminToken` unique to each client organization, which shall be used in the SDK initialization as described later.
	- **tenantId**: An id unique to each client of the Channel Partner. It will be used to identify the client organization and will let HyperVerge know which logical organization entity is being referred to for performing operations such as face enrollment, verification or recognition
	- **tenantKey**: A token used to authenticate a client of the Channel Partner. This will help HyperVerge ensure that all the communication to the server is secure and authenticated.
	- **adminToken**: A unique Admin Token for each client organisation's admin user. This token will let HyperVerge authorize the FR operations requested by the SDK at the Server
	
#### 2. Setting up Android Studio Project
- Add dependency to HyperSecure SDK's maven repo.
    - Add the following set of lines to your `app/build.gradle`

        ```
        dependencies {
            compile('co.hyperverge:hypersecuresdk:1.2.5@aar', {
                transitive=true
            })
        }
        ```
    - Add the following set of lines to the Project (top-level) `build.gradle`

        ```
        allprojects {
            repositories {
                maven {
                    url "s3://hvsdk-hvfrcamera/android/hvsecure/releases"
                    credentials(AwsCredentials) {
                        accessKey "aws_access_key"
                        secretKey "aws_secret_pass"
                    }
                }
            }
        }
        ```
    Kindly contact HyperVerge at contact@hyperverge.co for getting your `aws_access_key` and `aws_secret_pass`.
- **Permissions**: The app requires the following permissions to work.
    - *Camera*
    - *Autofocus*
    - *Read & Write External Storage*
    - *Internet*
    - *Access Network State*

    Kindly note that for android v23 (Marshmallow) and above, you need to handle the runtime permissions inside your app.
- **SDK Initialization**: Add the following line to your Application class (the class which extends android.app.Application) for initializing our Library. This must be run only once in an Application Lifecycle. Check [this](https://guides.codepath.com/android/Understanding-the-Android-Application-Class) link if you are unsure of what an Application class is. 

    ```
    HyperSecureSDK.init(context, tenantId, tenantKey, adminToken);
    ```

#### 3. Enrolling, Verifying or Recognizing User from Camera feed
The functionality for enrolling, verifying and recognizing a user is implemented in the SDK as a View called `HVFrCamera` which is a sub-class of FrameLayout. This View includes a camera preview, local face detection and execution of corresponding APIs for enrollment, verification (1:1) and recognition (1:N). 
    
##### Implementing a Listener for Communication
HVFrCamera communicates with your application through the `HVFrCameraListener` interface which needs to be implemented by your application. 

```
HVFrCamera.HVFrCameraListener myFrCamListener = new HVFrCamera.HVFrCameraListener(){
    @Override
    public void onFaceRecognitionResult(FRMode mode, JSONObject result) { 

    }

    @Override
    public void onError(int errCode, String errMsg, JSONObject info) {
        //errCode: error code stating type of error
        //errMsg: a message giving more info on the error
        //info: a JSON object having following keys:
        //    • imageUri: JSONArray of local path of the face images saved after photo is captured by camera. Can be empty or not present if error happens before capturing.
        
        //please note that for `REGISTRAION` and `FACE_ADD` mode, the captured images won't be cleared internally by the SDK in case of error. This essentially means that the state of images captured will be preserved and if `submit` method is called again, then the same images will be processed and uploaded to server.
        //in case the captured images need to be cleared, `clearCapturedImages` method can be called to manually clear the captured images. This method has been described later in this documentation.
    }
    
    @Override
    public void onCaptureCallback(boolean isSuccess, int errCode, String errMsg) {
        //please note that this callback is called as a response to the capture method(described later) and the sole purpose of it is to specify if the capture was successful of not
        //isSuccess: was manual capture successful
        //errCode: errCode to help developer debug the error(described later)
        //errMsg: slightly detailed description of the error
    }
}

```
- **Result JSON Object**:
    - Enroll Mode:
        - `tenantId`: Used to identify the organization where enroll has been performed.
        - `groupId`: Group to which the user has been enrolled
        - `userId`: Unique id of the user who has been enrolled
        - `userInfo`: Some more details about the user
        - `imageUri`: JSON Array of local file paths of the face image that has been used for enrolling
        - `faceIds`: JSON Array of faceId of the faces enrolled
    - Face Add Mode:
        - `tenantId`: Used to identify the organization where face add has been performed.
        - `userId`: Unique id of the user whose face is getting added
        - `imageUri`: JSON Array of local file paths of the face image that has been added to user's facelist
        - `faceIds`: JSON Array of faceId of the faces added to the user
    - Verification Mode:
        - `tenantId`: Used to identify the organization where Verification has been performed.
        - `userId`: Unique id of the user on whom Verification has been performed
        - `userInfo`: Some more details about the user
        - `imageUri`: JSON Array of String in which first element indicates the local file path of the face image on which Verification has been performed
    - Recognition Mode:
        - `tenantId`: Used to identify the organization where Recognition has been performed.
        - `groupId`: Group in which Recognition has been performed
        - `userId`: Unique id of the user recognized by Recognition
        - `userInfo`: Some more details about the user
        - `imageUri`: JSON Array of String in which first element indicates the local file path of the face image on which user has been recognized
    - Capture Mode:
    	- `imageUri`: JSON Array of String in which first element indicates the local file path of the face image that has been captured


##### Adding HVFrCamera View to your Activity/Fragment
HVFrCamera View is a sub-class of FrameLayout. This is a view with a fixed aspect ratio of 4:3

- Add the following to the XML

    ```
    <co.hyperverge.hypersecuresdk.workflows.fr.Views.HVFrCamera
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/cameraFL"
        >
    </co.hyperverge.hypersecuresdk.workflows.fr.Views.HVFrCamera>
    ```
- Add the following to the `onPause()` method of your Activity or Fragment to which HVFrCamera has been added:

	```
	hvfrcamera.pause();
	```
	
	This will pause the Camera and the Face Recognition processing.

- Add the following to the `onResume()` method of your Activity or Fragment to which HVFrCamera has been added:

	```
	hvfrcamera.resume();
	```
	
	This will resume the Camera and the Face Recognition processing.

- Add the following to the `onDestroy()` method of your Activity or Fragment to which HVFrCamera has been added:

	```
	hvfrcamera.stopCamera();
	```
	
	This will release the Camera and all the resources related to Face Recognition.

- Add the following to the `onCreate()` method of your Activity or Fragment to which HVFrCamera has been added:

	```
	hvfrcamera.startCamera(userData, mode, timeout, isAutoCaptureEnabled, useFrontCam, myFrCamListener);
	```
	
	The arguments accepted by the start camera function are the Configuration variables for HVFrCamera. The `startCamera()` method will set these variables and set HVFrCamera to start processing the camera feed. The details of the variables are given below.
	- `mode` can have a value among FRMode.REGISTER, FRMode.FACE_ADD, FRMode.RECOGNITION, FRMode.VERIFICATION, and FRMode.CAPTURE
    - `userData` is the JSONObject having userDetails (explanation below)
   	- `timeout` is the maximum time in milliseconds since startCamera() or resumeCamera() after which if the registration/recognition is not done, the onError will be called with Timeout Error. A value of 0 will disable the timeout
   	- `isAutoCaptureEnabled` is a boolean value that specifies if automatic capture of image should happen for recognition when a face matching the desired size is detected. Please note that auto-capture is not supported for Registration mode or Face Add mode 
   	- `useFrontCam` is a boolean value that specifies if the front camera should be used. If set to false, then the back camera will be used for processing
   	- `myFrCamListener` is the implementation of HVFrCameraListener you created above

	**UserData JSON Object**:
    - Enroll Mode:
        - `tenantId`: Used to identify the organization where enroll should be performed. Should be same as the one used to initialize the SDK
        - `groupId`: Group to which the user should be enrolled (Optional)
        - `userId`: Unique id of the user who is getting enrolled
        - `userInfo`: Some more information about the user
    - Face Add Mode:
        - `tenantId`: Used to identify the organization where face add should be performed. Should be same as the one used to initialize the SDK
        - `userId`: Unique id of the user whose face is getting added
    - Verification Mode:
        - `tenantId`: Used to identify the organization where Verification should be performed. Should be same as the one used to initialize the SDK
        - `groupId`: Group where Verification should be performed (Optional)
        - `userId`: Unique id of the user on whom Verification should performed
    - Recognition Mode:
        - `tenantId`: Used to identify the organization where Recognition should be performed. Should be same as the one used to initialize the SDK
        - `groupId`: Group where Recognition should be performed
    - Capture Mode:
        - Empty JSONObject

**Please note:** the group will have to be created before use in HVFrCamera. If no groupId is passed, we will assume that `default` group is being used. The `default` group is by default created for all tenants upon creation of the tenant
    

##### Pausing or resuming processing
To pause or resume processing the camera feed for face detection and recognition, the following functions can be used. 

- `hvfrcamera.pauseFR()` will pause the Face Detection and auto-capture of faces
- `hvfrcamera.resumeFR()` will resume the Face Detection and auto-capture of faces(if applicable).
        
##### Changing the Configuration variables at runtime 
If any of the Configuration variables need to be changed at runtime, after `startCamera()` has been called, then we can use the Setter function corresponding to that variable after the face processing has been safely paused with `pauseFR()`. 
        
```
hvfrcamera.pauseFR();
hvfrcamera.setUserData(userData);
hvfrcamera.setMode(mode);
hvfrcamera.setTimeout(timeout);
hvfrcamera.setAutoCaptureEnabled(isAutoCaptureEnabled);
hvfrcamera.setFRCameraListener(myFrCamListener);
hvfrcamera.shouldUseFrontCam(useFrontCam);
hvfrcamera.resumeFR();
```

##### Capturing Face Image Manually        
To trigger on-demand capture of image and start Face Recognition/Registration on the captured frame, following method can be called:
    
```
hvfrcamera.capture();
```
**Please note:**
- for registration/face add mode, no auto-capture will happen and this method should be explicitly called
- the status of the capture will be informed from the `onCaptureCallback` callback of the `myFrCamListener`

##### Submitting Captured image to Server
Once the required images have been captured, to start the transaction of Face Enrolling or Adding, following method should be captured:

```
hvfrcamera.submit();
```
**Please note:**
- this method can be called only for registration/face add mode
- after this method is called, `onFaceRecognitionResult` or `onError` callback of the `myFrCamListener` will inform the status of User Enrol or Face Add Operation

##### Clearing the images captured so far for REGISTRATION and FACE_ADD Mode
The following method can be called to clear the images that have been captured via `hvfrcamera.capture()` method:

```
hvfrcamera.clearCapturedImages();
```
**Please note:**
- This method will clear the reference of the images and delete the images from the disk as well.
- This method will work with only REGISTRATION and FACE_ADD Mode.
- This method will return an integer value which can be used to determine if the images were successfully cleared. Following are the values that can be returned:
    - `HVFrCamera.SUCCESS_CLEAR_CAPTURED_IMAGES`: This means that the images were cleared successfully
    - `HVFrCamera.ERROR_CLEAR_CAPTURED_IMAGES_INVALID_MODE`: This means that this method is called for an invalid mode. As mentioned above, this method only works for REGISTRATION and FACE_ADD mode.
    - `HVFrCamera.ERROR_CLEAR_CAPTURED_IMAGES_CAMERA_NOT_FREE`: This means that an image capture is in progress. Hence the images cannot be cleared now. When this error occurs, please retry after some time.
    - `HVFrCamera.ERROR_CLEAR_CAPTURED_IMAGES_PROCESSING_STARTED`: This means that an image upload is already in progress and hence the images cannot be cleared now. Please retry once the processing is done and an appropriate callback method of the listener is called.

##### Progress Callbacks
Following method can be used to set progress callback which includes methods that will be called when the processing on the captured image starts and ends.

```
hvfrcamera.setProgressListener(myHVFrCameraProgressListener);
```
where myHVFrCameraProgressListener can be implemented as:
```
HVFrCameraProgressListener myHVFrCameraProgressListener = new HVFrCamera.HVFrCameraProgressListener() {
	@Override
	public void onFaceProcessingStart(JSONObject info) {
		//called when the upload of image to server starts for any FR operation
		//info will have following details:
		//	• imageUri: localpath of the face crop image being uploaded to server
	}

	@Override
	public void onFaceProcessingEnd() {
		//called when the server has given a response for the image being uploaded
	}
}
```

Please note that **setting progress callback is completely optional** and if not set, the default progress would be shown by the SDK.

##### Screen Brightness
By default, the screen brightness is set to maximum whenever HVFrCamera View is being displayed. To control this, following method can be used:

```
hvfrcamera.enableDisableFullBrightness(activity, shouldEnable);
```
where 
- **activity** is the Activity Object which contains the HVFrCamera View. If HVFrCamera is a part of a Fragment or something similar, the enclosing Activity object needs to be passed.
- **shouldEnable** can be
	- **true**, if screen brightness has to be set to maximum
	- **false**, if screen brightness has to be set to a value equivalent to that in the Device's Settings

##### Description of the Error Codes in `onError` callback method of the `myFrCamListener`  is given below: 

|Error Code|Description|Explanation|Action|
|----------|-----------|-----------|------|
|0|No Error||No Action|
|1|Initialization Error|Occurs when SDK has not been initialized properly.|Check if the initialization of SDK is happening before any functionality is being used.|
|2|Input Error|Occurs when input provided to the specific flow(Recognition, Registration, Verification etc) is not correct.|Check if all the parameters provided are proper and as per the documentation|
|3|Network Error|Occurs when the internet is either non-existant or very patchy.|Check internet and try again. If Internet is proper, contact HyperVerge|
|4|Timeout Error|Occurs when the timeout(provided by user) is hit and the operation has not yet completed.|Try again|
|5|Authentication Error|Occurs when the request to server could not be Authenticated/Authorized. Happens when the tenantId, tenantKey and adminToken while initializing SDK are not correct.|Make sure tenantId, tenantKey and adminToken are correct|
|6|Internal Server Error|Occurs when there is an internal error at the server.|Notify HyperVerge|
|7|Request Error|Occurs when the request to server is missing some parameters.| Confirm if all the parameters are passed to the method properly|
|8|Internal SDK Error|Occurs when an unexpected error has happened with the HyperSecure SDK.|Notify HyperVerge|
|9|Face Recognition Error|Occurs when there is an error with the Face Recognition. This mosly happens when a Face Recognition/Verification flow is run on a person not already enrolled, face detected by the device is not very clear, unknown person is trying the recognition/verification.| Try again after ensuring that the person is already enrolled and the lighting is also proper.|

##### Description of the Error Codes in `onCaptureCallback` callback method of the `myFrCamListener`  is given below:
|Error Code|Description|Explanation|Action|
|----------|-----------|-----------|------|
|0|No Error||No Action|
|1|Initialization Error|Occurs when SDK has not been initialized properly.|Check if the initialization of SDK is happening before any functionality is being used.|
|2|Input Error|Occurs when input provided to the specific flow(Recognition, Registration, Verification etc) is not correct.|Check if all the parameters provided are proper and as per the documentation|
|3|Camera Not Free Error|Occurs when the capture method is called even before the last capture is not complete. This error can be avoided by waiting for `onCaptureCallback` call before `calling` capture again|Try again|
|4|Face Detection Error|Occurs when the captured frame doesnot have any face in it.|Make sure the face is present in the frame. Also, the movement of camera and the face should be kept minimal while capturing. Also ensure that lightening is proper|
|5|Maximum Image Clicked Error|Occurs when the capture method is called after the number of valid captured images reach a threshold of `5`.|No more images can be clicked. Captured images should be submitted using `submit` method|
    
            
#### 4. Other operations
 - **Managing Users, Groups and UserData**

	Management of Users, Groups and UserData requires the ability to perform operations such as creating/deleting a Group, adding/removing a User from a Group, adding/removing a face registered to a User, etc. A complete list of such operations is given in the table below. 

	| End Point | Request Object | Result |
	|-----------|----------------|--------|
	|/user/edit|{<br/> "userId" : string,<br/> "details" : string<br/>}|{<br/>}|
	|/user/get|{<br/> "userId" : string <br/>}|{<br/> "userId" : string,<br/> "roles" : Array(roles),<br/> "createdDate" : int, <br/> "details"	 : string, <br/>"groups" : Array({<br/>"groupId" : string,<br/>"role" : string<br/>}),<br/>"faces" : Array(faceId)<br/>}|
	|/user/remove|{<br/> "userId" : string<br/>}|{<br/>}|
	|/user/removeFace|{<br/> "userId" : string,<br/> "faceId" : string<br/> }|{<br/>}|
	|/user/fetchFaces|{<br/> "userId" : string <br/>}|{<br/> "faces": Array(string) <br/>}|
	|/user/fetchFaceUrls|{<br/> "userId" : string <br/>}|{<br/> "faceUrls": Array(string) <br/>}|
	|/group/create|{<br/> "groupname" : string,<br/> "sizeLimit" : number <br/>}|{<br/> "groupId" : string <br/>}|
	|/group/get|{<br/> "groupId" : string <br/>}|{<br/>"groupname" : string,<br/> "sizeLimit" : string,<br/> "createdDate" : int <br/>}|
	|/group/edit|{<br/> "groupId" : string,<br/> "params" : {<br/> "groupname" : string,<br/> "sizeLimit" : number <br/>} <br/>}|{<br/>}|
	|/group/remove|{<br/> "groupId" : string <br/>}|{<br/>}|
	|/group/addUser|{<br/> "groupId" : string,<br/> "userId" : string <br/>}|{<br/>}|
	|/group/removeUser|{<br/> "groupId" : string,<br/> "userId" : string <br/>}|{<br/>}|
	|/group/userRole|{<br/> "groupId" : string,<br/> "userId" : string,<br/> "groupRole" : groupRole,/* "user" or "groupAdmin" \*/ <br/>}|{<br/>}|
	|/group/listUsers|{<br/> "groupId" : string <br/>}|{<br/> "users":Array({<br/> "userId" : string,<br/> "details" : string,<br/> "createdTime" : int ,<br/> faces : Array(faceId)}) <br/>}|

	<br/>
	- Any of the operations mentioned in the table above can be performed using the following method:


	    int requestId = HVOperationManager.makeRequest(endpoint, requestObject, new HVOperationManager.HVOperationListener() {
		@Override
		public void onOperationComplete(JSONObject result) {
			//result is a JSON Object has been described earlier against each end point
		}

		@Override
		public void onError(int errCode, String errMsg) {
			//errCode: error code stating type of error
			//errMsg: a message giving more info on the error
		}
	    });
	    
 - **Process captured images**

	To perform an operation that requires one or more locally present images to be uploaded to the server, one of the following operation can be used. These operations can enable the developer to enroll a user using one or more face images, add one or more face images to a user, perform face based authentication using a face image, perform 1:1 recognition or 1:N recognition using an image saved in the device.

	| End Point | Request  | imageUriJSON | Result |
	|-----------|----------------|-----|--------|
	|/user/faceauth|{<br/> "userId" : String<br/>}| {<br/> "image" : String(local path of image)<br/>} |{<br/> "token" : String<br/>}|
	|/user/enroll|{<br/>"userId" : String,<br/> <br/>"groupId" : String,<br/> <br/> "details" : String<br/>}| {<br/> "image1" : String(local path of image)<br/>, <br/> "image2" : String(local path of image)<br/>, ...<br/>} |{<br/> "faceIds" : [<br/>{<br/>"label": "image1",<br/>"faceId": String<br/>},<br/>...<br/>],<faceId><br/> "faceId" : String<br/>}|
	|/user/addFace|{<br/> "userId" : String<br/>}| {<br/> "image1" : String(local path of image)<br/>, <br/> "image2" : String(local path of image)<br/>, ...<br/>} |{<br/> "faceIds" : [<br/>{<br/>"label": "image1",<br/>"faceId": String<br/>},<br/>...<br/>],<faceId><br/> "faceId" : String<br/>}|
	|/image/verify|{<br/> "userId" : String<br/>}| {<br/> "image" : String(local path of image)<br/>} |{<br/> "faceId" : String,<br/> "personId" : String,<br/> "userDetails" : {<br/> "details" : String,<br/>"userId": String<br/>}, <br/> "exists" : Boolean,<br/> "conf" : Integer<br/>}|
	|/image/recognize|{<br/> "groupId" : String<br/>}| {<br/> "image" : String(local path of image)<br/>} |{<br/> "faceId" : String,<br/>"personId" : String,<br/> "userDetails" : {<br/> "details" : String,<br/>"userId": String<br/>}, <br/> "exists" : Boolean,<br/> "conf" : Integer<br/>}|

	<br/>

	- Any of the operations mentioned in the table above can be performed using the following method:

	```
	int requestId = HVOperationManager.makeRequest(endpoint, imageUriJSON, requestObject, new HVOperationManager.HVOperationListener() {
		@Override
		public void onOperationComplete(JSONObject result) {
			//result is a JSON Object has been described earlier against each end point
		}

		@Override
		public void onError(int errCode, String errMsg) {
			//errCode: error code stating type of error
			//errMsg: a message giving more info on the error
		}
	});
	```

- **Cancelling Operation** 

	To `cancel` an operation started using HVOperationManager's `makeRequest` method, following method can be used:

	```
	boolean isCancelled = HVOperationManager.cancelRequest(requestId);
	```
	where `requestId` was returned by the corresponding `makeRequest` method that is needed to be cancelled.

##### Description of the Error Codes is given below: 

|Error Code|Description|Explanation|Action|
|----------|-----------|-----------|------|
|0|No Error||No Action|
|1|Initialization Error|Occurs when SDK has not been initialized properly.|Check if the initialization of SDK is happening before any functionality is being used.|
|2|Network Error|Occurs when the internet is either non-existant or very patchy.|Check internet and try again. If Internet is proper, contact HyperVerge|
|3|Authentication Error|Occurs when the request to server could not be Authenticated/Authorized. Happens when the tenantId, tenantKey and adminToken while initializing SDK are not correct.|Make sure tenantId, tenantKey and adminToken are correct|
|4|Internal Server Error|Occurs when there is an internal error at the server.|Notify HyperVerge|
|5|Internal SDK Error|Occurs when an unexpected error has happened with the HyperSecure SDK.|Notify HyperVerge|
|600|INPUT_MISSING_ENDPOINT |Occurs when the endPoint is null or empty|Provide correct endPoint and retry|
|601|INPUT_REQUEST_NULL |Occurs when request is null|Provide non-null request JSON Object and retry|
|602|INPUT_LISTENER_NULL |Occurs when a null instance of `HVOperationListener` has been passed|Provide non-null listener and retry|
|603|INPUT_MISSING_USER_ID |Occurs when the request JSON Object is missing `userId`|Provide `userId` in the request JSON Object and retry|
|604|INPUT_MISSING_USER_DETAILS |Occurs when the request JSON Object is missing `details`|Provide `details` in the request JSON Object and retry|
|605|INPUT_MISSING_GROUPS |Occurs when the request JSON Object is missing `groups`|Provide `groups` in the request JSON Object and retry|
|606|INPUT_MISSING_GROUP_ID |Occurs when the request JSON Object is missing `groupId`|Provide `groupId` in the request JSON Object and retry|
|607|INPUT_MISSING_GROUP_NAME |Occurs when the request JSON Object is missing `groupName`|Provide `groupName` in the request JSON Object and retry|
|608|INPUT_MISSING_GROUP_ROLE |Occurs when the request JSON Object is missing `groupRole`|Provide `groupRole` in the request JSON Object and retry|
|609|INPUT_MISSING_GROUP_SIZE_LIMIT |Occurs when the request JSON Object is missing `sizeLimit`|Provide `sizeLimit` in the request JSON Object and retry|
|610|INPUT_MISSING_IMAGE |Occurs when the request JSON Object is missing `image`|Provide `image` in the request JSON Object and retry|
|611|INPUT_MISSING_ROLE |Occurs when the request JSON Object is missing `role`|Provide `role` in the request JSON Object and retry|
|612|INPUT_MISSING_FACE |Occurs when the request JSON Object is missing `face`|Provide `face` in the request JSON Object and retry|
|613|INPUT_MISSING_FACE_ID |Occurs when the request JSON Object is missing `faceId`|Provide `faceId` in the request JSON Object and retry|
|614|INPUT_MISSING_FACE_IDS |Occurs when the request JSON Object is missing `faceIds`|Provide `faceIds` in the request JSON Object and retry|
|615|INPUT_USER_NOT_FOUND |Occurs when there is no user associated with the `userId` provided|Provide correct `userId` and retry|
|616|INPUT_USER_ALREADY_EXIST |Occurs when a user with `userId` provided already exists|Provide a new unique `userId` and retry|
|617|INPUT_GROUP_NOT_FOUND |Occurs when no group is associated with the `groupId` provided|Provide correct `groupId` and retry|
|618|INPUT_GROUP_ALREADY_EXIST |Occurs when a group already exists with the `groupId` provided|Provide a new unique `groupId` and retry|
|619|INPUT_FACE_NOT_PRESENT_IN_IMAGE |Occurs when no face is present in the image provided in `imageUriJSON`|Provide a correct image having atleast one face in `imageUriJSON` and retry|
|620|INPUT_FACE_NOT_MATCH |Occurs when face present in the image provided in `imageUriJSON` doesnot matched with the template used while `registration` or `face add`|Make sure the `userId` passed in `request` and face image passed in `imageUriJSON` match|
|621|INPUT_OTP_MISMATCH |Occurs when the OTP provided doesnot match the one that is sent to the user|Provide correct `otp` and retry|
|622|INPUT_INVALID_ENDPOINT |Occurs when the `endPoint` provided is not valid|Provide correct the `endPoint` and retry|
|623|INPUT_INVALID_IMAGE_PATH |Occurs when `imageUriJSON` is null or no file exists in one or more imagepath passed in `imageUriJSON`|Validate `imageUriJSON` and the image path passed in it and then retry|
|624|ERROR_INPUT_ILLEGAL_PARAMETER |Occurs when one or more illegal key is provided in `request` JSONObject|Remove the illegal key value pair from `request` and retry|
|699|INPUT_OTHER |Occurs when some other issue is with the input|Read the log message for detailed explanation|



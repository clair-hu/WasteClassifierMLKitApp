# # WasteClassifierMLKitApp

Hi! It is a **simple** waste classification android application developed with Google ML Kit and firebase. 

* I am using the model trained by AutoML api by H20.ai which is integrated in ML Kit. 

* It is a good quick practise to getting familiar with firebase, autoML, and ML Kit. (I spent two work days to develop the first version.)


## UI
* Welcome Page

![Welcome Page](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/welcome_page.png)
* Main Page

![Main Page](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/main_page.png)
* Gallery

![Gallery](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/gallery.png)
* Camera

![Camera](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/camera.png)
* Output Page (tested on device)

![Output Page](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/output_page.png)

## Firebase

Firebase is a comprehensive mobile development platform. It is integrated with Google ML Kit.
![Connecting firebase to android application](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/firebase_to_android_app.png)


## AutoML

AutoML vision is a new api integrated with Google ML Kit.

You need to prepare your dataset to classification. The limitation of the dataset size is 1000 images.

* Select autoML model latency and size

![Select autoML model latency and size](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/autoML_model_latency_and_size_selection.png)
* Training model with AutoML

![Training model with AutoML](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/autoML_training.png)
* Model evaluation

![Model evaluation](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/model_evaluation.png)
* Confusion matrix

![Confusion matrix](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/confusion_matrix.png)
* Publish model so we can use remotely in android app

![Publish model so we can use remotely in android app](https://github.com/clair-hu/WasteClassifierMLKitApp/blob/master/img/model_publish.png)

## Reference

* Codes heavily depend on https://github.com/jirawatee/ML-Kit-for-Firebase-Android.git by jirawatee.

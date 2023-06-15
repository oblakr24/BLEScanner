# BLE Device Detection App

This is a demonstration app designed for detecting nearby BLE (Bluetooth Low Energy) devices, connecting to them, 
listing their services and characteristics, and interacting with those characteristics.

Its purpose is to demonstrate the BLE functionalities, Coroutines and Rx interoperability and testing approaches.

## Video

https://github-production-user-asset-6210df.s3.amazonaws.com/32245831/246184961-d75fbcf2-050c-4e15-8f8e-7100ea24f5a7.mp4

## Usage

1. **Scan for devices:** Run the app and it will display a list of nearby BLE devices. Click on a device to proceed.

2. **Connect to a device:** Once you're in the connection screen, click on the "Connect" button. This will trigger a discovery process for the services and characteristics of the chosen device.

3. **Interact with characteristics:** Clicking on a characteristic will open a dialog that allows for interactions. Once an action is performed, the dialog closes and the corresponding event should appear in the logs.

## Technologies

**This app is made using:**

- Jetpack Compose for UI with Material 3
- RxJava 3 for the BLE module
- Coroutines for bridging the BLE functionalities with the Flows on the app module

**Stack:**
- MVVM architecture (mix of MVVM and MVI)
- RxJava3 in the BLE module
- Coroutines/Flows on the app module for interacting with RxJava3 observables from the BLE module
- [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Compose Navigation](https://developer.android.com/jetpack/compose/navigation): UI
- [Hilt](https://dagger.dev/hilt/): Dependency injection
- [KotlinX Serialization](https://github.com/Kotlin/kotlinx.serialization) for serialization and deserialization of models into and from files
- [Retrofit](https://github.com/square/retrofit) for persisting user preferences
- [Extended Material icons](https://developer.android.com/jetpack/androidx/releases/compose-material) for vector images
- [Accompanist Permissions](https://github.com/google/accompanist/tree/main/permissions) for Composable permission handling
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for persisting user preferences
- [MockK](https://mockk.io/) for mocking in tests

## Permissions

Bluetooth permissions are required (depending on Android version) to allow for the full functionality set.

## Screenshots

<p align="center">
  <img src="https://github-production-user-asset-6210df.s3.amazonaws.com/32245831/246185716-dbe9e467-fe90-4dc1-a18c-ba87b11c16c1.jpg" width="270" height="570">
  <img src="https://github-production-user-asset-6210df.s3.amazonaws.com/32245831/246185859-c5384f0f-543e-46db-a963-bc9454787512.jpg" width="270" height="570">
  <img src="https://github-production-user-asset-6210df.s3.amazonaws.com/32245831/246185901-44958f86-1faa-4253-8b11-47472215708e.jpg" width="270" height="570">
</p>

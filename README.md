# BLE Device Detection App

This is a simple app designed for detecting nearby BLE (Bluetooth Low Energy) devices, connecting to them, listing their services and characteristics, and interacting with those characteristics.

## Permissions

For the app to function properly, you need to grant Bluetooth permissions.

## Usage

1. **Scan for devices:** Run the app and it will display a list of nearby BLE devices. Click on a device to proceed.

2. **Connect to a device:** Once you're in the connection screen, click on the "Connect" button. This will trigger a discovery process for the services and characteristics of the chosen device.

3. **Interact with characteristics:** Clicking on a characteristic will open a dialog that allows for interactions. Once an action is performed, the dialog closes and the corresponding event should appear in the logs.

## Technologies

This app is made using:

- Jetpack Compose for UI
- RxJava 3 for the BLE module
- Coroutines for bridging the BLE functionalities with the Flows on the app module

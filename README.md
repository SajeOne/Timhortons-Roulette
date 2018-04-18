# Tim Hortons Roulette
An android app that routes you to a random Tim Hortons within a certain radius. Perfect for motorcycle or car drivers looking for a destination when enjoying their ride.

![roulette app](https://shane-brown.ca/img/75b851f.gif "Roulette Demo")

## Features
* Modifiable Radius
* GPS Navigation
* Coordinate access for use in other apps

## Dependencies
* [osmdroid](https://github.com/osmdroid/osmdroid) - Mapview implementation
* [osmbonuspack](https://github.com/MKergall/osmbonuspack) - Routing
* [JitPack](https://jitpack.io/) - Easy access to github dependencies

## Installation
### Binary Installation
* Transfer .APK from releases onto android device
* Open with file manager of choice
* Follow instructions on install prompt

### Compile From Source
* Clone repository ```git clone https://github.com/SajeOne/Timhortons-Roulette.git```
* Enter cloned repository ```cd Timhortons-Roulette```
* Build with gradle ```./gradlew assembleDebug```
* Retrieve .APK from ```./app/build/outputs/apk/debug```
* Follow \`Binary Installation\` steps

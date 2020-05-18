# React Native Twilio Chat

>React Native wrapper for the Twilio Programmable Chat iOS and Android SDKs

## Installation
```
yarn add 
```

### iOS - CocoaPods
Install the Twilio Chat SDK and this package via CocoaPods. See the [full Podfile example](./Example/ios/Podfile) for more details.

```ruby
pod 'Yoga', :path => '../node_modules/react-native/ReactCommon/yoga'
pod 'React', :subspecs => ['Core', /* any other subspecs you require */], :path => '../node_modules/react-native'
  
source 'https://github.com/twilio/cocoapod-specs'
pod 'TwilioChatClient', '~> 2.1.0'
pod 'TwilioAccessManager', '~> 1.0.0'
```
**Note: the underlying Twilio SDKs require a minimum deployment target of `8.1`**. If your project's target is less than this you will get a CocoaPods install error (`Unable to satisfy the following requirements...`).

Make sure that you add the `$(inherited)` value to `Other Linker Flags` and `Framework Search Paths` for your target's Build Settings. This is also assuming you have already loaded React via CocoaPods as well.

### Android
In `android/settings.gradle`:

```java
include ':RCTTwilioChat', ':app'
project(':RCTTwilioChat').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-twilio-chat/android')
```

In `android/app/build.gradle`:
```java
...
dependencies {
    ...
    compile project(':RCTTwilioChat')
}

```

Register the module in `MainApplication.java`:

**Note:** You might have to enable multidex in your `build.gradle` file and increase the heap size if you're getting errors while buliding. The minSdkVersion must also be at least 19, per the Twilio SDKs. 

```java
android {
    ....
    dexOptions {
        javaMaxHeapSize "2048M"
    }

    defaultConfig {
        ...
        minSdkVersion 19
        multiDexEnabled true
    }
```

## Usage
```javascript
/* Initialization */

import {
    AccessManager,
    Client,
    Constants
} from 'react-native-twilio-chat';

// create the access manager
const accessManager = new AccessManager(token);

// specify any handlers for events
accessManager.onTokenWillExpire = () => {
    getNewTokenFromServer()
    .then(accessManager.updateToken);
}

// create the client
const client = new Client(token);

// specify any global events
client.onError = ({error, userInfo}) => console.log(error);

// initialize the client
client.initialize();

// wait for sync to finish
client.onClientSynchronized = () => {
    client.getUserChannels()
    .then((channelPaginator) => console.log(channelPaginator.items));
}

/* Individual Channel */

// an instance of Channel is passed down in the app via props
let channel = this.props.channel

// specify channel specific events
channel.onMessageAdded = (message) => console.log(message.author + ": " + message.body);
channel.onTypingStarted = (member) => console.log(member.identity + " started typing...");
channel.onTypingEnded = (member) => console.log(member.identity + " stopped typing...");
channel.onMemberAdded = (member) => console.log(member.identity + " joined " + channel.friendlyName);

// sending a message
<TextInput 
  onChangeText={(body) => {
    this.setState({body});
    channel.typing();
  }}
  onSubmitEditing={() => { channel.sendMessage(this.state.body)} }
/>
````

## [Documentation](docs)

## License
This project is licensed under the [MIT License](LICENSE).

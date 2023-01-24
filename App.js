import React, {useState} from 'react';
import {View, Platform, StyleSheet, Text, NativeModules} from 'react-native';
import {runOnJS, useSharedValue} from 'react-native-reanimated';
import {
  Camera,
  useCameraDevices,
  useFrameProcessor,
} from 'react-native-vision-camera';
import {openCvTemplateMatch} from './NativeModules/OpenCvTemplateMatch';

export default function App() {
  const devices = useCameraDevices();
  const device = devices.back;
  const [frame, setFrame] = useState('');

  const currentFrame = useSharedValue('');

  const frameProcessor = useFrameProcessor(frame => {
    'worklet';
    let res = openCvTemplateMatch(frame);
    runOnJS(setFrame)('Result: ' + res);
  }, []);

  const MyCamera = () => {
    if (device == null) {
      return <Text>{'LOADING'}</Text>;
    } else {
      return (
        <Camera
          frameProcessor={frameProcessor}
          frameProcessorFps={2}
          style={StyleSheet.absoluteFill}
          enableHighQualityPhotos
          device={device}
          isActive={true}
        />
      );
    }
  };

  return (
    <View
      style={{
        flex: 1,
      }}>
      <Text style={{fontSize: 10}}>{'Frame: ' + frame}</Text>
    </View>
  );
}

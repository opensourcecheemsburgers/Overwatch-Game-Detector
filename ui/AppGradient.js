import LinearGradient from 'react-native-linear-gradient';
import {View} from 'react-native';
import * as React from 'react';
import {themes} from './colors';

const AppGradient = ({children}) => {
  return (
    <LinearGradient
      // useAngle={true}
      // angle={90}
      // angleCenter={{x: 0.5, y: 0.5}}
      colors={['#fff', '#fff']}
      style={{flex: 1}}
      renderToHardwareTextureAndroid>
      <View style={{flex: 1}}>{children}</View>
    </LinearGradient>
  );
};

export default AppGradient;

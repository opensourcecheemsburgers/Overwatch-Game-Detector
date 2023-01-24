import type {Frame} from 'react-native-vision-camera';

/**
 * Scans QR codes.
 */
export function openCvTemplateMatch(frame: Frame): String {
  'worklet';
  // eslint-disable-next-line no-undef
  return __openCvTemplateMatch(frame);
}

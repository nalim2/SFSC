package protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

class FrameParser {

  static byte[] getFrame(byte[][] multiPart, int position) {
    return multiPart[position];
  }

  static <T> T getFrame(byte[][] multiPart, Parser<T> parser, int position) throws InvalidProtocolBufferException {
    return parser.parseFrom(getFrame(multiPart, position));
  }

  static void putFrame(byte[][] message, byte[] frame, int position) {
    message[position] = frame;
  }

  static void putFrame(byte[][] message, Message frame, int position) {
    putFrame(message, frame.toByteArray(), position);
  }
}

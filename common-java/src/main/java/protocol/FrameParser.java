package protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

public class FrameParser {

  public static byte[] getFrame(byte[][] multiPart, int position) {
    return multiPart[position];
  }

  public static <T> T getFrameAs(byte[][] multiPart, Parser<T> parser, int position) throws InvalidProtocolBufferException {
    return parser.parseFrom(getFrame(multiPart, position));
  }

  public static void putFrame(byte[][] message, byte[] frame, int position) {
    message[position] = frame;
  }

  public static void putFrame(byte[][] message, Message frame, int position) {
    putFrame(message, frame.toByteArray(), position);
  }
}

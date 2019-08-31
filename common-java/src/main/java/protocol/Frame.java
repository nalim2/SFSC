package protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

public interface Frame {

  int getFramePosition();

  default byte[] get(byte[][] multiPart) {
    return FrameParser.getFrame(multiPart, getFramePosition());
  }

  default <T> T get(byte[][] multiPart, Parser<T> parser) throws InvalidProtocolBufferException {
    return FrameParser.getFrame(multiPart, parser, getFramePosition());
  }

  default void put(byte[][] message, byte[] frameData) {
    FrameParser.putFrame(message, frameData, getFramePosition());
  }

  default void put(byte[][] message, Message frameData) {
    FrameParser.putFrame(message, frameData, getFramePosition());
  }
}

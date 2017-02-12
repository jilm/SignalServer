/*
 * Copyright (C) 2017 jilm
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.lidinsky.signalserver;

import cz.lidinsky.spinel.RawBinarySpinelMessage;
import cz.lidinsky.spinel.SpinelMessage;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import org.json.JSONObject;

/**
 *
 * @author jilm
 */
public class MessageUtils {

  public static final byte ACK_OK = 0;
  public static final byte ACK_BAD_COMMAND = 1;
  public static final byte ACK_NO_VALUE = 2;
  public static final byte PUT_INST = 11;
  public static final byte GET_INST = 12;

  public static final int SPINEL_IDENTIFIER_SIZE = 8;

  public static final String COMMAND="COMMAND";

  public static final RawBinarySpinelMessage OK_RESPONSE
          = new RawBinarySpinelMessage(0xB0, new byte[] {ACK_OK});
  public static final RawBinarySpinelMessage BAD_COMMAND_RESPONSE
          = new RawBinarySpinelMessage(0xB0, new byte[] {ACK_BAD_COMMAND});
  public static final RawBinarySpinelMessage NO_VALUE_RESPONSE
          = new RawBinarySpinelMessage(0xB0, new byte[] {ACK_NO_VALUE});

  public static JSONObject getRequest(String key) {
    JSONObject request = new JSONObject();
    request.put(COMMAND, "GET");
    request.put("key", key);
    return request;
  }

  public static JSONObject getStatusRequest() {
    JSONObject request = new JSONObject();
    request.put(COMMAND, "STATUS");
    System.out.println(request.toString());
    return request;
  }

  public static JSONObject getCloseRequest() {
    JSONObject request = new JSONObject();
    request.put(COMMAND, "CLOSE");
    System.out.println(request.toString());
    return request;
  }

  public static String getKey(SpinelMessage message) {
    ByteBuffer buffer = message.getData();
    buffer.rewind();
    char[] key = new char[SPINEL_IDENTIFIER_SIZE];
    for (int i = 0; i < SPINEL_IDENTIFIER_SIZE; i++) {
      key[i] = buffer.getChar();
    }
    return new String(key).trim();
  }

  public static SpinelMessage putMessage(String key, long timestamp, boolean flag, double value, int address) {
    ByteBuffer buffer = ByteBuffer.allocate(50);
    for (int i = 0; i < 8; i++) {
      buffer.putChar(key.length() <= i ? ' ' : key.charAt(i));
    }
    buffer.putLong(timestamp);
    buffer.put((byte) (flag ? 0 : 1));
    buffer.putDouble(value);
    return new SpinelMessage(address, PUT_INST, buffer);
  }

  public static SpinelMessage getMessage(String key, int address) {
    ByteBuffer buffer = ByteBuffer.allocate(16);
    for (int i = 0; i < 8; i++) {
      buffer.putChar(key.length() <= i ? ' ' : key.charAt(i));
    }
    return new SpinelMessage(address, GET_INST, buffer);
  }

  public static byte[] wrap(String key, byte[] value) {
    int length = key.length() * Character.BYTES + Integer.BYTES + value.length;
    ByteBuffer buffer = ByteBuffer.allocate(length);
    buffer.putInt(key.length());
    for (int i = 0; i < key.length(); i++) {
      buffer.putChar(key.charAt(i));
    }
    buffer.put(value);
    return buffer.array();
  }

  public static byte[] wrap(byte command, byte[] value) {
    byte buffer[] = new byte[value.length + 1];
    buffer[0] = command;
    System.arraycopy(value, 0, buffer, 1, value.length);
    return buffer;
  }

  public static byte[] unwrapCommand(byte[] buffer) {
    byte[] result = new byte[buffer.length - 1];
    System.arraycopy(buffer, 1, result, 0, result.length);
    return result;
  }

  public static String getKey(byte[] buffer) {
    ByteBuffer bb = ByteBuffer.wrap(buffer);
    bb.rewind();
    int length = bb.getInt();
    CharBuffer chb = bb.asCharBuffer();
    char[] key = new char[length];
    chb.get(key, 0, length);
    return new String(key);
  }

  public static byte[] unwrapKey(byte[] buffer) {
    ByteBuffer bb = ByteBuffer.wrap(buffer);
    bb.rewind();
    int length = bb.getInt();
    byte[] result
        = new byte[buffer.length - length * Character.BYTES - Integer.BYTES];
    bb.position(length * Character.BYTES + Integer.BYTES);
    bb.get(result, 0, result.length);
    return result;
  }

}

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

import cz.lidinsky.spinel.SpinelMessage;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jilm
 */
public class MessageUtilsTest {

  public MessageUtilsTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getRequest method, of class MessageUtils.
   */
  @Test
  @Ignore
  public void testGetRequest() {
    System.out.println("getRequest");
    JSONObject expResult = null;
    JSONObject result = MessageUtils.getRequest("abcd");
    System.out.println(result.toString());
  }

  /**
   * Test of getStatusRequest method, of class MessageUtils.
   */
  @Test
  @Ignore
  public void testGetStatusRequest() {
    System.out.println("getStatusRequest");
    JSONObject expResult = null;
    JSONObject result = MessageUtils.getStatusRequest();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getCloseRequest method, of class MessageUtils.
   */
  @Test
  @Ignore
  public void testGetCloseRequest() {
    System.out.println("getCloseRequest");
    JSONObject expResult = null;
    JSONObject result = MessageUtils.getCloseRequest();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getKey method, of class MessageUtils.
   */
  @Test
  @Ignore
  public void testGetKey_SpinelMessage() {
    System.out.println("getKey");
    SpinelMessage message = null;
    String expResult = "";
    String result = MessageUtils.getKey(message);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of putMessage method, of class MessageUtils.
   */
  @Test
  @Ignore
  public void testPutMessage() {
    System.out.println("putMessage");
    String key = "";
    long timestamp = 0L;
    boolean flag = false;
    double value = 0.0;
    int address = 0;
    SpinelMessage expResult = null;
    SpinelMessage result = MessageUtils.putMessage(key, timestamp, flag, value, address);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getMessage method, of class MessageUtils.
   */
  @Test
  @Ignore
  public void testGetMessage() {
    System.out.println("getMessage");
    String key = "";
    int address = 0;
    SpinelMessage expResult = null;
    SpinelMessage result = MessageUtils.getMessage(key, address);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of wrap method, of class MessageUtils.
   */
  @Test
  public void testWrap_String_byteArr() {
    System.out.println("wrap");
    String key = "abcd";
    byte[] value = new byte[] {5, 4};
    byte[] expResult = new byte[] {0,0,0,4, 0,97,0,98,0,99,0,100,5,4};
    byte[] result = MessageUtils.wrap(key, value);
    System.out.println(Arrays.toString(result));
    assertArrayEquals(expResult, result);
    value = new byte[] {};
    expResult = new byte[] {0,0,0,4, 0,97,0,98,0,99,0,100};
    result = MessageUtils.wrap(key, value);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of wrap method, of class MessageUtils.
   */
  @Test
  public void testWrap_byte_byteArr() {
    System.out.println("wrap");
    byte command = 15;
    byte[] value = new byte[] {1,5, 10};
    byte[] expResult = new byte[] {15,1,5,10};
    byte[] result = MessageUtils.wrap(command, value);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of unwrapCommand method, of class MessageUtils.
   */
  @Test
  public void testUnwrapCommand() {
    System.out.println("unwrapCommand");
    byte[] buffer = new byte[] {15,1,5,10};;
    byte[] expResult = new byte[] {1,5, 10};
    byte[] result = MessageUtils.unwrapCommand(buffer);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of getKey method, of class MessageUtils.
   */
  @Test
  public void testGetKey_byteArr() {
    System.out.println("getKey");
    byte[] buffer = new byte[] {0,0,0,4, 0,97,0,98,0,99,0,100,5,4};
    String expResult = "abcd";
    String result = MessageUtils.getKey(buffer);
    assertEquals(expResult, result);
    buffer = new byte[] {0,0,0,4, 0,97,0,98,0,99,0,100};
    result = MessageUtils.getKey(buffer);
    expResult = "abcd";
    assertEquals(expResult, result);
  }

  /**
   * Test of unwrapKey method, of class MessageUtils.
   */
  @Test
  public void testUnwrapKey() {
    System.out.println("unwrapKey");
    byte[] buffer = new byte[] {0,0,0,4, 0,97,0,98,0,99,0,100,5,4};
    byte[] expResult = new byte[] {5,4};
    byte[] result = MessageUtils.unwrapKey(buffer);
    assertArrayEquals(expResult, result);
  }



}

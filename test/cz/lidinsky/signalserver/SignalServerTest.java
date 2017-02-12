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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jilm
 */
public class SignalServerTest {

  DistributedMap server;
  Thread serverThread;

  public SignalServerTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    server = new DistributedMap(DistributedMap.DEFAULT_PORT);
    serverThread = new Thread(server);
    serverThread.start();
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of main method, of class DistributedMap.
   */
  @Test
  @Ignore
  public void testMain() throws Exception {
    System.out.println("main");
    String[] args = null;
    DistributedMap.main(args);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of run method, of class DistributedMap.
   */
  @Test
  public void testRun() {
    try {
      Socket socket = new Socket("localhost", DistributedMap.DEFAULT_PORT);
      JSONObject message = new JSONObject();
      OutputStream os = socket.getOutputStream();
      Writer writer = new OutputStreamWriter(os);
      message.put("type", "GET");
      message.write(writer);
      writer.flush();
      //Thread.sleep(1000);
    } catch (IOException ex) {
      fail(ex.getMessage());
    //} catch (InterruptedException ex) {
    //  Logger.getLogger(SignalServerTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}

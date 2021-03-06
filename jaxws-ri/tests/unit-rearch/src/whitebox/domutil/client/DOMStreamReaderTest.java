/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package whitebox.domutil.client;

import com.sun.xml.ws.streaming.DOMStreamReader;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

/**
 * Tests {@link DOMStreamReader}.
 *
 * @author Kohsuke Kawaguchi
 */
public class DOMStreamReaderTest extends TestCase implements XMLStreamConstants {

    DocumentBuilder db;

    protected void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();
    }

    public void test1() throws Exception {
        String sample = "<?xml version='1.0' encoding='UTF-8'?><env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'><env:Body><env:Fault><faultcode>env:Server</faultcode><faultstring>Internal server error</faultstring></env:Fault></env:Body></env:Envelope>";
        Document dd = db.parse(new ByteArrayInputStream(sample.getBytes("UTF-8")));

        scanAll(dd);
    }

    public void test2() throws Exception {
        scanAll(buildBrokenTree());
    }

    /**
     * Makes sure that namespace URIs are properly repaired.
     */
    public void testNamespaceFix() throws Exception {
        DOMStreamReader sr = new DOMStreamReader(buildBrokenTree());
        sr.nextTag();
        assertEquals(1,sr.getNamespaceCount());
        assertEquals("foo",sr.getNamespaceURI(0));
        assertEquals("",sr.getNamespacePrefix(0));

        sr.nextTag();
        assertEquals(1,sr.getNamespaceCount());
        assertEquals("test",sr.getNamespaceURI(0));
        assertEquals("p",sr.getNamespacePrefix(0));
    }

    /**
     * Makes sure that adjacent text are concatanated.
     *
     * See https://jax-ws.dev.java.net/issues/show_bug.cgi?id=160
     */
    public void testAdjacentText() throws Exception {
        Document dd = db.newDocument();
        Element root = dd.createElementNS("foo", "bar");
        dd.appendChild(root);
        for( int i=0; i<3; i++ )
            root.appendChild(dd.createTextNode("foo"));

        DOMStreamReader r = new DOMStreamReader(dd);
        r.nextTag();
        r.next();

        assertEquals("foofoofoo",r.getText());
        r.next();
        assertEquals(END_ELEMENT,r.getEventType());
    }

    private Document buildBrokenTree() {
        Document dd = db.newDocument();
        Element root = dd.createElementNS("foo", "bar");
        dd.appendChild(root);
        root.appendChild(dd.createElementNS("test","p:test"));
        return dd;
    }

    private void scanAll(Document dd) throws XMLStreamException {
        DOMStreamReader dsr = new DOMStreamReader(dd);
        while (dsr.hasNext()) {
            System.out.println("dsr.next() = " + dsr.next());
            if (dsr.getEventType() == START_ELEMENT || dsr.getEventType() == END_ELEMENT) {
                System.out.println("dsr.getName = " + dsr.getName());
                if (dsr.getEventType() == START_ELEMENT) {
                    System.out.println("dsr.getAttributeCount() = " + dsr.getAttributeCount());
                    System.out.println("dsr.getNamespaceCount() = " + dsr.getNamespaceCount());
                }
            }
        }
    }
}

/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package fromjava.jaxws943.server;

import com.sun.xml.ws.developer.StatefulWebServiceManager;
import fromjava.jaxws943.server.book.Book;

import jakarta.jws.WebService;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * The entry point to the book store web application.
 *
 * <p>
 * Let's say we are building an online book store. A natural modeling
 * of such application would involve in havling {@link fromjava.jaxws943.server.book.Book} object
 * to represent each book. When you expose your bookstore as a web
 * service, it would be convenient to have a "remote reference" to
 * an individual book.
 *
 * <p>
 * {@link jakarta.xml.ws.wsaddressing.W3CEndpointReference} (EPR) is just that kind of remote reference.
 * You can turn a server-side {@link fromjava.jaxws943.server.book.Book} object to a "remote reference"
 * by calling {@link StatefulWebServiceManager#export(Object)} and then
 * send it back to the client. The remote client can then use that EPR
 * to talk back to the exported {@link fromjava.jaxws943.server.book.Book} object later. The client
 * can even pass that EPR to other web services, and have that service
 * talk to the exported {@link fromjava.jaxws943.server.book.Book} instance.
 *
 * <p>
 * In a way, this works a bit like a distributed object system.
 *
 * @since 2.1 EA2
 */
@WebService(portName = "BookStorePort", targetNamespace = "http://client.jaxws943.fromjava/") // target namespace is used for target java package
public class BookStore {
    /**
     * This web method is used by the client to obtain
     * a remote reference to a book instance.
     */
    public synchronized W3CEndpointReference getProduct(String id) {
        // in a real application, you'd probably be loading
        // such book instance from database, instead of
        // creating a new object.

        // when this method is called multiple times with the same ID,
        // the 2nd method invocation will return the EPR to the first
        // Book object created, because of Book.equals() implementation.

        // use the 'export' to turn an object reference into EPR.
        return Book.manager.export(new Book(id));

        // note that since there's no distributed GC, the exported object
        // remains in memory indefinitely. See StatefulWebServiceManager
        // javadoc for more about this, and how to avoid memory leak.
    }
}

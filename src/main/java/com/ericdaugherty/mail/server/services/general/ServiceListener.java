/******************************************************************************
 * $Workfile: ServiceListener.java $
 * $Revision: 144 $
 * $Author: edaugherty $
 * $Date: 2003-11-07 19:13:42 -0600 (Fri, 07 Nov 2003) $
 *
 ******************************************************************************
 * This program is a 100% Java Email Server.
 ******************************************************************************
 * Copyright (C) 2001, Eric Daugherty
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 ******************************************************************************
 * For current versions and more information, please visit:
 * http://www.ericdaugherty.com/java/mail
 *
 * or contact the author at:
 * java@ericdaugherty.com
 *
 ******************************************************************************
 * This program is based on the CSRMail project written by Calvin Smith.
 * http://crsemail.sourceforge.net/
 *****************************************************************************/

package com.ericdaugherty.mail.server.services.general;

//Java imports
import java.net.*;
import java.io.*;

//Log4j imports
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ericdaugherty.mail.server.configuration.ConfigurationManager;

//Local imports

/**
 * This class listens for incoming connections on the specified port and
 * starts a new thread to process the request.  This class abstracts common
 * functinality required to start any type of service (POP3 or SMTP), reducing
 * the requirement to duplicate this code in each package.
 *
 * @author Eric Daugherty
 */
public class ServiceListener implements Runnable {

    //***************************************************************
    // Variables
    //***************************************************************

    /** Logger Category for this class. */
    private static final Logger logger = LogManager.getLogger(ServiceListener.class.getName());

    /** Array of processors */
    private ConnectionProcessor[] processors;

    /** The port to listen on for incoming connections. */
    private final int port;

    /** The type of class to use to handle requests. */
    private final Class connectionProcessorClass;

    /** The number of threads to create to listen on this port */
    private final int threads;

    /** Thread pool */
    private Thread[] threadPool = null;

    /** server socket */
    private ServerSocket serverSocket;

    //***************************************************************
    // Public Interface
    //***************************************************************

    //***************************************************************
    // Constructor(s)

    /**
     * Creates a new instance and stores the initial paramters.
     * @param port
     * @param connectionProcessorClass
     * @param threads
     */
    public ServiceListener( int port, Class connectionProcessorClass, int threads ) {

        this.port = port;
        this.connectionProcessorClass = connectionProcessorClass;
        this.threads = threads;
    }

    //***************************************************************
    // Methods

    /**
     * Entry point for the thread.  Listens for incoming connections and
     * start a new handler thread for each.
     */
    @Override
    public void run() {

        if( logger.isDebugEnabled() ) logger.debug( "Starting ServiceListener on port: {}", port);

        InetAddress listenAddress = ConfigurationManager.getInstance().getListenAddress();
        try {
            if( listenAddress == null ) {
                // listen to the given port
                serverSocket = new ServerSocket( port );
            }
            else {
                // 50 is the default backlog size.
                serverSocket = new ServerSocket( port, 50, listenAddress );
            }
        }
        catch (IOException e) {
            String address = "localhost";
            if( listenAddress != null ) {
                address = listenAddress.getHostAddress();
            }

            logger.error("Could not create ServiceListener on address: {} port: {}.  No connections will be accepted on this port!", address, port);
            return;
        }

        logger.info( "Accepting Connections on port: {}", port );

        ConnectionProcessor processor;
        long threadCount = 0;
        String threadNameBase = Thread.currentThread().getName();

        //Initialize threadpools.
        try {

            processors = new ConnectionProcessor[ threads ];
            threadPool = new Thread[ threads ];

            for( int index = 0; index < threads; index++ ) {
                //Create the handler now to speed up connection time.
                processor = (ConnectionProcessor) connectionProcessorClass.newInstance();
                processors[index] = processor;

                processor.setSocket( serverSocket );

                //Create, name, and start a new thread to handle this request.
                threadPool[index] = new Thread(processor, threadNameBase + ":" + ++threadCount );
                threadPool[index].start();
            }
        }
        catch (IllegalAccessException | InstantiationException e)
        {
            logger.error("ServiceListener Connection failed on port: {}.  Error: {}", port, e );
        }
    }

    /**
     * Stops all processors.
     */
    public void shutdown() {
        for( int index = 0; index < processors.length; index++ ) {

            processors[index].shutdown();

            try{
                threadPool[index].join(10000);
            }
            catch (InterruptedException ie)
            {
                logger.error("Was interrupted while waiting for thread to die");
            }

            logger.info("Thread gracefully terminated");
            threadPool[index] = null;
        }

        try
        {
            serverSocket.close();
            logger.info("Server socket succcessfully closed");
        }
        catch(IOException e)
        {
            logger.error( "Failed to  close server socket {}", e );
        }
        serverSocket = null;
    }

}


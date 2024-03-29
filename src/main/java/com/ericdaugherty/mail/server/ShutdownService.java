/******************************************************************************
 * $Workfile: Mail.java $
 * $Revision: 115 $
 * $Author: edaugherty $
 * $Date: 2003-10-15 14:01:01 -0500 (Wed, 15 Oct 2003) $
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

package com.ericdaugherty.mail.server;

//Log imports
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This thread runs when the JVM is shutdown.
 *
 * @author Eric Daugherty
 */
public class ShutdownService implements Runnable {

    /** Logger */
    private final Logger logger = LogManager.getLogger(ShutdownService.class.getName());
    /**
     * Runs when the JVM is shutdown.  Stops all threads gracefully.
     */
    @Override
    public void run() {

        logger.warn( "Server is shutting down." );

        try {
            Mail.shutdown();

            logger.warn( "Server shutdown complete." );
        }
        catch (Exception e) {
            logger.error("Failed to terminate properly {}", e);
        }
    }
}

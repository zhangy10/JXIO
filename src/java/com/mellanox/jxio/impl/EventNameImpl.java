/*
 ** Copyright (C) 2013 Mellanox Technologies
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at:
 **
 ** http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 ** either express or implied. See the License for the specific language
 ** governing permissions and  limitations under the License.
 **
 */

package com.mellanox.jxio.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SessionEvents that are received by ClientSession, ServerSession and ServerPortal
 * 
 */
public enum EventNameImpl {
	/**
	 * SESSION_REJECT is received by ClientSession in case the server chose to reject the session
	 */
	SESSION_REJECT(0, 0),

	/**
	 * Session TEARDOWN - Internal event
	 */
	SESSION_TEARDOWN(1, -1),

	/**
	 * PORTAL_CLOSED is received by ServerPortal. In case the user initiated close
	 * of ServerPortal when there are still ServerSessions that are listening on him, all those ServerSessions are
	 * closed. When all ServerSessions are closed, PORTAL_CLOSED event is received.
	 */
	PORTAL_CLOSED(2, 2),

	/**
	 * SESSION_CLOSED received by ClientSession or ServerSession. 
	 * This event is received after session was properly
	 * closed (close method is asynchronous). This event is received if either of the sides initiated the close
	 * or if there is internal error on either of the sides. Matches connection teardown in Accelio
	 */
	SESSION_CLOSED(4, 1),

	/**
	 * SESSION_ERROR is received by ClientSession, ServerSession and ServerPortal.
	 */
	SESSION_ERROR(9, 3),

	/**
	 * CONNECTION_CLOSING & CONNECTION_REJECTED is an internal event indicating start of XIO CONNECTION CLOSING/REJECTED
	 */
	CONNECTION_CLOSING(6, 4),
	CONNECTION_REJECTED(7, 5),

	/**
	 * Unknown event
	 */
	UNKNOWN_EVENT(10, -1);

	private int                                  xioIndex;
	private int                                  publishedIndex;

	private static final Log LOG = LogFactory.getLog(EventNameImpl.class.getCanonicalName());

	private static final Map<Integer, EventNameImpl> intToTypeMap = new HashMap<Integer, EventNameImpl>();
	static {
		for (EventNameImpl eventNameImpl : EventNameImpl.values()) {
			intToTypeMap.put(eventNameImpl.xioIndex, eventNameImpl);
		}
	}

	private EventNameImpl(int xioIndex, int publishedIndex) {
		this.xioIndex = xioIndex;
		this.publishedIndex = publishedIndex;
	}

	public int getPublishedIndex() {
		return publishedIndex;
	}

	public static EventNameImpl getEventByIndex(int xioIndex) {
		EventNameImpl eventNameImpl = intToTypeMap.get(Integer.valueOf(xioIndex));
		if (eventNameImpl == null) {
			LOG.warn("Unmapped XIO event index = '" + xioIndex + "'. Returning with UNKNOWN_EVENT");
			return EventNameImpl.UNKNOWN_EVENT;
		}
		return eventNameImpl;
	}
}
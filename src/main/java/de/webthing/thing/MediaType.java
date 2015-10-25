/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Siemens AG and the thingweb community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.webthing.thing;

import java.util.HashMap;
import java.util.Map;

/**
 * Media Types
 */
public enum MediaType {
	// TODO decide whether to create new independent ones or re-use org.eclipse.californium.core.coap.MediaTypeRegistry
	// Note: http server uses again other ones. Hence it might be good to be independent
	
	/** text/plain */
	TEXT_PLAIN("text/plain"),
	/** application/xml */
	APPLICATION_XML("application/xml"),
	/** application/exi */
	APPLICATION_EXI("application/exi"),
	/** application/json */
	APPLICATION_JSON("application/json"),
	/** undefined/unknown */
	UNDEFINED("undefined");
	
	public final String mediaType; 
	
	static Map<String, MediaType> mediaTypes = new HashMap<>();
	static {
		mediaTypes.put(TEXT_PLAIN.mediaType, TEXT_PLAIN);
		mediaTypes.put(APPLICATION_XML.mediaType, APPLICATION_XML);
		mediaTypes.put(APPLICATION_EXI.mediaType, APPLICATION_EXI);
		mediaTypes.put(APPLICATION_JSON.mediaType, APPLICATION_JSON);
		// mediaTypes.put(UNDEFINED.mediaType, UNDEFINED);
	}
	
	public static MediaType getMediaType(String mediaType) {
		return mediaTypes.get(mediaType);
	}
	
	
	MediaType(String mediaType) {
		this.mediaType = mediaType;
	}
}
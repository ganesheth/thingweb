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

package de.thingweb.thing;

public class Content {
	
	final byte[] content;
	final MediaType mediaType;
	final String mediaTypeEx;
	private String locationPath;
	private ResponseType responseType = ResponseType.UNDEFINED;
	
	public enum ResponseType {CREATED, READ, UPDATED, DELETED, SUBSCRIBED, SERVER_ERROR, INVALID_REQUEST, UNDEFINED};
	
	public Content(byte[] content, MediaType mediaType) {
		this.content = content;
		this.mediaType = mediaType;
		this.mediaTypeEx = null;
	}
	
	public Content(byte[] content, String mediaType) {
		this.content = content;
		this.mediaTypeEx = mediaType;
		this.mediaType = MediaType.UNDEFINED;
	}
	
	public MediaType getMediaType() {
		return this.mediaType;
	}

	public String getMediaTypeEx() {
		return this.mediaTypeEx;
	}
	
	public byte[] getContent() {
		return this.content;
	}

	public String getLocationPath() {
		return locationPath;
	}

	public void setLocationPath(String locationPath) {
		this.locationPath = locationPath;
	}
	
	public ResponseType getResponseType(){
		return responseType;
	}
	
	public void setResponseType(ResponseType responseType){
		this.responseType = responseType;
	}
	
}
